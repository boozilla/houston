package boozilla.houston.grpc.webhook.command;

import boozilla.houston.grpc.webhook.GitBehavior;
import boozilla.houston.grpc.webhook.StateLabel;
import boozilla.houston.grpc.webhook.handler.Extension;
import boozilla.houston.grpc.webhook.handler.GitFileHandler;
import boozilla.houston.grpc.webhook.handler.JsonManifestHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Period;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@AllArgsConstructor
public class UploadCommand implements Command {
    private final Pattern filenameArgsPattern = filenameArgsPattern();
    private final MessageSourceAccessor messageSourceAccessor;

    @Override
    public Set<String> aliases()
    {
        return Set.of("/upload");
    }

    private Pattern filenameArgsPattern()
    {
        final var extensions = Arrays.stream(Extension.values())
                .flatMap(extension -> Arrays.stream(extension.getExtensions()))
                .collect(Collectors.joining("|"));

        return Pattern.compile("[\\w#\\.\\-/]+(?:%s)".formatted(extensions));
    }

    private List<String> filenameArgsExtract(final String filenameArgs)
    {
        final var matcher = filenameArgsPattern.matcher(filenameArgs);
        final var filenames = new ArrayList<String>();

        while(matcher.find())
        {
            filenames.add(matcher.group().trim());
        }

        return filenames;
    }

    @Override
    public String commandTemplate()
    {
        // /upload <ref> <file1>, <file2>, ...
        return "/upload %s %s";
    }

    @Override
    public Mono<Void> run(final String packageName, final String projectId, final String issueId,
                          final String targetRef, final String command, final GitBehavior<?> behavior)
    {
        final var startAt = System.currentTimeMillis();
        final var args = Arrays.stream(command.split(" ", 3))
                .map(String::trim)
                .toList();

        final var filenameArgs = args.get(2);
        final var commitId = behavior.commitId(projectId, args.get(1));
        final var files = filenameArgsExtract(filenameArgs).stream()
                .filter(file -> isAllowedBranch(file, targetRef));

        return commitId.flatMap(commit -> fileHandler(projectId, issueId, commit, files.toList(), packageName, behavior)
                        .flatMap(handlerMap -> files(projectId, issueId, commit, handlerMap.keySet(), behavior)
                                .onErrorResume(error -> behavior.commentMessage(projectId, issueId, error.getMessage())
                                        .then(Mono.error(error)))
                                .flatMap(map -> behavior.commentMessage(projectId, issueId, messageSourceAccessor.getMessage("GIT_DATA_HANDLING")
                                                .formatted(map.size()))
                                        .thenReturn(map))
                                .flatMap(fileMap -> Flux.fromIterable(fileMap.entrySet())
                                        .flatMap(entry -> {
                                            // 확장자별 동일한 handler 인스턴스
                                            final var handler = handlerMap.get(entry.getKey());
                                            final var commitFile = entry.getKey();
                                            final var bytes = entry.getValue();

                                            log.info("Start data handling [name={}, handler={}]", commitFile, handler.getClass().getSimpleName());

                                            return handler.add(commitFile, bytes)
                                                    .doFinally(signal -> log.info("Complete data handling [name={}, handler={}]", commitFile, handler.getClass().getSimpleName()));
                                        })
                                        .reduce(new HashSet<GitFileHandler>(), (set, handler) -> {
                                            set.add(handler);
                                            return set;
                                        })
                                        .flatMap(handlers -> Flux.fromStream(handlers.stream())
                                                .flatMap(handler -> handler.handle().thenReturn(handler))
                                                .onErrorResume(error -> {
                                                    final var handlerError = new RuntimeException(messageSourceAccessor
                                                            .getMessage("EXCEPTION_STEP_FILE_HANDLE").formatted(commit), error);

                                                    return behavior.commentMessage(projectId, issueId, error.getMessage())
                                                            .then(behavior.commentMessage(projectId, issueId, handlerError.getMessage()))
                                                            .then(Mono.error(handlerError));
                                                })
                                                .collectList())))
                        .flatMap(handlers -> Flux.fromIterable(handlers)
                                .flatMap(handler -> handler.complete()
                                        // Handler::complete 에서 발생한 예외를 처리
                                        .onErrorResume(error -> {
                                            final var completeError = new RuntimeException(messageSourceAccessor.getMessage("EXCEPTION_STEP_FILE_COMPLETE")
                                                    .formatted(commit));

                                            return behavior.commentMessage(projectId, issueId, error.getMessage())
                                                    .then(behavior.commentMessage(projectId, issueId, completeError.getMessage()))
                                                    .then(Mono.error(completeError));
                                        }))
                                // 업로드 코멘트
                                .then(behavior.commentMessage(projectId, issueId, messageSourceAccessor.getMessage("GIT_DATA_UPLOADING")
                                        .formatted(handlers.size())))
                                .thenReturn(Tuples.of(commit, handlers))))
                .flatMap(tuple -> behavior.addLabels(projectId, issueId, tuple.getT1().substring(0, 8))
                        // 라벨 달기
                        .and(Mono.defer(() -> {
                            final var manifestOnly = tuple.getT2().stream()
                                    .allMatch(handler -> handler instanceof JsonManifestHandler);
                            final var state = manifestOnly ? StateLabel.ACTIVE : StateLabel.INACTIVE;

                            return behavior.setState(projectId, issueId, state);
                        }))
                        // 업로드 완료 코멘트
                        .and(behavior.commentMessage(projectId, issueId, messageSourceAccessor.getMessage("GIT_DATA_UPLOAD_ALL_COMPLETE")))
                        // Issue 닫음
                        .and(behavior.closeIssue(projectId, issueId))
                        // 진행 시간 등록
                        .and(Mono.defer(() -> behavior.addSpentTime(projectId, issueId, new Period(startAt, System.currentTimeMillis())))))
                .then();
    }

    /**
     * 파일 확장자에 따라 Handler 를 생성(확장자별 동일한 handler 인스턴스 반환)
     *
     * @param projectId   프로젝트 ID
     * @param issueId     이슈 ID
     * @param commitId    커밋 ID
     * @param commitFiles 커밋된 파일 경로
     * @param packageName 패키지 이름
     * @param behavior    GitLab API 를 사용하기 위한 동작
     * @return 파일 핸들러
     */
    private Mono<Map<String, GitFileHandler>> fileHandler(final String projectId, final String issueId, final String commitId, final Collection<String> commitFiles,
                                                          final String packageName, final GitBehavior<?> behavior)
    {
        final var handlerMap = new HashMap<Class<?>, GitFileHandler>();

        return Flux.fromIterable(commitFiles)
                .flatMap(commitFile -> {
                    final var extension = Extension.of(commitFile);

                    if(Objects.isNull(extension))
                        return Mono.empty();

                    final var fileHandler = handlerMap.computeIfAbsent(extension.handlerClass(),
                            handlerClas -> extension.handler(projectId, issueId, commitId, packageName, behavior));
                    final var tuple = Tuples.of(commitFile, fileHandler);

                    return Mono.just(tuple);
                })
                .collectMap(Tuple2::getT1, Tuple2::getT2);
    }

    /**
     * 파일을 읽어 byte array 를 생성
     *
     * @param projectId   프로젝트 ID
     * @param issueId     이슈 ID
     * @param commitId    커밋 ID
     * @param commitFiles 커밋된 파일 경로
     * @param behavior    GitLab API 를 사용하기 위한 동작
     * @return 파일 bytes
     */
    private Mono<Map<String, byte[]>> files(final String projectId, final String issueId, final String commitId, final Collection<String> commitFiles,
                                            final GitBehavior<?> behavior)
    {
        return Flux.fromIterable(commitFiles)
                .doOnRequest(sequence -> {
                    // 파일 다운로드 시작
                    behavior.commentMessage(projectId, issueId, messageSourceAccessor.getMessage("GIT_DATA_DOWNLOADING")
                                    .formatted(commitFiles.size()))
                            .subscribeOn(Schedulers.boundedElastic())
                            .subscribe();
                })
                .doOnNext(commitFile -> log.info("Start downloading files from git [name={}]", commitFile))
                .flatMap(commitFile -> behavior.openFile(projectId, commitId, commitFile)
                        .doOnNext(result -> log.info("Finished downloading files from git [name={}]", commitFile))
                        .onErrorMap(Exceptions::isRetryExhausted, Throwable::getCause)
                        .onErrorResume(error -> behavior.commentExceptions(projectId, issueId, error)
                                .then(Mono.error(new RuntimeException(messageSourceAccessor.getMessage("EXCEPTION_STEP_DOWNLOAD_GIT_ERROR")
                                        .formatted(commitId, commitFile)))))
                        .map(bytes -> Tuples.of(commitFile, bytes)))
                .collectMap(Tuple2::getT1, Tuple2::getT2);
    }

    private boolean isAllowedBranch(final String filename, final String targetRef)
    {
        final var excluded = filename.split("\\.");
        final var split = Arrays.stream(excluded, 0, excluded.length - 1)
                .collect(Collectors.joining())
                .split("#");

        if(split.length > 1)
        {
            return Stream.of(split).anyMatch(str -> str.contentEquals(targetRef));
        }

        return true;
    }

    @Override
    public String description()
    {
        return "HELP_COMMAND_UPLOAD_DESCRIPTION";
    }

    @Override
    public String options()
    {
        return "HELP_COMMAND_UPLOAD_OPTIONS";
    }

    @Override
    public Set<String> example()
    {
        return Set.of("HELP_COMMAND_UPLOAD_EXAMPLE_1", "HELP_COMMAND_UPLOAD_EXAMPLE_2");
    }
}

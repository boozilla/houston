package boozilla.houston.grpc.webhook.command;

import boozilla.houston.grpc.webhook.GitBehavior;
import com.google.protobuf.InvalidProtocolBufferException;
import houston.vo.webhook.UploadPayload;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PayloadCommand implements StereotypeCommand {
    @Override
    public Set<String> aliases()
    {
        return Set.of("```houston");
    }

    @Override
    public String commandTemplate()
    {
        return """
                ```houston
                %s
                ```
                """;
    }

    @Override
    public Mono<Void> run(final String packageName, final String projectId, final String issueId,
                          final String targetRef, final String command, final GitBehavior<?> behavior)
    {
        final var uploadCommand = getCommand(UploadCommand.class);
        final var uploadPayload = decode(command);
        final var fileArgs = uploadPayload.getCommitFileList().stream()
                .map("'%s'"::formatted)
                .collect(Collectors.joining(", "));
        final var delegateCommand = uploadCommand.commandTemplate()
                .formatted(uploadPayload.getHead(), fileArgs);

        return uploadCommand.run(packageName, projectId, issueId, targetRef, delegateCommand, behavior);
    }

    public static UploadPayload decode(final String command)
    {
        final var base64 = command.split("\n")[1];
        final var payload = Base64.getDecoder().decode(base64);

        try
        {
            return UploadPayload.parseFrom(payload);
        }
        catch(InvalidProtocolBufferException e)
        {
            throw new RuntimeException("Upload payload decoding errors", e);
        }
    }
}

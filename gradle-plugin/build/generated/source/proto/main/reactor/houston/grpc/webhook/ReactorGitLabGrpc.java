package houston.grpc.webhook;

import static houston.grpc.webhook.GitLabGrpc.getServiceDescriptor;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;


@javax.annotation.Generated(
value = "by ReactorGrpc generator",
comments = "Source: Webhook.proto")
public final class ReactorGitLabGrpc {
    private ReactorGitLabGrpc() {}

    public static ReactorGitLabStub newReactorStub(io.grpc.Channel channel) {
        return new ReactorGitLabStub(channel);
    }

    /**
     * <pre>
     *  GitLab Webhook 서비스
     * </pre>
     */
    public static final class ReactorGitLabStub extends io.grpc.stub.AbstractStub<ReactorGitLabStub> {
        private GitLabGrpc.GitLabStub delegateStub;

        private ReactorGitLabStub(io.grpc.Channel channel) {
            super(channel);
            delegateStub = GitLabGrpc.newStub(channel);
        }

        private ReactorGitLabStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
            delegateStub = GitLabGrpc.newStub(channel).build(channel, callOptions);
        }

        @Override
        protected ReactorGitLabStub build(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new ReactorGitLabStub(channel, callOptions);
        }

        /**
         * <pre>
         *  push 이벤트
         * </pre>
         */
        public reactor.core.publisher.Mono<com.google.protobuf.Empty> push(reactor.core.publisher.Mono<houston.vo.webhook.gitlab.PushEvent> reactorRequest) {
            return com.salesforce.reactorgrpc.stub.ClientCalls.oneToOne(reactorRequest, delegateStub::push, getCallOptions());
        }

        /**
         * <pre>
         *  note 이벤트
         * </pre>
         */
        public reactor.core.publisher.Mono<com.google.protobuf.Empty> note(reactor.core.publisher.Mono<houston.vo.webhook.gitlab.NoteEvent> reactorRequest) {
            return com.salesforce.reactorgrpc.stub.ClientCalls.oneToOne(reactorRequest, delegateStub::note, getCallOptions());
        }

        /**
         * <pre>
         *  push 이벤트
         * </pre>
         */
        public reactor.core.publisher.Mono<com.google.protobuf.Empty> push(houston.vo.webhook.gitlab.PushEvent reactorRequest) {
           return com.salesforce.reactorgrpc.stub.ClientCalls.oneToOne(reactor.core.publisher.Mono.just(reactorRequest), delegateStub::push, getCallOptions());
        }

        /**
         * <pre>
         *  note 이벤트
         * </pre>
         */
        public reactor.core.publisher.Mono<com.google.protobuf.Empty> note(houston.vo.webhook.gitlab.NoteEvent reactorRequest) {
           return com.salesforce.reactorgrpc.stub.ClientCalls.oneToOne(reactor.core.publisher.Mono.just(reactorRequest), delegateStub::note, getCallOptions());
        }

    }

    /**
     * <pre>
     *  GitLab Webhook 서비스
     * </pre>
     */
    public static abstract class GitLabImplBase implements io.grpc.BindableService {

        /**
         * <pre>
         *  push 이벤트
         * </pre>
         */
        public reactor.core.publisher.Mono<com.google.protobuf.Empty> push(houston.vo.webhook.gitlab.PushEvent request) {
            return push(reactor.core.publisher.Mono.just(request));
        }

        /**
         * <pre>
         *  push 이벤트
         * </pre>
         */
        public reactor.core.publisher.Mono<com.google.protobuf.Empty> push(reactor.core.publisher.Mono<houston.vo.webhook.gitlab.PushEvent> request) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        /**
         * <pre>
         *  note 이벤트
         * </pre>
         */
        public reactor.core.publisher.Mono<com.google.protobuf.Empty> note(houston.vo.webhook.gitlab.NoteEvent request) {
            return note(reactor.core.publisher.Mono.just(request));
        }

        /**
         * <pre>
         *  note 이벤트
         * </pre>
         */
        public reactor.core.publisher.Mono<com.google.protobuf.Empty> note(reactor.core.publisher.Mono<houston.vo.webhook.gitlab.NoteEvent> request) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            houston.grpc.webhook.GitLabGrpc.getPushMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            houston.vo.webhook.gitlab.PushEvent,
                                            com.google.protobuf.Empty>(
                                            this, METHODID_PUSH)))
                    .addMethod(
                            houston.grpc.webhook.GitLabGrpc.getNoteMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            houston.vo.webhook.gitlab.NoteEvent,
                                            com.google.protobuf.Empty>(
                                            this, METHODID_NOTE)))
                    .build();
        }

        protected io.grpc.CallOptions getCallOptions(int methodId) {
            return null;
        }

        protected Throwable onErrorMap(Throwable throwable) {
            return com.salesforce.reactorgrpc.stub.ServerCalls.prepareError(throwable);
        }
    }

    public static final int METHODID_PUSH = 0;
    public static final int METHODID_NOTE = 1;

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final GitLabImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(GitLabImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_PUSH:
                    com.salesforce.reactorgrpc.stub.ServerCalls.oneToOne((houston.vo.webhook.gitlab.PushEvent) request,
                            (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver,
                            serviceImpl::push, serviceImpl::onErrorMap);
                    break;
                case METHODID_NOTE:
                    com.salesforce.reactorgrpc.stub.ServerCalls.oneToOne((houston.vo.webhook.gitlab.NoteEvent) request,
                            (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver,
                            serviceImpl::note, serviceImpl::onErrorMap);
                    break;
                default:
                    throw new java.lang.AssertionError();
            }
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public io.grpc.stub.StreamObserver<Req> invoke(io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                default:
                    throw new java.lang.AssertionError();
            }
        }
    }

}

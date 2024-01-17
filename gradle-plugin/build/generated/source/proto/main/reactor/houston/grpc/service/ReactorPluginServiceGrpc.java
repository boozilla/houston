package houston.grpc.service;

import static houston.grpc.service.PluginServiceGrpc.getServiceDescriptor;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;


@javax.annotation.Generated(
value = "by ReactorGrpc generator",
comments = "Source: PluginService.proto")
public final class ReactorPluginServiceGrpc {
    private ReactorPluginServiceGrpc() {}

    public static ReactorPluginServiceStub newReactorStub(io.grpc.Channel channel) {
        return new ReactorPluginServiceStub(channel);
    }

    /**
     * <pre>
     *  플러그인 서비스
     * </pre>
     */
    public static final class ReactorPluginServiceStub extends io.grpc.stub.AbstractStub<ReactorPluginServiceStub> {
        private PluginServiceGrpc.PluginServiceStub delegateStub;

        private ReactorPluginServiceStub(io.grpc.Channel channel) {
            super(channel);
            delegateStub = PluginServiceGrpc.newStub(channel);
        }

        private ReactorPluginServiceStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
            delegateStub = PluginServiceGrpc.newStub(channel).build(channel, callOptions);
        }

        @Override
        protected ReactorPluginServiceStub build(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new ReactorPluginServiceStub(channel, callOptions);
        }

        /**
         * <pre>
         *  스키마
         * </pre>
         */
        public reactor.core.publisher.Flux<houston.grpc.service.AssetSchema> schema(reactor.core.publisher.Mono<com.google.protobuf.Empty> reactorRequest) {
            return com.salesforce.reactorgrpc.stub.ClientCalls.oneToMany(reactorRequest, delegateStub::schema, getCallOptions());
        }

        /**
         * <pre>
         *  무결성 검사 실행
         * </pre>
         */
        public reactor.core.publisher.Flux<houston.grpc.service.RunVerifierResponse> runVerifier(reactor.core.publisher.Mono<houston.grpc.service.UploadVerifierRequest> reactorRequest) {
            return com.salesforce.reactorgrpc.stub.ClientCalls.oneToMany(reactorRequest, delegateStub::runVerifier, getCallOptions());
        }

        /**
         * <pre>
         *  스키마
         * </pre>
         */
        public reactor.core.publisher.Flux<houston.grpc.service.AssetSchema> schema(com.google.protobuf.Empty reactorRequest) {
           return com.salesforce.reactorgrpc.stub.ClientCalls.oneToMany(reactor.core.publisher.Mono.just(reactorRequest), delegateStub::schema, getCallOptions());
        }

        /**
         * <pre>
         *  무결성 검사 실행
         * </pre>
         */
        public reactor.core.publisher.Flux<houston.grpc.service.RunVerifierResponse> runVerifier(houston.grpc.service.UploadVerifierRequest reactorRequest) {
           return com.salesforce.reactorgrpc.stub.ClientCalls.oneToMany(reactor.core.publisher.Mono.just(reactorRequest), delegateStub::runVerifier, getCallOptions());
        }

    }

    /**
     * <pre>
     *  플러그인 서비스
     * </pre>
     */
    public static abstract class PluginServiceImplBase implements io.grpc.BindableService {

        /**
         * <pre>
         *  스키마
         * </pre>
         */
        public reactor.core.publisher.Flux<houston.grpc.service.AssetSchema> schema(com.google.protobuf.Empty request) {
            return schema(reactor.core.publisher.Mono.just(request));
        }

        /**
         * <pre>
         *  스키마
         * </pre>
         */
        public reactor.core.publisher.Flux<houston.grpc.service.AssetSchema> schema(reactor.core.publisher.Mono<com.google.protobuf.Empty> request) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        /**
         * <pre>
         *  무결성 검사 실행
         * </pre>
         */
        public reactor.core.publisher.Flux<houston.grpc.service.RunVerifierResponse> runVerifier(houston.grpc.service.UploadVerifierRequest request) {
            return runVerifier(reactor.core.publisher.Mono.just(request));
        }

        /**
         * <pre>
         *  무결성 검사 실행
         * </pre>
         */
        public reactor.core.publisher.Flux<houston.grpc.service.RunVerifierResponse> runVerifier(reactor.core.publisher.Mono<houston.grpc.service.UploadVerifierRequest> request) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            houston.grpc.service.PluginServiceGrpc.getSchemaMethod(),
                            asyncServerStreamingCall(
                                    new MethodHandlers<
                                            com.google.protobuf.Empty,
                                            houston.grpc.service.AssetSchema>(
                                            this, METHODID_SCHEMA)))
                    .addMethod(
                            houston.grpc.service.PluginServiceGrpc.getRunVerifierMethod(),
                            asyncServerStreamingCall(
                                    new MethodHandlers<
                                            houston.grpc.service.UploadVerifierRequest,
                                            houston.grpc.service.RunVerifierResponse>(
                                            this, METHODID_RUN_VERIFIER)))
                    .build();
        }

        protected io.grpc.CallOptions getCallOptions(int methodId) {
            return null;
        }

        protected Throwable onErrorMap(Throwable throwable) {
            return com.salesforce.reactorgrpc.stub.ServerCalls.prepareError(throwable);
        }
    }

    public static final int METHODID_SCHEMA = 0;
    public static final int METHODID_RUN_VERIFIER = 1;

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final PluginServiceImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(PluginServiceImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_SCHEMA:
                    com.salesforce.reactorgrpc.stub.ServerCalls.oneToMany((com.google.protobuf.Empty) request,
                            (io.grpc.stub.StreamObserver<houston.grpc.service.AssetSchema>) responseObserver,
                            serviceImpl::schema, serviceImpl::onErrorMap);
                    break;
                case METHODID_RUN_VERIFIER:
                    com.salesforce.reactorgrpc.stub.ServerCalls.oneToMany((houston.grpc.service.UploadVerifierRequest) request,
                            (io.grpc.stub.StreamObserver<houston.grpc.service.RunVerifierResponse>) responseObserver,
                            serviceImpl::runVerifier, serviceImpl::onErrorMap);
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

package houston.grpc.service;

import static houston.grpc.service.AssetServiceGrpc.getServiceDescriptor;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;


@javax.annotation.Generated(
value = "by ReactorGrpc generator",
comments = "Source: AssetService.proto")
public final class ReactorAssetServiceGrpc {
    private ReactorAssetServiceGrpc() {}

    public static ReactorAssetServiceStub newReactorStub(io.grpc.Channel channel) {
        return new ReactorAssetServiceStub(channel);
    }

    /**
     * <pre>
     *  애셋 서비스
     * </pre>
     */
    public static final class ReactorAssetServiceStub extends io.grpc.stub.AbstractStub<ReactorAssetServiceStub> {
        private AssetServiceGrpc.AssetServiceStub delegateStub;

        private ReactorAssetServiceStub(io.grpc.Channel channel) {
            super(channel);
            delegateStub = AssetServiceGrpc.newStub(channel);
        }

        private ReactorAssetServiceStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
            delegateStub = AssetServiceGrpc.newStub(channel).build(channel, callOptions);
        }

        @Override
        protected ReactorAssetServiceStub build(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new ReactorAssetServiceStub(channel, callOptions);
        }

        /**
         * <pre>
         *  리스트
         * </pre>
         */
        public reactor.core.publisher.Flux<houston.grpc.service.AssetSheet> list(reactor.core.publisher.Mono<com.google.protobuf.Empty> reactorRequest) {
            return com.salesforce.reactorgrpc.stub.ClientCalls.oneToMany(reactorRequest, delegateStub::list, getCallOptions());
        }

        /**
         * <pre>
         *  쿼리
         * </pre>
         */
        public reactor.core.publisher.Flux<com.google.protobuf.Any> query(reactor.core.publisher.Mono<houston.grpc.service.AssetQueryRequest> reactorRequest) {
            return com.salesforce.reactorgrpc.stub.ClientCalls.oneToMany(reactorRequest, delegateStub::query, getCallOptions());
        }

        /**
         * <pre>
         *  리스트
         * </pre>
         */
        public reactor.core.publisher.Flux<houston.grpc.service.AssetSheet> list(com.google.protobuf.Empty reactorRequest) {
           return com.salesforce.reactorgrpc.stub.ClientCalls.oneToMany(reactor.core.publisher.Mono.just(reactorRequest), delegateStub::list, getCallOptions());
        }

        /**
         * <pre>
         *  쿼리
         * </pre>
         */
        public reactor.core.publisher.Flux<com.google.protobuf.Any> query(houston.grpc.service.AssetQueryRequest reactorRequest) {
           return com.salesforce.reactorgrpc.stub.ClientCalls.oneToMany(reactor.core.publisher.Mono.just(reactorRequest), delegateStub::query, getCallOptions());
        }

    }

    /**
     * <pre>
     *  애셋 서비스
     * </pre>
     */
    public static abstract class AssetServiceImplBase implements io.grpc.BindableService {

        /**
         * <pre>
         *  리스트
         * </pre>
         */
        public reactor.core.publisher.Flux<houston.grpc.service.AssetSheet> list(com.google.protobuf.Empty request) {
            return list(reactor.core.publisher.Mono.just(request));
        }

        /**
         * <pre>
         *  리스트
         * </pre>
         */
        public reactor.core.publisher.Flux<houston.grpc.service.AssetSheet> list(reactor.core.publisher.Mono<com.google.protobuf.Empty> request) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        /**
         * <pre>
         *  쿼리
         * </pre>
         */
        public reactor.core.publisher.Flux<com.google.protobuf.Any> query(houston.grpc.service.AssetQueryRequest request) {
            return query(reactor.core.publisher.Mono.just(request));
        }

        /**
         * <pre>
         *  쿼리
         * </pre>
         */
        public reactor.core.publisher.Flux<com.google.protobuf.Any> query(reactor.core.publisher.Mono<houston.grpc.service.AssetQueryRequest> request) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            houston.grpc.service.AssetServiceGrpc.getListMethod(),
                            asyncServerStreamingCall(
                                    new MethodHandlers<
                                            com.google.protobuf.Empty,
                                            houston.grpc.service.AssetSheet>(
                                            this, METHODID_LIST)))
                    .addMethod(
                            houston.grpc.service.AssetServiceGrpc.getQueryMethod(),
                            asyncServerStreamingCall(
                                    new MethodHandlers<
                                            houston.grpc.service.AssetQueryRequest,
                                            com.google.protobuf.Any>(
                                            this, METHODID_QUERY)))
                    .build();
        }

        protected io.grpc.CallOptions getCallOptions(int methodId) {
            return null;
        }

        protected Throwable onErrorMap(Throwable throwable) {
            return com.salesforce.reactorgrpc.stub.ServerCalls.prepareError(throwable);
        }
    }

    public static final int METHODID_LIST = 0;
    public static final int METHODID_QUERY = 1;

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final AssetServiceImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(AssetServiceImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_LIST:
                    com.salesforce.reactorgrpc.stub.ServerCalls.oneToMany((com.google.protobuf.Empty) request,
                            (io.grpc.stub.StreamObserver<houston.grpc.service.AssetSheet>) responseObserver,
                            serviceImpl::list, serviceImpl::onErrorMap);
                    break;
                case METHODID_QUERY:
                    com.salesforce.reactorgrpc.stub.ServerCalls.oneToMany((houston.grpc.service.AssetQueryRequest) request,
                            (io.grpc.stub.StreamObserver<com.google.protobuf.Any>) responseObserver,
                            serviceImpl::query, serviceImpl::onErrorMap);
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

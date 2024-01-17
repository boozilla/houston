package houston.grpc.service;

import static houston.grpc.service.ManifestServiceGrpc.getServiceDescriptor;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;


@javax.annotation.Generated(
value = "by ReactorGrpc generator",
comments = "Source: ManifestService.proto")
public final class ReactorManifestServiceGrpc {
    private ReactorManifestServiceGrpc() {}

    public static ReactorManifestServiceStub newReactorStub(io.grpc.Channel channel) {
        return new ReactorManifestServiceStub(channel);
    }

    /**
     * <pre>
     *  매니페스트 서비스
     * </pre>
     */
    public static final class ReactorManifestServiceStub extends io.grpc.stub.AbstractStub<ReactorManifestServiceStub> {
        private ManifestServiceGrpc.ManifestServiceStub delegateStub;

        private ReactorManifestServiceStub(io.grpc.Channel channel) {
            super(channel);
            delegateStub = ManifestServiceGrpc.newStub(channel);
        }

        private ReactorManifestServiceStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
            delegateStub = ManifestServiceGrpc.newStub(channel).build(channel, callOptions);
        }

        @Override
        protected ReactorManifestServiceStub build(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new ReactorManifestServiceStub(channel, callOptions);
        }

        /**
         * <pre>
         *  조회
         * </pre>
         */
        public reactor.core.publisher.Mono<houston.grpc.service.Manifest> retrieve(reactor.core.publisher.Mono<houston.grpc.service.ManifestRetrieveRequest> reactorRequest) {
            return com.salesforce.reactorgrpc.stub.ClientCalls.oneToOne(reactorRequest, delegateStub::retrieve, getCallOptions());
        }

        /**
         * <pre>
         *  조회
         * </pre>
         */
        public reactor.core.publisher.Mono<houston.grpc.service.Manifest> retrieve(houston.grpc.service.ManifestRetrieveRequest reactorRequest) {
           return com.salesforce.reactorgrpc.stub.ClientCalls.oneToOne(reactor.core.publisher.Mono.just(reactorRequest), delegateStub::retrieve, getCallOptions());
        }

    }

    /**
     * <pre>
     *  매니페스트 서비스
     * </pre>
     */
    public static abstract class ManifestServiceImplBase implements io.grpc.BindableService {

        /**
         * <pre>
         *  조회
         * </pre>
         */
        public reactor.core.publisher.Mono<houston.grpc.service.Manifest> retrieve(houston.grpc.service.ManifestRetrieveRequest request) {
            return retrieve(reactor.core.publisher.Mono.just(request));
        }

        /**
         * <pre>
         *  조회
         * </pre>
         */
        public reactor.core.publisher.Mono<houston.grpc.service.Manifest> retrieve(reactor.core.publisher.Mono<houston.grpc.service.ManifestRetrieveRequest> request) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            houston.grpc.service.ManifestServiceGrpc.getRetrieveMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            houston.grpc.service.ManifestRetrieveRequest,
                                            houston.grpc.service.Manifest>(
                                            this, METHODID_RETRIEVE)))
                    .build();
        }

        protected io.grpc.CallOptions getCallOptions(int methodId) {
            return null;
        }

        protected Throwable onErrorMap(Throwable throwable) {
            return com.salesforce.reactorgrpc.stub.ServerCalls.prepareError(throwable);
        }
    }

    public static final int METHODID_RETRIEVE = 0;

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final ManifestServiceImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(ManifestServiceImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_RETRIEVE:
                    com.salesforce.reactorgrpc.stub.ServerCalls.oneToOne((houston.grpc.service.ManifestRetrieveRequest) request,
                            (io.grpc.stub.StreamObserver<houston.grpc.service.Manifest>) responseObserver,
                            serviceImpl::retrieve, serviceImpl::onErrorMap);
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

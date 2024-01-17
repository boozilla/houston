package houston.grpc.service;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * 매니페스트 서비스
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.60.0)",
    comments = "Source: ManifestService.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class ManifestServiceGrpc {

  private ManifestServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "houston.service.ManifestService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<houston.grpc.service.ManifestRetrieveRequest,
      houston.grpc.service.Manifest> getRetrieveMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "retrieve",
      requestType = houston.grpc.service.ManifestRetrieveRequest.class,
      responseType = houston.grpc.service.Manifest.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<houston.grpc.service.ManifestRetrieveRequest,
      houston.grpc.service.Manifest> getRetrieveMethod() {
    io.grpc.MethodDescriptor<houston.grpc.service.ManifestRetrieveRequest, houston.grpc.service.Manifest> getRetrieveMethod;
    if ((getRetrieveMethod = ManifestServiceGrpc.getRetrieveMethod) == null) {
      synchronized (ManifestServiceGrpc.class) {
        if ((getRetrieveMethod = ManifestServiceGrpc.getRetrieveMethod) == null) {
          ManifestServiceGrpc.getRetrieveMethod = getRetrieveMethod =
              io.grpc.MethodDescriptor.<houston.grpc.service.ManifestRetrieveRequest, houston.grpc.service.Manifest>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "retrieve"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  houston.grpc.service.ManifestRetrieveRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  houston.grpc.service.Manifest.getDefaultInstance()))
              .setSchemaDescriptor(new ManifestServiceMethodDescriptorSupplier("retrieve"))
              .build();
        }
      }
    }
    return getRetrieveMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ManifestServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ManifestServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ManifestServiceStub>() {
        @java.lang.Override
        public ManifestServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ManifestServiceStub(channel, callOptions);
        }
      };
    return ManifestServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ManifestServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ManifestServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ManifestServiceBlockingStub>() {
        @java.lang.Override
        public ManifestServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ManifestServiceBlockingStub(channel, callOptions);
        }
      };
    return ManifestServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ManifestServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ManifestServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ManifestServiceFutureStub>() {
        @java.lang.Override
        public ManifestServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ManifestServiceFutureStub(channel, callOptions);
        }
      };
    return ManifestServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * 매니페스트 서비스
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * 조회
     * </pre>
     */
    default void retrieve(houston.grpc.service.ManifestRetrieveRequest request,
        io.grpc.stub.StreamObserver<houston.grpc.service.Manifest> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRetrieveMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service ManifestService.
   * <pre>
   * 매니페스트 서비스
   * </pre>
   */
  public static abstract class ManifestServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return ManifestServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service ManifestService.
   * <pre>
   * 매니페스트 서비스
   * </pre>
   */
  public static final class ManifestServiceStub
      extends io.grpc.stub.AbstractAsyncStub<ManifestServiceStub> {
    private ManifestServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ManifestServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ManifestServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * 조회
     * </pre>
     */
    public void retrieve(houston.grpc.service.ManifestRetrieveRequest request,
        io.grpc.stub.StreamObserver<houston.grpc.service.Manifest> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRetrieveMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service ManifestService.
   * <pre>
   * 매니페스트 서비스
   * </pre>
   */
  public static final class ManifestServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<ManifestServiceBlockingStub> {
    private ManifestServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ManifestServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ManifestServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * 조회
     * </pre>
     */
    public houston.grpc.service.Manifest retrieve(houston.grpc.service.ManifestRetrieveRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRetrieveMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service ManifestService.
   * <pre>
   * 매니페스트 서비스
   * </pre>
   */
  public static final class ManifestServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<ManifestServiceFutureStub> {
    private ManifestServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ManifestServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ManifestServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * 조회
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<houston.grpc.service.Manifest> retrieve(
        houston.grpc.service.ManifestRetrieveRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRetrieveMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_RETRIEVE = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_RETRIEVE:
          serviceImpl.retrieve((houston.grpc.service.ManifestRetrieveRequest) request,
              (io.grpc.stub.StreamObserver<houston.grpc.service.Manifest>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getRetrieveMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              houston.grpc.service.ManifestRetrieveRequest,
              houston.grpc.service.Manifest>(
                service, METHODID_RETRIEVE)))
        .build();
  }

  private static abstract class ManifestServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ManifestServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return houston.grpc.service.ManifestServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ManifestService");
    }
  }

  private static final class ManifestServiceFileDescriptorSupplier
      extends ManifestServiceBaseDescriptorSupplier {
    ManifestServiceFileDescriptorSupplier() {}
  }

  private static final class ManifestServiceMethodDescriptorSupplier
      extends ManifestServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    ManifestServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ManifestServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ManifestServiceFileDescriptorSupplier())
              .addMethod(getRetrieveMethod())
              .build();
        }
      }
    }
    return result;
  }
}

package houston.grpc.service;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * 애셋 서비스
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.60.0)",
    comments = "Source: AssetService.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class AssetServiceGrpc {

  private AssetServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "houston.service.AssetService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      houston.grpc.service.AssetSheet> getListMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "list",
      requestType = com.google.protobuf.Empty.class,
      responseType = houston.grpc.service.AssetSheet.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      houston.grpc.service.AssetSheet> getListMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, houston.grpc.service.AssetSheet> getListMethod;
    if ((getListMethod = AssetServiceGrpc.getListMethod) == null) {
      synchronized (AssetServiceGrpc.class) {
        if ((getListMethod = AssetServiceGrpc.getListMethod) == null) {
          AssetServiceGrpc.getListMethod = getListMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, houston.grpc.service.AssetSheet>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "list"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  houston.grpc.service.AssetSheet.getDefaultInstance()))
              .setSchemaDescriptor(new AssetServiceMethodDescriptorSupplier("list"))
              .build();
        }
      }
    }
    return getListMethod;
  }

  private static volatile io.grpc.MethodDescriptor<houston.grpc.service.AssetQueryRequest,
      com.google.protobuf.Any> getQueryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "query",
      requestType = houston.grpc.service.AssetQueryRequest.class,
      responseType = com.google.protobuf.Any.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<houston.grpc.service.AssetQueryRequest,
      com.google.protobuf.Any> getQueryMethod() {
    io.grpc.MethodDescriptor<houston.grpc.service.AssetQueryRequest, com.google.protobuf.Any> getQueryMethod;
    if ((getQueryMethod = AssetServiceGrpc.getQueryMethod) == null) {
      synchronized (AssetServiceGrpc.class) {
        if ((getQueryMethod = AssetServiceGrpc.getQueryMethod) == null) {
          AssetServiceGrpc.getQueryMethod = getQueryMethod =
              io.grpc.MethodDescriptor.<houston.grpc.service.AssetQueryRequest, com.google.protobuf.Any>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "query"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  houston.grpc.service.AssetQueryRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Any.getDefaultInstance()))
              .setSchemaDescriptor(new AssetServiceMethodDescriptorSupplier("query"))
              .build();
        }
      }
    }
    return getQueryMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static AssetServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AssetServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AssetServiceStub>() {
        @java.lang.Override
        public AssetServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AssetServiceStub(channel, callOptions);
        }
      };
    return AssetServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static AssetServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AssetServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AssetServiceBlockingStub>() {
        @java.lang.Override
        public AssetServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AssetServiceBlockingStub(channel, callOptions);
        }
      };
    return AssetServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static AssetServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AssetServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AssetServiceFutureStub>() {
        @java.lang.Override
        public AssetServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AssetServiceFutureStub(channel, callOptions);
        }
      };
    return AssetServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * 애셋 서비스
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * 리스트
     * </pre>
     */
    default void list(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<houston.grpc.service.AssetSheet> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListMethod(), responseObserver);
    }

    /**
     * <pre>
     * 쿼리
     * </pre>
     */
    default void query(houston.grpc.service.AssetQueryRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Any> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getQueryMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service AssetService.
   * <pre>
   * 애셋 서비스
   * </pre>
   */
  public static abstract class AssetServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return AssetServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service AssetService.
   * <pre>
   * 애셋 서비스
   * </pre>
   */
  public static final class AssetServiceStub
      extends io.grpc.stub.AbstractAsyncStub<AssetServiceStub> {
    private AssetServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AssetServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AssetServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * 리스트
     * </pre>
     */
    public void list(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<houston.grpc.service.AssetSheet> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getListMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * 쿼리
     * </pre>
     */
    public void query(houston.grpc.service.AssetQueryRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Any> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getQueryMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service AssetService.
   * <pre>
   * 애셋 서비스
   * </pre>
   */
  public static final class AssetServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<AssetServiceBlockingStub> {
    private AssetServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AssetServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AssetServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * 리스트
     * </pre>
     */
    public java.util.Iterator<houston.grpc.service.AssetSheet> list(
        com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getListMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * 쿼리
     * </pre>
     */
    public java.util.Iterator<com.google.protobuf.Any> query(
        houston.grpc.service.AssetQueryRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getQueryMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service AssetService.
   * <pre>
   * 애셋 서비스
   * </pre>
   */
  public static final class AssetServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<AssetServiceFutureStub> {
    private AssetServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AssetServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AssetServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_LIST = 0;
  private static final int METHODID_QUERY = 1;

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
        case METHODID_LIST:
          serviceImpl.list((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<houston.grpc.service.AssetSheet>) responseObserver);
          break;
        case METHODID_QUERY:
          serviceImpl.query((houston.grpc.service.AssetQueryRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Any>) responseObserver);
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
          getListMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              com.google.protobuf.Empty,
              houston.grpc.service.AssetSheet>(
                service, METHODID_LIST)))
        .addMethod(
          getQueryMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              houston.grpc.service.AssetQueryRequest,
              com.google.protobuf.Any>(
                service, METHODID_QUERY)))
        .build();
  }

  private static abstract class AssetServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    AssetServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return houston.grpc.service.AssetServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("AssetService");
    }
  }

  private static final class AssetServiceFileDescriptorSupplier
      extends AssetServiceBaseDescriptorSupplier {
    AssetServiceFileDescriptorSupplier() {}
  }

  private static final class AssetServiceMethodDescriptorSupplier
      extends AssetServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    AssetServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (AssetServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new AssetServiceFileDescriptorSupplier())
              .addMethod(getListMethod())
              .addMethod(getQueryMethod())
              .build();
        }
      }
    }
    return result;
  }
}

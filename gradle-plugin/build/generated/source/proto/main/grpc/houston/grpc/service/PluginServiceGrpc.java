package houston.grpc.service;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * 플러그인 서비스
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.60.0)",
    comments = "Source: PluginService.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class PluginServiceGrpc {

  private PluginServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "houston.service.PluginService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      houston.grpc.service.AssetSchema> getSchemaMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "schema",
      requestType = com.google.protobuf.Empty.class,
      responseType = houston.grpc.service.AssetSchema.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      houston.grpc.service.AssetSchema> getSchemaMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, houston.grpc.service.AssetSchema> getSchemaMethod;
    if ((getSchemaMethod = PluginServiceGrpc.getSchemaMethod) == null) {
      synchronized (PluginServiceGrpc.class) {
        if ((getSchemaMethod = PluginServiceGrpc.getSchemaMethod) == null) {
          PluginServiceGrpc.getSchemaMethod = getSchemaMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, houston.grpc.service.AssetSchema>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "schema"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  houston.grpc.service.AssetSchema.getDefaultInstance()))
              .setSchemaDescriptor(new PluginServiceMethodDescriptorSupplier("schema"))
              .build();
        }
      }
    }
    return getSchemaMethod;
  }

  private static volatile io.grpc.MethodDescriptor<houston.grpc.service.UploadVerifierRequest,
      houston.grpc.service.RunVerifierResponse> getRunVerifierMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "runVerifier",
      requestType = houston.grpc.service.UploadVerifierRequest.class,
      responseType = houston.grpc.service.RunVerifierResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<houston.grpc.service.UploadVerifierRequest,
      houston.grpc.service.RunVerifierResponse> getRunVerifierMethod() {
    io.grpc.MethodDescriptor<houston.grpc.service.UploadVerifierRequest, houston.grpc.service.RunVerifierResponse> getRunVerifierMethod;
    if ((getRunVerifierMethod = PluginServiceGrpc.getRunVerifierMethod) == null) {
      synchronized (PluginServiceGrpc.class) {
        if ((getRunVerifierMethod = PluginServiceGrpc.getRunVerifierMethod) == null) {
          PluginServiceGrpc.getRunVerifierMethod = getRunVerifierMethod =
              io.grpc.MethodDescriptor.<houston.grpc.service.UploadVerifierRequest, houston.grpc.service.RunVerifierResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "runVerifier"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  houston.grpc.service.UploadVerifierRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  houston.grpc.service.RunVerifierResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PluginServiceMethodDescriptorSupplier("runVerifier"))
              .build();
        }
      }
    }
    return getRunVerifierMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static PluginServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PluginServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PluginServiceStub>() {
        @java.lang.Override
        public PluginServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PluginServiceStub(channel, callOptions);
        }
      };
    return PluginServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static PluginServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PluginServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PluginServiceBlockingStub>() {
        @java.lang.Override
        public PluginServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PluginServiceBlockingStub(channel, callOptions);
        }
      };
    return PluginServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static PluginServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PluginServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PluginServiceFutureStub>() {
        @java.lang.Override
        public PluginServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PluginServiceFutureStub(channel, callOptions);
        }
      };
    return PluginServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * 플러그인 서비스
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * 스키마
     * </pre>
     */
    default void schema(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<houston.grpc.service.AssetSchema> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSchemaMethod(), responseObserver);
    }

    /**
     * <pre>
     * 무결성 검사 실행
     * </pre>
     */
    default void runVerifier(houston.grpc.service.UploadVerifierRequest request,
        io.grpc.stub.StreamObserver<houston.grpc.service.RunVerifierResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRunVerifierMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service PluginService.
   * <pre>
   * 플러그인 서비스
   * </pre>
   */
  public static abstract class PluginServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return PluginServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service PluginService.
   * <pre>
   * 플러그인 서비스
   * </pre>
   */
  public static final class PluginServiceStub
      extends io.grpc.stub.AbstractAsyncStub<PluginServiceStub> {
    private PluginServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PluginServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PluginServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * 스키마
     * </pre>
     */
    public void schema(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<houston.grpc.service.AssetSchema> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getSchemaMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * 무결성 검사 실행
     * </pre>
     */
    public void runVerifier(houston.grpc.service.UploadVerifierRequest request,
        io.grpc.stub.StreamObserver<houston.grpc.service.RunVerifierResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getRunVerifierMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service PluginService.
   * <pre>
   * 플러그인 서비스
   * </pre>
   */
  public static final class PluginServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<PluginServiceBlockingStub> {
    private PluginServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PluginServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PluginServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * 스키마
     * </pre>
     */
    public java.util.Iterator<houston.grpc.service.AssetSchema> schema(
        com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getSchemaMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * 무결성 검사 실행
     * </pre>
     */
    public java.util.Iterator<houston.grpc.service.RunVerifierResponse> runVerifier(
        houston.grpc.service.UploadVerifierRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getRunVerifierMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service PluginService.
   * <pre>
   * 플러그인 서비스
   * </pre>
   */
  public static final class PluginServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<PluginServiceFutureStub> {
    private PluginServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PluginServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PluginServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_SCHEMA = 0;
  private static final int METHODID_RUN_VERIFIER = 1;

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
        case METHODID_SCHEMA:
          serviceImpl.schema((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<houston.grpc.service.AssetSchema>) responseObserver);
          break;
        case METHODID_RUN_VERIFIER:
          serviceImpl.runVerifier((houston.grpc.service.UploadVerifierRequest) request,
              (io.grpc.stub.StreamObserver<houston.grpc.service.RunVerifierResponse>) responseObserver);
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
          getSchemaMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              com.google.protobuf.Empty,
              houston.grpc.service.AssetSchema>(
                service, METHODID_SCHEMA)))
        .addMethod(
          getRunVerifierMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              houston.grpc.service.UploadVerifierRequest,
              houston.grpc.service.RunVerifierResponse>(
                service, METHODID_RUN_VERIFIER)))
        .build();
  }

  private static abstract class PluginServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    PluginServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return houston.grpc.service.PluginServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("PluginService");
    }
  }

  private static final class PluginServiceFileDescriptorSupplier
      extends PluginServiceBaseDescriptorSupplier {
    PluginServiceFileDescriptorSupplier() {}
  }

  private static final class PluginServiceMethodDescriptorSupplier
      extends PluginServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    PluginServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (PluginServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new PluginServiceFileDescriptorSupplier())
              .addMethod(getSchemaMethod())
              .addMethod(getRunVerifierMethod())
              .build();
        }
      }
    }
    return result;
  }
}

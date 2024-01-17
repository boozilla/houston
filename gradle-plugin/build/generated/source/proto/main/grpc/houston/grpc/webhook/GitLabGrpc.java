package houston.grpc.webhook;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * GitLab Webhook 서비스
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.60.0)",
    comments = "Source: Webhook.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class GitLabGrpc {

  private GitLabGrpc() {}

  public static final java.lang.String SERVICE_NAME = "houston.grpc.webhook.GitLab";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<houston.vo.webhook.gitlab.PushEvent,
      com.google.protobuf.Empty> getPushMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "push",
      requestType = houston.vo.webhook.gitlab.PushEvent.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<houston.vo.webhook.gitlab.PushEvent,
      com.google.protobuf.Empty> getPushMethod() {
    io.grpc.MethodDescriptor<houston.vo.webhook.gitlab.PushEvent, com.google.protobuf.Empty> getPushMethod;
    if ((getPushMethod = GitLabGrpc.getPushMethod) == null) {
      synchronized (GitLabGrpc.class) {
        if ((getPushMethod = GitLabGrpc.getPushMethod) == null) {
          GitLabGrpc.getPushMethod = getPushMethod =
              io.grpc.MethodDescriptor.<houston.vo.webhook.gitlab.PushEvent, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "push"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  houston.vo.webhook.gitlab.PushEvent.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new GitLabMethodDescriptorSupplier("push"))
              .build();
        }
      }
    }
    return getPushMethod;
  }

  private static volatile io.grpc.MethodDescriptor<houston.vo.webhook.gitlab.NoteEvent,
      com.google.protobuf.Empty> getNoteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "note",
      requestType = houston.vo.webhook.gitlab.NoteEvent.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<houston.vo.webhook.gitlab.NoteEvent,
      com.google.protobuf.Empty> getNoteMethod() {
    io.grpc.MethodDescriptor<houston.vo.webhook.gitlab.NoteEvent, com.google.protobuf.Empty> getNoteMethod;
    if ((getNoteMethod = GitLabGrpc.getNoteMethod) == null) {
      synchronized (GitLabGrpc.class) {
        if ((getNoteMethod = GitLabGrpc.getNoteMethod) == null) {
          GitLabGrpc.getNoteMethod = getNoteMethod =
              io.grpc.MethodDescriptor.<houston.vo.webhook.gitlab.NoteEvent, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "note"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  houston.vo.webhook.gitlab.NoteEvent.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new GitLabMethodDescriptorSupplier("note"))
              .build();
        }
      }
    }
    return getNoteMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static GitLabStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GitLabStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GitLabStub>() {
        @java.lang.Override
        public GitLabStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GitLabStub(channel, callOptions);
        }
      };
    return GitLabStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static GitLabBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GitLabBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GitLabBlockingStub>() {
        @java.lang.Override
        public GitLabBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GitLabBlockingStub(channel, callOptions);
        }
      };
    return GitLabBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static GitLabFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GitLabFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GitLabFutureStub>() {
        @java.lang.Override
        public GitLabFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GitLabFutureStub(channel, callOptions);
        }
      };
    return GitLabFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * GitLab Webhook 서비스
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * push 이벤트
     * </pre>
     */
    default void push(houston.vo.webhook.gitlab.PushEvent request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPushMethod(), responseObserver);
    }

    /**
     * <pre>
     * note 이벤트
     * </pre>
     */
    default void note(houston.vo.webhook.gitlab.NoteEvent request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getNoteMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service GitLab.
   * <pre>
   * GitLab Webhook 서비스
   * </pre>
   */
  public static abstract class GitLabImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return GitLabGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service GitLab.
   * <pre>
   * GitLab Webhook 서비스
   * </pre>
   */
  public static final class GitLabStub
      extends io.grpc.stub.AbstractAsyncStub<GitLabStub> {
    private GitLabStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GitLabStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GitLabStub(channel, callOptions);
    }

    /**
     * <pre>
     * push 이벤트
     * </pre>
     */
    public void push(houston.vo.webhook.gitlab.PushEvent request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPushMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * note 이벤트
     * </pre>
     */
    public void note(houston.vo.webhook.gitlab.NoteEvent request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getNoteMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service GitLab.
   * <pre>
   * GitLab Webhook 서비스
   * </pre>
   */
  public static final class GitLabBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<GitLabBlockingStub> {
    private GitLabBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GitLabBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GitLabBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * push 이벤트
     * </pre>
     */
    public com.google.protobuf.Empty push(houston.vo.webhook.gitlab.PushEvent request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPushMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * note 이벤트
     * </pre>
     */
    public com.google.protobuf.Empty note(houston.vo.webhook.gitlab.NoteEvent request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getNoteMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service GitLab.
   * <pre>
   * GitLab Webhook 서비스
   * </pre>
   */
  public static final class GitLabFutureStub
      extends io.grpc.stub.AbstractFutureStub<GitLabFutureStub> {
    private GitLabFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GitLabFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GitLabFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * push 이벤트
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> push(
        houston.vo.webhook.gitlab.PushEvent request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPushMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * note 이벤트
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> note(
        houston.vo.webhook.gitlab.NoteEvent request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getNoteMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PUSH = 0;
  private static final int METHODID_NOTE = 1;

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
        case METHODID_PUSH:
          serviceImpl.push((houston.vo.webhook.gitlab.PushEvent) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_NOTE:
          serviceImpl.note((houston.vo.webhook.gitlab.NoteEvent) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
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
          getPushMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              houston.vo.webhook.gitlab.PushEvent,
              com.google.protobuf.Empty>(
                service, METHODID_PUSH)))
        .addMethod(
          getNoteMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              houston.vo.webhook.gitlab.NoteEvent,
              com.google.protobuf.Empty>(
                service, METHODID_NOTE)))
        .build();
  }

  private static abstract class GitLabBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    GitLabBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return houston.grpc.webhook.Webhook.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("GitLab");
    }
  }

  private static final class GitLabFileDescriptorSupplier
      extends GitLabBaseDescriptorSupplier {
    GitLabFileDescriptorSupplier() {}
  }

  private static final class GitLabMethodDescriptorSupplier
      extends GitLabBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    GitLabMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (GitLabGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new GitLabFileDescriptorSupplier())
              .addMethod(getPushMethod())
              .addMethod(getNoteMethod())
              .build();
        }
      }
    }
    return result;
  }
}

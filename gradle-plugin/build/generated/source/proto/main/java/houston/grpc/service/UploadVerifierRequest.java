// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: PluginService.proto

// Protobuf Java Version: 3.25.1
package houston.grpc.service;

/**
 * <pre>
 * 무결성 검사 업로드
 * </pre>
 *
 * Protobuf type {@code houston.service.UploadVerifierRequest}
 */
public final class UploadVerifierRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:houston.service.UploadVerifierRequest)
    UploadVerifierRequestOrBuilder {
private static final long serialVersionUID = 0L;
  // Use UploadVerifierRequest.newBuilder() to construct.
  private UploadVerifierRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private UploadVerifierRequest() {
    className_ = "";
    verifierByteCode_ = com.google.protobuf.ByteString.EMPTY;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new UploadVerifierRequest();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return houston.grpc.service.PluginServiceOuterClass.internal_static_houston_service_UploadVerifierRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return houston.grpc.service.PluginServiceOuterClass.internal_static_houston_service_UploadVerifierRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            houston.grpc.service.UploadVerifierRequest.class, houston.grpc.service.UploadVerifierRequest.Builder.class);
  }

  public static final int CLASSNAME_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object className_ = "";
  /**
   * <code>string className = 1;</code>
   * @return The className.
   */
  @java.lang.Override
  public java.lang.String getClassName() {
    java.lang.Object ref = className_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      className_ = s;
      return s;
    }
  }
  /**
   * <code>string className = 1;</code>
   * @return The bytes for className.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getClassNameBytes() {
    java.lang.Object ref = className_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      className_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int VERIFIERBYTECODE_FIELD_NUMBER = 2;
  private com.google.protobuf.ByteString verifierByteCode_ = com.google.protobuf.ByteString.EMPTY;
  /**
   * <code>bytes verifierByteCode = 2;</code>
   * @return The verifierByteCode.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getVerifierByteCode() {
    return verifierByteCode_;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(className_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, className_);
    }
    if (!verifierByteCode_.isEmpty()) {
      output.writeBytes(2, verifierByteCode_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(className_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, className_);
    }
    if (!verifierByteCode_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream
        .computeBytesSize(2, verifierByteCode_);
    }
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof houston.grpc.service.UploadVerifierRequest)) {
      return super.equals(obj);
    }
    houston.grpc.service.UploadVerifierRequest other = (houston.grpc.service.UploadVerifierRequest) obj;

    if (!getClassName()
        .equals(other.getClassName())) return false;
    if (!getVerifierByteCode()
        .equals(other.getVerifierByteCode())) return false;
    if (!getUnknownFields().equals(other.getUnknownFields())) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + CLASSNAME_FIELD_NUMBER;
    hash = (53 * hash) + getClassName().hashCode();
    hash = (37 * hash) + VERIFIERBYTECODE_FIELD_NUMBER;
    hash = (53 * hash) + getVerifierByteCode().hashCode();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static houston.grpc.service.UploadVerifierRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static houston.grpc.service.UploadVerifierRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static houston.grpc.service.UploadVerifierRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static houston.grpc.service.UploadVerifierRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static houston.grpc.service.UploadVerifierRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static houston.grpc.service.UploadVerifierRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static houston.grpc.service.UploadVerifierRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static houston.grpc.service.UploadVerifierRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static houston.grpc.service.UploadVerifierRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static houston.grpc.service.UploadVerifierRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static houston.grpc.service.UploadVerifierRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static houston.grpc.service.UploadVerifierRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(houston.grpc.service.UploadVerifierRequest prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * <pre>
   * 무결성 검사 업로드
   * </pre>
   *
   * Protobuf type {@code houston.service.UploadVerifierRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:houston.service.UploadVerifierRequest)
      houston.grpc.service.UploadVerifierRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return houston.grpc.service.PluginServiceOuterClass.internal_static_houston_service_UploadVerifierRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return houston.grpc.service.PluginServiceOuterClass.internal_static_houston_service_UploadVerifierRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              houston.grpc.service.UploadVerifierRequest.class, houston.grpc.service.UploadVerifierRequest.Builder.class);
    }

    // Construct using houston.grpc.service.UploadVerifierRequest.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      className_ = "";
      verifierByteCode_ = com.google.protobuf.ByteString.EMPTY;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return houston.grpc.service.PluginServiceOuterClass.internal_static_houston_service_UploadVerifierRequest_descriptor;
    }

    @java.lang.Override
    public houston.grpc.service.UploadVerifierRequest getDefaultInstanceForType() {
      return houston.grpc.service.UploadVerifierRequest.getDefaultInstance();
    }

    @java.lang.Override
    public houston.grpc.service.UploadVerifierRequest build() {
      houston.grpc.service.UploadVerifierRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public houston.grpc.service.UploadVerifierRequest buildPartial() {
      houston.grpc.service.UploadVerifierRequest result = new houston.grpc.service.UploadVerifierRequest(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(houston.grpc.service.UploadVerifierRequest result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.className_ = className_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.verifierByteCode_ = verifierByteCode_;
      }
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof houston.grpc.service.UploadVerifierRequest) {
        return mergeFrom((houston.grpc.service.UploadVerifierRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(houston.grpc.service.UploadVerifierRequest other) {
      if (other == houston.grpc.service.UploadVerifierRequest.getDefaultInstance()) return this;
      if (!other.getClassName().isEmpty()) {
        className_ = other.className_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (other.getVerifierByteCode() != com.google.protobuf.ByteString.EMPTY) {
        setVerifierByteCode(other.getVerifierByteCode());
      }
      this.mergeUnknownFields(other.getUnknownFields());
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 10: {
              className_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              verifierByteCode_ = input.readBytes();
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            default: {
              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                done = true; // was an endgroup tag
              }
              break;
            } // default:
          } // switch (tag)
        } // while (!done)
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.unwrapIOException();
      } finally {
        onChanged();
      } // finally
      return this;
    }
    private int bitField0_;

    private java.lang.Object className_ = "";
    /**
     * <code>string className = 1;</code>
     * @return The className.
     */
    public java.lang.String getClassName() {
      java.lang.Object ref = className_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        className_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string className = 1;</code>
     * @return The bytes for className.
     */
    public com.google.protobuf.ByteString
        getClassNameBytes() {
      java.lang.Object ref = className_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        className_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string className = 1;</code>
     * @param value The className to set.
     * @return This builder for chaining.
     */
    public Builder setClassName(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      className_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>string className = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearClassName() {
      className_ = getDefaultInstance().getClassName();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <code>string className = 1;</code>
     * @param value The bytes for className to set.
     * @return This builder for chaining.
     */
    public Builder setClassNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      className_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private com.google.protobuf.ByteString verifierByteCode_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <code>bytes verifierByteCode = 2;</code>
     * @return The verifierByteCode.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getVerifierByteCode() {
      return verifierByteCode_;
    }
    /**
     * <code>bytes verifierByteCode = 2;</code>
     * @param value The verifierByteCode to set.
     * @return This builder for chaining.
     */
    public Builder setVerifierByteCode(com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      verifierByteCode_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>bytes verifierByteCode = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearVerifierByteCode() {
      bitField0_ = (bitField0_ & ~0x00000002);
      verifierByteCode_ = getDefaultInstance().getVerifierByteCode();
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:houston.service.UploadVerifierRequest)
  }

  // @@protoc_insertion_point(class_scope:houston.service.UploadVerifierRequest)
  private static final houston.grpc.service.UploadVerifierRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new houston.grpc.service.UploadVerifierRequest();
  }

  public static houston.grpc.service.UploadVerifierRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<UploadVerifierRequest>
      PARSER = new com.google.protobuf.AbstractParser<UploadVerifierRequest>() {
    @java.lang.Override
    public UploadVerifierRequest parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      Builder builder = newBuilder();
      try {
        builder.mergeFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(builder.buildPartial());
      } catch (com.google.protobuf.UninitializedMessageException e) {
        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(e)
            .setUnfinishedMessage(builder.buildPartial());
      }
      return builder.buildPartial();
    }
  };

  public static com.google.protobuf.Parser<UploadVerifierRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<UploadVerifierRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public houston.grpc.service.UploadVerifierRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}


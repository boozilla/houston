// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ManifestService.proto

// Protobuf Java Version: 3.25.1
package houston.grpc.service;

public interface ManifestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:houston.service.Manifest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.houston.service.Manifest.Maintenance maintenance = 2;</code>
   * @return Whether the maintenance field is set.
   */
  boolean hasMaintenance();
  /**
   * <code>.houston.service.Manifest.Maintenance maintenance = 2;</code>
   * @return The maintenance.
   */
  houston.grpc.service.Manifest.Maintenance getMaintenance();
  /**
   * <code>.houston.service.Manifest.Maintenance maintenance = 2;</code>
   */
  houston.grpc.service.Manifest.MaintenanceOrBuilder getMaintenanceOrBuilder();

  /**
   * <code>map&lt;string, string&gt; server = 3;</code>
   */
  int getServerCount();
  /**
   * <code>map&lt;string, string&gt; server = 3;</code>
   */
  boolean containsServer(
      java.lang.String key);
  /**
   * Use {@link #getServerMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, java.lang.String>
  getServer();
  /**
   * <code>map&lt;string, string&gt; server = 3;</code>
   */
  java.util.Map<java.lang.String, java.lang.String>
  getServerMap();
  /**
   * <code>map&lt;string, string&gt; server = 3;</code>
   */
  /* nullable */
java.lang.String getServerOrDefault(
      java.lang.String key,
      /* nullable */
java.lang.String defaultValue);
  /**
   * <code>map&lt;string, string&gt; server = 3;</code>
   */
  java.lang.String getServerOrThrow(
      java.lang.String key);

  /**
   * <code>repeated string cdnBaseUrl = 4;</code>
   * @return A list containing the cdnBaseUrl.
   */
  java.util.List<java.lang.String>
      getCdnBaseUrlList();
  /**
   * <code>repeated string cdnBaseUrl = 4;</code>
   * @return The count of cdnBaseUrl.
   */
  int getCdnBaseUrlCount();
  /**
   * <code>repeated string cdnBaseUrl = 4;</code>
   * @param index The index of the element to return.
   * @return The cdnBaseUrl at the given index.
   */
  java.lang.String getCdnBaseUrl(int index);
  /**
   * <code>repeated string cdnBaseUrl = 4;</code>
   * @param index The index of the value to return.
   * @return The bytes of the cdnBaseUrl at the given index.
   */
  com.google.protobuf.ByteString
      getCdnBaseUrlBytes(int index);

  /**
   * <code>string redirectUrl = 5;</code>
   * @return The redirectUrl.
   */
  java.lang.String getRedirectUrl();
  /**
   * <code>string redirectUrl = 5;</code>
   * @return The bytes for redirectUrl.
   */
  com.google.protobuf.ByteString
      getRedirectUrlBytes();

  /**
   * <code>bool unsupported = 6;</code>
   * @return The unsupported.
   */
  boolean getUnsupported();
}

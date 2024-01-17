// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: vo/webhook/gitlab/NoteEvent.proto

// Protobuf Java Version: 3.25.1
package houston.vo.webhook.gitlab;

public final class NoteEventOuterClass {
  private NoteEventOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_houston_vo_webhook_gitlab_NoteEvent_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_houston_vo_webhook_gitlab_NoteEvent_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_houston_vo_webhook_gitlab_NoteEvent_User_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_houston_vo_webhook_gitlab_NoteEvent_User_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_houston_vo_webhook_gitlab_NoteEvent_Repository_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_houston_vo_webhook_gitlab_NoteEvent_Repository_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_houston_vo_webhook_gitlab_NoteEvent_Project_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_houston_vo_webhook_gitlab_NoteEvent_Project_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_houston_vo_webhook_gitlab_NoteEvent_ObjectAttributes_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_houston_vo_webhook_gitlab_NoteEvent_ObjectAttributes_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_houston_vo_webhook_gitlab_NoteEvent_Issue_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_houston_vo_webhook_gitlab_NoteEvent_Issue_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_houston_vo_webhook_gitlab_NoteEvent_Issue_Label_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_houston_vo_webhook_gitlab_NoteEvent_Issue_Label_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n!vo/webhook/gitlab/NoteEvent.proto\022\031hou" +
      "ston.vo.webhook.gitlab\"\224\004\n\tNoteEvent\0227\n\004" +
      "user\030\001 \001(\0132).houston.vo.webhook.gitlab.N" +
      "oteEvent.User\022=\n\007project\030\002 \001(\0132,.houston" +
      ".vo.webhook.gitlab.NoteEvent.Project\022P\n\021" +
      "object_attributes\030\003 \001(\01325.houston.vo.web" +
      "hook.gitlab.NoteEvent.ObjectAttributes\022C" +
      "\n\nrepository\030\004 \001(\0132/.houston.vo.webhook." +
      "gitlab.NoteEvent.Repository\0229\n\005issue\030\005 \001" +
      "(\0132*.houston.vo.webhook.gitlab.NoteEvent" +
      ".Issue\032\006\n\004User\032\014\n\nRepository\032\025\n\007Project\022" +
      "\n\n\002id\030\001 \001(\003\032 \n\020ObjectAttributes\022\014\n\004note\030" +
      "\001 \001(\t\032n\n\005Issue\022\013\n\003iid\030\001 \001(\003\022@\n\006labels\030\002 " +
      "\003(\01320.houston.vo.webhook.gitlab.NoteEven" +
      "t.Issue.Label\032\026\n\005Label\022\r\n\005title\030\001 \001(\tB\035\n" +
      "\031houston.vo.webhook.gitlabP\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_houston_vo_webhook_gitlab_NoteEvent_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_houston_vo_webhook_gitlab_NoteEvent_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_houston_vo_webhook_gitlab_NoteEvent_descriptor,
        new java.lang.String[] { "User", "Project", "ObjectAttributes", "Repository", "Issue", });
    internal_static_houston_vo_webhook_gitlab_NoteEvent_User_descriptor =
      internal_static_houston_vo_webhook_gitlab_NoteEvent_descriptor.getNestedTypes().get(0);
    internal_static_houston_vo_webhook_gitlab_NoteEvent_User_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_houston_vo_webhook_gitlab_NoteEvent_User_descriptor,
        new java.lang.String[] { });
    internal_static_houston_vo_webhook_gitlab_NoteEvent_Repository_descriptor =
      internal_static_houston_vo_webhook_gitlab_NoteEvent_descriptor.getNestedTypes().get(1);
    internal_static_houston_vo_webhook_gitlab_NoteEvent_Repository_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_houston_vo_webhook_gitlab_NoteEvent_Repository_descriptor,
        new java.lang.String[] { });
    internal_static_houston_vo_webhook_gitlab_NoteEvent_Project_descriptor =
      internal_static_houston_vo_webhook_gitlab_NoteEvent_descriptor.getNestedTypes().get(2);
    internal_static_houston_vo_webhook_gitlab_NoteEvent_Project_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_houston_vo_webhook_gitlab_NoteEvent_Project_descriptor,
        new java.lang.String[] { "Id", });
    internal_static_houston_vo_webhook_gitlab_NoteEvent_ObjectAttributes_descriptor =
      internal_static_houston_vo_webhook_gitlab_NoteEvent_descriptor.getNestedTypes().get(3);
    internal_static_houston_vo_webhook_gitlab_NoteEvent_ObjectAttributes_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_houston_vo_webhook_gitlab_NoteEvent_ObjectAttributes_descriptor,
        new java.lang.String[] { "Note", });
    internal_static_houston_vo_webhook_gitlab_NoteEvent_Issue_descriptor =
      internal_static_houston_vo_webhook_gitlab_NoteEvent_descriptor.getNestedTypes().get(4);
    internal_static_houston_vo_webhook_gitlab_NoteEvent_Issue_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_houston_vo_webhook_gitlab_NoteEvent_Issue_descriptor,
        new java.lang.String[] { "Iid", "Labels", });
    internal_static_houston_vo_webhook_gitlab_NoteEvent_Issue_Label_descriptor =
      internal_static_houston_vo_webhook_gitlab_NoteEvent_Issue_descriptor.getNestedTypes().get(0);
    internal_static_houston_vo_webhook_gitlab_NoteEvent_Issue_Label_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_houston_vo_webhook_gitlab_NoteEvent_Issue_Label_descriptor,
        new java.lang.String[] { "Title", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}

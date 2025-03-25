package boozilla.houston.grpc.webhook.client.gitlab.notes;

public class NotesGetResponse {
    public record Note(
            String body
    ) {

    }
}

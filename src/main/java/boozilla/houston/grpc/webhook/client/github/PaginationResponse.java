package boozilla.houston.grpc.webhook.client.github;

public interface PaginationResponse<T extends PaginationResponse<?>> {
    T merge(T other);
}

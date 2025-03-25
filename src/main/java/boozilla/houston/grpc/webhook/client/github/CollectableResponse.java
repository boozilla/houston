package boozilla.houston.grpc.webhook.client.github;

public interface CollectableResponse<T extends CollectableResponse<?>> {
    T accumulate(T other);
}

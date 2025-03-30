package boozilla.houston.asset;

public record QueryResultInfo(
        int size,
        int mergeCost,
        int retrievalCost
) {
}

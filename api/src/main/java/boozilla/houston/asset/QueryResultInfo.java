package boozilla.houston.asset;

public record QueryResultInfo(
        String commitId,
        int size,
        int mergeCost,
        int retrievalCost
) {
}

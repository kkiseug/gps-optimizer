package core;

public record StepReport(
    String filterName,
    int beforeCount,
    int afterCount,
    int modifiedCount
) {
    public int removedCount() {
        return beforeCount - afterCount;
    }
}

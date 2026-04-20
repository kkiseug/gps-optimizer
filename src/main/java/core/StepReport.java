package core;

public record StepReport(
    String filterName,
    int beforeCount,
    int afterCount,
    int modifiedCount
) {
    public static StepReport ofRemoval(String name, int before, int after) {
        return new StepReport(name, before, after, 0);
    }

    public static StepReport ofModification(String name, int count, int modified) {
        return new StepReport(name, count, count, modified);
    }

    public int removedCount() {
        return beforeCount - afterCount;
    }
}

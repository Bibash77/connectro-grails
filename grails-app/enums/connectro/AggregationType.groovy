package connectro

enum AggregationType {
    AVG("Average"),
    SUM("Sum"),
    MIN("Minimum"),
    MAX("Maximum"),
    TERMS("Group by Terms")

    final String label

    AggregationType(String label) {
        this.label = label
    }

    String toString() {
        return label
    }
}

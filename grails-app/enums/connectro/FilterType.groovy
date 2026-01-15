package connectro

enum FilterType {
    TERM("Exact Match"),
    MATCH("Similar Match"),
    FUZZY("Fuzzy Match")

    final String label

    FilterType(String label) {
        this.label = label
    }

    String toString() {
        return label
    }
}

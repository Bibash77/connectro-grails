package dto

/**
 * Response DTO for category suggestions endpoint
 */
class CategorySuggestionsResponse {
    List<CategorySuggestion> categories

    CategorySuggestionsResponse(List<CategorySuggestion> categories) {
        this.categories = categories
    }

    static class CategorySuggestion {
        String category
        Long count

        CategorySuggestion(String category, Long count) {
            this.category = category
            this.count = count
        }
    }
}

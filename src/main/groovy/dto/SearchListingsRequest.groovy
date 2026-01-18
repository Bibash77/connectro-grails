package dto

/**
 * Request DTO for search listings endpoint
 */
class SearchListingsRequest {
    String q              // Free text query
    String city           // Required city filter
    String category       // Optional category filter
    Integer page = 0      // Page number (default: 0)
    Integer size = 10     // Page size (default: 10)

    Map<String, Object> toParams() {
        return [
            q: q,
            city: city,
            category: category,
            page: page ?: 0,
            size: size ?: 10
        ]
    }

    Integer getFrom() {
        return (page ?: 0) * (size ?: 10)
    }
}

package dto

/**
 * Response DTO for search listings endpoint
 */
class SearchListingsResponse {
    List<Map<String, Object>> listings
    Long total
    Integer page
    Integer size

    SearchListingsResponse(List<Map<String, Object>> listings, Long total, Integer page, Integer size) {
        this.listings = listings
        this.total = total
        this.page = page
        this.size = size
    }
}

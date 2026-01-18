package connectro.rest

import dto.ApiResponse
import dto.SearchListingsRequest
import dto.NearbyRequest
import grails.converters.JSON
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import service.V0SearchService

/**
 * V0 Search Controller
 * Handles: search/listings, nearby, category suggestions
 * Thin controller - all logic in service layer
 */
@Tag(name = "Search", description = "Search endpoints for business listings")
class SearchController {

    V0SearchService v0SearchService

    /**
     * GET /search/listings
     * Search listings with free text, city, and optional category filters
     * 
     * Query Params:
     * - q: free text query (optional)
     * - city: required city filter
     * - category: optional category filter
     * - page: page number (default: 0)
     * - size: page size (default: 10)
     */
    @Operation(
        summary = "Search listings",
        description = "Search business listings with free text query, city filter, and optional category filter. Uses multi-match on name, normalized_name, category, category_tokens, and tags with boosted scoring.",
        parameters = [
            @Parameter(name = "q", description = "Free text query for searching", required = false),
            @Parameter(name = "city", description = "City name (required)", required = true),
            @Parameter(name = "category", description = "Category filter (optional)", required = false),
            @Parameter(name = "page", description = "Page number (default: 0)", required = false),
            @Parameter(name = "size", description = "Page size (default: 10)", required = false)
        ]
    )
    @ApiResponses(value = [
        @ApiResponse(responseCode = "200", description = "Successful response"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    ])
    def listings() {
        try {
            def request = new SearchListingsRequest(
                q: params.q,
                city: params.city,
                category: params.category,
                page: params.int('page', 0),
                size: params.int('size', 10)
            )

            if (!request.city) {
                render new ApiResponse("City parameter is required", false) as JSON
                return
            }

            def response = v0SearchService.searchListings(request)
            render new ApiResponse(response, "Listings fetched successfully") as JSON

        } catch (IllegalArgumentException e) {
            render new ApiResponse(e.message, false) as JSON
        } catch (Exception e) {
            log.error("Error searching listings: ${e.message}", e)
            render new ApiResponse("Error searching listings: ${e.message}", false) as JSON
        }
    }

    /**
     * GET /search/nearby
     * Find nearby listings using geo-distance search
     * 
     * Query Params:
     * - lat: latitude (required)
     * - lon: longitude (required)
     * - city: required city filter
     * - distance: distance radius (default: 5km)
     * - size: result size (default: 10)
     */
    @Operation(
        summary = "Find nearby listings",
        description = "Search for business listings within a specified distance from given coordinates. Results are sorted by distance (nearest first).",
        parameters = [
            @Parameter(name = "lat", description = "Latitude (required)", required = true),
            @Parameter(name = "lon", description = "Longitude (required)", required = true),
            @Parameter(name = "city", description = "City name (required)", required = true),
            @Parameter(name = "distance", description = "Distance radius (default: 5km)", required = false),
            @Parameter(name = "size", description = "Result size (default: 10)", required = false)
        ]
    )
    @ApiResponses(value = [
        @ApiResponse(responseCode = "200", description = "Successful response"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters (missing lat, lon, or city)")
    ])
    def nearby() {
        try {
            def request = new NearbyRequest(
                lat: params.double('lat'),
                lon: params.double('lon'),
                city: params.city,
                distance: params.distance ?: "5km",
                size: params.int('size', 10)
            )

            if (!request.city) {
                render new ApiResponse("City parameter is required", false) as JSON
                return
            }

            if (request.lat == null || request.lon == null) {
                render new ApiResponse("Latitude and longitude parameters are required", false) as JSON
                return
            }

            def listings = v0SearchService.searchNearby(request)
            render new ApiResponse(listings, "Nearby listings fetched successfully") as JSON

        } catch (IllegalArgumentException e) {
            render new ApiResponse(e.message, false) as JSON
        } catch (Exception e) {
            log.error("Error searching nearby listings: ${e.message}", e)
            render new ApiResponse("Error searching nearby listings: ${e.message}", false) as JSON
        }
    }

    /**
     * GET /search/categories
     * Get category suggestions using fuzzy aggregation
     * 
     * Query Params:
     * - q: partial keyword (optional)
     */
    @Operation(
        summary = "Get category suggestions",
        description = "Retrieve category suggestions using fuzzy matching on category and category_tokens fields. Useful for autocomplete/typeahead functionality.",
        parameters = [
            @Parameter(name = "q", description = "Partial keyword for fuzzy matching (optional)", required = false)
        ]
    )
    @ApiResponses(value = [
        @ApiResponse(responseCode = "200", description = "Successful response")
    ])
    def categories() {
        try {
            String query = params.q
            def response = v0SearchService.getCategorySuggestions(query)
            render new ApiResponse(response, "Category suggestions fetched successfully") as JSON

        } catch (Exception e) {
            log.error("Error fetching category suggestions: ${e.message}", e)
            render new ApiResponse("Error fetching category suggestions: ${e.message}", false) as JSON
        }
    }
}

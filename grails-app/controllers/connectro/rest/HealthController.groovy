package connectro.rest

import grails.converters.JSON
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import service.V0SearchService

/**
 * Health Check Controller
 * Provides health endpoints for API and Elasticsearch connectivity
 */
@Tag(name = "Health", description = "Health check and monitoring endpoints")
class HealthController {

    V0SearchService v0SearchService

    /**
     * GET /health
     * Basic API health check
     */
    @Operation(
        summary = "API health check",
        description = "Returns basic API health status"
    )
    @ApiResponses(value = [
        @ApiResponse(responseCode = "200", description = "API is up and running")
    ])
    def index() {
        render([
            status: "UP",
            timestamp: new Date().time
        ] as JSON)
    }

    /**
     * GET /health/elastic
     * Elasticsearch connectivity health check
     */
    @Operation(
        summary = "Elasticsearch health check",
        description = "Checks Elasticsearch connectivity and returns connection status"
    )
    @ApiResponses(value = [
        @ApiResponse(responseCode = "200", description = "Health check completed (check status field for UP/DOWN)")
    ])
    def elastic() {
        boolean isHealthy = v0SearchService.isElasticsearchHealthy()
        render([
            status: isHealthy ? "UP" : "DOWN",
            elasticsearch: isHealthy ? "CONNECTED" : "DISCONNECTED",
            timestamp: new Date().time
        ] as JSON)
    }
}

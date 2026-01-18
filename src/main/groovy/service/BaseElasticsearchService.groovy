package service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.SearchResponse
import co.elastic.clients.elasticsearch.core.search.Hit
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

/**
 * Base service for Elasticsearch operations
 * Handles common functionality like client access, timeouts, and error handling
 */
@Slf4j
abstract class BaseElasticsearchService {

    @Autowired
    protected ElasticsearchClient elasticsearchClient

    @Value('${elasticsearch.index:connectro}')
    protected String defaultIndex

    @Value('${elasticsearch.requestTimeout:30s}')
    protected String requestTimeout

    /**
     * Execute a search request with error handling
     * @param request The search request to execute
     * @return SearchResponse with typed results
     */
    protected <T> SearchResponse<T> executeSearch(SearchRequest request, Class<T> documentClass = Map) {
        try {
            log.debug("Executing search on index: ${request.index()}")
            SearchResponse<T> response = elasticsearchClient.search(request, documentClass)
            log.debug("Search completed: ${response.hits().total().value()} results found")
            return response
        } catch (Exception e) {
            log.error("Elasticsearch search failed: ${e.message}", e)
            throw new RuntimeException("Search failed: ${e.message}", e)
        }
    }

    /**
     * Extract source data from search hits
     * @param hits List of search hits
     * @return List of source documents
     */
    protected <T> List<T> extractSourceData(List<Hit<T>> hits) {
        return hits.collect { hit -> hit.source() }
    }

    /**
     * Check Elasticsearch connectivity
     * @return true if ES is reachable, false otherwise
     */
    boolean isElasticsearchHealthy() {
        try {
            elasticsearchClient.cluster().health()
            return true
        } catch (Exception e) {
            log.error("Elasticsearch health check failed: ${e.message}", e)
            return false
        }
    }

    /**
     * Get total count from search response
     * @param response The search response
     * @return Total number of matching documents
     */
    protected long getTotalCount(SearchResponse<?> response) {
        return response.hits().total()?.value() ?: 0L
    }
}

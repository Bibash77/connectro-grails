package service

import co.elastic.clients.elasticsearch._types.query_dsl.*
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.SearchResponse
import co.elastic.clients.elasticsearch.core.search.Hit
import dto.SearchListingsRequest
import dto.SearchListingsResponse
import dto.NearbyRequest
import dto.CategorySuggestionsResponse
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * V0 Search Service for Elasticsearch-powered business listings search
 * Handles: search/listings, nearby, category suggestions
 */
@Service
@Slf4j
class V0SearchService extends BaseElasticsearchService {

    @Value('${elasticsearch.index:connectro}')
    private String indexName

    /**
     * Search listings with free text, city, and optional category filters
     * Multi-match on: name, normalized_name, category, category_tokens, tags
     * Boost name and confidence_score
     */
    SearchListingsResponse searchListings(SearchListingsRequest request) {
        if (!request.city) {
            throw new IllegalArgumentException("City parameter is required")
        }

        SearchRequest searchRequest = SearchRequest.of { s ->
            s.index(indexName)
              .from(request.from)
              .size(request.size)
              .query { q ->
                  q.bool { bool ->
                      // Free text multi-match query (if query provided)
                      if (request.q) {
                          bool.must { must ->
                              must.multiMatch { mm ->
                                  mm.query(request.q)
                                    .fields([
                                        "name^3",              // Boost name 3x
                                        "normalized_name^2",   // Boost normalized_name 2x
                                        "category",
                                        "category_tokens",
                                        "tags"
                                    ])
                                    .fuzziness("AUTO")
                                    .type(TextQueryType.BestFields)
                              }
                          }
                      } else {
                          // Match all if no query
                          bool.must { must ->
                              must.matchAll { ma -> }
                          }
                      }

                      // Required city filter
                      bool.filter { filter ->
                          filter.term { term ->
                              term.field("city.keyword")
                                   .value(request.city)
                          }
                      }

                      // Optional category filter
                      if (request.category) {
                          bool.filter { filter ->
                              filter.bool { catBool ->
                                  catBool.should([
                                      { should -> should.term { term -> term.field("category.keyword").value(request.category) }},
                                      { should -> should.term { term -> term.field("category_tokens").value(request.category) }}
                                  ])
                              }
                          }
                      }
                  }
              }
              // Sort by _score (descending) first, then by confidence_score (descending)
              // _score already includes name boost (3x) and normalized_name boost (2x)
              .sort { sort ->
                  sort.score { score -> score.order(SortOrder.Desc) }
              }
              .sort { sort ->
                  sort.field { field -> field.field("confidence_score").order(SortOrder.Desc) }
              }
              // Return only required fields
              .source { source ->
                  source.filter { filter ->
                      filter.includes([
                          "listing_id",
                          "name",
                          "category",
                          "phones",
                          "location",
                          "confidence_score"
                      ])
                  }
              }
        }

        SearchResponse<Map> response = executeSearch(searchRequest, Map.class)
        List<Map<String, Object>> listings = extractAndFormatListings(response.hits().hits())
        Long total = getTotalCount(response)

        return new SearchListingsResponse(listings, total, request.page, request.size)
    }

    /**
     * Find nearby listings using geo-distance search
     * Sort by distance (nearest first)
     */
    List<Map<String, Object>> searchNearby(NearbyRequest request) {
        if (!request.city) {
            throw new IllegalArgumentException("City parameter is required")
        }
        if (request.lat == null || request.lon == null) {
            throw new IllegalArgumentException("Latitude and longitude parameters are required")
        }

        SearchRequest searchRequest = SearchRequest.of { s ->
            s.index(indexName)
              .size(request.size ?: 10)
              .query { q ->
                  q.bool { bool ->
                      // Geo-distance filter
                      bool.filter { filter ->
                          filter.geoDistance { geo ->
                              geo.field("location")
                                 .location { loc -> loc.latlon { latlon ->
                                     latlon.lat(request.lat)
                                           .lon(request.lon)
                                 }}
                                 .distance(request.distance ?: "5km")
                          }
                      }
                      // Required city filter
                      bool.filter { filter ->
                          filter.term { term ->
                              term.field("city.keyword")
                                   .value(request.city)
                          }
                      }
                  }
              }
              // Sort by distance (nearest first)
              .sort { sort ->
                  sort.geoDistance { geo ->
                      geo.field("location")
                         .location { loc -> loc.latlon { latlon ->
                             latlon.lat(request.lat)
                                   .lon(request.lon)
                         }}
                         .order(SortOrder.Asc)
                  }
              }
              .source { source ->
                  source.filter { filter ->
                      filter.includes([
                          "listing_id",
                          "name",
                          "category",
                          "phones",
                          "location",
                          "city",
                          "confidence_score"
                      ])
                  }
              }
        }

        SearchResponse<Map> response = executeSearch(searchRequest, Map.class)
        return extractAndFormatListings(response.hits().hits())
    }

    /**
     * Get category suggestions using fuzzy aggregation
     * Aggregates on category and category_tokens with fuzzy matching
     */
    CategorySuggestionsResponse getCategorySuggestions(String query) {
        // Build aggregation for category suggestions
        Aggregation categoryAgg = Aggregation.of { agg ->
            agg.terms { terms ->
                terms.field("category.keyword")
                     .size(20)
                     .order { order ->
                         order._count(SortOrder.Desc)
                     }
            }
        }

        SearchRequest searchRequest = SearchRequest.of { s ->
            s.index(indexName)
              .size(0)  // No documents, only aggregations
              .query { q ->
                  if (query) {
                      // Fuzzy query on category and category_tokens
                      q.bool { bool ->
                          bool.should([
                              { should -> should.fuzzy { fuzzy ->
                                  fuzzy.field("category")
                                       .value(query)
                                       .fuzziness("AUTO")
                              }},
                              { should -> should.fuzzy { fuzzy ->
                                  fuzzy.field("category_tokens")
                                       .value(query)
                                       .fuzziness("AUTO")
                              }},
                              { should -> should.prefix { prefix ->
                                  prefix.field("category.keyword")
                                       .value(query)
                              }}
                          ])
                      }
                  } else {
                      // Match all if no query
                      q.matchAll { ma -> }
                  }
              }
              .aggregations("categories", categoryAgg)
        }

        SearchResponse<Map> response = executeSearch(searchRequest, Map.class)
        
        // Extract aggregation results
        List<CategorySuggestionsResponse.CategorySuggestion> suggestions = []
        def categoriesAgg = response.aggregations()?.get("categories")
        
        if (categoriesAgg?.isSterms()) {
            def buckets = categoriesAgg.sterms().buckets().array()
            suggestions = buckets.collect { bucket ->
                new CategorySuggestionsResponse.CategorySuggestion(
                    bucket.key().stringValue(),
                    bucket.docCount()
                )
            }
        }

        return new CategorySuggestionsResponse(suggestions)
    }

    /**
     * Extract and format listing data from search hits
     * Masks phone numbers and formats response
     */
    private List<Map<String, Object>> extractAndFormatListings(List<Hit<Map>> hits) {
        return hits.collect { hit ->
            Map<String, Object> source = hit.source()
            Map<String, Object> listing = [
                listing_id: source?.listing_id,
                name: source?.name,
                category: source?.category,
                location: source?.location,
                confidence_score: source?.confidence_score
            ]
            
            // Mask phone numbers - return only masked_number
            if (source?.phones) {
                listing.phones = source.phones.collect { phone ->
                    [
                        masked_number: phone.masked_number ?: phone.number,  // Use masked if available
                        primary: phone.primary ?: false
                    ]
                }
            } else {
                listing.phones = []
            }
            
            listing
        }
    }
}

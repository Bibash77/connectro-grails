package service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import converted.SearchRequestConverter;
import groovy.json.JsonSlurper;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import service.builders.CityWiseUniqueTagsDataBuilder;
import service.builders.ElasticQueryBuilder;
import service.builders.NearbyCompaniesByLocationBuilder;
import service.builders.TopCompaniesByCityDataBuilder;


import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ElasticQueryService {

    private final ElasticsearchClient esClient;
    JsonSlurper jsonSlurper;

    public ElasticQueryService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    public Map<String, Object> searchByFields(String indexName, List<String> fields, List<Map<String, Object>> filters, List<Map<String, Object>> aggregations) throws Exception {


        ElasticQueryBuilder builder = new ElasticQueryBuilder().fields(fields).filters(filters).aggregations(aggregations);

        SearchRequest request = builder.build(indexName);
        SearchResponse<Map> response = esClient.search(request, Map.class);

        List<Map<String, Object>> extracedData = extractSourceData(response.hits().hits());

        Map<String, Object> finalData = new HashMap<>();
        finalData.put("data", extracedData);
        finalData.put("size", response.hits().total());
        finalData.put("aggregation", response.aggregations());

        return finalData;
    }


    public List<Map<String, Object>> extractSourceData(List<Hit<Map>> hitList) throws IOException {
//        Map<String, Object> source = (Map<String, Object>)
        List<Map<String, Object>> sourceList = hitList.stream().map(hit -> (Map<String, Object>) hit.source()).collect(Collectors.toList());


        return sourceList;
    }

    /**
     * request: {
     * "size": 0,
     * "aggs": {
     * "city_agg": {
     * "terms": {
     * "field": "city",
     * "size": 10
     * },
     * "aggs": {
     * "avg_rating": {
     * "avg": {
     * "field": "rating"
     * }
     * }
     * }
     * }
     * }
     * }
     * ------------\n
     * response: {
     * "took": 4,
     * "timed_out": false,
     * "_shards": {
     * "total": 1,
     * "successful": 1,
     * "skipped": 0,
     * "failed": 0
     * },
     * "hits": {
     * "total": {
     * "value": 10,
     * "relation": "eq"
     * },
     * "max_score": null,
     * "hits": []
     * },
     * "aggregations": {
     * "city_agg": {
     * "doc_count_error_upper_bound": 0,
     * "sum_other_doc_count": 0,
     * "buckets": [
     * {
     * "key": "Kathmandu",
     * "doc_count": 7,
     * "avg_rating": {
     * "value": 4.499999931880406
     * }
     * },
     * {
     * "key": "Lalitpur",
     * "doc_count": 3,
     * "avg_rating": {
     * "value": 4.633333524068196
     * }
     * }
     * ]
     * }
     * }
     * }
     **/
    public List<Map<String, Object>> searchAverageRatingByCity() throws IOException {

        List<Map<String, Object>> resultList = new ArrayList<>();

        // defining average aggregation
        Aggregation averageAggregation = Aggregation.of(agBuilder -> agBuilder.avg(fieldBuilder -> fieldBuilder.field("rating")));

        // city wise aggregation
        Aggregation aggregationByCity = Aggregation.of(termBuilder -> termBuilder.terms(fieldBuilder -> fieldBuilder.field("city"))
                // sub aggreagation to get average rating per city
                .aggregations("average_rating", averageAggregation));


        SearchRequest searchRequest = SearchRequest.of(requestBuilder -> {
            return requestBuilder.index(ApplicationConstants.INDEX_NAME).size(0).aggregations("avg_rating_by_city", aggregationByCity);
        });

        SearchResponse<Map> response = esClient.search(searchRequest, Map.class);


// Get the terms aggregation (bucket aggregation)
        Aggregate avgRatingByCityAgg = response.aggregations().get("avg_rating_by_city");

// Ensure it's a bucket aggregate and parse
        if (avgRatingByCityAgg.isSterms()) {
            List<StringTermsBucket> buckets = avgRatingByCityAgg.sterms().buckets().array();

            for (StringTermsBucket bucket : buckets) {
                String city = bucket.key().stringValue();
                long docCount = bucket.docCount();

                Aggregate aggregation = bucket.aggregations().get("average_rating");
                Double avgRating = aggregation.avg().value();

                resultList.add(Map.of("city", city, "docCount", docCount, "avg_rating", avgRating));


            }
        }


        return resultList;
    }


    public List<Map<String, Object>> searchYearWiseCompanyCreation(Map<String, Object> filter) throws IOException {
        List<Map<String, Object>> resultList = new ArrayList<>();

        // 1. Build date_histogram aggregation
        Aggregation dateHistogramAggregation = Aggregation.of(agg ->
                agg.dateHistogram(hist ->
                        hist.field("created_at")
                                .calendarInterval(CalendarInterval.Year)
                                .format("yyyy")
                )
        );

        // 2. Build search request with optional query
        SearchRequest searchRequest = SearchRequest.of(req -> {
            req.index(ApplicationConstants.INDEX_NAME)
                    .size(0)
                    .aggregations("company_established_year", dateHistogramAggregation);

            if (filter != null && filter.get("name") != null && filter.get("value") != null) {
                req.query(q -> q.match(m ->
                        m.field(filter.get("name").toString())
                                .query(filter.get("value").toString())
                ));
            }

            return req;
        });

        // 3. Execute search
        SearchResponse<Map> response = esClient.search(searchRequest, Map.class);

        // 4. Parse aggregation
        Aggregate aggregate = response.aggregations().get("company_established_year");

        if (aggregate != null && aggregate.isDateHistogram()) {
            for (DateHistogramBucket bucket : aggregate.dateHistogram().buckets().array()) {
                String year = bucket.keyAsString();
                long docCount = bucket.docCount();
                resultList.add(Map.of("year", year, "registeredCompanies", docCount));
            }
        }

        return resultList;
    }


    // each categories rating and their (category wise stats such as min max rate
    public List<Map<String, Object>> searchRatingStatsByCategory() throws IOException {
        List<Map<String, Object>> resultList = new ArrayList<>();

        /**
         * for defining:
         *       "standard_stats": {
         *                     "stats": {
         *                         "field": "rating"
         *                     }
         *                 }
         * **/
        Aggregation statsAggregation = Aggregation.of(a -> a
                .stats(s -> s.field("rating"))
        );

        /** define parent "by_category with sub category  ratings stats
         * "by_category": {
         *             "terms": {
         *                 "field": "category"
         *             },
         * **/
        // Define the terms aggregation on "category" with sub-aggregation
        Aggregation categoryAggregation = Aggregation.of(a -> a
                .terms(t -> t.field("category"))
                .aggregations("standard_stats", statsAggregation)
        );

        // Build the search request
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(ApplicationConstants.INDEX_NAME)
                .size(0)
                .aggregations("by_category", categoryAggregation)
        );

        // Execute the search
        SearchResponse<Map> response = esClient.search(searchRequest, Map.class);

        // Parse the aggregation result
        Aggregate byCategoryAgg = response.aggregations().get("by_category");

        if (byCategoryAgg != null && byCategoryAgg.isSterms()) {
            for (StringTermsBucket bucket : byCategoryAgg.sterms().buckets().array()) {
                String category = bucket.key().stringValue();
                long docCount = bucket.docCount();

                Aggregate statsAgg = bucket.aggregations().get("standard_stats");
                if (statsAgg != null && statsAgg.isStats()) {
                    var stats = statsAgg.stats();
                    resultList.add(Map.of(
                            "category", category,
                            "docCount", docCount,
                            "min", stats.min(),
                            "max", stats.max(),
                            "avg", stats.avg(),
                            "sum", stats.sum(),
                            "count", stats.count()
                    ));
                }
            }
        }

        return resultList;
    }


    public List<Map<String, Object>> findUniqueTagsFilteredByCityAndVerification(
            List<String> cities,
            String fromDate,
            String toDate,
            Boolean verified

    ) throws Exception {
        // Build your custom query map
        Map<String, Object> queryMap = CityWiseUniqueTagsDataBuilder.buildFilteredTagAggregationQuery(
                verified, cities, fromDate, toDate
        );
        // Deserialize the JSON into a SearchRequest
        // actual conversion is happening in here
        // it takes the input json data and makle it searchable request
        SearchRequest searchRequest = SearchRequestConverter.fromMap(queryMap);

        // Execute the search
        SearchResponse<Map> response = esClient.search(searchRequest, Map.class);
        return CityWiseUniqueTagsDataBuilder.parseResponse(response);
    }


    public Map<String, Object>  searchTopCompaniesByCity(double maxAvgCost, List<Map<String, Object>> params, int topN) throws IOException {
        Map<String, Object> queryMap = TopCompaniesByCityDataBuilder.buildQuery(maxAvgCost,  params, topN, 100);
        SearchRequest searchRequest = SearchRequestConverter.fromMap(queryMap);
        // 2. Perform search
        SearchResponse<Map> response = esClient.search(searchRequest, Map.class);
        // 3. Parse response
        Map<String, Object> responses = (Map) response.aggregations();
        return TopCompaniesByCityDataBuilder.parseResponse(responses);
    }

    public Map<String, Object> searchNearbyCompanies(
            double latitude,
            double longitude,
            List<Map<String, Object>> filters,
            String distanceKm,
            int size
    ) throws IOException {

        Map<String, Object> queryMap = NearbyCompaniesByLocationBuilder.buildQuery(
                latitude,
                longitude,
                filters,
                distanceKm,
                size
        );

        SearchRequest searchRequest = SearchRequestConverter.fromMap(queryMap);
        SearchResponse<Map> response = esClient.search(searchRequest, Map.class);
        return NearbyCompaniesByLocationBuilder.parseResponse(response);
    }

}

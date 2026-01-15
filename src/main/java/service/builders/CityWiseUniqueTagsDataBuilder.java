package service.builders;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;

import java.util.*;
import java.util.Map;
import java.util.HashMap;

public class CityWiseUniqueTagsDataBuilder {


    public static Map<String, Object> buildFilteredTagAggregationQuery(
            boolean verified,
            List<String> cities,
            String dateFrom,
            String dateTo
    ) {
        Map<String, Object> query = new HashMap<>();

        // Filters
        List<Map<String, Object>> filters = new ArrayList<>();

        // single term only since
        // we need verified as a single value
        filters.add(Map.of("term", Map.of("verified", verified)));

        // terms for multiple filter
        if (cities != null && !cities.isEmpty()) {
            filters.add(Map.of("terms", Map.of("city", cities)));
        }

        filters.add(Map.of("range", Map.of("created_at", Map.of(
                "gt", dateFrom,
                "lt", dateTo
        ))));

        // Query
        Map<String, Object> boolQuery = Map.of("bool", Map.of("filter", filters));
        query.put("query", boolQuery);
        query.put("size", 0);

        // Aggregations
        Map<String, Object> aggs = new HashMap<>();
        aggs.put("listed_tags", Map.of("terms", Map.of("field", "tags")));

        // for now 100 as a precision threshold since i dont have much data
        aggs.put("unique_tags", Map.of("cardinality", Map.of(
                "field", "tags",
                "precision_threshold", 1000
        )));
        query.put("aggs", aggs);

        return query;
    }


    public static List<Map<String, Object>> parseResponse(SearchResponse<Map> response) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        // Parse "listed_tags" aggregation
        Aggregate listedTags = response.aggregations().get("listed_tags");
        if (listedTags != null && listedTags.isSterms()) {
            for (StringTermsBucket bucket : listedTags.sterms().buckets().array()) {
                resultList.add(Map.of(
                        "tag", bucket.key().stringValue(),
                        "docCount", bucket.docCount()
                ));
            }
        }

        // Parse "unique_tags" aggregation
        Aggregate uniqueTags = response.aggregations().get("unique_tags");
        if (uniqueTags != null && uniqueTags.isCardinality()) {
            resultList.add(Map.of(
                    "uniqueTagCount", uniqueTags.cardinality().value()
            ));
        }

        return resultList;
    }

}

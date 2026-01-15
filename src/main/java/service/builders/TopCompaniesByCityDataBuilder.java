package service.builders;


import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.TopHitsAggregate;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.JsonValue;

import java.io.IOException;
import java.util.*;

public class TopCompaniesByCityDataBuilder {

    public static Map<String, Object> buildQuery(double maxAverageCost, List<Map<String, Object>> additionalFilters, int topNPerCity, int sizePerCity) {
        // parent fopr filtering the data in top lvel
        Map<String, Object> query = new HashMap<>();

        // === QUERY ===
        List<Map<String, Object>> filters = new ArrayList<>();

        // Range on average_cost
        filters.add(Map.of("range", Map.of("average_cost", Map.of("lte", maxAverageCost))));

        // Term on category
//
        if (additionalFilters != null && !additionalFilters.isEmpty()) {
            for (Map<String, Object> filter : additionalFilters) {
                for (Map.Entry<String, Object> entry : filter.entrySet()) {
                    String field = entry.getKey();
                    Object value = entry.getValue();

                    // Minimal coercion: convert "true"/"false" string to boolean
                    if ("true".equalsIgnoreCase(value.toString())) {
                        value = true;
                    } else if ("false".equalsIgnoreCase(value.toString())) {
                        value = false;
                    }

                    filters.add(Map.of("term", Map.of(field, value)));
                }
            }
        }

        // Combine filters with bool/filter
        query.put("query", Map.of("bool", Map.of("filter", filters)));
        query.put("size", 0);  // No top-level hits, only aggs

        //-------------------------------------------- dcreate top hits and required data with the number of record
        // topNperCity ::
        // required data in _source

        // topCompanies -> top_hits
        Map<String, Object> topHitsAgg = Map.of(
                "top_hits", Map.of(
                        "size", topNPerCity,
                        "_source", Map.of("includes", List.of("company_name", "address", "average_cost", "rating"))
                )
        );

        // citywise -> terms
        // parent level aggregation..
        // other aggregationwill be merged (tophits data)
        Map<String, Object> citywiseAgg = new HashMap<>();
        citywiseAgg.put("terms", Map.of(
                "field", "city",
                "size", sizePerCity
        ));
        citywiseAgg.put("aggs", Map.of("topCompanies", topHitsAgg));


        // Final aggs
        // consists actual filter and aggregation both
        query.put("aggs", Map.of("citywise", citywiseAgg));

        return query;
    }
    static ObjectMapper mapper = new ObjectMapper();



    public static Map<String, Object> parseResponse(Map<String, Object> esResponse) {
        List<Map<String, Object>> data = new ArrayList<>();

        Aggregate citywiseAgg = (Aggregate) esResponse.get("citywise");
        if (citywiseAgg == null) return Map.of("data", data, "status", true, "message", "No data");

        StringTermsAggregate cityBucketsData = (StringTermsAggregate) citywiseAgg._get();
        List<StringTermsBucket> cityBuckets = cityBucketsData.buckets().array();

        for (StringTermsBucket cityBucket : cityBuckets) {
            String cityName = cityBucket.key().stringValue();

            TopHitsAggregate topCompaniesAgg = (TopHitsAggregate) cityBucket.aggregations()
                    .get("topCompanies")._get();
            List<Hit<JsonData>> hits = topCompaniesAgg.hits().hits();

            List<Map<String, Object>> companies = new ArrayList<>();
            for (Hit<JsonData> hit : hits) {
                JsonData sourceData = hit.source();
                if (sourceData != null) {
                   Map<String, Object> company = mapper.convertValue(
                            sourceData.to(Map.class),
                            new TypeReference<>() {
                            }
                    );
                    companies.add(company);
                }
            }

//            for (Hit<JsonData> hit : hits) {
//                JsonValue jsonData = hit.source().toJson();
//                if (jsonData != null) {
//                    Map<String, Object> flattened = new  HashMap<>();
//                    jsonData.asJsonObject().forEach((key, value) -> {
//                        flattened.put(key, value);
//                    });
//                    companies.add(flattened);
//                }
//            }

            Map<String, Object> cityEntry = new LinkedHashMap<>();
            cityEntry.put("city", cityName);
            cityEntry.put("companies", companies);

            data.add(cityEntry);
        }

        Map<String, Object> finalResponse = new LinkedHashMap<>();
        finalResponse.put("data", data);
        finalResponse.put("status", true);
        finalResponse.put("message", "Data fetched");

        return finalResponse;
    }

}

package service.builders;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.JsonValue;

import java.util.*;

public class NearbyCompaniesByLocationBuilder {

    static ObjectMapper mapper = new ObjectMapper();

    public static Map<String, Object> buildQuery(
            double latitude,
            double longitude,
            List<Map<String, Object>> additionalFilters,
            String distanceKm,
            int size
    ) {
        Map<String, Object> query = new LinkedHashMap<>();

        // Source filtering
        query.put("_source", Map.of("includes", List.of("company_name", "rating", "average_cost", "email", "address")));

        // Filters
        List<Map<String, Object>> filters = new ArrayList<>();
        filters.add(Map.of(
                "geo_distance", Map.of(
                        "distance", distanceKm,
                        "location", Map.of("lat", latitude, "lon", longitude)
                )
        ));
        if (additionalFilters != null && !additionalFilters.isEmpty()) {
            for (Map<String, Object> filter : additionalFilters) {
                for (Map.Entry<String, Object> entry : filter.entrySet()) {
                    String field = entry.getKey();
                    Object value = entry.getValue();

                    if ("true".equalsIgnoreCase(value.toString())) {
                        value = true;
                    } else if ("false".equalsIgnoreCase(value.toString())) {
                        value = false;
                    }

                    filters.add(Map.of("term", Map.of(field, value)));
                }
            }
        }

        query.put("query", Map.of("bool", Map.of("filter", filters)));

        query.put("aggs", Map.of(
                "avg_cost", Map.of("avg", Map.of("field", "average_cost"))
        ));
        // Sorting by geo distance
        query.put("sort", List.of(
                Map.of("_geo_distance", Map.of(
                        "location", Map.of("lat", latitude, "lon", longitude),
                        "order", "asc",
                        "unit", "km",
                        "distance_type", "arc"
                ))
        ));

        query.put("size", size);

        return query;
    }
    public static Map<String, Object> parseResponse(SearchResponse<Map> response) {
        List<Map<String, Object>> data = new ArrayList<>();
        double totalCost = 0.0;
        int count = 0;

        for (Hit<Map> hit : response.hits().hits()) {
            Map<String, Object> source = hit.source();
            if (source == null) continue;

            Object cost = source.get("average_cost");
            if (cost instanceof Number) {
                totalCost += ((Number) cost).doubleValue();
                count++;
            }

            List<FieldValue> sortList = hit.sort();
            if (sortList != null && !sortList.isEmpty()) {
                FieldValue distanceRaw = sortList.get(0);
                if (distanceRaw._kind() == FieldValue.Kind.Double) {
                    source.put("distance_km", distanceRaw.doubleValue());
                } else if (distanceRaw._kind() == FieldValue.Kind.Long) {
                    source.put("distance_km", distanceRaw.longValue());
                }
            }

            data.add(source);
        }

        double avg = count > 0 ? totalCost / count : 0.0;

        return Map.of(
                "data", data,
                "average_cost", avg
        );
    }

}

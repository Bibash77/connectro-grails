package service.builders;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.util.ObjectBuilder;

import java.util.*;
import java.util.function.Function;

public class ElasticQueryBuilder {

    private List<String> fieldsSelected;
    private List<Map<String, Object>> filters;
    private List<Map<String, Object>> aggregations;

    public ElasticQueryBuilder fields(List<String> fields) {
        this.fieldsSelected = fields;
        return this;
    }

    public ElasticQueryBuilder filters(List<Map<String, Object>> filters) {
        this.filters = filters;
        return this;
    }

    public ElasticQueryBuilder aggregations(List<Map<String, Object>> aggregations) {
        this.aggregations = aggregations;
        return this;
    }

    public SearchRequest build(String indexName) {
        return SearchRequest.of(s -> {
            // tis can be dynamic as

            s.index(indexName).size(100);

            if (fieldsSelected != null && !fieldsSelected.isEmpty()) {
                s.source(src -> src.filter(f -> f.includes(fieldsSelected)));
            }

            if (filters != null && !filters.isEmpty()) {
                s.query(buildFilterQuery());
            }

            if (aggregations != null && !aggregations.isEmpty()) {
                s.aggregations(buildAggregations());
            }

            return s;
        });
    }


    private Function<Query.Builder, ObjectBuilder<Query>> buildFilterQuery() {
        return q -> q.bool(b -> b.filter(buildTermQueries()));
    }


    private List<Query> buildTermQueries() {
        List<Query> queries = new ArrayList<>();
        for (Map<String, Object> filter : filters) {
            String name = (String) filter.get("name");
            Object value = filter.get("value");
            Query termQuery = new Query.Builder()
                    .match(t -> t.field(name).query(FieldValue.of(value)))
                    .build();

            queries.add(termQuery);
        }
        return queries;
    }


    private Map<String, Aggregation> buildAggregations() {
        Map<String, Aggregation> aggs = new HashMap<>();
        for (Map<String, Object> agg : aggregations) {
            String name = (String) agg.get("name");
            String type = (String) agg.get("type");

            if ("avg".equalsIgnoreCase(type)) {
                aggs.put(name, Aggregation.of(a -> a.avg(avg -> avg.field(name))));
            } else if ("sum".equalsIgnoreCase(type)) {
                aggs.put(name, Aggregation.of(a -> a.sum(sum -> sum.field(name))));
            } else if ("terms".equalsIgnoreCase(type)) {
                aggs.put(name, Aggregation.of(a -> a.terms(t -> t.field(name))));
            }
        }
        return aggs;
    }
}

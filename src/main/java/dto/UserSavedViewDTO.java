//package dto;
//
//import ch.qos.logback.core.util.AggregationType;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.context.annotation.FilterType;
//
//import java.io.Serializable;
//import java.util.List;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class UserSavedViewDTO implements Serializable {
//
//    private Long userId;
//    private String viewName;
//    private String visualizationType;
//
//    private List<String> fieldsSelected; // e.g., ["name", "rating"]
//    private List<Filter> filters;        // e.g., [{ "name": "category", "type": "term" }]
//    private List<Aggregation> aggregations; // e.g., [{ "name": "price_level", "type": "avg" }]
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class Filter {
//        private String name;
//        private FilterType type;
//    }
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class Aggregation {
//        private String name;
//        private AggregationType type;
//    }
//}

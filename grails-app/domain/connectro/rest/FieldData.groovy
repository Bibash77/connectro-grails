package connectro.rest

/** to store property and its type and the operation we can perform **/
class FieldData {
        String name
        String fieldName
        String type           // e.g., text, number, geo
        String displayName
        Boolean isFilterable = true
        Boolean isAggregatable = true

        static constraints = {
            name nullable: false
            fieldName nullable: false
            type nullable: false
            displayName nullable: true
        }
}
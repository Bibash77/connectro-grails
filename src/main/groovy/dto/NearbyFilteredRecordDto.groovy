package dto

class NearbyFilteredRecordDto {

    Double lat
    Double lon
    String distance           // e.g. "5km"
    Integer size = 10         // default to 10
    List<Map<String, Object>> filters = []

    NearbyFilteredRecordDto(Map json) {
        this.lat = json.lat as Double
        this.lon = json.lon as Double
        this.distance = json.distance ?: "5km"
        this.size = json.size ?: 10

        // Accept filters as a list of { field: value }
        def rawFilters = json.filters
        if (rawFilters instanceof List) {
            this.filters = rawFilters.collect { it as Map<String, Object> }
        }
    }
}


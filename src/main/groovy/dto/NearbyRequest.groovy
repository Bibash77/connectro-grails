package dto

/**
 * Request DTO for nearby listings endpoint
 */
class NearbyRequest {
    Double lat              // Latitude
    Double lon              // Longitude
    String city             // Required city filter
    String distance = "5km" // Distance radius (default: 5km)
    Integer size = 10       // Result size (default: 10)

    Map<String, Object> toParams() {
        return [
            lat: lat,
            lon: lon,
            city: city,
            distance: distance ?: "5km",
            size: size ?: 10
        ]
    }
}

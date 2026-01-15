package connectro.rest

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import dto.UserSavedViewDto;

class UserSavedView {
//        User user
        String viewName
        String visualizationType   // e.g., table, chart, map

        // JSON fields stored as text
        String fieldsSelected       // JSON string: ["name", "rating"]
        String filters              // JSON string: [{ "name": "category", "type": "term" }]
        String aggregations         // JSON string: [{ "name": "price_level", "type": "avg" }]

    Boolean showOneDashboard = false;
//        static belongsTo = [user: User]

    static mapping = {
        fieldsSelected type: 'text'
        filters type: 'text'
        aggregations type: 'text'
    }

    static constraints = {
            viewName nullable: false
            visualizationType nullable: true
            fieldsSelected nullable: true, maxSize: 10000
            filters nullable: true, maxSize: 10000
            aggregations nullable: true, maxSize: 10000
        }


    static UserSavedView fromDTO(UserSavedViewDto dto, ObjectMapper mapper) {
        def instance = new UserSavedView()
        instance.viewName = dto.viewName
        instance.visualizationType = dto.visualizationType
        instance.showOneDashboard= dto.showOnDashboard

        try {
            instance.fieldsSelected = dto.fieldsSelected ? mapper.writeValueAsString(dto.fieldsSelected) : null
            instance.filters = dto.filters ? mapper.writeValueAsString(dto.filters) : null
            instance.aggregations = dto.aggregations ? mapper.writeValueAsString(dto.aggregations) : null
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON fields", e)
        }

        return instance
    }
    static UserSavedView fromDTO(UserSavedViewDto dto, ObjectMapper mapper, UserSavedView instance) {
        instance.viewName = dto.viewName
        instance.visualizationType = dto.visualizationType
        instance.showOneDashboard= dto.showOnDashboard

        try {
            instance.fieldsSelected = dto.fieldsSelected ? mapper.writeValueAsString(dto.fieldsSelected) : null
            instance.filters = dto.filters ? mapper.writeValueAsString(dto.filters) : null
            instance.aggregations = dto.aggregations ? mapper.writeValueAsString(dto.aggregations) : null
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON fields", e)
        }

        return instance
    }
}
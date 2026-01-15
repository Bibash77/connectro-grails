package dto

import connectro.rest.UserSavedView
import groovy.json.JsonSlurper

class UserSavedViewDto {
     Long userId
   Long id;
        String viewName
        String visualizationType

        List<String> fieldsSelected
        List<Filter> filters
        List<Aggregation> aggregations

    Boolean showOnDashboard = false;



   static UserSavedViewDto fromEntity(UserSavedView entity) {
      def jsonSlurper = new JsonSlurper()

      return new UserSavedViewDto(
//              userId: entity.user?.ide,


              id: entity.id,
              viewName: entity?.viewName ?: "Untitled View",
              visualizationType: entity?.visualizationType ?: "Unknown",
              fieldsSelected: JsonUtils.safeParseList(entity?.fieldsSelected, String),
              filters: entity.filters != null ?  (List<Filter>) jsonSlurper.parseText(entity.filters): null,
              showOnDashboard: entity.showOneDashboard == null ? false: entity.showOneDashboard,
              aggregations: entity.aggregations != null ?  (List<Aggregation>) jsonSlurper.parseText(entity.aggregations) : null
      )

   }
}

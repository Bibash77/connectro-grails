package connectro.rest

import constants.ApplicationConstants
import dto.ApiRenderResponse
import dto.CardinalTagsCityWiseInSpecificDate
import dto.CityWiseFilteredRecordDto
import dto.Filter
import dto.NearbyFilteredRecordDto
import grails.converters.JSON
import groovy.json.JsonSlurper
import service.ElasticQueryService


class SearchExecutionController {

    JsonSlurper jsonSlurper;
    ElasticQueryService elasticQueryService

    def search(Long id) {
        def view = UserSavedView.get(id)
        if (!view) {
            render new ApiRenderResponse("UserSavedView not found with ID: $id", false) as JSON
            return
        }


        List<String> fields = jsonSlurper.parseText(view.fieldsSelected) as List<String>
        List<Map<String, Object>> aggregations = (view.aggregations != null ? jsonSlurper.parseText(view.aggregations) : Collections.emptyList()) as List<Map<String, Object>>
        List<Map<String, Object>> filters = (view.filters != null ? jsonSlurper.parseText(view.filters) : Collections.emptyList()) as List<Map<String, Object>>
        def result = elasticQueryService.searchByFields(ApplicationConstants.INDEX_NAME, fields, filters, aggregations)
        render result as JSON
    }


    def searchAverageRatingByCity() {

        def result = elasticQueryService.searchAverageRatingByCity();

        render new ApiRenderResponse(result, "Data fetched") as JSON
    }

    def yearWiseCompanyCreation() {

        def filterDto = new Filter(request.JSON)

        def filterMap = filterDto.properties.subMap(['name', 'value'])
        def result = elasticQueryService.searchYearWiseCompanyCreation(filterMap);

        render new ApiRenderResponse(result, "Data fetched") as JSON
    }

    def ratingWiseStatsData() {
        def result = elasticQueryService.searchRatingStatsByCategory();
        render new ApiRenderResponse(result, "Data fetched") as JSON
    }


    def findCardinalTagsFilteredWithCount() {
        def filterDto = new CardinalTagsCityWiseInSpecificDate(request.JSON)

        def result = elasticQueryService.findUniqueTagsFilteredByCityAndVerification(
                filterDto.cities,
                filterDto.createdAtFromDate,
                filterDto.createdAtToDate,
                filterDto.verified
        )
        render new ApiRenderResponse(result, "Data fetched") as JSON
    }

    def findCityWiseTopRatedCompaniesWithPriceFilter() {
        def filterDto = new CityWiseFilteredRecordDto(request.JSON)
        print(filterDto.filters)

        // Transform filters from List<Filter> to List<Map<String, Object>>
        List<Map<String, Object>> normalizedFilters = filterDto.filters.collect { filter ->
            def value = filter.value

            // Convert string booleans to actual booleans
            if (value?.toLowerCase() == 'true') {
                value = true
            } else if (value?.toLowerCase() == 'false') {
                value = false
            }

            [(filter.name): value]
        }

        def result = elasticQueryService.searchTopCompaniesByCity(
                filterDto.maxAvgCost,
                normalizedFilters,
                filterDto.topN
        )
        render new ApiRenderResponse(result, "Data fetched") as JSON

    }

    def findNearbyCompanies() {
        def filterDto = new NearbyFilteredRecordDto(request.JSON)
        println "Received filters: ${filterDto.filters}"

        def result = elasticQueryService.searchNearbyCompanies(
                filterDto.lat,
                filterDto.lon,
                filterDto.filters,
                filterDto.distance,
                filterDto.size
        )

        render new ApiRenderResponse(result, "Nearby companies fetched") as JSON
    }

}
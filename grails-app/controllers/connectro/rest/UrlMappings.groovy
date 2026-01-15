package connectro.rest

class UrlMappings {
    static mappings = {

        // default routing
//        delete "/$controller/$id(.$format)?"(action:"delete")

        //...

        group "/user-saved-view", {
            post "/"(controller: 'userSavedView', action: 'save')
            get "/$id?"(controller: 'userSavedView', action: 'findById')
            get "/"(controller: 'userSavedView', action: 'findAll')
            put "/$id"(controller: 'userSavedView', action: 'update')
            delete "/$id"(controller: 'userSavedView', action: 'delete')
        }


        group "/field-data", {
            post "/"(controller: 'fieldData', action: 'save')
            get "/$id?"(controller: 'fieldData', action: 'findById')
            get "/"(controller: 'fieldData', action: 'findAll')
            put "/$id"(controller: 'fieldData', action: 'update')
            delete "/$id"(controller: 'fieldData', action: 'delete')
        }

        group "/dashboard-info", {
            get "/"(controller: 'userSavedView', action: 'dashboardInfo')
        }

        group "/search-execute", {
            get "/$id"(controller: 'searchExecution', action: 'search')
        }

        group "/search", {
            get "/average-rating-by-city"(controller: 'searchExecution', action: 'searchAverageRatingByCity')
            post "/histogram/year-wise-company-creation"(controller: 'searchExecution', action: 'yearWiseCompanyCreation')
            get "/rating-wise-stats"(controller: 'searchExecution', action: 'ratingWiseStatsData')
            post "/cardinal-tags-filtered"(controller: 'searchExecution', action: 'findCardinalTagsFilteredWithCount')
            post "/city-wise-top-n-records-filtered"(controller: 'searchExecution', action: 'findCityWiseTopRatedCompaniesWithPriceFilter')
            post "/nearby-companies"(controller: 'searchExecution', action: 'findNearbyCompanies')
        }

        "500"(view: '/error')
        "404"(view: '/notFound')
    }


    /* for specific maping
      group "/conf", {
            get "/talks/$id?"(controller: 'conference', action: 'talks')
            get "/speakers/$id?"(controller: 'conference', action: 'speakers')
            get "/agenda"(controller: 'conference', action: 'agenda')
        }
     */
}

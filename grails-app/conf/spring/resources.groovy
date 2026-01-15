package spring

beans = {

    jsonSlurper(groovy.json.JsonSlurper)
    elasticQueryService(service.ElasticQueryService)


    // manually create instance of your config class
    elasticSearchConfig(configurations.ElasticSearchConfig)



}

package spring

beans = {
    // JsonSlurper can be shared as a singleton
    jsonSlurper(groovy.json.JsonSlurper)
    
    // ElasticQueryService - removed (handled by @Service annotation in Java class)
    // ElasticSearchConfig - removed (handled by @Configuration annotation in Java class)
    // V0SearchService - automatically created via @Service annotation + component scanning
}

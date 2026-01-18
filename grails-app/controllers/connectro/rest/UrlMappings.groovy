package connectro.rest

class UrlMappings {
    static mappings = {

        // V0 API endpoints

        group "/search", {
            get "/listings"(controller: 'search', action: 'listings')
            get "/nearby"(controller: 'search', action: 'nearby')
            get "/categories"(controller: 'search', action: 'categories')
        }

        group "/health", {
            get "/"(controller: 'health', action: 'index')
            get "/elastic"(controller: 'health', action: 'elastic')
        }

        // Error handling
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}

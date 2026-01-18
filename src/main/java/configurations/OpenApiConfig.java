package configurations;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI connectroOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Development Server");

        Server prodServer = new Server();
        prodServer.setUrl("https://api.connectro.com");
        prodServer.setDescription("Production Server");

        Contact contact = new Contact();
        contact.setName("Connectro API Support");
        contact.setEmail("support@connectro.com");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Connectro REST API")
                .version("v0")
                .description("""
                        V0 MVP REST API for location-based business listings search in Nepal.
                        
                        ## Features
                        - Elasticsearch-powered search
                        - Geo-based nearby search
                        - Category suggestions with fuzzy matching
                        - Health monitoring endpoints
                        
                        ## Authentication
                        No authentication required for V0 MVP.
                        
                        ## Base URL
                        All endpoints are prefixed with the API base URL.
                        """)
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}

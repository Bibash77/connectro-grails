package configurations;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

    @Value("${elasticsearch.host:localhost}")
    private String host;

    @Value("${elasticsearch.port:9200}")
    private int port;

    @Value("${elasticsearch.scheme:http}")
    private String scheme;

    @Value("${elasticsearch.apiKey:}")
    private String apiKey;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, scheme));

        // Set API Key header only if it's provided
        if (apiKey != null && !apiKey.isEmpty()) {
            builder.setDefaultHeaders(new BasicHeader[]{
                new BasicHeader(HttpHeaders.AUTHORIZATION, "ApiKey " + apiKey)
            });
        }

//        low level client setup from
        // rest client 8.1.12
        RestClient restClient = builder.build();

        ElasticsearchTransport transport = new RestClientTransport(
            restClient,
            // serializable for req/response
            new JacksonJsonpMapper()
        );

        // actual usable high level client wrapped from low level transport client
        return new ElasticsearchClient(transport);
    }
}

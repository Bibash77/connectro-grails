package converted;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonParser;

import java.io.StringReader;
import java.util.Map;

public class SearchRequestConverter {

    public static SearchRequest fromMap(Map<String, Object> queryMap) {
        // Step 1: Serialize map to JSON string
      try {
          ObjectMapper jacksonMapper = new ObjectMapper();
          String jsonQuery = jacksonMapper.writeValueAsString(queryMap);

          // Step 2: Deserialize JSON into SearchRequest
          JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper();
          JsonpMapper mapper = jsonpMapper;
          JsonProvider provider = mapper.jsonProvider();
          JsonParser parser = provider.createParser(new StringReader(jsonQuery));

          return SearchRequest._DESERIALIZER.deserialize(parser, mapper);
      } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException(e);
      }
    }
}

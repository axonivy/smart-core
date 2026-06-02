package ch.ivyteam.smart.core.aspire;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AspireAPI {

  private final WebTarget target;

  private final static ObjectMapper MAPPER = new ObjectMapper();

  private AspireAPI(WebTarget target) {
    this.target = target;
  }

  public JsonNode spansOfResource(String resource) {
    var request = target
        .path("spans")
        .queryParam("resource", resource)
        .request();
    try (var response = request.get()) {
      return MAPPER.readTree(response.readEntity(String.class))
          .get("data")
          .get("resourceSpans").get(0)
          .get("scopeSpans").get(0)
          .get("spans");
    } catch (JsonProcessingException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static AspireAPI create(String baseUrl) {
    return new AspireAPI(
        ClientBuilder.newClient()
            .target(baseUrl)
            .path("api")
            .path("telemetry"));
  }
}

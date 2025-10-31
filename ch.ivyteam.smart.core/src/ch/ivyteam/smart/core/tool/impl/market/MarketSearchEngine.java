package ch.ivyteam.smart.core.tool.impl.market;

import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response.Status.Family;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.ivyteam.ivy.jersey.client.JerseyClientBuilder;
import ch.ivyteam.log.Logger;

public class MarketSearchEngine {

  private static final Logger LOGGER = Logger.getLogger(MarketSearchEngine.class);

  private static final String MARKET_API = "https://market.axonivy.com/marketplace-service/api";
  private static final Client CLIENT = JerseyClientBuilder.create("market-tool").toClient();

  public static String searchProducts(String vendor, String product) {
    LOGGER.debug("List all product: ignoring search requested for " + vendor + " " + product);

    var target = market();
    var response = target.request().get();
    var json = response.readEntity(JsonNode.class);
    enrich(json);
    System.out.println(json);

    return json.toString();
  }

  private static WebTarget market() {
    // https://market.axonivy.com/marketplace-service/api/product?type=all&sort=standard&keyword=&language=en&page=0&size=100&isRESTClient=true
    return CLIENT.target(MARKET_API).path("/product")
        .queryParam("type", "all")
        .queryParam("sort", "standard")
        .queryParam("language", "en")
        .queryParam("page", 0)
        .queryParam("size", 200)
        .queryParam("isRESTClient", true);
  }

  public static Optional<ArrayNode> enrich(JsonNode json) {
    var products = Optional.ofNullable(json.get("_embedded"))
        .map(embedded -> embedded.get("products"))
        .filter(ArrayNode.class::isInstance)
        .map(ArrayNode.class::cast);
    products.get().forEach(MarketSearchEngine::enrichProduct);
    return products;
  }

  private static void enrichProduct(JsonNode product) {
    JsonNode id = product.get("id");
    if (id != null) {
      if (product.get("_links") instanceof ObjectNode links) {
        if (links.get("self") instanceof ObjectNode self) {
          self.put("publicUri", "https://market.axonivy.com/" + id.asText());
          self.put("versions", MARKET_API + "/product-details/" + id.asText() + "/versions?designerVersion=&isShowDevVersion=true");
        }
      }
    }
  }

  private static JsonNode versions(String id) {
    // https://market.axonivy.com/marketplace-service/api/product-details/coffee-machine-connector/versions?designerVersion=&isShowDevVersion=true
    var path = CLIENT.target(MARKET_API).path("product-details").path(id).path("versions")
        .queryParam("isShowDevVersion", true);
    var response = path.request().get();
    if (!Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
      throw new IllegalArgumentException("Failed to fetch information for product with id " + id + ", got status " + response.getStatus());
    }
    return response.readEntity(JsonNode.class);
  }

  public static String installer(String id) {
    var text = new StringBuilder();
    text.append("Maven pom.xml dependency:\n");
    var versions = versions(id);
    text.append(toPomXml(versions));
    return text.toString();
  }

  public static String toPomXml(JsonNode jsonVersions) {
    return Artifact.parseReleases(jsonVersions)
        .map(Artifact::xml)
        .collect(Collectors.joining("\n"));
  }

}

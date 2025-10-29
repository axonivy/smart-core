package ch.ivyteam.smart.core.tool.market;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.McpClientMock;
import ch.ivyteam.smart.core.tool.impl.market.MarketSearchEngine;
import ch.ivyteam.smart.core.tool.impl.market.MarketSearchTool;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.Tool;

@ManagedServer
public class MarketSearchToolTest {

  McpAsyncClient client;

  @BeforeEach
  void beforeEach() {
    client = McpClientMock.create();
  }

  @Test
  void callTool() {
    var result = client.callTool(CallToolRequest.builder()
        .name(MarketSearchTool.NAME)
        .arguments(Map.of(
            MarketSearchTool.Param.VENDOR, "atlassian",
            MarketSearchTool.Param.PRODUCT, "JIRA"))
        .build())
        .block();

    List<Content> what = result.content();
    assertThat(what).hasSize(1);
  }

  @Test
  void toolSpec() {
    Tool tool = new MarketSearchTool().specification().tool();
    Map<String, Object> props = tool.inputSchema().properties();
    assertThat(props.keySet()).containsOnly(
        MarketSearchTool.Param.VENDOR,
        MarketSearchTool.Param.PRODUCT);
  }

  @Test
  void installHints() {
    var mockResponse = read("mockProducts.json");
    MarketSearchEngine.enrich(mockResponse);

    var prods = (ArrayNode) mockResponse.get("_embedded").get("products");
    var portal = prods.get(0);
    System.out.println(portal.toPrettyString());

    JsonNode node = portal.get("_links").get("self");
    assertThat(node.get("publicUri").asText())
        .as("URI are correct to official front-end instead of its API. "
            + "Reason: this URI is often shown to the user in chats")
        .isEqualTo("https://market.axonivy.com/portal");
    assertThat(node.get("versions").asText())
        .as("a cheap hint on where to fetch version information from")
        .isEqualTo("https://market.axonivy.com/marketplace-service/api/product-details/portal/versions?designerVersion=&isShowDevVersion=true");
    // assert: know install coordinates from Maven (knows metadata URI?)
    // interpolates variables.yaml instructions?
  }

  private static JsonNode read(String resource) {
    var MAPPER = new ObjectMapper();
    try (var jsonIn = MarketSearchToolTest.class.getResourceAsStream(resource)) {
      return MAPPER.reader().readTree(jsonIn);
    } catch (IOException ex) {
      throw new RuntimeException("Failed to read resource " + resource, ex);
    }
  }

}

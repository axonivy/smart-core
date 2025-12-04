package ch.ivyteam.smart.core.tool.market;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.McpClientAccess;
import ch.ivyteam.smart.core.tool.market.impl.MarketInstallTool;
import ch.ivyteam.smart.core.tool.market.impl.MarketSearchEngine;

@ManagedServer
public class MarketInstallToolTest {

  @RegisterExtension
  McpClientAccess mcp = new McpClientAccess();

  @Test
  void callTool(McpAsyncClient client) {
    var result = client.callTool(CallToolRequest.builder()
        .name(MarketInstallTool.NAME)
        .arguments(Map.of(
            MarketInstallTool.Param.ID, "smart-workflow"))
        .build())
        .block();

    List<Content> what = result.content();
    assertThat(what).hasSize(1);
    System.out.println(what.get(0));
  }

  @Test
  void toolSpec() {
    Tool tool = new MarketInstallTool().specification().tool();
    Map<String, Object> props = tool.inputSchema().properties();
    assertThat(props.keySet()).containsOnly(
        MarketInstallTool.Param.ID);
  }

  @Test
  void installHints() {
    var mockResponse = read("mockVersions.json");
    var XML = MarketSearchEngine.toPomXml(mockResponse);
    assertThat(XML).startsWith("""
      <dependency>
        <groupId>com.axonivy.utils.ai</groupId>
        <artifactId>smart-workflow</artifactId>
        <version>13.2.0-a5</version>
        <type>iar</type>
      </dependency>
      """);
  }

  private static JsonNode read(String resource) {
    var MAPPER = new ObjectMapper();
    try (var jsonIn = MarketInstallToolTest.class.getResourceAsStream(resource)) {
      return MAPPER.reader().readTree(jsonIn);
    } catch (IOException ex) {
      throw new RuntimeException("Failed to read resource " + resource, ex);
    }
  }

}

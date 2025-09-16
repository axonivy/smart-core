package ch.ivyteam.smart.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import ch.ivyteam.ivy.request.EngineUriResolver;
import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.test.log.LoggerAccess;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;

@ManagedServer
public class McpServletTest {

  @RegisterExtension
  LoggerAccess mcpLog = new LoggerAccess("MCP");

  @Test
  void smartCoreMcp() {
    try (var mcpClient = sseClient()) {
      var tools = mcpClient.listTools();
      assertThat(tools).isNotEmpty();
      var temperatureTool = tools.stream()
          .filter(spec -> spec.name().contains("temperature-service"))
          .findFirst()
          .get();
      assertThat(temperatureTool.description())
          .isEqualTo("Determines the current temperature in a city.");
      assertThat(temperatureTool.parameters().properties().keySet())
          .containsOnly("cityName");

    } finally {
      mcpLog.all().stream()
          .forEach(System.out::println);
    }
  }

  @SuppressWarnings("all")
  private static DefaultMcpClient sseClient() {
    McpTransport transport = new HttpMcpTransport.Builder()
        .sseUrl(EngineUriResolver.instance().local().toASCIIString() + "/smart-core/sse")
        .timeout(Duration.ofSeconds(5))
        .logRequests(true)
        .logResponses(true)
        .build();

    return new DefaultMcpClient.Builder()
        .transport(transport)
        .build();
  }
}

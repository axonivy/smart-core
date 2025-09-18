package ch.ivyteam.smart.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import ch.ivyteam.ivy.request.EngineUriResolver;
import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.tool.IvyDataClassCreatorTool;
import ch.ivyteam.test.log.LoggerAccess;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;

@ManagedServer
public class McpServletTest {

  @RegisterExtension
  LoggerAccess mcpLog = new LoggerAccess(DefaultMcpClient.class.getName());

  @RegisterExtension
  LoggerAccess transportLog = new LoggerAccess(StreamableHttpMcpTransport.class.getName());

  @Test
  void smartCoreMcp() {
    try (var mcpClient = mcpClient()) {
      var tools = mcpClient.listTools();
      assertThat(tools).isNotEmpty();
      var temperatureTool = tools.stream()
          .filter(spec -> spec.name().contains(IvyDataClassCreatorTool.NAME))
          .findFirst()
          .get();
      assertThat(temperatureTool).isNotNull();

    } finally {
      mcpLog.all().stream()
          .forEach(System.out::println);
      transportLog.all().stream()
          .forEach(System.out::println);
    }
  }

  @SuppressWarnings("all")
  private static DefaultMcpClient mcpClient() {
    McpTransport transport = new StreamableHttpMcpTransport.Builder()
        .url(EngineUriResolver.instance().local().toASCIIString() + "/smart-core/mcp")
        .timeout(Duration.ofSeconds(10))
        .logRequests(true)
        .logResponses(true)
        .build();

    return new DefaultMcpClient.Builder()
        .transport(transport)
        .build();
  }
}

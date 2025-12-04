package ch.ivyteam.smart.core.tool;

import static ch.ivyteam.smart.core.McpClientUtils.SMART_CORE_MCP_URL;

import java.util.List;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;

public interface ToolUtils {

  static List<ToolSpecification> toolSpecifications(String toolName) {
    var transport = new StreamableHttpMcpTransport.Builder()
        .url(SMART_CORE_MCP_URL)
        .build();

    try (var mcpClient = new DefaultMcpClient.Builder()
        .transport(transport)
        .build()) {

      var toolSpecification = mcpClient.listTools().stream()
          .filter(specification -> specification.name().endsWith(toolName))
          .findAny();
      return toolSpecification.map(List::of).orElseThrow();
    }
  }
}

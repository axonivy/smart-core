package ch.ivyteam.smart.core;

import java.util.List;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class SmartCoreMcpServer {

  public static void main(String[] args) {
    var temperatureService = Tool.builder()
        .name("Temperature Service")
        .description("Determines the current temperature in a city.")
        .inputSchema("""
          {
            "type":"object",
            "properties": { "cityName": { "type":"string" } },
            "description": "The name of the city to get the temperature for.",
            "required":["cityName"]
          }
          """)
        .build();
    var temperatureServiceSpecification = SyncToolSpecification.builder()
        .tool(temperatureService)
        .callHandler((exchange, request) -> new CallToolResult(
            List.of(new TextContent("The temperature in " + request.arguments().get("cityName") + " is 24 degrees Celsius.")),
            false))
        .build();

    McpServer.sync(new StdioServerTransportProvider())
        .serverInfo("my-simple-server", "0.0.1")
        .capabilities(ServerCapabilities.builder().tools(true).build())
        .tools(List.of(temperatureServiceSpecification))
        .build();
  }
}

package ch.ivyteam.smart.core;

import java.util.List;

import javax.servlet.ServletContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import ch.ivyteam.ivy.webserver.extension.ServletContextStartupListener;
import ch.ivyteam.smart.core.mcp.transport.JavaxHttpServletSeeTransportProvider;

public class SmartCoreMcpServer implements ServletContextStartupListener {

  @Override
  public void onStartup(ServletContext ctx) {
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

    var transportProvider = JavaxHttpServletSeeTransportProvider.builder()
        .objectMapper(new ObjectMapper())
        .messageEndpoint("/smart-core")
        .build();

    McpServer.sync(transportProvider)
        .serverInfo("smart-core-sse", "0.0.1")
        .capabilities(ServerCapabilities.builder().tools(true).build())
        .tools(List.of(temperatureServiceSpecification))
        .build();

    var servlet = ctx.addServlet("smart-core-mcp", transportProvider);
    servlet.addMapping("/smart-core/*");
    servlet.setAsyncSupported(true);
  }
}

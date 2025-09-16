package ch.ivyteam.smart.core;

import static io.modelcontextprotocol.spec.McpSchema.Role.USER;

import java.util.List;

import javax.servlet.ServletContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.SamplingMessage;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import ch.ivyteam.ivy.webserver.extension.ServletContextStartupListener;
import ch.ivyteam.smart.core.mcp.transport.JavaxHttpServletSeeTransportProvider;

public class SmartCoreMcpServer implements ServletContextStartupListener {

  @Override
  public void onStartup(ServletContext ctx) {
    var temperatureService = Tool.builder()
        .name("temperature-service")
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

    var randomWordToolUsingSampling = Tool.builder()
        .name("random-word")
        .description("Generates a random word.")
        .build();
    var randomWordToolUsingSamplingSpecification = SyncToolSpecification.builder()
        .tool(randomWordToolUsingSampling)
        .callHandler((exchange, request) -> {
          var clientCapabilities = exchange.getClientCapabilities();
          if (clientCapabilities == null || clientCapabilities.sampling() == null) {
            throw new IllegalStateException("Client does not support sampling");
          }

          var samplingMessage = new SamplingMessage(
              USER,
              new TextContent("Please reply with a single random English word. No punctuation, no explanation."));
          var samplingRequest = CreateMessageRequest.builder()
              .messages(List.of(samplingMessage))
              .build();
          var samplingResult = exchange.createMessage(samplingRequest);

          TextContent callToolResultContent;
          if (samplingResult == null || samplingResult.content() == null) {
            callToolResultContent = new TextContent("[no sampling response]");
          } else {
            var content = samplingResult.content();
            if (content instanceof TextContent textContent) {
              callToolResultContent = new TextContent(textContent.text());
            } else {
              callToolResultContent = new TextContent(content.toString());
            }
          }
          return new CallToolResult(List.of(callToolResultContent), false);
        })
        .build();

    var transportProvider = JavaxHttpServletSeeTransportProvider.builder()
        .objectMapper(new ObjectMapper())
        .messageEndpoint("/smart-core")
        .build();

    McpServer.sync(transportProvider)
        .serverInfo("smart-core-sse", "0.0.1")
        .capabilities(ServerCapabilities.builder().tools(true).build())
        .tools(List.of(temperatureServiceSpecification, randomWordToolUsingSamplingSpecification))
        .build();

    var servlet = ctx.addServlet("smart-core-mcp", transportProvider);
    servlet.addMapping("/smart-core/*");
    servlet.setAsyncSupported(true);
  }
}

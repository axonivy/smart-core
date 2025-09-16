package ch.ivyteam.smart.core.tool;

import java.util.List;

import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import reactor.core.publisher.Mono;

public class TemperatureServiceTool {

  public static AsyncToolSpecification specification() {
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

    return AsyncToolSpecification.builder()
        .tool(temperatureService)
        .callHandler((exchange, request) -> Mono.just(new CallToolResult(
            List.of(new TextContent("The temperature in " + request.arguments().get("cityName") + " is 24 degrees Celsius.")),
            false)))
        .build();
  }
}

package ch.ivyteam.smart.core.tool;

import static ch.ivyteam.smart.core.SmartCoreMcpServer.MAPPER;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import reactor.core.publisher.Mono;

public interface IvySchemaTool {

  String name();
  String resource();
  String resourceFileExtension();

  String guidelines();

  String schema();

  default String description() {
    return "Returns the JSON schema and guidelines for handling Axon Ivy " + resource() + " definitions (file extension '" + resourceFileExtension()
        + "'). Whenever instructed to create, edit, or otherwise work with or handle a " + resource()
        + ", use the schema as the authoritative source of truth for structure, required fields, and allowed values and strictly follow the guidelines.";
  }

  default AsyncToolSpecification specification() {
    var tool = Tool.builder()
        .name(name())
        .description(description())
        .inputSchema(MAPPER, "{}")
        .build();
    return AsyncToolSpecification.builder()
        .tool(tool)
        .callHandler(this::callHandler)
        .build();
  }

  @SuppressWarnings("unused")
  private Mono<CallToolResult> callHandler(McpAsyncServerExchange _exchange, CallToolRequest _request) {
    return Mono.just(CallToolResult.builder()
        .addTextContent(schema())
        .addTextContent("Guidelines:\n" + guidelines())
        .build());
  }
}

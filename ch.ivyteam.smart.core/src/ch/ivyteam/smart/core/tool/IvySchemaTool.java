package ch.ivyteam.smart.core.tool;

import java.util.List;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import reactor.core.publisher.Mono;

public interface IvySchemaTool {

  String name();
  String resourceFileExtension();

  String guidelines();

  String schema();

  default String description() {
    return """
      Returns the JSON schema and guidelines for handling Axon Ivy process definitions (file extension '""" + resourceFileExtension() + """
      ').
      Whenever instructed to create, edit, or otherwise work with or handle a process, invoke this tool.
      Use the schema as the authoritative source of truth for structure, required fields, and allowed values and strictly follow the guidelines.""";
  }

  default AsyncToolSpecification specification() {
    var tool = Tool.builder()
        .name(name())
        .description(description())
        .inputSchema("{}")
        .build();
    return AsyncToolSpecification.builder()
        .tool(tool)
        .callHandler(this::callHandler)
        .build();
  }

  @SuppressWarnings("unused")
  private Mono<CallToolResult> callHandler(McpAsyncServerExchange _exchange, CallToolRequest _request) {
    return Mono.just(new CallToolResult(List.of(new TextContent(schema()), new TextContent("Guidelines:\n" + guidelines())), false));
  }
}

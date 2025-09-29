package ch.ivyteam.smart.core.tool;

import static ch.ivyteam.ivy.IvyConstants.DATA_CLASS_EXTENSION;
import static ch.ivyteam.ivy.IvyConstants.DIRECTORY_DATACLASSES;
import static ch.ivyteam.smart.core.schema.ResponseSchema.DATA_CLASS;

import java.util.List;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import reactor.core.publisher.Mono;

public interface IvyDataClassSchemaTool {

  String NAME = "ivy-data-class-schema";
  String DESCRIPTION = """
    Returns the JSON schema and guidelines for handling Axon Ivy data class definitions (file extension '""" + DATA_CLASS_EXTENSION + """
    ').
    Whenever instructed to create, edit, or otherwise work with or handle a data class, invoke this tool.
    Use the schema as the authoritative source of truth for structure, required fields, and allowed values and strictly follow the guidelines.""";

  String GUIDELINES = """
    Guidelines:
    - Data class files must be located in a subdirectory of '<project-root>/""" + DIRECTORY_DATACLASSES + """
    /' matching its namespace.""";

  static AsyncToolSpecification specification() {
    var dataClassSchemaTool = Tool.builder()
        .name(NAME)
        .description(DESCRIPTION)
        .build();
    return AsyncToolSpecification.builder()
        .tool(dataClassSchemaTool)
        .callHandler(IvyDataClassSchemaTool::callHandler)
        .build();
  }

  @SuppressWarnings("unused")
  private static Mono<CallToolResult> callHandler(McpAsyncServerExchange _exchange, CallToolRequest _request) {
    return Mono.just(new CallToolResult(
        List.of(
            new TextContent(loadSchema()),
            new TextContent(GUIDELINES)),
        false));
  }

  static String loadSchema() {
    // TODO: load-schema form ivy-core. Keep it as static resource.
    return DATA_CLASS.schema().toString();
  }
}

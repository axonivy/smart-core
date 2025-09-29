package ch.ivyteam.smart.core.tool;

import static ch.ivyteam.ivy.IvyConstants.DIRECTORY_PROCESSES;
import static ch.ivyteam.ivy.IvyConstants.PROCESS_EXTENSION;
import static ch.ivyteam.smart.core.schema.ResponseSchema.PROCESS;

import java.util.List;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import reactor.core.publisher.Mono;

public interface IvyProcessSchemaTool {

  String NAME = "ivy-process-schema";
  String DESCRIPTION = """
    Returns the JSON schema and guidelines for handling Axon Ivy process definitions (file extension '""" + PROCESS_EXTENSION + """
    ').
    Whenever instructed to create, edit, or otherwise work with or handle a process, invoke this tool.
    Use the schema as the authoritative source of truth for structure, required fields, and allowed values and strictly follow the guidelines.""";

  String GUIDELINES = """
    Guidelines:
    - Process files must be located in a subdirectory of '<project-root>/""" + DIRECTORY_PROCESSES + """
    /'.
    - Omit as many defaults as possible, but at any rate produce the required values.
    - Create unique instances for element IDs, starting from 'f1'.
    - Draw elements as graphs.
    - Do not set any visual attributes on elements, except the position 'at'.
    - Visualize roles as pools.
    - A process requires a process data class. Create it if needed.""";

  static AsyncToolSpecification specification() {
    var dataClassSchemaTool = Tool.builder()
        .name(NAME)
        .description(DESCRIPTION)
        .inputSchema("{}")
        .build();
    return AsyncToolSpecification.builder()
        .tool(dataClassSchemaTool)
        .callHandler(IvyProcessSchemaTool::callHandler)
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
    // TODO: load-schema form ivy-core. Keep it as static resource in process.model.io bundle.
    return PROCESS.schema().toString();
  }
}

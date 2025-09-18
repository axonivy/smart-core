package ch.ivyteam.smart.core.tool;

import java.util.List;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import ch.ivyteam.smart.core.schema.ResponseSchema;
import reactor.core.publisher.Mono;

public interface IvyProcessSchemaTool {

  String NAME = "ivy-process-schema";
  String DESCRIPTION = """
    Returns the JSON schema for Axon Ivy processes.
    Call this tool whenever you are working with Axon Ivy processes and strictly comply with it.

    Guidelines:
    - Omit as many defaults as possible, but at any rate produce the required values.
    - Create unique instances for element IDs, starting from 'f1'.
    - Draw elements as graphs.
    - Do not set any visual attributes on elements, except the position 'at'.
    - Visualize roles as pools.""";

  static AsyncToolSpecification specification() {
    var dataClassGenerator = Tool.builder()
        .name(NAME)
        .description(DESCRIPTION)
        .build();
    return AsyncToolSpecification.builder()
        .tool(dataClassGenerator)
        .callHandler(IvyProcessSchemaTool::callHandler)
        .build();
  }

  private static Mono<CallToolResult> callHandler(@SuppressWarnings("unused") McpAsyncServerExchange _exchange, CallToolRequest request) {
    return Mono.just(new CallToolResult(List.of(new TextContent(loadSchema())), false));
  }

  private static String loadSchema() {
    // TODO: load-schema form ivy-core. Keep it as static resource.
    return ResponseSchema.DATA_CLASS.schema().toString();
  }
}

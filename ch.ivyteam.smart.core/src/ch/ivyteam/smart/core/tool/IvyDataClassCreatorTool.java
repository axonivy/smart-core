package ch.ivyteam.smart.core.tool;

import static ch.ivyteam.ivy.IvyConstants.DATA_CLASS_EXTENSION;
import static ch.ivyteam.ivy.IvyConstants.DIRECTORY_DATACLASSES;

import java.util.List;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import ch.ivyteam.smart.core.schema.ResponseSchema;
import reactor.core.publisher.Mono;

public interface IvyDataClassCreatorTool {

  String NAME = "ivy-dataclass-creator";
  String DESCRIPTION = """
    Creates an Axon Ivy data class definition based on a user request.
    Use this tool when the user asks to create, model, or design a new data class""";
  String INPUT_SCHEMA = loadSchema();

  String TOOL_RESULT_INTRO = """
    Create a file for the data class definition in the directory '<project-root>/""" + DIRECTORY_DATACLASSES + """
    ' with the file extension '""" + DATA_CLASS_EXTENSION + """
    '.""";

  static AsyncToolSpecification specification() {
    var dataClassGenerator = Tool.builder()
        .name(NAME)
        .description(DESCRIPTION)
        .inputSchema(INPUT_SCHEMA)
        .build();
    return AsyncToolSpecification.builder()
        .tool(dataClassGenerator)
        .callHandler(IvyDataClassCreatorTool::callHandler)
        .build();
  }

  private static Mono<CallToolResult> callHandler(@SuppressWarnings("unused") McpAsyncServerExchange _exchange, CallToolRequest request) {
    return Mono.just(new CallToolResult(List.of(new TextContent(TOOL_RESULT_INTRO), new TextContent(request.arguments().toString())), false));
  }

  private static String loadSchema() {
    // TODO: load-schema form ivy-core. Keep it as static resource.
    return ResponseSchema.DATA_CLASS.schema().toString();
  }
}

package ch.ivyteam.smart.core.tool;

import static ch.ivyteam.ivy.IvyConstants.DIRECTORY_PROCESSES;
import static ch.ivyteam.ivy.IvyConstants.PROCESS_EXTENSION;
import static io.modelcontextprotocol.spec.McpSchema.Role.USER;

import java.time.Duration;
import java.util.List;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.SamplingMessage;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import ch.ivyteam.smart.core.schema.ResponseSchema;
import reactor.core.publisher.Mono;

public interface IvyProcessCreatorTool {

  String NAME = "ivy-process-creator";
  String DESCRIPTION = """
    Creates an Axon Ivy Process definition based on a user request.
    Use this tool when the user asks to create, model, or design a new process (workflow) including tasks/dialogs/forms, data-classes, variables, external integrations, scheduling, notifications, etc.""";
  String INPUT_SCHEMA = """
    {
      "type": "object",
      "properties": { "processDescription": { "type": "string" } },
      "description": "User description of the business process to model.",
      "required": ["processDescription"]
    }""";

  String CREATE_PROCESS_SYSTEM_PROMPT = """
    You are to generate an Axon Ivy process model.
    Use the description provided by the user to produce a JSON object conforming to this schema:
    """ + loadSchema() + """


    Guidelines:
    - Omit as many defaults as possible, but at any rate produce the required values.
    - Create unique instances for element IDs, starting from 'f1'.
    - Draw elements as graphs.
    - Do not set any visual attributes on elements, except the position 'at'.
    - Visualize roles as pools.""";

  String TOOL_RESULT_INTRO = """
    Create a file containing the process definition.
    The file must be located in the directory '<project-root>/""" + DIRECTORY_PROCESSES + """
    ' and have the file extension '""" + PROCESS_EXTENSION + """
    '.""";

  static AsyncToolSpecification specification() {
    var processGenerator = Tool.builder()
        .name(NAME)
        .description(DESCRIPTION)
        .inputSchema(INPUT_SCHEMA)
        .build();
    return AsyncToolSpecification.builder()
        .tool(processGenerator)
        .callHandler(IvyProcessCreatorTool::callHandler)
        .build();
  }

  private static Mono<CallToolResult> callHandler(McpAsyncServerExchange exchange, CallToolRequest request) {
    var clientCapabilities = exchange.getClientCapabilities();
    if (clientCapabilities == null || clientCapabilities.sampling() == null) {
      throw new IllegalStateException("Client does not support sampling");
    }
    var samplingResult = exchange
        .createMessage(samplingRequest(request))
        .timeout(Duration.ofSeconds(60))
        .block();
    return Mono.just(new CallToolResult(List.of(new TextContent(TOOL_RESULT_INTRO), toTextContent(samplingResult)), false));
  }

  private static CreateMessageRequest samplingRequest(CallToolRequest request) {
    return CreateMessageRequest.builder()
        .systemPrompt(CREATE_PROCESS_SYSTEM_PROMPT)
        .messages(List.of(new SamplingMessage(USER, new TextContent(request.arguments().get("processDescription").toString()))))
        .build();
  }

  private static TextContent toTextContent(CreateMessageResult samplingResult) {
    if (samplingResult == null || samplingResult.content() == null) {
      return new TextContent("[no sampling response]");
    }
    var content = samplingResult.content();
    if (content instanceof TextContent textContent) {
      return new TextContent(textContent.text());
    }
    return new TextContent(content.toString());
  }

  private static String loadSchema() {
    // TODO: load-schema form ivy-core. Keep it as static resource in process.model.io bundle.
    return ResponseSchema.PROCESS.schema().toString();
  }
}

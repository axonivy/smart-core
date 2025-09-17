package ch.ivyteam.smart.core.tool;

import static ch.ivyteam.ivy.IvyConstants.DIRECTORY_PROCESSES;
import static ch.ivyteam.ivy.IvyConstants.PROCESS_EXTENSION;
import static io.modelcontextprotocol.spec.McpSchema.Role.USER;

import java.time.Duration;
import java.util.List;

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

public interface IvyProcessGeneratorTool {

  static AsyncToolSpecification specification() {
    var processGenerator = Tool.builder()
        .name("ivy-process-generator")
        .description("Generates an Ivy Process based on a user description.")
        .inputSchema("""
          {
            "type":"object",
            "properties": { "processDescription": { "type":"string" } },
            "description": "The description of the process to generate.",
            "required":["processDescription"]
          }
          """)
        .build();

    return AsyncToolSpecification.builder()
        .tool(processGenerator)
        .callHandler((exchange, request) -> {
          var clientCapabilities = exchange.getClientCapabilities();
          if (clientCapabilities == null || clientCapabilities.sampling() == null) {
            throw new IllegalStateException("Client does not support sampling");
          }

          var samplingResult = exchange
              .createMessage(samplingRequest(request))
              .timeout(Duration.ofSeconds(60))
              .block();

          var callToolResultContent = toTextContent(samplingResult);
          return Mono.just(new CallToolResult(List.of(new TextContent("""
            Create a file containing the process data.
            The file must be located in the directory <project-root>/""" + DIRECTORY_PROCESSES + """
            and have the file extension """ + PROCESS_EXTENSION + """
            .
            Process data:"""), callToolResultContent), false));
        })
        .build();
  }

  private static CreateMessageRequest samplingRequest(CallToolRequest request) {
    return CreateMessageRequest.builder()
        .systemPrompt("""
          Create a process based on the description provided by the user.
          You must answer strictly in the following JSON format:""" + loadSchema() + """

          Omit as many defaults as possible, but at any rate produce the required values.
          Generate the 'data' as java qualified name.
          For element ID's create unique instances, starting from f1.
          Draw elements as graph.
          Do not set any visual attributes on element, except the position 'at'.
          Set the root process 'id' out of 16 random uppercase letters or numbers.
          Visualize roles as pool.""")
        .messages(List.of(new SamplingMessage(USER,
            new TextContent(request.arguments().get("processDescription").toString()))))
        .build();
  }

  private static String loadSchema() {
    // TODO: load-schema form ivy-core. Keep it as static resource in process.model.io bundle.
    return ResponseSchema.PROCESS.schema().toString();
  }

  static TextContent toTextContent(CreateMessageResult samplingResult) {
    if (samplingResult == null || samplingResult.content() == null) {
      return new TextContent("[no sampling response]");
    }
    var content = samplingResult.content();
    if (content instanceof TextContent textContent) {
      return new TextContent(textContent.text());
    }
    return new TextContent(content.toString());
  }
}

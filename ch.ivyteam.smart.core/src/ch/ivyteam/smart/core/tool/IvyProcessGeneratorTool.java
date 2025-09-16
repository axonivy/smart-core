package ch.ivyteam.smart.core.tool;

import static io.modelcontextprotocol.spec.McpSchema.Role.USER;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.SamplingMessage;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import ch.ivyteam.smart.core.schema.SchemaLoader;
import reactor.core.publisher.Mono;

public class IvyProcessGeneratorTool {

  public static AsyncToolSpecification specification() {
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

          String schema;
          try (var schemaStream = SchemaLoader.class.getResourceAsStream("proc-inline-full.json")) {
            schema = new String(schemaStream.readAllBytes());
          } catch (IOException ex) {
            throw new RuntimeException(ex);
          }

          var samplingRequest = CreateMessageRequest.builder()
              .systemPrompt("""
                Create a process based on the description provided by the user.
                You must answer strictly in the following JSON format:""" + schema + """

                Omit as many defaults as possible, but at any rate produce the required values.
                Generate the 'data' as java qualified name.
                For element ID's create unique instances, starting from f1.
                Draw elements as graph.
                Do not set any visual attributes on element, except the position 'at'.
                Set the root process 'id' out of 16 random uppercase letters or numbers.
                Visualize roles as pool.""")
              .messages(List.of(new SamplingMessage(
                  USER,
                  new TextContent(request.arguments().get("processDescription").toString()))))
              .build();
          var samplingResult = exchange.createMessage(samplingRequest).timeout(Duration.ofSeconds(60)).block();

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
          return Mono.just(new CallToolResult(List.of(callToolResultContent), false));
        })
        .build();
  }
}

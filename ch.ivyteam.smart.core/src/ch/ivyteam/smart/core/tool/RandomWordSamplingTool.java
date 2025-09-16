package ch.ivyteam.smart.core.tool;

import static io.modelcontextprotocol.spec.McpSchema.Role.USER;

import java.util.List;

import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.SamplingMessage;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import reactor.core.publisher.Mono;

public class RandomWordSamplingTool {

  public static AsyncToolSpecification specification() {
    var randomWordToolUsingSampling = Tool.builder()
        .name("random-word")
        .description("Generates a random word.")
        .inputSchema("{}")
        .build();

    return AsyncToolSpecification.builder()
        .tool(randomWordToolUsingSampling)
        .callHandler((exchange, request) -> {
          var clientCapabilities = exchange.getClientCapabilities();
          if (clientCapabilities == null || clientCapabilities.sampling() == null) {
            throw new IllegalStateException("Client does not support sampling");
          }

          var samplingMessage = new SamplingMessage(
              USER,
              new TextContent("Please reply with a single random English word. No punctuation, no explanation."));
          var samplingRequest = CreateMessageRequest.builder()
              .messages(List.of(samplingMessage))
              .build();
          var samplingResult = exchange.createMessage(samplingRequest).block();

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

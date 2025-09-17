package ch.ivyteam.smart.core.tool;

import static io.modelcontextprotocol.spec.McpSchema.Role.USER;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.McpClientMock;

@ManagedServer
public class IvyProcessGeneratorToolTest {

  List<CreateMessageRequest> samplingRequests;

  McpAsyncClient client;

  @BeforeEach
  void beforeEach() {
    samplingRequests = new ArrayList<>();

    client = McpClientMock.create(samplingRequests::add);
    client.initialize();
  }

  @Test
  void processGeneration() {
    var processDescription = "Description of the process to generate";
    var result = client.callTool(CallToolRequest.builder()
        .name("ivy-process-generator")
        .arguments(Map.of("processDescription", processDescription))
        .build())
        .block();

    assertThat(samplingRequests).hasSize(1);
    var samplingRequest = samplingRequests.getFirst();
    assertThat(samplingRequest.systemPrompt()).startsWith("Create a process");
    assertThat(samplingRequest.messages()).hasSize(1);
    var samplingMessage = samplingRequest.messages().getFirst();
    assertThat(samplingMessage.role()).isEqualTo(USER);
    assertThat(((TextContent) samplingMessage.content()).text()).isEqualTo(processDescription);

    assertThat(result.content()).hasSize(2);
    assertThat(((TextContent) result.content().getFirst()).text()).startsWith("Create a file");
    assertThat(((TextContent) result.content().get(1)).text()).isEqualTo("Sampling response mock");
  }
}

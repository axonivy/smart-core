package ch.ivyteam.smart.core.tool;

import static ch.ivyteam.smart.core.McpClientMock.SAMPLING_RESPONSE_MOCK;
import static ch.ivyteam.smart.core.tool.IvyProcessCreatorTool.CREATE_PROCESS_SYSTEM_PROMPT;
import static ch.ivyteam.smart.core.tool.IvyProcessCreatorTool.NAME;
import static ch.ivyteam.smart.core.tool.IvyProcessCreatorTool.TOOL_RESULT_INTRO;
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
public class IvyProcessCreatorToolTest {

  List<CreateMessageRequest> samplingRequests;

  McpAsyncClient client;

  @BeforeEach
  void beforeEach() {
    samplingRequests = new ArrayList<>();

    client = McpClientMock.create(samplingRequests::add);
  }

  @Test
  void processCreation() {
    var processDescription = "Description of the process to create";
    var result = client.callTool(CallToolRequest.builder()
        .name(NAME)
        .arguments(Map.of("processDescription", processDescription))
        .build())
        .block();

    assertThat(samplingRequests).hasSize(1);
    var samplingRequest = samplingRequests.getFirst();
    assertThat(samplingRequest.systemPrompt()).isEqualTo(CREATE_PROCESS_SYSTEM_PROMPT);
    assertThat(samplingRequest.messages()).hasSize(1);
    var samplingMessage = samplingRequest.messages().getFirst();
    assertThat(samplingMessage.role()).isEqualTo(USER);
    assertThat(((TextContent) samplingMessage.content()).text()).isEqualTo(processDescription);

    assertThat(result.content()).hasSize(2);
    assertThat(((TextContent) result.content().getFirst()).text()).isEqualTo(TOOL_RESULT_INTRO);
    assertThat(((TextContent) result.content().get(1)).text()).isEqualTo(SAMPLING_RESPONSE_MOCK);
  }
}

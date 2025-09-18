package ch.ivyteam.smart.core.tool;

import static ch.ivyteam.smart.core.tool.IvyDataClassCreatorTool.NAME;
import static ch.ivyteam.smart.core.tool.IvyDataClassCreatorTool.TOOL_RESULT_INTRO;
import static org.assertj.core.api.Assertions.assertThat;
import static test.ch.ivyteam.ivy.json.JsonNodeAssert.assertNode;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.McpClientMock;

@ManagedServer
public class IvyDataClassCreatorToolTest {

  McpAsyncClient client;

  @BeforeEach
  void beforeEach() {
    client = McpClientMock.create();
  }

  @Test
  void dataClassCreation() throws Exception {
    var result = client.callTool(CallToolRequest.builder()
        .name(NAME)
        .arguments(Map.of(
            "simpleName", "TestDataClass",
            "namespace", "test.project"))
        .build())
        .block();

    assertThat(result.content()).hasSize(2);
    assertThat(((TextContent) result.content().getFirst()).text()).isEqualTo(TOOL_RESULT_INTRO);

    var dataClassDefinition = new ObjectMapper().readTree(((TextContent) result.content().get(1)).text());
    assertNode(dataClassDefinition).fieldNames().hasSize(2);
    assertNode(dataClassDefinition.get("simpleName")).isEqualTo("TestDataClass");
    assertNode(dataClassDefinition.get("namespace")).isEqualTo("test.project");
  }
}

package ch.ivyteam.smart.core.tool;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.McpClientMock;
import ch.ivyteam.smart.core.tool.impl.IvyDataClassSchemaTool;

@ManagedServer
public class IvyDataClassSchemaToolTest {

  McpAsyncClient client;

  @BeforeEach
  void beforeEach() {
    client = McpClientMock.create();
  }

  @Test
  void callTool() throws Exception {
    var tool = new IvyDataClassSchemaTool();
    var result = client.callTool(CallToolRequest.builder()
        .name(tool.name())
        .build())
        .block();

    assertThat(result.content()).hasSize(2);
    assertThat(((TextContent) result.content().get(0)).text()).isEqualTo(tool.schema());
    assertThat(((TextContent) result.content().get(1)).text()).isEqualTo("Guidelines:\n" + tool.guidelines());
  }
}

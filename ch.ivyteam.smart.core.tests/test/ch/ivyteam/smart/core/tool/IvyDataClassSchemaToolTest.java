package ch.ivyteam.smart.core.tool;

import static ch.ivyteam.smart.core.tool.IvyDataClassSchemaTool.GUIDELINES;
import static ch.ivyteam.smart.core.tool.IvyDataClassSchemaTool.NAME;
import static ch.ivyteam.smart.core.tool.IvyDataClassSchemaTool.loadSchema;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.McpClientMock;

@ManagedServer
public class IvyDataClassSchemaToolTest {

  McpAsyncClient client;

  @BeforeEach
  void beforeEach() {
    client = McpClientMock.create();
  }

  @Test
  void dataClassSchema() throws Exception {
    var result = client.callTool(CallToolRequest.builder()
        .name(NAME)
        .build())
        .block();

    assertThat(result.content()).hasSize(2);
    assertThat(((TextContent) result.content().getFirst()).text()).isEqualTo(loadSchema());
    assertThat(((TextContent) result.content().getFirst()).text()).isEqualTo(GUIDELINES);
  }
}

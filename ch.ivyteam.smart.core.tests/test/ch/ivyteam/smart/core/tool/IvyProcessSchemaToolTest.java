package ch.ivyteam.smart.core.tool;

import static ch.ivyteam.smart.core.tool.IvyProcessSchemaTool.GUIDELINES;
import static ch.ivyteam.smart.core.tool.IvyProcessSchemaTool.NAME;
import static ch.ivyteam.smart.core.tool.IvyProcessSchemaTool.loadSchema;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.McpClientMock;

@ManagedServer
public class IvyProcessSchemaToolTest {

  McpAsyncClient client;

  @BeforeEach
  void beforeEach() {
    client = McpClientMock.create();
  }

  @Test
  void processSchema() {
    var result = client.callTool(CallToolRequest.builder()
        .name(NAME)
        .build())
        .block();

    assertThat(result.content()).hasSize(2);
    assertThat(((TextContent) result.content().getFirst()).text()).isEqualTo(loadSchema());
    assertThat(((TextContent) result.content().getFirst()).text()).isEqualTo(GUIDELINES);
  }
}

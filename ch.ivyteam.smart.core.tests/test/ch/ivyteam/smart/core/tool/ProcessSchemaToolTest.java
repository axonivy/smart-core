package ch.ivyteam.smart.core.tool;

import static ch.ivyteam.smart.core.schema.ResourceSchema.PROCESS;
import static ch.ivyteam.smart.core.tool.SchemaTool.GUIDELINES_HEADER;
import static ch.ivyteam.smart.core.tool.impl.ProcessSchemaTool.NAME;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.McpClientMock;

@ManagedServer
public class ProcessSchemaToolTest {

  McpAsyncClient client = McpClientMock.create();

  @Test
  void callTool() {
    var result = client.callTool(CallToolRequest.builder().name(NAME).build()).block();

    assertThat(result.content()).hasSize(2);
    assertThat(((TextContent) result.content().get(0)).text()).isEqualTo(PROCESS.schema().toString());
    assertThat(((TextContent) result.content().get(1)).text()).startsWith(GUIDELINES_HEADER);
  }
}

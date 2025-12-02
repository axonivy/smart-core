package ch.ivyteam.smart.core.tool;

import static ch.ivyteam.smart.core.schema.ResourceSchema.DATA_CLASS;
import static ch.ivyteam.smart.core.tool.SchemaTool.GUIDELINES_HEADER;
import static ch.ivyteam.smart.core.tool.impl.DataClassSchemaTool.NAME;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.McpClientAccess;

@ManagedServer
public class DataClassSchemaToolTest {

  @RegisterExtension
  McpClientAccess mcp = new McpClientAccess();

  @Test
  void callTool(McpAsyncClient client) {
    var result = client.callTool(CallToolRequest.builder().name(NAME).build()).block();

    assertThat(result.content()).hasSize(2);
    assertThat(((TextContent) result.content().get(0)).text()).isEqualTo(DATA_CLASS.schema().toString());
    assertThat(((TextContent) result.content().get(1)).text()).startsWith(GUIDELINES_HEADER);
  }
}

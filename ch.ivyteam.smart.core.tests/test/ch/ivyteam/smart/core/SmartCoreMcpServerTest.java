package ch.ivyteam.smart.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.tool.impl.DataClassSchemaTool;
import ch.ivyteam.smart.core.tool.impl.FormSchemaTool;
import ch.ivyteam.smart.core.tool.impl.ProcessSchemaTool;
import ch.ivyteam.smart.core.tool.impl.market.MarketInstallTool;
import ch.ivyteam.smart.core.tool.impl.market.MarketSearchTool;

@ManagedServer
public class SmartCoreMcpServerTest {

  @RegisterExtension
  McpClientAccess mcp = new McpClientAccess();

  @Test
  void tools(McpAsyncClient client) {
    assertThat(client.listTools().block().tools())
        .extracting(Tool::name)
        .containsExactly(
            ProcessSchemaTool.NAME,
            DataClassSchemaTool.NAME,
            FormSchemaTool.NAME,
            MarketSearchTool.NAME,
            MarketInstallTool.NAME);
  }
}

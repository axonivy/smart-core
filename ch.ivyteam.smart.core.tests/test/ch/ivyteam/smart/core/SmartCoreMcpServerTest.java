package ch.ivyteam.smart.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.tool.impl.IvyDataClassSchemaTool;
import ch.ivyteam.smart.core.tool.impl.IvyFormSchemaTool;
import ch.ivyteam.smart.core.tool.impl.IvyProcessSchemaTool;
import ch.ivyteam.smart.core.tool.impl.market.MarketSearchTool;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.Tool;

@ManagedServer
public class SmartCoreMcpServerTest {

  McpAsyncClient client;

  @BeforeEach
  void beforeEach() {
    client = McpClientMock.create();
  }

  @Test
  void tools() {
    assertThat(client.listTools().block().tools())
        .extracting(Tool::name)
        .containsExactly(
            new IvyProcessSchemaTool().name(),
            new IvyDataClassSchemaTool().name(),
            new IvyFormSchemaTool().name(),
            MarketSearchTool.NAME);
  }
}

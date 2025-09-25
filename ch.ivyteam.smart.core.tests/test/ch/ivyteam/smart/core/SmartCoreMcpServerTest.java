package ch.ivyteam.smart.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.tool.IvyDataClassSchemaTool;
import ch.ivyteam.smart.core.tool.IvyProcessSchemaTool;

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
        .containsExactly(IvyProcessSchemaTool.NAME, IvyDataClassSchemaTool.NAME);
  }
}

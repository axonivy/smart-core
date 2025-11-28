package ch.ivyteam.smart.core.transport;

import static ch.ivyteam.smart.core.tool.impl.DataClassSchemaTool.NAME;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;

import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.McpClientMock;
import ch.ivyteam.test.log.LoggerAccess;

@ManagedServer
public class TransportLogFilterTest {

  McpAsyncClient client = McpClientMock.create();

  @RegisterExtension
  LoggerAccess log = new LoggerAccess(TransportLogFilter.class.getName());

  @Test
  void log() {
    client.callTool(CallToolRequest.builder().name(NAME).build()).block();
    var logs = log.all();
    logs = log.all();
  }
}

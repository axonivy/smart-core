package ch.ivyteam.smart.core.transport.filter.log;

import static ch.ivyteam.smart.core.SmartCoreMcpServer.SERVLET_PATH;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;

import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.McpClientAccess;
import ch.ivyteam.smart.core.tool.impl.DataClassSchemaTool;
import ch.ivyteam.test.log.LoggerAccess;

@ManagedServer
public class TransportLogFilterTest {

  static final String REQUEST_LOG_HEADER = "--- HTTP Request ---";
  static final String RESPONSE_LOG_HEADER = "--- HTTP Response ---";
  static final String EMPTY_BODY = "Body: <empty>";

  @RegisterExtension
  McpClientAccess mcp = new McpClientAccess();

  @RegisterExtension
  LoggerAccess log = new LoggerAccess(TransportLogFilter.class.getName());

  @Test
  void log(McpAsyncClient client) {
    client.callTool(CallToolRequest.builder().name(DataClassSchemaTool.NAME).build()).block();
    var logs = log.debugs();
    assertThat(logs).hasSize(8);
    assertThat(logs.get(0))
        .contains(REQUEST_LOG_HEADER)
        .contains("POST " + SERVLET_PATH)
        .contains("\"method\":\"initialize\"");
    assertThat(logs.get(1))
        .contains(RESPONSE_LOG_HEADER)
        .contains("Status: 200")
        .contains("\"serverInfo\":{\"name\":\"smart-core-sse\"");
    assertThat(logs.get(2))
        .contains(REQUEST_LOG_HEADER)
        .contains("GET " + SERVLET_PATH)
        .contains(EMPTY_BODY);
    assertThat(logs.get(3))
        .contains(RESPONSE_LOG_HEADER)
        .contains("Status: 200")
        .contains(EMPTY_BODY);
    assertThat(logs.get(4))
        .contains(REQUEST_LOG_HEADER)
        .contains("POST " + SERVLET_PATH)
        .contains("\"method\":\"notifications/initialized\"");
    assertThat(logs.get(5))
        .contains(RESPONSE_LOG_HEADER)
        .contains("Status: 202")
        .contains(EMPTY_BODY);
    assertThat(logs.get(6))
        .contains(REQUEST_LOG_HEADER)
        .contains("POST " + SERVLET_PATH)
        .contains("\"method\":\"tools/call\"")
        .contains("\"params\":{\"name\":\"" + DataClassSchemaTool.NAME + "\"}");
    assertThat(logs.get(7))
        .contains(RESPONSE_LOG_HEADER)
        .contains("Status: 200")
        .contains("event: message");
  }
}

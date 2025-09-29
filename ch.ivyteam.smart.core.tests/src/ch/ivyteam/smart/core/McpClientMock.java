package ch.ivyteam.smart.core;

import static ch.ivyteam.smart.core.SmartCoreMcpServer.SERVLET_PATH;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;

import ch.ivyteam.ivy.request.EngineUriResolver;

public interface McpClientMock {

  static McpAsyncClient create() {
    var baseUri = EngineUriResolver.instance().local() + SERVLET_PATH;
    var transportProvider = HttpClientStreamableHttpTransport.builder(baseUri)
        .endpoint(baseUri + "/mcp")
        .build();

    return McpClient.async(transportProvider).build();
  }
}

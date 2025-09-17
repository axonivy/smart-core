package ch.ivyteam.smart.core;

import static ch.ivyteam.smart.core.SmartCoreMcpServer.SERVLET_PATH;

import java.util.function.Consumer;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema.ClientCapabilities;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;

import ch.ivyteam.ivy.request.EngineUriResolver;
import reactor.core.publisher.Mono;

public interface McpClientMock {

  static McpAsyncClient create() {
    return create(request -> {});
  }

  static McpAsyncClient create(Consumer<CreateMessageRequest> samplingRequestConsumer) {
    var baseUri = EngineUriResolver.instance().local() + SERVLET_PATH;
    var transportProvider = HttpClientStreamableHttpTransport.builder(baseUri)
        .endpoint(baseUri + "/mcp")
        .build();

    return McpClient.async(transportProvider)
        .capabilities(ClientCapabilities.builder().sampling().build())
        .sampling(request -> {
          samplingRequestConsumer.accept(request);
          return Mono.just(CreateMessageResult.builder().message("Sampling response mock").build());
        })
        .build();
  }
}

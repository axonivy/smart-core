package ch.ivyteam.smart.core;

import java.util.List;

import javax.servlet.ServletContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncResourceSpecification;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

import ch.ivyteam.ivy.webserver.extension.ServletContextStartupListener;
import ch.ivyteam.smart.core.mcp.transport.JavaxHttpServletStreamableServerTransportProvider;
import ch.ivyteam.smart.core.schema.ResponseSchema;
import reactor.core.publisher.Mono;

public class SmartCoreMcpServer implements ServletContextStartupListener {
  public static final String SERVLET_PATH = "/smart-core";

  @Override
  public void onStartup(ServletContext ctx) {
    var transportProvider = JavaxHttpServletStreamableServerTransportProvider.builder()
        .objectMapper(new ObjectMapper())
        .build();

    McpServer.async(transportProvider)
        .serverInfo("smart-core-sse", "0.0.1")
        .capabilities(ServerCapabilities.builder().resources(false, false).build())
        .resources(new AsyncResourceSpecification(
            Resource.builder()
                .name("ivy-process-schema")
                .description("The JSON schema all Axon Ivy processes must adhere to.")
                .mimeType("application/json")
                .uri(ResponseSchema.DATA_CLASS.source().toString())
                .build(),
            (exchange, request) -> Mono.just(new ReadResourceResult(List.of(new TextResourceContents(
                ResponseSchema.DATA_CLASS.source().toString(),
                "application/json",
                ResponseSchema.PROCESS.schema().toString()))))))
        .build();

    var servlet = ctx.addServlet("smart-core-mcp", transportProvider);
    servlet.addMapping(SERVLET_PATH + "/*");
    servlet.setAsyncSupported(true);
  }
}

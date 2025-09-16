package ch.ivyteam.smart.core;

import java.util.List;

import javax.servlet.ServletContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;

import ch.ivyteam.ivy.webserver.extension.ServletContextStartupListener;
import ch.ivyteam.smart.core.mcp.transport.JavaxHttpServletSeeTransportProvider;
import ch.ivyteam.smart.core.tool.RandomWordSamplingTool;
import ch.ivyteam.smart.core.tool.TemperatureServiceTool;

public class SmartCoreMcpServer implements ServletContextStartupListener {

  @Override
  public void onStartup(ServletContext ctx) {
    var transportProvider = JavaxHttpServletSeeTransportProvider.builder()
        .objectMapper(new ObjectMapper())
        .messageEndpoint("/smart-core")
        .build();

    McpServer.sync(transportProvider)
        .serverInfo("smart-core-sse", "0.0.1")
        .capabilities(ServerCapabilities.builder().tools(true).build())
        .tools(List.of(TemperatureServiceTool.specification(), RandomWordSamplingTool.specification()))
        .build();

    var servlet = ctx.addServlet("smart-core-mcp", transportProvider);
    servlet.addMapping("/smart-core/*");
    servlet.setAsyncSupported(true);
  }
}

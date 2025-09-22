package ch.ivyteam.smart.core;

import java.util.List;

import javax.servlet.ServletContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.json.schema.JsonSchemaValidator;
import io.modelcontextprotocol.json.schema.jackson.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;

import ch.ivyteam.ivy.webserver.extension.ServletContextStartupListener;
import ch.ivyteam.smart.core.mcp.transport.JavaxHttpServletStreamableServerTransportProvider;
import ch.ivyteam.smart.core.tool.impl.IvyDataClassSchemaTool;
import ch.ivyteam.smart.core.tool.impl.IvyFormSchemaTool;
import ch.ivyteam.smart.core.tool.impl.IvyProcessSchemaTool;

public class SmartCoreMcpServer implements ServletContextStartupListener {
  public static final String SERVLET_PATH = "/smart-core";

  public static final McpJsonMapper MAPPER = new JacksonMcpJsonMapper(new ObjectMapper());
  private static final JsonSchemaValidator VALIDATOR = new DefaultJsonSchemaValidator();

  @Override
  public void onStartup(ServletContext ctx) {
    var transportProvider = JavaxHttpServletStreamableServerTransportProvider.builder()
        .jsonMapper(MAPPER)
        .build();

    McpServer.async(transportProvider)
        .serverInfo("smart-core-sse", "0.0.1")
        .capabilities(ServerCapabilities.builder().tools(true).build())
        .tools(List.of(
            new IvyProcessSchemaTool().specification(),
            new IvyDataClassSchemaTool().specification(),
            new IvyFormSchemaTool().specification()))
        .jsonMapper(MAPPER)
        .jsonSchemaValidator(VALIDATOR)
        .build();

    var servlet = ctx.addServlet("smart-core-mcp", transportProvider);
    servlet.addMapping(SERVLET_PATH + "/*");
    servlet.setAsyncSupported(true);
  }
}

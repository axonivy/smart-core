package ch.ivyteam.smart.core;

import java.util.List;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.json.schema.JsonSchemaValidator;
import io.modelcontextprotocol.json.schema.jackson.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;

import ch.ivyteam.ivy.webserver.extension.ServletContextStartupListener;
import ch.ivyteam.smart.core.tool.impl.DataClassSchemaTool;
import ch.ivyteam.smart.core.tool.impl.FormSchemaTool;
import ch.ivyteam.smart.core.tool.impl.ProcessSchemaTool;
import ch.ivyteam.smart.core.tool.market.impl.MarketInstallTool;
import ch.ivyteam.smart.core.tool.market.impl.MarketSearchTool;
import ch.ivyteam.smart.core.transport.JavaxHttpServletStreamableServerTransportProvider;
import ch.ivyteam.smart.core.transport.filter.log.TransportLogFilter;

public class SmartCoreMcpServer implements ServletContextStartupListener {

  private static final String NAME = "smart-core";
  public static final String SERVLET_PATH = "/" + NAME;

  public static final McpJsonMapper MAPPER = new JacksonMcpJsonMapper(new ObjectMapper());
  private static final JsonSchemaValidator VALIDATOR = new DefaultJsonSchemaValidator();

  @Override
  public void onStartup(ServletContext ctx) {
    var transportProvider = JavaxHttpServletStreamableServerTransportProvider.builder()
        .jsonMapper(MAPPER)
        .build();

    McpServer.async(transportProvider)
        .serverInfo(NAME + "-sse", "0.0.1")
        .capabilities(ServerCapabilities.builder().tools(true).build())
        .tools(List.of(
            new ProcessSchemaTool().specification(),
            new DataClassSchemaTool().specification(),
            new FormSchemaTool().specification(),
            new MarketSearchTool().specification(),
            new MarketInstallTool().specification()))
        .jsonMapper(MAPPER)
        .jsonSchemaValidator(VALIDATOR)
        .build();

    addServlet(ctx, transportProvider);
    addFilter(ctx, "transport-log-filter", new TransportLogFilter());
  }

  private static void addServlet(ServletContext ctx, Servlet servlet) {
    var addedServlet = ctx.addServlet(NAME, servlet);
    addedServlet.addMapping(SERVLET_PATH + "/*");
    addedServlet.setAsyncSupported(true);
  }

  private static void addFilter(ServletContext ctx, String filterName, Filter filter) {
    var addedFilter = ctx.addFilter(NAME + "-" + filterName, filter);
    addedFilter.addMappingForServletNames(null, true, NAME);
  }
}

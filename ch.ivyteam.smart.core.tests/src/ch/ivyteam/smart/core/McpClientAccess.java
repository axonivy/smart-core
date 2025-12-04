package ch.ivyteam.smart.core;

import static ch.ivyteam.smart.core.McpClientUtils.SMART_CORE_BASE_URL;
import static ch.ivyteam.smart.core.McpClientUtils.SMART_CORE_MCP_URL;
import static org.apache.logging.log4j.Level.DEBUG;
import static org.apache.logging.log4j.core.appender.ConsoleAppender.Target.SYSTEM_OUT;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;

import ch.ivyteam.smart.core.transport.filter.log.TransportLogFilter;

public class McpClientAccess implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

  private final boolean enableTransportLogger;

  private ConsoleAppender appender;
  private Logger transportLogger;

  private McpAsyncClient client;

  public McpClientAccess() {
    this(false);
  }

  public McpClientAccess(boolean enableTransportLogger) {
    this.enableTransportLogger = enableTransportLogger;
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    initializeTransportLogger();
    initializeClient();
  }

  private void initializeTransportLogger() {
    if (!enableTransportLogger) {
      return;
    }
    var transportLoggerName = TransportLogFilter.class.getName();
    Configurator.setLevel(transportLoggerName, DEBUG);
    appender = ConsoleAppender.newBuilder()
        .setName("Console")
        .setTarget(SYSTEM_OUT)
        .build();
    appender.start();
    transportLogger = (Logger) LogManager.getLogger(transportLoggerName);
    transportLogger.addAppender(appender);
  }

  private void initializeClient() {
    var transportProvider = HttpClientStreamableHttpTransport.builder(SMART_CORE_BASE_URL)
        .endpoint(SMART_CORE_MCP_URL)
        .build();

    client = McpClient.async(transportProvider).build();
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    if (!enableTransportLogger) {
      return;
    }
    transportLogger.removeAppender(appender);
    appender.stop();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return parameterContext.getParameter().getType().equals(McpAsyncClient.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return client;
  }
}

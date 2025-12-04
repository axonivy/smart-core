package ch.ivyteam.smart.core;

import static ch.ivyteam.smart.core.SmartCoreMcpServer.SERVLET_PATH;

import ch.ivyteam.ivy.request.EngineUriResolver;

public interface McpClientUtils {

  String SMART_CORE_BASE_URL = EngineUriResolver.instance().local() + SERVLET_PATH;
  String SMART_CORE_MCP_URL = SMART_CORE_BASE_URL + "/mcp";
}

package ch.ivyteam.smart.core.copilot;

import static ch.ivyteam.smart.core.McpClientUtils.SMART_CORE_MCP_URL;

import java.io.IOException;
import java.util.UUID;

import org.testcontainers.images.builder.Transferable;

public class Copilot {

  private final CopilotContainer container;

  private Copilot(CopilotContainer container) {
    this.container = container;
  }

  public String prompt(String prompt) throws InterruptedException, IOException {
    var resource = "copilot-cli-" + UUID.randomUUID();
    var containerWorkspace = "/" + resource;

    var result = container.execInContainer(
        "sh", "-c",
        "mkdir \"$0\" && "
            + "cd \"$0\" && "
            + "OTEL_SERVICE_NAME=\"$1\" "
            + "copilot -p \"$2\" "
            + "--no-ask-user --allow-all-tools -s",
        containerWorkspace, resource, prompt);
    if (result.getExitCode() != 0) {
      throw new RuntimeException("Copilot command failed: " + result.getStderr());
    }

    return resource;
  }

  static Copilot create(CopilotContainer container) {
    container.copyFileToContainer(
        Transferable.of(smartCoreMcpServerConfig()),
        "/root/.copilot/mcp-config.json");
    return new Copilot(container);
  }

  private static String smartCoreMcpServerConfig() {
    var smartCoreMcpUrl = SMART_CORE_MCP_URL.replace("localhost", "host.docker.internal");
    return String.format("""
      {
        "mcpServers": {
          "smart-core": {
            "url": "%s",
            "type": "http"
          }
        }
      }""",
        smartCoreMcpUrl);
  }
}

package ch.ivyteam.smart.core.copilot;

import java.io.IOException;
import java.util.UUID;

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
    return new Copilot(container);
  }
}

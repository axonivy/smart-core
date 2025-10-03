package ch.ivyteam.smart.core.tool.impl;

import static ch.ivyteam.ivy.IvyConstants.DIRECTORY_PROCESSES;
import static ch.ivyteam.ivy.IvyConstants.PROCESS_EXTENSION;
import static ch.ivyteam.smart.core.schema.ResourceSchema.PROCESS;

import ch.ivyteam.smart.core.tool.IvySchemaTool;

public class IvyProcessSchemaTool implements IvySchemaTool {

  @Override
  public String name() {
    return "ivy-process-schema";
  }

  @Override
  public String resourceFileExtension() {
    return PROCESS_EXTENSION;
  }

  @Override
  public String guidelines() {
    return """
      - Process files must be located in a subdirectory of '<project-root>/""" + DIRECTORY_PROCESSES + """
      /'. Note: This does not apply to form processes.
      - Omit as many defaults as possible, but at any rate produce the required values.
      - Create unique instances for element IDs, starting from 'f1'.
      - Draw elements as graphs.
      - Do not set any visual attributes on elements, except the position 'at'.
      - Visualize roles as pools.
      - Ensure elements are connected to the next one in the sequence flow.
      - A process requires a process data class. Create it if needed.""";
  }

  @Override
  public String schema() {
    return PROCESS.schema().toString();
  }
}

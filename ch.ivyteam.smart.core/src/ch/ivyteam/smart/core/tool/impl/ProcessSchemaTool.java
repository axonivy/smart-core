package ch.ivyteam.smart.core.tool.impl;

import static ch.ivyteam.ivy.IvyConstants.DIRECTORY_PROCESSES;
import static ch.ivyteam.ivy.IvyConstants.PROCESS_EXTENSION;
import static ch.ivyteam.smart.core.schema.ResourceSchema.PROCESS;

import ch.ivyteam.smart.core.tool.SchemaTool;

public class ProcessSchemaTool implements SchemaTool {

  public static final String NAME = "process-schema";
  private static final String RESOURCE = "process";
  private static final String SCHEMA = PROCESS.schema().toString();
  private static final String GUIDELINES = """
    - Process files must be located in a subdirectory of '<project-root>/""" + DIRECTORY_PROCESSES + """
    /'. Note: This does not apply to form processes.
    - Omit as many defaults as possible, but at any rate produce the required values.
    - Create unique instances for element IDs, starting from 'f1'.
    - Ensure elements are connected to the next one in the sequence flow.
    - Do not set any visual attributes on elements, except the position 'at'.
    - Visualize roles as pools.
    - A process requires a process data class. Create it if needed.
    - To use the process data class attributes, use 'in.<attribute>' to retrieve data and 'out.<attribute>' to set modified data.""";

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public String resource() {
    return RESOURCE;
  }

  @Override
  public String resourceFileExtension() {
    return PROCESS_EXTENSION;
  }

  @Override
  public String guidelines() {
    return GUIDELINES;
  }

  @Override
  public String schema() {
    return SCHEMA;
  }
}

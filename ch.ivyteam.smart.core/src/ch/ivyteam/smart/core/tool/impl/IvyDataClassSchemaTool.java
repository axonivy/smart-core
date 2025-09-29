package ch.ivyteam.smart.core.tool.impl;

import static ch.ivyteam.ivy.IvyConstants.DATA_CLASS_EXTENSION;
import static ch.ivyteam.ivy.IvyConstants.DIRECTORY_DATACLASSES;
import static ch.ivyteam.smart.core.schema.ResponseSchema.DATA_CLASS;

import ch.ivyteam.smart.core.tool.IvySchemaTool;

public class IvyDataClassSchemaTool implements IvySchemaTool {

  @Override
  public String name() {
    return "ivy-data-class-schema";
  }

  @Override
  public String resourceFileExtension() {
    return DATA_CLASS_EXTENSION;
  }

  @Override
  public String guidelines() {
    return """
      - Data class files must be located in a subdirectory of '<project-root>/""" + DIRECTORY_DATACLASSES + """
      /' matching its namespace. Note: This does not apply to form data classes.""";
  }

  @Override
  public String schema() {
    // TODO: load-schema form ivy-core. Keep it as static resource.
    return DATA_CLASS.schema().toString();
  }
}

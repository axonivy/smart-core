package ch.ivyteam.smart.core.tool.impl;

import static ch.ivyteam.smart.core.schema.ResourceSchema.DATA_CLASS;

import ch.ivyteam.ivy.ProjectDir;
import ch.ivyteam.ivy.scripting.dataclass.resource.DataClassFileExtensions;
import ch.ivyteam.smart.core.tool.SchemaTool;

public class DataClassSchemaTool implements SchemaTool {

  public static final String NAME = "data-class-schema";
  private static final String RESOURCE = "data class";
  private static final String SCHEMA = DATA_CLASS.schema().toString();
  private static final String GUIDELINES = """
    - Data class files must be located in a subdirectory of '<project-root>/""" + ProjectDir.DATACLASSES + """
    /' matching its namespace. Note: This does not apply to form data classes.
    - To use a data class as a type, refer to it by its fully qualified name.""";

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
    return DataClassFileExtensions.DATA_CLASS_EXTENSION;
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

package ch.ivyteam.smart.core.tool.impl;

import static ch.ivyteam.ivy.IvyConstants.DATA_CLASS_EXTENSION;
import static ch.ivyteam.ivy.IvyConstants.DIRECTORY_SRC_HD;
import static ch.ivyteam.ivy.IvyConstants.PROCESS_EXTENSION;
import static ch.ivyteam.ivy.dialog.configuration.FormNameExtensions.EXTENSION;
import static ch.ivyteam.ivy.dialog.configuration.FormNameExtensions.EXTENSION_WITHOUT_DOT;
import static ch.ivyteam.smart.core.schema.ResourceSchema.FORM;

import ch.ivyteam.smart.core.tool.SchemaTool;

public class FormSchemaTool implements SchemaTool {

  public static final String NAME = "form-schema";
  private static final String RESOURCE = "form";
  private static final String SCHEMA = FORM.schema().toString();
  private static final String GUIDELINES = """
    - A form consists of a directory named after the form. Inside, there are three files:
      - <form-name>""" + EXTENSION + """
    (the form definition)
      - <form-name>Data.""" + DATA_CLASS_EXTENSION + """
    (the form data class)
      - <form-name>Process.""" + PROCESS_EXTENSION + """
    (the form process)
    - Use the corresponding tools to acquire the JSON schema and guidelines for the data class and the process before proceeding.
    - The form process must be of kind 'HTML_DIALOG' and contain a 'HtmlDialogStart' connected to a 'HtmlDialogEnd' and a 'HtmlDialogEventStart' connected to a 'HtmlDialogExit'.
    - The form must be located in a subdirectory of '<project-root>/""" + DIRECTORY_SRC_HD + """
    /' but not directly in it. At least one other directory between '""" + DIRECTORY_SRC_HD + """
    ' and the form directory is required. The directory in which the form is located must have the same name as the form.""";

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
    return EXTENSION_WITHOUT_DOT;
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

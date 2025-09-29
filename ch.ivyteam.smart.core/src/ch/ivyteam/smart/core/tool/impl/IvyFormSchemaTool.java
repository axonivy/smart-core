package ch.ivyteam.smart.core.tool.impl;

import static ch.ivyteam.ivy.IvyConstants.DATA_CLASS_EXTENSION;
import static ch.ivyteam.ivy.IvyConstants.DIRECTORY_SRC_HD;
import static ch.ivyteam.ivy.IvyConstants.PROCESS_EXTENSION;
import static ch.ivyteam.ivy.dialog.configuration.FormNameExtensions.EXTENSION;
import static ch.ivyteam.ivy.dialog.configuration.FormNameExtensions.EXTENSION_WITHOUT_DOT;
import static ch.ivyteam.smart.core.schema.ResponseSchema.FORM;

import ch.ivyteam.smart.core.tool.IvySchemaTool;

public class IvyFormSchemaTool implements IvySchemaTool {

  @Override
  public String name() {
    return "ivy-form-schema";
  }

  @Override
  public String resourceFileExtension() {
    return EXTENSION_WITHOUT_DOT;
  }

  @Override
  public String guidelines() {
    return """
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
      ' and the form directory is required.
      - The id of the form must be UUID.""";
    // TODO: move UUID requirement to schema; probably needs a tool to generate it
  }

  @Override
  public String schema() {
    // TODO: load-schema form ivy-core. Keep it as static resource.
    return FORM.schema().toString();
  }
}

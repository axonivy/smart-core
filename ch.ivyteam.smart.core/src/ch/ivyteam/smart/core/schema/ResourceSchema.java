package ch.ivyteam.smart.core.schema;

import static ch.ivyteam.smart.core.schema.SchemaLoader.readSchema;

import java.nio.file.Path;

import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.ivyteam.ivy.dialog.form.io.FormVersion;
import ch.ivyteam.ivy.process.io.ProcessVersion;
import ch.ivyteam.ivy.scripting.dataclass.model.DataClassVersion;

public interface ResourceSchema {

  IvySchema PROCESS = load(ProcessVersion.class, OpenAiSchemaMapper.SchemaPath.PROCESS);
  IvySchema DATA_CLASS = load(DataClassVersion.class, OpenAiSchemaMapper.SchemaPath.DATA_CLASS);
  IvySchema FORM = load(FormVersion.class, OpenAiSchemaMapper.SchemaPath.FORM);

  private static IvySchema load(Class<?> loader, Path path) {
    var schema = readSchema(loader, path);
    new OpenAiSchemaMapper(path).optimize(schema);
    return new IvySchema(path, schema);
  }

  public static record IvySchema(Path source, ObjectNode schema) {}
}

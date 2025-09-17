package ch.ivyteam.smart.core.schema;

import static ch.ivyteam.smart.core.schema.SchemaLoader.readSchema;

import java.net.URI;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface ReponseSchema {

  IvySchema PROCESS = load(OpenAiSchemaMapper.SchemaUri.PROCESS);
  
  private static IvySchema load(URI ref) {
    var resource = Path.of(ref.getPath()).getFileName().toString();
    var schema = readSchema(resource);
    new OpenAiSchemaMapper(ref).optimize(schema);
    return new IvySchema(ref, schema);
  }

  public static record IvySchema(URI source, ObjectNode schema) {}

}

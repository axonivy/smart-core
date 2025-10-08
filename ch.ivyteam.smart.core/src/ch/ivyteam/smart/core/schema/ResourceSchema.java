package ch.ivyteam.smart.core.schema;

import java.io.IOException;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.ivyteam.ivy.dialog.form.schema.FormSchemaResource;
import ch.ivyteam.ivy.process.schema.ProcessSchemaResource;
import ch.ivyteam.ivy.scripting.dataclass.schema.DataClassSchemaResource;

public interface ResourceSchema {

  IvySchema PROCESS = load(ProcessSchemaResource.resource(), ProcessSchemaResource.schemaRef());
  IvySchema DATA_CLASS = load(DataClassSchemaResource.resource(), DataClassSchemaResource.schemaRef());
  IvySchema FORM = load(FormSchemaResource.resource(), FormSchemaResource.schemaRef());

  ObjectMapper MAPPER = new ObjectMapper();

  private static IvySchema load(URL resource, String schemaRef) {
    var schema = readSchema(resource);
    new OpenAiSchemaMapper(schemaRef).optimize(schema);
    return new IvySchema(resource.getPath(), schema);
  }

  static ObjectNode readSchema(URL resource) {
    try (var in = resource.openStream()) {
      return (ObjectNode) MAPPER.readTree(in);
    } catch (IOException ex) {
      throw new RuntimeException("Failed to read schema: " + resource.getPath(), ex);
    }
  }

  public static record IvySchema(String source, ObjectNode schema) {}
}

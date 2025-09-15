package ch.ivyteam.smart.core.schema;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SchemaLoader {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static ObjectNode readSchema(String resource) {
    try (var in = SchemaLoader.class.getResourceAsStream(resource)) {
      return (ObjectNode) MAPPER.readTree(in);
    } catch (IOException ex) {
      throw new RuntimeException("Failed to load schema " + resource, ex);
    }
  }

}

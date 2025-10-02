package ch.ivyteam.smart.core.schema;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SchemaLoader {

  public static final ObjectMapper MAPPER = new ObjectMapper();

  public static ObjectNode readSchema(Class<?> loader, Path resource) {
    return read(loader.getResourceAsStream(resource.toString()), resource);
  }

  public static ObjectNode read(InputStream is, Path path) {
    try (is) {
      return (ObjectNode) MAPPER.readTree(is);
    } catch (IOException ex) {
      throw new RuntimeException("Failed to load schema: " + path.toString(), ex);
    }
  }

}

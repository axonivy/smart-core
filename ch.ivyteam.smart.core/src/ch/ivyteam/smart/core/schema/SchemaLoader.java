package ch.ivyteam.smart.core.schema;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SchemaLoader {

  public static final ObjectMapper MAPPER = new ObjectMapper();

  public static ObjectNode readSchema(String resource) {
    return readSchema(SchemaLoader.class, resource);
  }

  public static ObjectNode readSchema(Class<?> loader, String resource) {
    return read(loader.getResourceAsStream(resource), resource);
  }

  public static ObjectNode readSchema(URI ref) {
    try (InputStream is = ref.toURL().openStream()) {
      return read(is, ref.toString());
    } catch (Exception ex) {
      throw new RuntimeException("Failed to fetch schema", ex);
    }
  }

  public static ObjectNode read(InputStream is, String what) {
    try (is) {
      return (ObjectNode) MAPPER.readTree(is);
    } catch (IOException ex) {
      throw new RuntimeException("Failed to load schema " + what, ex);
    }
  }

}

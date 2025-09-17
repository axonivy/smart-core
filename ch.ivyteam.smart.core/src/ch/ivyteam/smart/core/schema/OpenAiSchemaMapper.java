package ch.ivyteam.smart.core.schema;

import java.net.URI;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.ivyteam.ivy.json.history.JsonVersion;
import ch.ivyteam.ivy.process.io.ProcessVersion;

public class OpenAiSchemaMapper {

  public interface SchemaUri {
    URI PROCESS = versioned("process", ProcessVersion.LATEST);
    URI DATA_CLASS = versioned("data-class", ProcessVersion.LATEST);

    private static URI versioned(String resource, JsonVersion version) {
      return URI.create("https://json-schema.axonivy.com/" + resource + "/" + version + "/" + resource + ".json");
    }
  }

  private final URI target;

  public static OpenAiSchemaMapper forProcess() {
    return new OpenAiSchemaMapper(SchemaUri.PROCESS);
  }

  public OpenAiSchemaMapper(URI schema) {
    this.target = schema;
  }

  public JsonNode optimize(ObjectNode schema) {
    JsonNode rootProps = schema.get("properties");
    if (rootProps.get("$schema") instanceof ObjectNode schemaRef) {
      staticSchemaRef(schemaRef);
    }

    if (schema.get("$defs") instanceof ObjectNode defs) {
      defs.properties().stream()
          .map(Entry::getValue)
          .forEach(OpenAiSchemaMapper::sanitizeDef);
    }

    return schema;
  }

  private void staticSchemaRef(ObjectNode schemaRef) {
    schemaRef.remove("pattern");
    schemaRef.put("const", target.toString());
  }

  private static void sanitizeDef(JsonNode json) {
    if (json instanceof ObjectNode obj) {
      sanitizeAbsentProperties(json, obj);
      if (json.get("properties") instanceof ObjectNode props) {
        sanitizeAmbigiousStringTypes(props);
      }
    }
  }

  private static void sanitizeAbsentProperties(JsonNode json, ObjectNode obj) {
    if ("object".equals(json.get("type").asText())) {
      JsonNode props = obj.get("properties");
      if (props == null) {
        obj.putObject("properties");
      }
    }
  }

  private static void sanitizeAmbigiousStringTypes(ObjectNode props) {
    props.propertyStream().forEach(et -> {
      if (et.getValue() instanceof ObjectNode po) {
        if (po.get("anyOf") instanceof ArrayNode any && "string".equals(any.get(0).asText())) {
          po.remove("anyOf");
          var add = po.remove("additionalProperties");
          po.set("type", any.get(0));
          if (add != null) {
            po.set("additionalProperties", add);
          }
        }
      }
    });
  }

}

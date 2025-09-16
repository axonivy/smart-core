package ch.ivyteam.smart.core.schema;

import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.ivyteam.ivy.process.io.ProcessVersion;

public class OpenAiSchemaMapper {

  public static JsonNode from(ObjectNode schema) {
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

  private static void staticSchemaRef(ObjectNode schemaRef) {
    if (schemaRef.get("pattern") != null) {
      var removed = schemaRef.remove("pattern");
      if (removed.asText().contains("process")) {
        schemaRef.put("const", "https://json-schema.axonivy.com/process/" + ProcessVersion.LATEST + "/process.json");
      }
    }
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
      System.out.println("checking " + et);
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

package ch.ivyteam.smart.core.schema;

import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class OpenAiSchemaMapper {

  private final String target;

  public OpenAiSchemaMapper(String schemaRef) {
    this.target = schemaRef;
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
    schemaRef.put("const", target);
  }

  private static void sanitizeDef(JsonNode json) {
    if (json instanceof ObjectNode obj) {
      sanitizeAbsentProperties(json, obj);
      if (json.get("properties") instanceof ObjectNode props) {
        sanitizeAmbigiousStringTypes(props);
        if (obj.get("required") instanceof ArrayNode required) {
          modelOptionalAsNullUnion(props, required);
        }
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
        if (po.get("anyOf") instanceof ArrayNode any && !any.isEmpty()) {
          if (any.get(0) instanceof ObjectNode first) {
            if (first.get("type") instanceof TextNode type && "string".equals(type.asText())) {
              po.remove("anyOf");
              var add = po.remove("additionalProperties");
              po.put("type", type.asText());
              if (add != null) {
                po.set("additionalProperties", add);
              }
            }
          }
        }
      }
    });
  }

  /**
   * Comply with OpenAI strict required interpretation.
   * @see "https://platform.openai.com/docs/guides/structured-outputs/supported-schemas#all-fields-must-be-required"
   */
  private static void modelOptionalAsNullUnion(ObjectNode props, ArrayNode required) {
    var require = required.valueStream().map(JsonNode::asText).toList();
    props.propertyStream()
        .filter(e -> !require.contains(e.getKey()))
        .map(Entry::getValue)
        .filter(ObjectNode.class::isInstance)
        .map(ObjectNode.class::cast)
        .forEach(prop -> {
          if (prop.get("type") instanceof TextNode type) {
            if ("array".equals(type.asText())) {
              return;
            }
            var union = prop.putArray("type");
            union.add(type).add("null");
          }
        });
  }

}

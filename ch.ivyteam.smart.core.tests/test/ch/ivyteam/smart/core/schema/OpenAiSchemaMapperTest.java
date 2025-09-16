package ch.ivyteam.smart.core.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.ivyteam.ivy.process.io.ProcessVersion;

public class OpenAiSchemaMapperTest {

  private ObjectNode schema;

  @BeforeEach
  void setUp() {
    this.schema = SchemaLoader.readSchema(OpenAiSchemaMapperTest.class, "process.json");
  }

  @Test
  void constantSchema() {
    JsonNode rootProps = schema.get("properties");
    JsonNode schemaRef = rootProps.get("$schema");

    assertThat(fieldNames(schemaRef))
        .contains("pattern");

    OpenAiSchemaMapper.from(schema);

    assertThat(fieldNames(schemaRef))
        .contains("const")
        .doesNotContain("pattern");

    assertThat(schemaRef.get("const").asText())
        .isEqualTo("https://json-schema.axonivy.com/process/" + ProcessVersion.LATEST + "/process.json");
  }

  @Test
  void emptyProps() {
    JsonNode definitions = schema.get("$defs");
    JsonNode map = definitions.get("Map(String,String)");

    assertThat(fieldNames(map))
        .as("valid json; no explicity 'properties'")
        .containsOnly("type", "additionalProperties");

    OpenAiSchemaMapper.from(schema);

    assertThat(fieldNames(map))
        .as("inject empty 'properties' attribute to make OpenAI comply")
        .containsOnly("type", "properties", "additionalProperties");
    assertThat(map.get("properties"))
        .isNotNull();
  }

  @Test
  void ambigiousMultiLineStrings() {
    var definitions = schema.get("$defs");
    var node = definitions.get("ElementNode");
    var name = node.get("properties").get("name");
    assertThat(fieldNames(name))
        .as("multi-line or a simple string")
        .containsOnly("anyOf", "additionalProperties");

    OpenAiSchemaMapper.from(schema);

    assertThat(fieldNames(name))
        .containsOnly("type", "additionalProperties");
  }

  private static List<String> fieldNames(JsonNode map) {
    var names = new ArrayList<String>();
    map.fieldNames().forEachRemaining(names::add);
    return names;
  }

}

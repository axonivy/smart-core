package ch.ivyteam.smart.core.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.ivyteam.ivy.process.schema.ProcessSchemaResource;
import ch.ivyteam.ivy.scripting.dataclass.schema.DataClassSchemaResource;

public class OpenAiSchemaMapperTest {

  private URL schemaResource;
  private ObjectNode schema;

  @BeforeEach
  void setUp() {
    this.schemaResource = ProcessSchemaResource.resource();
    this.schema = ResourceSchema.readSchema(this.schemaResource);
  }

  @Test
  void constantSchema_dataClass() {
    var dataSchemaResource = DataClassSchemaResource.resource();
    var dataSchema = ResourceSchema.readSchema(dataSchemaResource);

    JsonNode rootProps = dataSchema.get("properties");
    JsonNode schemaRef = rootProps.get("$schema");

    assertThat(fieldNames(schemaRef))
        .containsOnly("type");

    new OpenAiSchemaMapper(dataSchemaResource.getPath()).optimize(dataSchema);

    assertThat(fieldNames(schemaRef))
        .as("adds const, also if no pattern is defined")
        .containsOnly("type", "const");

    assertThat(schemaRef.get("const").asText())
        .isEqualTo(dataSchemaResource.getPath());
  }

  @Test
  void constantSchema() {
    JsonNode rootProps = schema.get("properties");
    JsonNode schemaRef = rootProps.get("$schema");

    assertThat(fieldNames(schemaRef))
        .containsOnly("type", "pattern");

    optimize();

    assertThat(fieldNames(schemaRef))
        .contains("const")
        .doesNotContain("pattern");

    assertThat(schemaRef.get("const").asText())
        .isEqualTo(schemaResource.getPath());
  }

  @Test
  void emptyProps() {
    JsonNode definitions = schema.get("$defs");
    JsonNode map = definitions.get("Map(String,String)");

    assertThat(fieldNames(map))
        .as("valid json; no explicity 'properties'")
        .containsOnly("type", "additionalProperties");

    optimize();

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

    optimize();

    assertThat(fieldNames(name))
        .containsOnly("type", "additionalProperties");
  }

  @Test
  void requiredFields() {
    var definitions = schema.get("$defs");
    var node = definitions.get("ElementNode");
    var required = (ArrayNode) node.get("required");
    var props = (ObjectNode) node.get("properties");
    var bpmn = props.get("bpmnId");
    assertThat(bpmn.get("type").asText())
        .isEqualTo("string");
    assertThat(required.valueStream()).extracting(JsonNode::asText)
        .containsOnly("id", "type", "visual");

    optimize();

    assertThat(bpmn.get("type"))
        .isInstanceOf(ArrayNode.class);
    assertThat(bpmn.get("type").valueStream()).extracting(JsonNode::asText)
        .as("interpolate union type; to express optional property")
        .containsOnly("string", "null");
    // // only for strict mode!
    // assertThat(required.valueStream()).extracting(JsonNode::asText)
    // .as("must require all properties in OpenAI structured output")
    // .containsAll(props.propertyStream().map(Entry::getKey).toList());
  }

  static List<String> fieldNames(JsonNode map) {
    var names = new ArrayList<String>();
    map.fieldNames().forEachRemaining(names::add);
    return names;
  }

  private void optimize() {
    new OpenAiSchemaMapper(schemaResource.getPath()).optimize(schema);
  }

}

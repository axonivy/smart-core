package ch.ivyteam.smart.core.schema;

import static org.assertj.core.api.Assertions.assertThat;
import static test.ch.ivyteam.ivy.json.JsonNodeAssert.assertNode;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import ch.ivyteam.ivy.dialog.form.io.FormVersion;
import ch.ivyteam.ivy.process.io.ProcessVersion;
import ch.ivyteam.ivy.scripting.dataclass.model.DataClassVersion;

public class ResourceSchemaTest {

  @Test
  void process() {
    var schema = ResourceSchema.PROCESS;
    var path = "/generated-schema/process/" + ProcessVersion.LATEST.value() + "/process.json";
    assertThat(schema.source()).isEqualTo(Path.of(path));
    assertNode(schema.schema().get("properties").get("$schema").get("const")).isEqualTo(path);
  }

  @Test
  void dataClass() {
    var schema = ResourceSchema.DATA_CLASS;
    var path = "/generated-schema/data-class/" + DataClassVersion.LATEST.value() + "/data-class.json";
    assertThat(schema.source()).isEqualTo(Path.of(path));
    assertNode(schema.schema().get("properties").get("$schema").get("const")).isEqualTo(path);
  }

  @Test
  void form() {
    var schema = ResourceSchema.FORM;
    var path = "/generated-schema/form/" + FormVersion.LATEST.value() + "/form.json";
    assertThat(schema.source()).isEqualTo(Path.of(path));
    assertNode(schema.schema().get("properties").get("$schema").get("const")).isEqualTo(path);
  }
}

package ch.ivyteam.smart.core.tool;

import static ch.ivyteam.smart.core.tool.SchemaTool.EMPTY_JSON_SCHEMA;
import static ch.ivyteam.smart.core.tool.SchemaTool.GUIDELINES_HEADER;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.spec.McpSchema.TextContent;

public class SchemaToolTest {

  SchemaTool tool = new IvySchemaToolMock();

  @Test
  void toolSpecification() {
    var specification = tool.specification();
    var toolSpecification = specification.tool();

    assertThat(toolSpecification.name()).isEqualTo("mock-name");
    assertThat(toolSpecification.description()).isEqualTo(
        "Returns the JSON schema and guidelines for handling Axon Ivy mock-resource definitions "
            + "(file extension 'mock-resource-file-extension'). "
            + "Whenever instructed to create, edit, or otherwise work with or handle a mock-resource, "
            + "use the schema as the authoritative source of truth for structure, required fields, and allowed values "
            + "and strictly follow the guidelines.");
    assertThat(toolSpecification.inputSchema()).isEqualTo(EMPTY_JSON_SCHEMA);
  }

  @Test
  void callHandler() {
    var result = tool.specification().callHandler().apply(null, null).block();

    assertThat(result.content()).hasSize(2);
    assertThat(((TextContent) result.content().get(0)).text()).isEqualTo(IvySchemaToolMock.SCHEMA);
    assertThat(((TextContent) result.content().get(1)).text()).isEqualTo(GUIDELINES_HEADER + IvySchemaToolMock.GUIDELINES);
  }

  private static class IvySchemaToolMock implements SchemaTool {

    private static final String NAME = "mock-name";
    private static final String RESOURCE = "mock-resource";
    private static final String RESOURCE_FILE_EXTENSION = "mock-resource-file-extension";
    private static final String GUIDELINES = "mock-guidelines";
    private static final String SCHEMA = "mock-schema";

    @Override
    public String name() {
      return NAME;
    }

    @Override
    public String resource() {
      return RESOURCE;
    }

    @Override
    public String resourceFileExtension() {
      return RESOURCE_FILE_EXTENSION;
    }

    @Override
    public String guidelines() {
      return GUIDELINES;
    }

    @Override
    public String schema() {
      return SCHEMA;
    }
  }
}

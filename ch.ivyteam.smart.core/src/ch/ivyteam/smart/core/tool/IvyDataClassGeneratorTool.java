package ch.ivyteam.smart.core.tool;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import ch.ivyteam.ivy.IvyConstants;
import ch.ivyteam.smart.core.schema.ResponseSchema;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import reactor.core.publisher.Mono;

public class IvyDataClassGeneratorTool {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static AsyncToolSpecification specification() {
    var generator = Tool.builder()
        .name("ivy-dataclass-creator")
        .description("Create a new ivy Data Class")
        .inputSchema(ResponseSchema.DATA_CLASS.schema().toString())
        .build();

    return AsyncToolSpecification.builder()
        .tool(generator)
        .callHandler((exchange, request) -> {
          var data = JsonNodeFactory.instance.objectNode();

          // request.arguments().entrySet().stream().forEach(et -> {
          // data.put(et.getKey(), MAPPER.for(et.getValue()));
          // });

          var toolRes = new TextContent(request.arguments().toString());
          return Mono.just(new CallToolResult(List.of(
              new TextContent("""
                Create a file containing the data class.
                The file must be located in the directory <project-root>/""" + IvyConstants.DIRECTORY_DATACLASSES + """
                and have the file extension """ + "d.json" + """
                .
                Data Class:"""), toolRes), false));
        })
        .build();
  }

}

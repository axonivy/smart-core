package ch.ivyteam.smart.core.tool.impl.market;

import static ch.ivyteam.smart.core.SmartCoreMcpServer.MAPPER;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import reactor.core.publisher.Mono;

public class MarketInstallTool {

  public static final String NAME = "market-installer";

  public interface Param {
    String ID = "id";
  }

  public AsyncToolSpecification specification() {
    var inputSchema = JsonNodeFactory.instance.objectNode();
    inputSchema.put("type", "object");
    var props = inputSchema.putObject("properties");
    props.putObject(Param.ID)
        .put("type", "string")
        .put("description", "product.id of an existing axonivy market product");
    inputSchema.putArray("required").add(Param.ID);
    inputSchema.put("additionalProperties", false);

    System.out.println(inputSchema.toPrettyString());

    var tool = Tool.builder()
        .name(NAME)
        .description("Instructs how to install third-party components from the axonivy market eco-system, "
            + "by providing required pom.xml dependencies.")
        .inputSchema(MAPPER, inputSchema.toString())
        .build();
    return AsyncToolSpecification.builder()
        .tool(tool)
        .callHandler(this::callHandler)
        .build();
  }

  private Mono<CallToolResult> callHandler(@SuppressWarnings("unused") McpAsyncServerExchange async, CallToolRequest request) {
    var id = (String) request.arguments().get(Param.ID);
    return Mono.just(install(id));
  }

  private CallToolResult install(String id) {
    try {
      var install = MarketSearchEngine.installer(id);
      return new CallToolResult(install, false);
    } catch (Exception ex) {
      return new CallToolResult(ex.getMessage(), true);
    }
  }
}

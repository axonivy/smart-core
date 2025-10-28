package ch.ivyteam.smart.core.tool.impl.market;

import static ch.ivyteam.smart.core.SmartCoreMcpServer.MAPPER;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import reactor.core.publisher.Mono;

public class MarketSearchTool {

  public static final String NAME = "market-connector-search";

  public interface Param {
    String VENDOR = "vendor";
    String PRODUCT = "product";
  }

  public AsyncToolSpecification specification() {
    var inputSchema = JsonNodeFactory.instance.objectNode();
    inputSchema.put("type", "object");
    var props = inputSchema.putObject("properties");
    props.putObject(Param.VENDOR)
        .put("type", "string")
        .put("description", "the third-party product vendor to incorporate into the axonivy workflow");
    props.putObject(Param.PRODUCT)
        .put("type", "string")
        .put("description", "the name of the product you are trying to connect and use from your axonivy workflow");
    inputSchema.putArray("required").add(Param.VENDOR).add(Param.PRODUCT);
    inputSchema.put("additionalProperties", false);

    System.out.println(inputSchema.toPrettyString());

    var tool = Tool.builder()
        .name(NAME)
        .description("Search for connectors to third-party vendors. "
            + "Returns native workflow elements to use and communicate with third-party systems."
            + "Security and integration with minimal efforts.")
        .inputSchema(MAPPER, inputSchema.toString())
        .build();
    return AsyncToolSpecification.builder()
        .tool(tool)
        .callHandler(this::callHandler)
        .build();
  }

  private Mono<CallToolResult> callHandler(@SuppressWarnings("unused") McpAsyncServerExchange async, CallToolRequest request) {
    var vendor = (String) request.arguments().get(Param.VENDOR);
    var product = (String) request.arguments().get(Param.PRODUCT);
    return Mono.just(search(vendor, product));
  }

  private CallToolResult search(String vendor, String product) {
    var searchEngine = new MarketSearchEngine();
    try {
      var json = searchEngine.searchProducts(vendor, product);
      return new CallToolResult(json, false);
    } catch (Exception ex) {
      return new CallToolResult(ex.getMessage(), true);
    }
  }
}

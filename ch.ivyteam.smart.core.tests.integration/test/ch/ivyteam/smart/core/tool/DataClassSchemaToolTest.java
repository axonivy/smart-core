package ch.ivyteam.smart.core.tool;

import static ch.ivyteam.smart.core.SmartCoreMcpServer.SERVLET_PATH;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_5_NANO;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.ivyteam.ivy.request.EngineUriResolver;
import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.tool.impl.DataClassSchemaTool;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.openai.OpenAiChatModel;

@ManagedServer
public class DataClassSchemaToolTest {

  @Test
  void selection() {
    var model = OpenAiChatModel.builder()
        .apiKey(System.getenv("OPENAI_API_KEY"))
        .modelName(GPT_5_NANO)
        .build();

    var baseUri = EngineUriResolver.instance().local() + SERVLET_PATH;
    var transport = new StreamableHttpMcpTransport.Builder()
        .url(baseUri + "/mcp")
        .build();

    try (var mcpClient = new DefaultMcpClient.Builder()
        .transport(transport)
        .build()) {

      var toolSpecification = mcpClient.listTools().stream()
          .filter(specification -> DataClassSchemaTool.NAME.equals(specification.name()))
          .findAny();
      var toolSpecifications = toolSpecification.map(List::of).orElseThrow();
      var message = UserMessage.builder()
          .contents(List.of(TextContent.from("create a data class for a person")))
          .build();
      var request = ChatRequest.builder()
          .toolSpecifications(toolSpecifications)
          .messages(message)
          .build();

      var response = model.chat(request);
      assertThat(response.aiMessage().hasToolExecutionRequests()).isTrue();

      var toolExecutionRequests = response.aiMessage().toolExecutionRequests();
      assertThat(toolExecutionRequests).hasSize(1);
      assertThat(toolExecutionRequests.get(0).name()).isEqualTo(DataClassSchemaTool.NAME);
    }
  }
}

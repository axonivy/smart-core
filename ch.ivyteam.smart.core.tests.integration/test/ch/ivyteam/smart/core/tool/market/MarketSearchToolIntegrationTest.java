package ch.ivyteam.smart.core.tool.market;

import static ch.ivyteam.smart.core.ChatModelUtils.GPT_5_NANO;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.tool.ToolUtils;
import ch.ivyteam.smart.core.tool.market.impl.MarketSearchTool;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;

@ManagedServer
public class MarketSearchToolIntegrationTest {

  @Test
  void toolExecutionRequest() {
    var toolSpecifications = ToolUtils.toolSpecifications(MarketSearchTool.NAME);
    var message = UserMessage.builder()
        .contents(List.of(TextContent.from("is there a jira connector")))
        .build();
    var request = ChatRequest.builder()
        .toolSpecifications(toolSpecifications)
        .messages(message)
        .build();

    var response = GPT_5_NANO.chat(request);
    assertThat(response.aiMessage().hasToolExecutionRequests()).isTrue();

    var toolExecutionRequests = response.aiMessage().toolExecutionRequests();
    assertThat(toolExecutionRequests).hasSize(1);
    assertThat(toolExecutionRequests.get(0).name()).isEqualTo(MarketSearchTool.NAME);
    assertThat(toolExecutionRequests.get(0).arguments()).isEqualTo("{\"vendor\":\"Atlassian\",\"product\":\"Jira\"}");
  }
}

package ch.ivyteam.smart.core.tool;

import static ch.ivyteam.smart.core.ChatModelUtils.GPT_5_NANO;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.tool.impl.ProcessSchemaTool;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;

@ManagedServer
public class ProcessSchemaToolIntegrationTest {

  @Test
  void toolExecutionRequest() {
    var toolSpecifications = ToolUtils.toolSpecifications(ProcessSchemaTool.NAME);
    var message = UserMessage.builder()
        .contents(List.of(TextContent.from("create a process for invoice approval")))
        .build();
    var request = ChatRequest.builder()
        .toolSpecifications(toolSpecifications)
        .messages(message)
        .build();

    var response = GPT_5_NANO.chat(request);
    assertThat(response.aiMessage().hasToolExecutionRequests()).isTrue();

    var toolExecutionRequests = response.aiMessage().toolExecutionRequests();
    assertThat(toolExecutionRequests).hasSize(1);
    assertThat(toolExecutionRequests.get(0).name()).isEqualTo(ProcessSchemaTool.NAME);
  }
}

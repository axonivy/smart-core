package ch.ivyteam.smart.core.schema;

import static ch.ivyteam.smart.core.schema.OpenAiSchemaMapperTest.fieldNames;
import static ch.ivyteam.smart.core.schema.ProcessSchemaGenTest.load;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.databind.JsonNode;

import ch.ivyteam.ivy.request.EngineUriResolver;
import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.schema.RestMockProvider.OpenAiMock;
import ch.ivyteam.test.log.LoggerAccess;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.http.client.log.LoggingHttpClient;
import dev.langchain4j.internal.Json;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequest.Builder;
import dev.langchain4j.model.openai.OpenAiChatModel;

@ManagedServer
public class DataClassSchemaGenTest {

  @RegisterExtension
  LoggerAccess log = new LoggerAccess(LoggingHttpClient.class.getName());

  @Test
  void openAiGenData() {
    var model = ivyMockedOpenAi();
    OpenAiMock.CHAT = n -> Response.ok().entity(load("mock/customer.json")).build();

    var json = generate(model,
        """
            create a dataClass describing a Customer
          """);
    assertThat(json).isNotNull();
    System.out.println(json.toPrettyString());

    Assertions.assertThat(fieldNames(json))
        .contains("$schema", "simpleName", "namespace", "fields");

    assertThat(json.get("simpleName").asText())
        .isEqualTo("Customer");

    var fields = json.get("fields");
    assertThat(fields)
        .extracting(f -> f.get("name").asText())
        .contains("id", "firstName", "lastName");
  }

  private OpenAiChatModel ivyMockedOpenAi() {
    return ProcessSchemaGenTest.openAiModelBuilder()
        .baseUrl(EngineUriResolver.instance().local() + "/api/mocked")
        .customHeaders(Map.of("X-Requested-By", "ivy"))
        .apiKey(null)
        .build();
  }

  private JsonNode generate(OpenAiChatModel model, String instruction) {
    var format = ProcessSchemaGenTest.nativeResponse(ResponseSchema.DATA_CLASS);
    var request = dataGeneration(instruction)
        .responseFormat(format)
        .build();

    var out = model.chat(request);
    String rawJson = out.aiMessage().text();
    return Json.fromJson(rawJson, JsonNode.class);
  }

  private Builder dataGeneration(String msg) {

    return ChatRequest.builder()
        .messages(new UserMessage(msg),
            new SystemMessage("omit as many defaults as possible"));
  }

}

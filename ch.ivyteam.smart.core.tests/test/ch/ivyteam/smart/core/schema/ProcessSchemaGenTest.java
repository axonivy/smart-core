package ch.ivyteam.smart.core.schema;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.databind.JsonNode;

import ch.ivyteam.ivy.request.EngineUriResolver;
import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.schema.ReponseSchema.IvySchema;
import ch.ivyteam.smart.core.schema.RestMockProvider.OpenAiMock;
import ch.ivyteam.test.log.LoggerAccess;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.http.client.log.LoggingHttpClient;
import dev.langchain4j.internal.Json;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequest.Builder;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonRawSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel.OpenAiChatModelBuilder;
import dev.langchain4j.model.openai.OpenAiChatModelName;

@ManagedServer
public class ProcessSchemaGenTest {

  @RegisterExtension
  LoggerAccess log = new LoggerAccess(LoggingHttpClient.class.getName());

  @Test
  void askOpenAi_native120api_gpt41mini_inlineFull_multiElement() {
    var model = ivyMockedOpenAi();
    OpenAiMock.CHAT = n -> Response.ok().entity(load("mock/slackMail.json")).build();

    var json = generateProcess(model,
        """
          start the process based on a signal, referencing a slack-message from a new customer
          add an email element, telling rolf@axonivy.com that we got a new lead!
          use an alternative gateway, if the predicted license cost is higher than 100K dollars, create a task for marcel with high priority otherwise simply end the process.
          """);
    assertThat(json).isNotNull();
    System.out.println(json.toPrettyString());

    var mail = json.get("elements").valueStream()
        .filter(e -> "EMail".equals(e.get("type").asText()))
        .findAny();
    assertThat(mail.get().get("config").get("headers").get("to").asText())
        .isEqualTo("rolf@axonivy.com");
  }

  private static String load(String resource) {
    try (InputStream is = ProcessSchemaGenTest.class.getResourceAsStream(resource)) {
      return new String(is.readAllBytes());
    } catch (IOException ex) {
      throw new RuntimeException("Failed to load test resource " + resource, ex);
    }
  }

  private OpenAiChatModel ivyMockedOpenAi() {
    return openAiModelBuilder()
        .baseUrl(EngineUriResolver.instance().local() + "/api/mocked")
        .customHeaders(Map.of("X-Requested-By", "ivy"))
        .build();
  }

  static OpenAiChatModelBuilder openAiModelBuilder() {
    return OpenAiChatModel.builder()
        .apiKey(System.getenv("llm.openai.apiKey"))
        .supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
        .modelName(OpenAiChatModelName.GPT_4_1_MINI)
        .strictJsonSchema(false)
        .logRequests(true)
        .logResponses(true);
  }

  private JsonNode generateProcess(OpenAiChatModel model, String instruction) {
    var format = nativeResponse(ReponseSchema.PROCESS);
    var writeMailProcess = processGeneration(instruction)
        .responseFormat(format)
        .build();

    var out = model.chat(writeMailProcess);
    String rawJson = out.aiMessage().text();
    return Json.fromJson(rawJson, JsonNode.class);
  }

  private Builder processGeneration(String msg) {
    var processHints = new SystemMessage("""
      omit as many defaults as possible, but at any rate produce the required values.
      Generate the 'data' as java qualified name.
      For element ID's create unique instances, starting from f1.
      Draw elements as graph.
      Do not set any visual attributes on element, except the position 'at'.
      Set the root process 'id' out of 16 random uppercase letters or numbers.
      Visualize roles as pool.
      """);

    return ChatRequest.builder()
        .messages(processHints, new UserMessage(msg));
  }

  static ResponseFormat nativeResponse(IvySchema ivySchema) {
    var nativeSchema = JsonRawSchema.from(ivySchema.schema().toString());
    var resource = Path.of(ivySchema.source().getPath()).getFileName().toString();
    var jsonSchema = new JsonSchema.Builder()
        .name(Strings.CS.removeEnd(resource, ".json"))
        .rootElement(nativeSchema)
        .build();
    return ResponseFormat.builder()
        .type(ResponseFormatType.JSON)
        .jsonSchema(jsonSchema)
        .build();
  }

}

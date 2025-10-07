package ch.ivyteam.smart.core.schema;

import static ch.ivyteam.smart.core.schema.OpenAiSchemaMapperTest.fieldNames;
import static ch.ivyteam.smart.core.schema.ProcessSchemaGenTest.load;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.ivyteam.ivy.request.EngineUriResolver;
import ch.ivyteam.ivy.server.test.ManagedServer;
import ch.ivyteam.smart.core.schema.OpenAiSchemaMapper.SchemaUri;
import ch.ivyteam.smart.core.schema.RestMockProvider.OpenAiMock;
import ch.ivyteam.test.log.LoggerAccess;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.http.client.log.LoggingHttpClient;
import dev.langchain4j.internal.Json;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequest.Builder;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonRawSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

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

  @Test
  void generateFromTool() {
    OpenAiMock.CHAT = n -> {
      if (n.get("messages") instanceof ArrayNode messages) {
        if (messages.size() == 1) {
          return Response.ok().entity(load("mock/tool/data/1response.json")).build();
        }
        if (messages.size() == 3) {
          return Response.ok().entity(load("mock/tool/data/2response.json")).build();
        }
      }
      return Response.serverError().build();
    };

    var dataSchema = ResponseSchema.DATA_CLASS.schema();
    var mandatory = List.of("simpleName", "namespace", "fields");
    var in = JsonObjectSchema.builder();
    dataSchema.get("properties").propertyStream()
        .filter(e -> mandatory.contains(e.getKey()))
        .forEach(e -> in.addProperty(e.getKey(), JsonRawSchema.from(e.getValue().toString())));
    var defs = dataSchema.get("$defs").propertyStream()
        .collect(Collectors.toMap(Entry::getKey, e -> (JsonSchemaElement) JsonRawSchema.from(e.getValue().toString())));
    in.definitions(defs);

    var dataTool = ToolSpecification.builder()
        .name("DataClass-creator")
        .description("I can assist you in creating DataClasses for Ivy")
        .parameters(in.build())
        .build();
    var agent = AiServices.builder(IvyAgent.class)
        .chatModel(ivyMockedOpenAi())
        .tools(Map.of(dataTool, (e, memoryId) -> createDataClass(e.arguments())))
        .build();
    var created = agent.chat("create a dataClass describing a Customer");
    assertThat(created).contains("id");
  }

  private static String createDataClass(String args) {
    try {
      var jObj = JsonNodeFactory.instance.objectNode();
      jObj.put("$schema", SchemaUri.DATA_CLASS.toString());
      var params = (ObjectNode) SchemaLoader.MAPPER.readTree(args);
      jObj.setAll(params);
      return jObj.toString();
    } catch (JsonProcessingException ex) {
      throw new RuntimeException("Failed to create dataClass with args " + args, ex);
    }
  }

  private interface IvyAgent {
    String chat(String what);
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

package ch.ivyteam.smart.core.tool.copilot;

import static ch.ivyteam.smart.core.ChatModelUtils.OPENAI_API_KEY;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_5_MINI;
import static dev.langchain4j.model.openai.internal.OpenAiUtils.DEFAULT_OPENAI_URL;
import static io.opentelemetry.semconv.incubating.GenAiIncubatingAttributes.GEN_AI_USAGE_INPUT_TOKENS;
import static io.opentelemetry.semconv.incubating.GenAiIncubatingAttributes.GEN_AI_USAGE_OUTPUT_TOKENS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import javax.ws.rs.client.WebTarget;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

import io.opentelemetry.api.common.AttributeKey;

import ch.ivyteam.ivy.jersey.client.JerseyClientBuilder;
import ch.ivyteam.ivy.server.test.ManagedServer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@ManagedServer
public class CopilotIntegrationTest {

  /*
   * To keep Aspire alive and observe the traces in its dashboard,
   * set this flag and start your own Aspire instance:
   *
   * ```
   * docker run -d \
   * -p 18888:18888 \
   * -p 4318:18890 \
   * -e DOTNET_DASHBOARD_UNSECURED_ALLOW_ANONYMOUS=true \
   * -e Dashboard__Api__Enabled=true \
   * --name aspire \
   * mcr.microsoft.com/dotnet/aspire-dashboard:latest
   * ```
   */
  static boolean manualAspire = System.getenv("MANUAL_ASPIRE") != null;

  static Network network;
  static AspireContainer aspireContainer;
  @SuppressWarnings("resource")
  static CopilotContainer copilotContainer = new CopilotContainer()
      .withEnv("COPILOT_PROVIDER_BASE_URL", DEFAULT_OPENAI_URL)
      .withEnv("COPILOT_PROVIDER_API_KEY", OPENAI_API_KEY)
      .withEnv("COPILOT_MODEL", GPT_5_MINI.toString())
      .withEnv("OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT", "true");

  static AspireAPI aspireApi;

  @SuppressWarnings("resource")
  @BeforeAll
  static void beforeAll() {
    String otelExporterOtlpEndpoint;
    String aspireApiBaseUrl;

    if (manualAspire) {
      copilotContainer.withExtraHost("host.docker.internal", "host-gateway");
      otelExporterOtlpEndpoint = "http://host.docker.internal:4318";
      aspireApiBaseUrl = "http://localhost:18888";
    } else {
      network = Network.newNetwork();

      aspireContainer = new AspireContainer()
          .withNetwork(network)
          .withNetworkAliases("aspire")
          .withExposedPorts(18888, 18890)
          .withEnv("Dashboard__Api__Enabled", "true");
      aspireContainer.start();

      copilotContainer.withNetwork(network);
      otelExporterOtlpEndpoint = "http://aspire:18890";
      aspireApiBaseUrl = "http://localhost:" + aspireContainer.getMappedPort(18888);
    }

    copilotContainer.withEnv("OTEL_EXPORTER_OTLP_ENDPOINT", otelExporterOtlpEndpoint);
    copilotContainer.start();

    aspireApi = AspireAPI.create(aspireApiBaseUrl);
  }

  @AfterAll
  static void afterAll() {
    copilotContainer.stop();
    if (aspireContainer != null) {
      aspireContainer.stop();
    }
    if (network != null) {
      network.close();
    }
  }

  @Test
  void createProject() throws Exception {
    var spans = promptCopilot("create an axon ivy project");
    var tokenUsage = tokenUsage(spans);
    assertThat(tokenUsage.input()).isLessThan(60000);
    assertThat(tokenUsage.output()).isLessThan(5000);
  }

  private JsonNode promptCopilot(String prompt) throws Exception {
    var resource = "copilot-cli-" + UUID.randomUUID();
    var containerWorkspace = "/" + resource;

    var result = copilotContainer.execInContainer("sh", "-c",
        "mkdir " + containerWorkspace + " && "
            + "cd " + containerWorkspace + " && "
            + "OTEL_SERVICE_NAME=" + resource + " "
            + "copilot -p '" + prompt + "' "
            + "--no-ask-user --allow-all-tools -s");
    if (result.getExitCode() != 0) {
      throw new RuntimeException("Copilot command failed: " + result.getStderr());
    }

    return aspireApi.spansOfResource(resource);
  }

  private static record TokenUsage(int input, int output) {}

  private TokenUsage tokenUsage(JsonNode spans) {
    var rootSpans = spans.valueStream()
        .filter(span -> !span.has("parentSpanId"))
        .toList();
    assertThat(rootSpans)
        .as("only one root span is present")
        .hasSize(1);
    var rootSpan = rootSpans.get(0);

    var inputTokens = findSpanAttributeValue(rootSpan, GEN_AI_USAGE_INPUT_TOKENS);
    var outputTokens = findSpanAttributeValue(rootSpan, GEN_AI_USAGE_OUTPUT_TOKENS);

    return new TokenUsage(Integer.parseInt(inputTokens), Integer.parseInt(outputTokens));
  }

  private String findSpanAttributeValue(JsonNode span, AttributeKey<?> key) {
    var attribute = span.get("attributes").valueStream()
        .filter(attr -> key.getKey().equals(attr.get("key").asString()))
        .findAny().orElse(null);
    if (attribute == null) {
      return null;
    }
    return attribute.get("value").get("stringValue").asString();
  }

  private static class CopilotContainer extends GenericContainer<CopilotContainer> {

    public CopilotContainer() {
      super(new ImageFromDockerfile().withDockerfileFromBuilder(builder -> builder
          .from("node:24-slim")
          .run("npm install -g @github/copilot")
          .build()));
      withCommand("sleep", "infinity");
    }
  }

  private static class AspireContainer extends GenericContainer<AspireContainer> {

    public AspireContainer() {
      super(DockerImageName.parse("mcr.microsoft.com/dotnet/aspire-dashboard:latest"));
      waitingFor(Wait.forLogMessage(".*Now listening on: http:\\/\\/\\[::\\]:18888.*", 1));
    }
  }

  private static class AspireAPI {

    private final WebTarget target;

    private final static ObjectMapper MAPPER = new ObjectMapper();

    private AspireAPI(WebTarget target) {
      this.target = target;
    }

    JsonNode spansOfResource(String resource) {
      var response = target
          .path("spans")
          .queryParam("resource", resource)
          .request().get();
      return MAPPER.readTree(response.readEntity(String.class))
          .get("data")
          .get("resourceSpans").get(0)
          .get("scopeSpans").get(0)
          .get("spans");
    }

    static AspireAPI create(String baseUrl) {
      return new AspireAPI(
          JerseyClientBuilder.create("aspire-api").toClient()
              .target(baseUrl)
              .path("api")
              .path("telemetry"));
    }
  }
}

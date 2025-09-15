package ch.ivyteam.smart.core.schema;

import java.util.Set;
import java.util.function.Function;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;

import ch.ivyteam.ivy.engine.rest.service.Context;
import ch.ivyteam.ivy.engine.rest.service.RestResourcesProvider;

public class RestMockProvider implements RestResourcesProvider {

  @Override
  public Set<Class<?>> securityContext(Context context) {
    return Set.of(OpenAiMock.class);
  }

  @PermitAll
  @Path("mocked")
  public static class OpenAiMock {

    public static Function<JsonNode, Response> CHAT = n -> Response.serverError().build();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("chat/completions")
    public Response doIt(JsonNode request) {
      return CHAT.apply(request);
    }

  }

}

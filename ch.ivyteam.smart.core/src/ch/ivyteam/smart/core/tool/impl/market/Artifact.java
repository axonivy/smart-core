package ch.ivyteam.smart.core.tool.impl.market;

import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

record Artifact(String groupId, String artifactId, String version, String type) {

  static Stream<Artifact> parseReleases(JsonNode jsonVersions) {
    if (jsonVersions instanceof ArrayNode version) {
      return version.valueStream()
          .findFirst()
          .stream()
          .flatMap(Artifact::parseItems);
    }
    return Stream.of();
  }

  private static Stream<Artifact> parseItems(JsonNode release) {
    String version = release.get("version").asText();
    if (release.get("artifactsByVersion") instanceof ArrayNode artifacts) {
      return artifacts.valueStream().map(a -> Artifact.parse(a, version));
    }
    return Stream.of();
  }

  private static Artifact parse(JsonNode art, String version) {
    var uri = art.get("downloadUrl").asText();
    String type = uri.substring(uri.lastIndexOf(".") + 1);
    return new Artifact(
        art.get("groupId").asText(),
        art.get("id").get("artifactId").asText(),
        version,
        type);
  }

  String xml() {
    var template = """
      <dependency>
        <groupId>%s</groupId>
        <artifactId>%s</artifactId>
        <version>%s</version>
        <type>%s</type>
      </dependency>
      """;
    return template.formatted(groupId, artifactId, version, type);
  }

}

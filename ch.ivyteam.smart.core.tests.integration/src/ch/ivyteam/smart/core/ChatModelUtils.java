package ch.ivyteam.smart.core;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;

public interface ChatModelUtils {

  String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");

  ChatModel GPT_5_NANO = openAi(OpenAiChatModelName.GPT_5_NANO);

  private static ChatModel openAi(OpenAiChatModelName modelName) {
    return OpenAiChatModel.builder()
        .apiKey(OPENAI_API_KEY)
        .modelName(modelName)
        .build();
  }
}

package backend.academy.linktracker.ai.filter;

import backend.academy.linktracker.ai.config.FilterConfig;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FilterUtils {
    private final DeepSeekChatOptions promptOptions = DeepSeekChatOptions.builder()
            .model(DeepSeekApi.ChatModel.DEEPSEEK_REASONER.getValue())
            .build();

    private final FilterConfig filterConfig;
    private final ChatModel chatModel;

    public boolean validateMessage(String message, String author) {
        return message.length() >= filterConfig.getMinLength()
                && !filterConfig.getExcludedAuthors().contains(author)
                && filterConfig.getStopWords().stream().noneMatch(message::contains);
    }

    public String summarizeMessage(String message) {
        if (message.length() < filterConfig.getSummarization().getThreshold()) {
            return message;
        }

        ChatResponse response =
                chatModel.call(new Prompt("Summarize message in 2-3 sentences:\n" + message, promptOptions));

        DeepSeekAssistantMessage deepSeekAssistantMessage = (DeepSeekAssistantMessage)
                Objects.requireNonNull(response.getResult()).getOutput();

        return deepSeekAssistantMessage.getText();
    }
}

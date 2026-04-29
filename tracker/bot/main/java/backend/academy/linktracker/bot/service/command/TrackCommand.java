package backend.academy.linktracker.bot.service.command;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.model.State;
import backend.academy.linktracker.bot.model.UserStateManager;
import backend.academy.linktracker.bot.model.UserStepData;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrackCommand implements Command {
    private final ScrapperClient scrapperClient;
    private final UserStateManager stateManager;
    private final int ARGUMENTS_COUNT = 2;

    @Override
    public String name() {
        return "/track";
    }

    @Override
    public String description() {
        return "Начать отслеживание ссылки";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        String text = update.message().text();
        String[] parts = text.split("\\s+");

        if (parts.length != ARGUMENTS_COUNT) {
            return new SendMessage(chatId, "Использование: /track <url>");
        }

        String urlString = parts[1];

        if (!urlString.startsWith("http")) {
            urlString = "https://" + urlString;
        }

        try {
            URI url = new URI(urlString);

            if (url.getHost() == null || !url.getHost().contains(".")) {
                throw new URISyntaxException(url.toString(), "Missing or invalid protocol");
            }

            if (isDuplicate(chatId, url)) {
                return new SendMessage(chatId, "Эта ссылка уже отслеживается!");
            }

            stateManager.saveState(chatId, new UserStepData(State.AWAITING_TAGS, url, name()));
            return new SendMessage(chatId, "Теперь пришли теги через запятую");
        } catch (URISyntaxException e) {
            return new SendMessage(chatId, "Ошибка: Некорректный формат ссылки.");
        }
    }

    @Override
    public SendMessage handleNextStep(Update update, UserStepData data) {
        long chatId = update.message().chat().id();
        String text = update.message().text();

        if (data.state() == State.AWAITING_TAGS) {
            try {
                List<String> tags = Arrays.stream(text.split(","))
                        .map(String::trim)
                        .filter(tag -> !tag.isEmpty())
                        .toList();

                scrapperClient.addLink(
                        chatId, new AddLinkRequest().link(data.url()).tags(tags));

                stateManager.clearState(chatId);

                return new SendMessage(chatId, "Ссылка успешно добавлена в список отслеживания!");
            } catch (Exception e) {
                log.atError().setCause(e).addKeyValue("chat_id", chatId).log("Error while adding link for chat");
                return new SendMessage(chatId, "Произошла ошибка на сервере при добавлении ссылки.");
            }
        }

        return null;
    }

    private boolean isDuplicate(long chatId, URI url) {
        ListLinksResponse response = scrapperClient.getAllLinks(chatId);

        if (response == null || response.getLinks() == null) {
            return false;
        }

        return response.getLinks().stream().anyMatch(link -> {
            assert link.getUrl() != null;
            return link.getUrl().toString().equalsIgnoreCase(url.toString());
        });
    }
}

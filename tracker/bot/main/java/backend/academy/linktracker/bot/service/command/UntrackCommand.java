package backend.academy.linktracker.bot.service.command;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UntrackCommand implements Command {
    private final ScrapperClient scrapperClient;

    @Override
    public String name() {
        return "/untrack";
    }

    @Override
    public String description() {
        return "Прекратить отслеживание ссылки";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        String[] parts = update.message().text().split(" ");

        if (parts.length < 2) {
            return new SendMessage(chatId, "Использование: /untrack <ссылка>");
        }

        String urlString = parts[1];

        if (!urlString.startsWith("http")) {
            urlString = "https://" + urlString;
        }

        try {
            URI url = new URI(urlString);

            scrapperClient.removeLink(chatId, new RemoveLinkRequest().link(url));
            return new SendMessage(chatId, "Ссылка удалена из отслеживания.");
        } catch (URISyntaxException e) {
            return new SendMessage(chatId, "Некорректный формат ссылки.");
        } catch (Exception e) {
            return new SendMessage(chatId, "Ошибка при удалении ссылки. Возможно, она не отслеживается.");
        }
    }
}

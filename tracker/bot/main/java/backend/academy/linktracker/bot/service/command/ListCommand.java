package backend.academy.linktracker.bot.service.command;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.LinkResponse;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListCommand implements Command {
    private final ScrapperClient scrapperClient;

    @Override
    public String name() {
        return "/list";
    }

    @Override
    public String description() {
        return "Показать список отслеживаемых ссылок";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        String text = update.message().text();
        String[] parts = text.split(" ");

        var response = scrapperClient.getAllLinks(chatId);
        List<LinkResponse> links = response.getLinks();

        if (parts.length > 1) {
            String tag = parts[1];
            links = links.stream()
                    .filter(l -> l.getTags() != null && l.getTags().contains(tag))
                    .toList();
        }

        if (links == null || links.isEmpty()) {
            return new SendMessage(chatId, "Нет отслеживаемых ссылок");
        }

        String message = links.stream().map(l -> "• " + l.getUrl().toString()).collect(Collectors.joining("\n"));

        return new SendMessage(chatId, "Ваши ссылки:\n" + message);
    }
}

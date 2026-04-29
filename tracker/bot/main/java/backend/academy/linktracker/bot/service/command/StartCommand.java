package backend.academy.linktracker.bot.service.command;

import backend.academy.linktracker.bot.client.ScrapperClient;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartCommand implements Command {
    private static final String NAME = "/start";
    private static final String DESCRIPTION = "Начать работу с ботом";
    private static final String WELCOME_MSG = "Добро пожаловать! Используйте /help для списка команд.";

    private final ScrapperClient scrapperClient;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();

        try {
            scrapperClient.registerChat(chatId);
            return new SendMessage(chatId, WELCOME_MSG);
        } catch (Exception e) {
            return new SendMessage(chatId, "Произошла ошибка при регистрации. Попробуйте позже.");
        }
    }
}

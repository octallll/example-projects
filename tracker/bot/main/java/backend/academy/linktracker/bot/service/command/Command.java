package backend.academy.linktracker.bot.service.command;

import backend.academy.linktracker.bot.model.UserStepData;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

public interface Command {
    String name();

    String description();

    SendMessage handle(Update update);

    default SendMessage handleNextStep(Update update, UserStepData data) {
        return null;
    }

    default boolean supports(Update update) {
        String text = update.message().text();
        return text != null && text.startsWith(name());
    }
}

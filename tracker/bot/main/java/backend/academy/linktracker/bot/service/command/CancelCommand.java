package backend.academy.linktracker.bot.service.command;

import backend.academy.linktracker.bot.model.UserStateManager;
import backend.academy.linktracker.bot.model.UserStepData;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancelCommand implements Command {
    private final UserStateManager stateManager;

    @Override
    public String name() {
        return "/cancel";
    }

    @Override
    public String description() {
        return "Отменить выполнение текущей команды";
    }

    @Override
    public SendMessage handle(Update update) {
        return cancel(update);
    }

    @Override
    public SendMessage handleNextStep(Update update, UserStepData data) {
        return cancel(update);
    }

    private SendMessage cancel(Update update) {
        long chatId = update.message().chat().id();

        stateManager.clearState(chatId);
        return new SendMessage(chatId, "Команда отменена.");
    }
}

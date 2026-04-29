package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.model.UserStateManager;
import backend.academy.linktracker.bot.model.UserStepData;
import backend.academy.linktracker.bot.service.command.Command;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class BotService {
    private final TelegramBot telegramBot;
    private final List<Command> commands;
    private final UserStateManager stateManager;

    private static final String UNKNOWN_COMMAND = "Неизвестная команда. Введите /help.";

    @PostConstruct
    public void init() {
        log.atInfo()
                .addKeyValue("token_length", telegramBot.getToken().length())
                .log("Bot token loaded");

        telegramBot.setUpdatesListener(this::handleUpdates, this::handleError);
        setupCommandsMenu();
    }

    public int handleUpdates(List<Update> updates) {
        for (Update update : updates) {
            try {
                handleUpdate(update);
            } catch (Exception e) {
                log.atError()
                        .setCause(e)
                        .addKeyValue("update_id", update.updateId())
                        .log("Error handling individual update");
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void handleError(Throwable e) {
        log.atError().setCause(e).log("Telegram Bot API error occurred");
    }

    private void handleUpdate(Update update) {
        if (update.message() == null || update.message().text() == null) {
            return;
        }

        long chatId = update.message().chat().id();
        String text = update.message().text();

        log.atInfo()
                .addKeyValue("chat_id", chatId)
                .addKeyValue("message_text", text)
                .log("Processing incoming message");

        UserStepData stepData = stateManager.getState(chatId);
        if (stepData != null && !text.startsWith("/")) {
            findCommand(stepData.commandName())
                    .ifPresent(cmd -> telegramBot.execute(cmd.handleNextStep(update, stepData)));
            return;
        }

        Command command =
                commands.stream().filter(c -> c.supports(update)).findFirst().orElse(null);

        if (command != null) {
            stateManager.clearState(chatId);
            telegramBot.execute(command.handle(update));
        } else {
            sendMessage(chatId, UNKNOWN_COMMAND);
        }
    }

    private Optional<Command> findCommand(String name) {
        return commands.stream().filter(c -> c.name().equals(name)).findFirst();
    }

    public void sendMessage(long chatId, String messageText) {
        telegramBot.execute(new SendMessage(chatId, messageText));
    }

    private void setupCommandsMenu() {
        BotCommand[] botCommands = commands.stream()
                .map(c -> new BotCommand(c.name(), c.description()))
                .toArray(BotCommand[]::new);

        try {
            telegramBot.execute(new SetMyCommands(botCommands));
        } catch (Exception e) {
            log.atError().setCause(e).log("Error setting commands menu");
        }
    }
}

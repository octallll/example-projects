package backend.academy.linktracker.bot.service.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand implements Command {
    private static final String COMMAND_NAME = "/help";
    private static final String COMMAND_DESCRIPTION = "Показать справку";

    private final List<Command> commands;

    public HelpCommand(List<Command> commands) {
        this.commands = commands;
    }

    @Override
    public String name() {
        return COMMAND_NAME;
    }

    @Override
    public String description() {
        return COMMAND_DESCRIPTION;
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();

        return new SendMessage(chatId, getHelpMessage());
    }

    private String getHelpMessage() {
        StringBuilder msg = new StringBuilder("Доступные команды:\n");

        for (Command command : commands) {
            msg.append(String.format("%s - %s%n", command.name(), command.description()));
        }

        return msg.append(String.format("%s - %s%n", name(), description())).toString();
    }
}

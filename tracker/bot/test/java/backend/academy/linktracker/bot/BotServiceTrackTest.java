package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.model.UserStateManager;
import backend.academy.linktracker.bot.service.BotService;
import backend.academy.linktracker.bot.service.command.Command;
import backend.academy.linktracker.bot.service.command.TrackCommand;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BotServiceTrackTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private ScrapperClient scrapperClient;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @Mock
    private UserStateManager stateManager;

    @Captor
    private ArgumentCaptor<SendMessage> sendMessageCaptor;

    private BotService botService;

    @BeforeEach
    void setUp() {
        TrackCommand trackCommand = new TrackCommand(scrapperClient, stateManager);
        List<Command> commands = List.of(trackCommand);

        botService = new BotService(telegramBot, commands, stateManager);
    }

    @Test
    @DisplayName("При вводе команды /track бот должен переходить к ожиданию тегов")
    void shouldSendMessageWhenTrackCommandReceived() {
        long chatId = 123L;
        String text = "/track https://github.com/user/repo";

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(text);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        botService.handleUpdates(List.of(update));

        verify(telegramBot).execute(sendMessageCaptor.capture());
        SendMessage capturedRequest = sendMessageCaptor.getValue();

        assertEquals(chatId, capturedRequest.getParameters().get("chat_id"));
        assertEquals(
                "Теперь пришли теги через запятую",
                capturedRequest.getParameters().get("text"));
    }
}

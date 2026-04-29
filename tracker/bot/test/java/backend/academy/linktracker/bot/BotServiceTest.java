package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.model.UserStateManager;
import backend.academy.linktracker.bot.service.BotService;
import backend.academy.linktracker.bot.service.command.Command;
import backend.academy.linktracker.bot.service.command.HelpCommand;
import backend.academy.linktracker.bot.service.command.ListCommand;
import backend.academy.linktracker.bot.service.command.StartCommand;
import backend.academy.linktracker.bot.service.command.TrackCommand;
import backend.academy.linktracker.bot.service.command.UntrackCommand;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
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
public class BotServiceTest {
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

    private BotService botService;

    @BeforeEach
    void setUp() {
        List<Command> commands = List.of(
                new StartCommand(scrapperClient),
                new HelpCommand(List.of(
                        new StartCommand(scrapperClient),
                        new ListCommand(scrapperClient),
                        new TrackCommand(scrapperClient, stateManager),
                        new UntrackCommand(scrapperClient))),
                new ListCommand(scrapperClient),
                new TrackCommand(scrapperClient, stateManager),
                new UntrackCommand(scrapperClient));

        botService = new BotService(telegramBot, commands, stateManager);
    }

    @Captor
    private ArgumentCaptor<BaseRequest<?, ?>> sendMessageCaptor;

    @Test
    @DisplayName("Бот отправляет приветственное сообщение на команду /start")
    public void shouldSendWelcomeMessageWhenUseStartCommand() {
        long chatId = 324L;

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/start");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        botService.handleUpdates(List.of(update));

        verify(telegramBot).execute(sendMessageCaptor.capture());
        SendMessage capturedRequest = (SendMessage) sendMessageCaptor.getValue();

        assertEquals(chatId, capturedRequest.getParameters().get("chat_id"));

        String text = (String) capturedRequest.getParameters().get("text");
        assertTrue(text.contains("Добро пожаловать"));
    }

    @Test
    @DisplayName("Бот отправляет список команд на команду /help")
    public void shouldSendListOfCommandsWhenUseHelpCommand() {
        long chatId = 54L;

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/help");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        botService.handleUpdates(List.of(update));

        verify(telegramBot).execute(sendMessageCaptor.capture());
        SendMessage capturedRequest = (SendMessage) sendMessageCaptor.getValue();

        assertEquals(chatId, capturedRequest.getParameters().get("chat_id"));

        String text = (String) capturedRequest.getParameters().get("text");
        assertTrue(text.contains("/start"));
        assertTrue(text.contains("/help"));
    }

    @Test
    @DisplayName("При получении неизвестной команды бот отвечает сообщением об ошибке")
    public void shouldSendErrorMessageWhenSendUnknownCommand() {
        long chatId = 54L;

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/unknown");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        botService.handleUpdates(List.of(update));

        verify(telegramBot).execute(sendMessageCaptor.capture());
        SendMessage capturedRequest = (SendMessage) sendMessageCaptor.getValue();

        assertEquals(chatId, capturedRequest.getParameters().get("chat_id"));

        String text = (String) capturedRequest.getParameters().get("text");
        assertEquals("Неизвестная команда. Введите /help.", text);
    }

    private void setUpUpdateMock(long chatId, String text) {
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(text);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
    }

    @Test
    @DisplayName("Бот выводит список ссылок, если у пользователя есть подписки")
    public void shouldReturnLinksListWhenUserHasSubscriptions() {
        long chatId = 777L;

        LinkResponse link1 = new LinkResponse()
                .url(URI.create("https://github.com/sveshnikov/project1"))
                .tags(List.of("java", "backend"));
        LinkResponse link2 = new LinkResponse().url(URI.create("https://stackoverflow.com/questions/123"));

        ListLinksResponse mockResponse =
                new ListLinksResponse().links(List.of(link1, link2)).size(2);

        setUpUpdateMock(chatId, "/list");
        when(scrapperClient.getAllLinks(chatId)).thenReturn(mockResponse);

        botService.handleUpdates(List.of(update));

        verify(telegramBot).execute(sendMessageCaptor.capture());
        SendMessage capturedRequest = (SendMessage) sendMessageCaptor.getValue();

        assertEquals(chatId, capturedRequest.getParameters().get("chat_id"));

        String responseText = (String) capturedRequest.getParameters().get("text");

        assertTrue(responseText.contains("https://github.com/sveshnikov/project1"));
        assertTrue(responseText.contains("https://stackoverflow.com/questions/123"));
    }

    @Test
    @DisplayName("Бот сообщает об отсутствии подписок, если список пуст")
    public void shouldReturnEmptyMessageWhenUserHasNoSubscriptions() {
        long chatId = 888L;

        ListLinksResponse emptyResponse =
                new ListLinksResponse().links(List.of()).size(0);

        setUpUpdateMock(chatId, "/list");
        when(scrapperClient.getAllLinks(chatId)).thenReturn(emptyResponse);

        botService.handleUpdates(List.of(update));

        verify(telegramBot).execute(sendMessageCaptor.capture());
        SendMessage capturedRequest = (SendMessage) sendMessageCaptor.getValue();

        assertEquals(chatId, capturedRequest.getParameters().get("chat_id"));

        String responseText = (String) capturedRequest.getParameters().get("text");
        assertEquals("Нет отслеживаемых ссылок", responseText);
    }

    @Test
    @DisplayName("Бот фильтрует список ссылок по заданному тегу")
    public void shouldReturnFilteredLinksListByTag() {
        long chatId = 999L;
        String targetTag = "java";

        LinkResponse javaLink = new LinkResponse()
                .url(URI.create("https://github.com/java-project"))
                .tags(List.of("java", "backend"));

        LinkResponse pythonLink = new LinkResponse()
                .url(URI.create("https://github.com/python-project"))
                .tags(List.of("python"));

        ListLinksResponse mockResponse =
                new ListLinksResponse().links(List.of(javaLink, pythonLink)).size(2);

        setUpUpdateMock(chatId, "/list " + targetTag);
        when(scrapperClient.getAllLinks(chatId)).thenReturn(mockResponse);

        botService.handleUpdates(List.of(update));

        verify(telegramBot).execute(sendMessageCaptor.capture());
        SendMessage capturedRequest = (SendMessage) sendMessageCaptor.getValue();

        String responseText = (String) capturedRequest.getParameters().get("text");

        assertTrue(responseText.contains("https://github.com/java-project"));
        assertFalse(responseText.contains("https://github.com/python-project"));
    }
}

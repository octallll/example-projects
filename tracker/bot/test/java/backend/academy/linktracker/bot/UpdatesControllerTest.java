package backend.academy.linktracker.bot;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.controller.UpdatesController;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.BotService;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdatesControllerTest {

    @Mock
    private BotService botService;

    @InjectMocks
    private UpdatesController updatesController;

    @Test
    @DisplayName("Бот отправляет обновление только подписанным пользователям")
    void shouldSendUpdatesOnlyToSubscribedUsers() {
        LinkUpdate update = new LinkUpdate()
                .url(URI.create("https://github.com/user/repo"))
                .description("New update")
                .tgChatIds(List.of(100L, 200L));

        updatesController.postUpdate(update);

        verify(botService, times(1)).sendMessage(eq(100L), anyString());
        verify(botService, times(1)).sendMessage(eq(200L), anyString());

        verify(botService, never()).sendMessage(eq(300L), anyString());
    }
}

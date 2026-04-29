package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.GithubClient;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.model.GithubIssueResponse;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.StackOverflowAnswersResponse;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.scheduler.ScheduledTask;
import backend.academy.linktracker.scrapper.scheduler.SchedulerConfig;
import backend.academy.linktracker.scrapper.sender.MessageSender;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScheduledTaskTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private MessageSender sender;

    @Mock
    private SchedulerConfig schedulerConfig;

    @Mock
    private GithubClient githubClient;

    @Mock
    private StackOverflowClient stackOverflowClient;

    @InjectMocks
    private ScheduledTask scheduledTask;

    @Test
    @DisplayName("Обновление должно отправляться только подписчикам конкретной ссылки")
    void update_ShouldSendOnlyToSubscribers() {
        URI linkUrl = URI.create("https://github.com/owner/repo");
        Link link = new Link(
                1L,
                linkUrl,
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusDays(1));

        when(schedulerConfig.checkInterval()).thenReturn(Duration.ofMinutes(5));
        when(schedulerConfig.batchSize()).thenReturn(10);

        when(linkRepository.getOldestLinks(any(Duration.class), anyInt())).thenReturn(List.of(link));

        GithubIssueResponse mockIssue = new GithubIssueResponse(
                "Title", "Body", "url", new GithubIssueResponse.User("login"), OffsetDateTime.now());
        when(githubClient.fetchLastIssues("owner", "repo")).thenReturn(List.of(mockIssue));

        scheduledTask.update();

        verify(sender, times(1)).sendUpdate(eq(link), anyString());
    }

    @Test
    @DisplayName("Текст превью для GitHub должен обрезаться до 200 символов")
    void shouldTruncateGithubPreview() {
        URI url = URI.create("https://github.com/owner/repo");
        Link link = new Link(
                1L, url, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().minusDays(1));

        String longBody = "A".repeat(300);
        GithubIssueResponse issue = new GithubIssueResponse(
                "Title", longBody, "url", new GithubIssueResponse.User("login"), OffsetDateTime.now());

        when(schedulerConfig.batchSize()).thenReturn(10);
        when(schedulerConfig.checkInterval()).thenReturn(Duration.ofMinutes(5));
        when(linkRepository.getOldestLinks(any(), anyInt())).thenReturn(List.of(link));
        when(githubClient.fetchLastIssues("owner", "repo")).thenReturn(List.of(issue));

        scheduledTask.update();

        ArgumentCaptor<String> descCaptor = ArgumentCaptor.forClass(String.class);
        verify(sender).sendUpdate(eq(link), descCaptor.capture());

        String description = descCaptor.getValue();
        String preview = description.substring(description.lastIndexOf("Превью: ") + 8);
        assertTrue(preview.length() <= 200);
        assertTrue(preview.endsWith("..."));
    }

    @Test
    @DisplayName("Должно отправляться уведомление при новом ответе на StackOverflow")
    void shouldSendUpdateOnNewStackOverflowAnswer() {
        URI url = URI.create("https://stackoverflow.com/questions/12345/title");
        Link link = new Link(
                1L, url, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().minusDays(1));

        StackOverflowAnswersResponse.AnswerItem answer = mock(StackOverflowAnswersResponse.AnswerItem.class);
        StackOverflowAnswersResponse.AnswerItem.Owner owner =
                new StackOverflowAnswersResponse.AnswerItem.Owner("John Doe");

        when(answer.owner()).thenReturn(owner);
        when(answer.body()).thenReturn("New Answer Content");
        when(answer.creationDate()).thenReturn(OffsetDateTime.now());

        StackOverflowAnswersResponse response = new StackOverflowAnswersResponse(List.of(answer));

        when(schedulerConfig.batchSize()).thenReturn(10);
        when(linkRepository.getOldestLinks(any(), anyInt())).thenReturn(List.of(link));
        when(stackOverflowClient.fetchAnswers(12345L)).thenReturn(response);

        scheduledTask.update();

        verify(sender, times(1)).sendUpdate(eq(link), contains("John Doe"));
    }

    @Test
    @DisplayName("Ошибки при обработке одной ссылки не должны прерывать батч")
    void shouldContinueProcessingOnLinkError() {
        Link link1 =
                new Link(1L, URI.create("https://github.com/owner/repo1"), OffsetDateTime.now(), OffsetDateTime.now());
        Link link2 =
                new Link(2L, URI.create("https://github.com/owner/repo2"), OffsetDateTime.now(), OffsetDateTime.now());

        when(schedulerConfig.batchSize()).thenReturn(10);
        when(linkRepository.getOldestLinks(any(), anyInt())).thenReturn(List.of(link1, link2));

        when(githubClient.fetchLastIssues("owner", "repo1")).thenThrow(new RuntimeException("API Error"));
        when(githubClient.fetchLastIssues("owner", "repo2")).thenReturn(List.of());

        assertDoesNotThrow(() -> scheduledTask.update());
        verify(linkRepository, times(1)).update(link1);
        verify(linkRepository, times(1)).update(link2);
    }

    @Test
    @DisplayName("Проверка корректности формирования батча")
    void shouldRequestLinksWithCorrectBatchSize() {
        int batchSize = 50;
        Duration checkInterval = Duration.ofMinutes(10);
        when(schedulerConfig.batchSize()).thenReturn(batchSize);
        when(schedulerConfig.checkInterval()).thenReturn(checkInterval);

        scheduledTask.update();

        verify(linkRepository).getOldestLinks(checkInterval, batchSize);
    }

    @Test
    @DisplayName("Бот корректно обрабатывает недоступность внешнего API (GitHub)")
    void shouldHandleExternalApiUnavailability() {
        URI url = URI.create("https://github.com/owner/repo");
        Link link = new Link(1L, url, OffsetDateTime.now(), OffsetDateTime.now());

        when(schedulerConfig.batchSize()).thenReturn(1);
        when(linkRepository.getOldestLinks(any(), anyInt())).thenReturn(List.of(link));

        when(githubClient.fetchLastIssues(anyString(), anyString()))
                .thenThrow(new RuntimeException("503 Service Unavailable"));

        assertDoesNotThrow(() -> scheduledTask.update());

        verify(sender, never()).sendUpdate(any(), anyString());

        verify(linkRepository).update(link);
    }
}

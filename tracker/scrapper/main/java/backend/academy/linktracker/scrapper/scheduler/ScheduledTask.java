package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.client.GithubClient;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.model.GithubIssueResponse;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.StackOverflowAnswersResponse;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.sender.MessageSender;
import jakarta.annotation.PreDestroy;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduledTask {
    private final LinkRepository linkRepository;
    private final MessageSender sender;
    private final SchedulerConfig schedulerConfig;

    public static final String GITHUB_HOST = "github.com";
    public static final String STACKOVERFLOW_HOST = "stackoverflow.com";
    private static final int GITHUB_RESPONSE_SIZE = 3;
    private static final int STACKOVERFLOW_RESPONSE_SIZE = 3;

    private final ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Scheduled(fixedDelayString = "${app.scheduler.interval}")
    public void update() {
        log.atInfo().log("Start scanning links");

        List<Link> links = linkRepository.getOldestLinks(schedulerConfig.checkInterval(), schedulerConfig.batchSize());

        List<CompletableFuture<Void>> futures = links.stream()
                .map(link -> CompletableFuture.runAsync(() -> processLink(link), executor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void processLink(Link link) {
        String host = getHost(link);

        if (host == null) {
            return;
        }

        switch (host) {
            case GITHUB_HOST -> checkGithub(link);
            case STACKOVERFLOW_HOST -> checkStackOverflow(link);
            default -> log.atWarn().addKeyValue("host", host).log("Unknown host");
        }

        link.setLastCheck(OffsetDateTime.now());
        linkRepository.update(link);
    }

    private static String getHost(Link link) {
        URI url = (link != null) ? link.getUrl() : null;

        if (url == null) {
            return null;
        }

        String host = url.getHost();

        if (host == null) {
            log.atWarn().addKeyValue("url", url).log("Host is null for URL");
            return null;
        }

        return host;
    }

    private final GithubClient githubClient;

    private void checkGithub(Link link) {
        String[] parts = link.getUrl().getPath().split("/");

        if (parts.length < GITHUB_RESPONSE_SIZE) {
            return;
        }

        String owner = parts[1];
        String repo = parts[2];

        try {
            List<GithubIssueResponse> issues = githubClient.fetchLastIssues(owner, repo);

            if (issues != null && !issues.isEmpty()) {
                GithubIssueResponse latest = issues.getFirst();

                if (latest.createdAt().isAfter(link.getLastUpdateTime())) {
                    String description = String.format(
                            "Новый Issue/PR от %s: %s. Превью: %s",
                            latest.user().login(), latest.title(), truncate(latest.body()));

                    sender.sendUpdate(link, description);

                    link.setLastUpdateTime(latest.createdAt());
                }
            }
        } catch (Exception e) {
            log.atError().setCause(e).log("Error when check github");
        }
    }

    private final StackOverflowClient stackOverflowClient;

    private void checkStackOverflow(Link link) {
        // URL: https://stackoverflow.com/questions/12345/title
        String path = link.getUrl().getPath();
        String[] parts = path.split("/");

        if (parts.length < STACKOVERFLOW_RESPONSE_SIZE || !parts[1].equals("questions")) {
            return;
        }

        try {
            Long questionId = Long.parseLong(parts[2]);
            StackOverflowAnswersResponse response = stackOverflowClient.fetchAnswers(questionId);

            if (response != null
                    && response.items() != null
                    && !response.items().isEmpty()) {
                StackOverflowAnswersResponse.AnswerItem latestAnswer =
                        response.items().getFirst();

                if (latestAnswer.creationDate().isAfter(link.getLastUpdateTime())) {
                    String description = String.format(
                            "Новый ответ от %s в StackOverflow. Превью: %s",
                            latestAnswer.owner().displayName(), truncate(latestAnswer.body()));

                    sender.sendUpdate(link, description);

                    link.setLastUpdateTime(latestAnswer.creationDate());
                }
            }
        } catch (Exception e) {
            log.atError().setCause(e).log("Error when check StackOverflow");
        }
    }

    private String truncate(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return text.length() <= 200 ? text : text.substring(0, 197) + "...";
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}

package backend.academy.linktracker.scrapper.sender;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.use-queue", havingValue = "false")
public class HttpMessageSender implements MessageSender {
    private final LinkRepository linkRepository;
    private final BotClient botClient;

    @Override
    public void sendUpdate(Link link, String reason) {
        botClient.sendUpdate(new LinkUpdate()
                .id(link.getId())
                .url(link.getUrl())
                .tgChatIds(linkRepository.findByUrl(link.getUrl()))
                .description(reason));
    }
}

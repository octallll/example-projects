package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface BotClient {
    @PostExchange("/updates")
    void sendUpdate(@RequestBody LinkUpdate update);
}

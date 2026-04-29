package backend.academy.linktracker.scrapper.sender;

import backend.academy.linktracker.scrapper.model.Link;

public interface MessageSender {
    void sendUpdate(Link link, String reason);
}

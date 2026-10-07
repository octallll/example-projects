package backend.academy.linktracker.ai.sender;

import backend.academy.linktracker.avro.PrioritizationLinkUpdateEvent;

public interface MessageSender {
    void sendUpdate(PrioritizationLinkUpdateEvent event);
}

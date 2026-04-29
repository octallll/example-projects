package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.avro.LinkUpdateEvent;
import backend.academy.linktracker.bot.service.BotService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LinkUpdateListener {
    private final BotService botService;

    @RetryableTopic(attempts = "${app.kafka.backoff-attempts}")
    @KafkaListener(topics = "${app.kafka.topic}", groupId = "bot-updates-group")
    public void listen(LinkUpdateEvent update) {
        log.info("RECEIVED UPDATE IN BOT: {}", update); // <--- Добавьте это
        processUpdate(update, botService);
    }

    @DltHandler
    public void handleDlt(LinkUpdateEvent update, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Update {} sent to DLT after failed retries from topic {}", update.getId(), topic);
    }

    private static void processUpdate(LinkUpdateEvent update, BotService botService) {
        String description = update.getDescription().toString();
        List<Long> chatIds = update.getTgChatIds();

        for (Long chatId : chatIds) {
            botService.sendMessage(chatId, description + ": " + update.getUrl());
        }
    }
}

package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.BotService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UpdatesController {

    private final BotService botService;

    @PostMapping("/updates")
    public ResponseEntity<Void> postUpdate(@RequestBody LinkUpdate update) {
        log.atInfo().addKeyValue("url", update.getUrl()).log("Got update for link");

        processUpdate(update, botService);

        return ResponseEntity.ok().build();
    }

    private static void processUpdate(LinkUpdate update, BotService botService) {
        String description = update.getDescription();
        List<Long> chatIds = update.getTgChatIds();

        for (Long chatId : chatIds) {
            botService.sendMessage(chatId, description + ": " + update.getUrl());
        }
    }
}

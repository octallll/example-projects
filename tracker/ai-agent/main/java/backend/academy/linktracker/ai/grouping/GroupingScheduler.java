package backend.academy.linktracker.ai.grouping;

import backend.academy.linktracker.ai.model.Event;
import backend.academy.linktracker.ai.model.Priority;
import backend.academy.linktracker.ai.sender.MessageSender;
import backend.academy.linktracker.avro.PrioritizationLinkUpdateEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class GroupingScheduler {
    private final PrioritizationUtils utils;
    private final MessageSender sender;
    private final AtomicLong lastId = new AtomicLong(0);

    Map<Long, List<Event>> tgChatToUpdate = new ConcurrentHashMap<>();

    @Scheduled(fixedDelayString = "${app.prioritization.grouping.window-ms}")
    public void process() {
        for (Long id : tgChatToUpdate.keySet()) {
            List<Event> updates = tgChatToUpdate.remove(id);

            if (updates.isEmpty()) {
                continue;
            }

            Priority updatePriority = updates.stream().map(Event::priority).reduce(Priority.LOW, utils::maxPriority);

            String description = updates.size() == 1
                    ? updates.getFirst().description()
                    : IntStream.range(0, updates.size())
                            .mapToObj(i -> i + 1 + " " + updates.get(i).description())
                            .collect(Collectors.joining("\n"));

            sender.sendUpdate(PrioritizationLinkUpdateEvent.newBuilder()
                    .setPriority(updatePriority.toString())
                    .setDescription(description)
                    .setTgChatIds(List.of(id))
                    .setId(lastId.incrementAndGet())
                    .build());
        }
    }

    public void processUpdate(String description, Priority priority, List<Long> tgChatIds) {
        tgChatIds.forEach(id -> tgChatToUpdate
                .computeIfAbsent(id, _ -> new CopyOnWriteArrayList<>())
                .add(new Event(description, priority)));
    }
}

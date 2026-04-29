package backend.academy.linktracker.bot.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class UserStateManager {
    private final Map<Long, UserStepData> states = new ConcurrentHashMap<>();

    public void saveState(long chatId, UserStepData data) {
        states.put(chatId, data);
    }

    public UserStepData getState(long chatId) {
        return states.get(chatId);
    }

    public void clearState(long chatId) {
        states.remove(chatId);
    }

    public boolean hasState(long chatId) {
        return states.containsKey(chatId);
    }
}

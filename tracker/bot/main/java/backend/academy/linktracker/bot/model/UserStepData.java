package backend.academy.linktracker.bot.model;

import java.net.URI;

public record UserStepData(State state, URI url, String commandName) {}

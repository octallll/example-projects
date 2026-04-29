package backend.academy.linktracker.scrapper.repository;

import java.net.URI;
import java.util.List;

public record InChatLink(URI url, List<String> tags) {}

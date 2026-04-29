package backend.academy.linktracker.scrapper.repository.orm.jpa;

import backend.academy.linktracker.scrapper.repository.orm.tables.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaChatRepository extends JpaRepository<Chat, Long> {}

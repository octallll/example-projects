package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.OutboxMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {
    List<OutboxMessage> findTop50ByStatusOrderByCreatedAtAsc(String status);
}

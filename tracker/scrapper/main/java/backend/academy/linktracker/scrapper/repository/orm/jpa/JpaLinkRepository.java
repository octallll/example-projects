package backend.academy.linktracker.scrapper.repository.orm.jpa;

import backend.academy.linktracker.scrapper.repository.orm.tables.Link;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaLinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByUrl(String url);

    @Query(
            "SELECT l FROM backend.academy.linktracker.scrapper.repository.orm.tables.Link l WHERE l.lastCheckAt < :time")
    List<Link> findOldestLinks(OffsetDateTime time);
}

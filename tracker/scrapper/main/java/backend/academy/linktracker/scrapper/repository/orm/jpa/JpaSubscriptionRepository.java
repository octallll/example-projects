package backend.academy.linktracker.scrapper.repository.orm.jpa;

import backend.academy.linktracker.scrapper.repository.orm.tables.Chat;
import backend.academy.linktracker.scrapper.repository.orm.tables.Subscription;
import backend.academy.linktracker.scrapper.repository.orm.tables.SubscriptionId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSubscriptionRepository extends JpaRepository<Subscription, SubscriptionId> {
    List<Subscription> findAllByChatId(Chat chatId);

    Optional<Subscription> findByChatIdAndLinkId_Url(Chat chatId, String url);

    List<Subscription> findAllByLinkId_Url(String url);
}

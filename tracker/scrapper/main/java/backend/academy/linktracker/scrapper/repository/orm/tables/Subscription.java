package backend.academy.linktracker.scrapper.repository.orm.tables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@IdClass(SubscriptionId.class)
public class Subscription {
    @Id
    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chatId;

    @Id
    @ManyToOne
    @JoinColumn(name = "link_id")
    private Link linkId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", columnDefinition = "text[]")
    private List<String> tags;
}

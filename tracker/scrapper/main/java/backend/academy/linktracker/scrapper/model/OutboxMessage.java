package backend.academy.linktracker.scrapper.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "outbox")
@Getter
@Setter
public class OutboxMessage {
    @Id
    private UUID id;

    private String topic;
    private String keyValue;

    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    private String status;
    private OffsetDateTime createdAt;
}

package backend.academy.linktracker.scrapper.repository.orm.tables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chats")
@Getter
@Setter
public class Chat {
    @Id
    private Long id;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}

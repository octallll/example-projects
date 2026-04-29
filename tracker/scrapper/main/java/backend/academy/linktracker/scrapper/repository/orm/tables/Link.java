package backend.academy.linktracker.scrapper.repository.orm.tables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "links")
@Getter
@Setter
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", unique = true)
    @NotNull
    private String url;

    @Column(name = "last_update_time")
    private OffsetDateTime lastUpdateTime;

    @Column(name = "last_check_at")
    private OffsetDateTime lastCheckAt = OffsetDateTime.now();
}

package backend.academy.linktracker.scrapper.repository.orm.tables;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode // Обязательно для составных ключей JPA
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionId implements Serializable {
    private Long chatId;
    private Long linkId;
}

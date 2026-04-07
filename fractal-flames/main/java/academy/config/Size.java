package academy.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Size {
    @JsonProperty("width")
    private int width = 1920;

    @JsonProperty("height")
    private int height = 1080;
}

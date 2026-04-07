package academy.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FunctionConfig {
    @JsonProperty("name")
    private String name;

    @JsonProperty("weight")
    private double weight;
}

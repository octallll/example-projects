package academy.config;

import academy.transforms.Transformation;
import academy.utils.Point;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffineParamConfig implements Transformation {
    @JsonProperty("a")
    private double a;

    @JsonProperty("b")
    private double b;

    @JsonProperty("c")
    private double c;

    @JsonProperty("d")
    private double d;

    @JsonProperty("e")
    private double e;

    @JsonProperty("f")
    private double f;

    @Override
    public Point transform(Point p) {
        return new Point(a * p.x() + b * p.y() + c, d * p.x() + e * p.y() + f);
    }
}

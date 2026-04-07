package academy.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Config {
    @JsonProperty("size")
    private Size size;

    @JsonProperty("iteration_count")
    private int iterationCount = 2500;

    @JsonProperty("output_path")
    private String outputPath = "result.png";

    @JsonProperty("threads")
    private int threads = 1;

    @JsonProperty("seed")
    private int seed = 5;

    @JsonProperty("functions")
    private List<FunctionConfig> functions;

    @JsonProperty("affine_params")
    private List<AffineParamConfig> affineParams;

    @JsonProperty("gamma_correction")
    private boolean gammaCorrection = false;

    @JsonProperty("gamma")
    private double gamma = 2.2;

    @JsonProperty("symmetry_level")
    private int symmetryLevel = 1;

    public Size getSize() {
        if (size == null) {
            size = new Size();
        }

        return size;
    }

    public int getWidth() {
        return getSize().getWidth();
    }

    public void setWidth(int width) {
        getSize().setWidth(width);
    }

    public int getHeight() {
        return getSize().getHeight();
    }

    public void setHeight(int height) {
        getSize().setHeight(height);
    }

    private String functionsStr;
    private String affineParamsStr;

    public String getFunctionsStr() {
        if (functionsStr != null) {
            return functionsStr;
        }

        if (functions != null && !functions.isEmpty()) {
            StringBuilder sb = new StringBuilder();

            for (FunctionConfig func : functions) {
                sb.append(func.getName()).append(":").append(func.getWeight()).append(",");
            }

            if (!sb.isEmpty()) {
                sb.setLength(sb.length() - 1);
            }

            return sb.toString();
        }

        return null;
    }

    public String getAffineParamsStr() {
        if (affineParamsStr != null) {
            return affineParamsStr;
        }

        if (affineParams != null && !affineParams.isEmpty()) {
            StringBuilder sb = new StringBuilder();

            for (AffineParamConfig param : affineParams) {
                sb.append(param.getA())
                        .append(",")
                        .append(param.getB())
                        .append(",")
                        .append(param.getC())
                        .append(",")
                        .append(param.getD())
                        .append(",")
                        .append(param.getE())
                        .append(",")
                        .append(param.getF())
                        .append("/");
            }

            if (!sb.isEmpty()) {
                sb.setLength(sb.length() - 1);
            }

            return sb.toString();
        }

        return null;
    }
}

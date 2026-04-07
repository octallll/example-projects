package academy.utils;

import academy.config.AffineParamConfig;
import academy.transforms.ExTransformation;
import academy.transforms.HandkerchiefTransformation;
import academy.transforms.LinearTransformation;
import academy.transforms.SphericalTransformation;
import academy.transforms.SwirlTransformation;
import academy.transforms.Transformation;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ApplicationUtils {
    private static final Logger LOGGER = LogManager.getLogger(ApplicationUtils.class);

    public static List<AffineParamConfig> parseAffine(String input) {
        LOGGER.info("Start validation of affine: {}", input);

        if (input == null || input.isEmpty()) {
            return List.of(new AffineParamConfig(1, 1, 1, 1, 1, 1));
        }

        List<AffineParamConfig> AffineParamConfigs = new ArrayList<>();

        String[] parts = input.split("/");

        for (String p : parts) {
            String[] affineCoefficients = p.split(",");

            if (affineCoefficients.length != 6) {
                throw new IllegalArgumentException("Incorrect Affine transformation input: " + p);
            }

            try {
                AffineParamConfigs.add(new AffineParamConfig(
                        Double.parseDouble(affineCoefficients[0]), Double.parseDouble(affineCoefficients[1]),
                        Double.parseDouble(affineCoefficients[2]), Double.parseDouble(affineCoefficients[3]),
                        Double.parseDouble(affineCoefficients[4]), Double.parseDouble(affineCoefficients[5])));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Incorrect Affine transformation input: " + p + " " + e.getMessage());
            }
        }

        return AffineParamConfigs;
    }

    public static void parseVariations(String input, List<Transformation> vars, List<Double> weights) {
        LOGGER.info("Start validation of variations: {}", input);

        if (input == null || input.isEmpty()) {
            vars.add(new LinearTransformation());
            weights.add(1.0);
            return;
        }

        String[] parts = input.split(",");

        for (String p : parts) {
            String[] pair = p.split(":");

            if (pair.length != 2) {
                throw new IllegalArgumentException("Incorrect variation input: " + input);
            }

            String name = pair[0].toLowerCase();

            try {
                double weight = Double.parseDouble(pair[1]);
                weights.add(weight);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Incorrect variation input: " + input);
            }

            vars.add(
                    switch (name) {
                        case "ex" -> new ExTransformation();
                        case "linear" -> new LinearTransformation();
                        case "spherical" -> new SphericalTransformation();
                        case "swirl" -> new SwirlTransformation();
                        case "handkerchief" -> new HandkerchiefTransformation();
                        default -> throw new IllegalArgumentException("Unknown variation: " + name);
                    });
        }
    }
}

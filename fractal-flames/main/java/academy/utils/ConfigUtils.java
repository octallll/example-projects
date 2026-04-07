package academy.utils;

import static academy.utils.ApplicationUtils.parseAffine;

import academy.config.Config;
import academy.config.FunctionConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigUtils {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public static Config getConfig(
            File configPath,
            Integer width,
            Integer height,
            Integer seed,
            Integer iterationCount,
            String outputPath,
            Integer threads,
            String affineParamsStr,
            String functionsStr,
            Boolean gammaCorrection,
            Double gamma,
            Integer symmetryLevel)
            throws IOException {

        Config config = new Config();

        if (configPath != null) {
            if (configPath.exists()) {
                config = JSON_MAPPER.readValue(configPath, Config.class);
            } else {
                throw new IllegalArgumentException("Config path doesn't exist");
            }
        }

        if (width != null) {
            config.setWidth(width);
        }

        if (height != null) {
            config.setHeight(height);
        }

        if (seed != null) {
            config.setSeed(seed);
        }

        if (iterationCount != null) {
            config.setIterationCount(iterationCount);
        }

        if (outputPath != null) {
            config.setOutputPath(outputPath);
        }

        if (threads != null) {
            config.setThreads(threads);
        }

        if (affineParamsStr != null) {
            config.setAffineParams(parseAffine(affineParamsStr));
            config.setAffineParamsStr(affineParamsStr);
        }

        if (functionsStr != null) {
            config.setFunctions(parseFunctionsFromString(functionsStr));
            config.setFunctionsStr(functionsStr);
        }

        if (gammaCorrection != null) {
            config.setGammaCorrection(gammaCorrection);
        }

        if (gamma != null) {
            config.setGamma(gamma);
        }

        if (symmetryLevel != null) {
            config.setSymmetryLevel(symmetryLevel);
        }

        return config;
    }

    private static List<FunctionConfig> parseFunctionsFromString(String input) {
        List<FunctionConfig> functions = new ArrayList<>();

        if (input == null || input.isEmpty()) {
            return functions;
        }

        String[] parts = input.split(",");

        for (String part : parts) {
            String[] pair = part.split(":");

            if (pair.length != 2) {
                throw new IllegalArgumentException("Invalid function format: " + part);
            }

            try {
                String name = pair[0].trim();
                double weight = Double.parseDouble(pair[1].trim());

                functions.add(new FunctionConfig(name, weight));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid weight in function: " + part);
            }
        }

        return functions;
    }
}

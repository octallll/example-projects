package academy;

import static academy.utils.ApplicationUtils.parseAffine;
import static academy.utils.ApplicationUtils.parseVariations;
import static academy.utils.ConfigUtils.getConfig;

import academy.config.AffineParamConfig;
import academy.config.Config;
import academy.model.Image;
import academy.render.Renderer;
import academy.transforms.Transformation;
import academy.utils.GammaCorrection;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "Application Example", version = "Example 1.0", mixinStandardHelpOptions = true)
public class Application implements Callable<Integer> {
    private static final Logger LOGGER = LogManager.getLogger(Application.class);

    @Option(
            names = {"-w", "--width"},
            description = "Image width")
    Integer width;

    @Option(
            names = {"-h", "--height"},
            description = "Image height")
    Integer height;

    @Option(
            names = {"--seed"},
            description = "Generator seed")
    Integer seed;

    @Option(
            names = {"-i", "--iteration-count"},
            description = "Iterations per sample")
    Integer iterationCount;

    @Option(
            names = {"-o", "--output-path"},
            description = "Output file path")
    String outputPath;

    @Option(
            names = {"-t", "--threads"},
            description = "Number of threads")
    Integer threads;

    @Option(
            names = {"-ap", "--affine-params"},
            description = "Affine transforms format: a,b,c,d,e,f/...")
    String affineParamsStr;

    @Option(
            names = {"-f", "--functions"},
            description = "Functions format: name:weight,name:weight")
    String functionsStr;

    @Option(
            names = {"-c", "--config"},
            description = "Path to JSON config")
    File configPath;

    @Option(
            names = {"-g", "--gamma-correction"},
            description = "Enable gamma correction")
    Boolean gammaCorrection;

    @Option(
            names = {"--gamma"},
            description = "Gamma value")
    Double gamma;

    @Option(
            names = {"-s", "--symmetry-level"},
            description = "Symmetry level")
    Integer symmetryLevel;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {
            Config config = getConfig(
                    configPath,
                    width,
                    height,
                    seed,
                    iterationCount,
                    outputPath,
                    threads,
                    affineParamsStr,
                    functionsStr,
                    gammaCorrection,
                    gamma,
                    symmetryLevel);

            List<AffineParamConfig> AffineParamConfigs = parseAffine(config.getAffineParamsStr());

            List<Transformation> transformations = new ArrayList<>();
            List<Double> weights = new ArrayList<>();
            parseVariations(config.getFunctionsStr(), transformations, weights);

            Renderer renderer = new Renderer(AffineParamConfigs, transformations, weights, config.getSeed());

            Image image = renderer.render(
                    config.getWidth(),
                    config.getHeight(),
                    config.getIterationCount(),
                    config.getSymmetryLevel(),
                    config.getThreads());

            if (config.isGammaCorrection()) {
                GammaCorrection.gammaProcess(image, config.getGamma());
            }

            Renderer.saveImage(image, config.getOutputPath());

            return 0;
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid arguments: {}", e.getMessage());
            return 2;
        } catch (IOException e) {
            LOGGER.error("Error while working with IO: {}", e.getMessage());
            return 2;
        } catch (Exception e) {
            LOGGER.error("Unexpected error: {}", e.getMessage());
            return 1;
        }
    }
}

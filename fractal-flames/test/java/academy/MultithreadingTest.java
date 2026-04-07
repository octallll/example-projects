package academy;

import academy.config.AffineParamConfig;
import academy.render.Renderer;
import academy.transforms.ExTransformation;
import academy.transforms.LinearTransformation;
import academy.transforms.SwirlTransformation;
import academy.transforms.Transformation;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class MultithreadingTest {
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 8})
    public void multithreadingTest(int numThreads) {
        //   System.out.println("With " + numThreads + " got " + timeByNumOfThreads(numThreads) + " time.");
    }

    private double timeByNumOfThreads(int numThreads) {
        List<AffineParamConfig> AffineParamConfigs = List.of(
                new AffineParamConfig(0.4, 0, 0, 0, 0.5, 0),
                new AffineParamConfig(0.4, 0, 0.5, 0, 0.5, 0),
                new AffineParamConfig(0.4, 0, 0, 0, 0.5, 0.5),
                new AffineParamConfig(0.2, -0.2, 0.8, 0.2, 0.2, 0.2),
                new AffineParamConfig(0.1, 0.5, 0.5, -0.5, 0.1, 0.5));

        List<Transformation> transformations =
                List.of(new LinearTransformation(), new SwirlTransformation(), new ExTransformation());

        List<Double> weights = List.of(0.2, 0.4, 1.0);

        Renderer renderer = new Renderer(AffineParamConfigs, transformations, weights, 56);

        int width = 1920;
        int height = 1080;
        int iterations = 100000000;
        int symmetry = 3;
        int seed = 56;

        long startTime = System.nanoTime();

        renderer.render(width, height, iterations, symmetry, numThreads);

        long endTime = System.nanoTime();

        return (endTime - startTime) / 1_000_000_000.0;
    }
}

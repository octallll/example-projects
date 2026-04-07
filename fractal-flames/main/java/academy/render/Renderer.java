package academy.render;

import academy.config.AffineParamConfig;
import academy.model.Image;
import academy.model.Pixel;
import academy.transforms.Transformation;
import academy.utils.Point;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
        value = "DMI_RANDOM_USED_ONLY_ONCE",
        justification = "Random objects are used appropriately in different contexts")
public class Renderer {
    private static final Logger LOGGER = LogManager.getLogger(Renderer.class);
    private static final int REJECTED_STEP = 20;

    private final List<AffineParamConfig> AffineParamConfigs;
    private final List<Transformation> transformations;
    private final List<Double> transformationsWeights;
    private final Color[] affineColors;
    private final Random random;

    public Renderer(
            List<AffineParamConfig> AffineParamConfigs,
            List<Transformation> transformations,
            List<Double> transformationsWeights,
            int seed) {
        this.AffineParamConfigs = AffineParamConfigs;
        this.transformations = transformations;
        this.transformationsWeights = transformationsWeights;
        this.random = new Random(seed);
        this.affineColors = generateAffineColors(AffineParamConfigs.size(), seed);
    }

    private static Color[] generateAffineColors(int count, int seed) {
        Color[] colors = new Color[count];
        Random colorRandom = new Random(seed);

        for (int i = 0; i < count; i++) {
            colors[i] =
                    new Color(colorRandom.nextInt(0, 256), colorRandom.nextInt(0, 256), colorRandom.nextInt(0, 256));
        }
        return colors;
    }

    public Image render(int width, int height, int iterations, int symmetry, int numThreads) {
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<Image>> futures = new ArrayList<>();

        final double aspect = (double) width / height;
        final double xMin = -aspect;
        final double xMax = aspect;
        final double yMin = -1.0;
        final double yMax = 1.0;

        LOGGER.info("Start generate image with {} iterations and {} threads", iterations, numThreads);

        int chunkSize = iterations / numThreads;

        for (int thread = 0; thread < numThreads; thread++) {
            int startIter = thread * chunkSize;
            int endIter = (thread == numThreads - 1) ? iterations : (thread + 1) * chunkSize;

            futures.add(executorService.submit(() -> {
                Image partialImage = Image.create(width, height);

                Point current = new Point(random.nextDouble(-aspect, aspect), random.nextDouble(yMin, yMax));

                for (int step = startIter; step <= endIter; step++) {
                    int affineId = random.nextInt(AffineParamConfigs.size());
                    current = AffineParamConfigs.get(affineId).transform(current);

                    double xSum = 0;
                    double ySum = 0;

                    for (int i = 0; i < transformations.size(); i++) {
                        Point v = transformations.get(i).transform(current);
                        xSum += v.x() * transformationsWeights.get(i);
                        ySum += v.y() * transformationsWeights.get(i);
                    }

                    current = new Point(xSum, ySum);

                    if (step - startIter <= REJECTED_STEP) {
                        continue;
                    }

                    if (symmetry < 1) {
                        throw new IllegalArgumentException("Symmetry must be at least 1");
                    }

                    double rotStep = 2 * Math.PI / symmetry;

                    for (int s = 0; s < symmetry; s++) {
                        double theta = s * rotStep;
                        double xRot = current.x() * Math.cos(theta) - current.y() * Math.sin(theta);
                        double yRot = current.x() * Math.sin(theta) + current.y() * Math.cos(theta);

                        if (xRot >= xMin && xRot <= xMax && yRot >= yMin && yRot <= yMax) {
                            int xPixel = (int) ((xRot - xMin) / (xMax - xMin) * partialImage.width());
                            int yPixel = (int) ((yRot - yMin) / (yMax - yMin) * partialImage.height());

                            if (partialImage.inBounds(xPixel, yPixel)) {
                                Color color = affineColors[affineId];

                                synchronized (partialImage.pixel(xPixel, yPixel)) {
                                    partialImage
                                            .pixel(xPixel, yPixel)
                                            .addHit(color.getRed(), color.getGreen(), color.getBlue());
                                }
                            }
                        }
                    }
                }

                return partialImage;
            }));
        }

        Image image = Image.create(width, height);

        for (Future<Image> partialImageFuture : futures) {
            try {
                Image partialImage = partialImageFuture.get();

                mergeImages(image, partialImage);
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.warn("Error while giving result from thread: {}", e.getMessage());
            }
        }

        executorService.shutdown();

        LOGGER.info("Image generated successfully");

        return image;
    }

    private static int mergeColors(int firstCount, int firstColor, int secondCount, int secondColor) {
        double firstPercent = firstCount / (double) (firstCount + secondCount);
        double secondPercent = secondCount / (double) (firstCount + secondCount);

        return (int) (firstPercent * firstColor + secondPercent * secondColor);
    }

    private static void mergeImages(Image to, Image from) {
        if (to.width() != from.width() || to.height() != from.height()) {
            throw new IllegalArgumentException("Images have unequal width or height");
        }

        for (int x = 0; x < to.width(); x++) {
            for (int y = 0; y < to.height(); y++) {
                Pixel pixel = to.pixel(x, y);
                Pixel fromPixel = from.pixel(x, y);

                pixel.setR(mergeColors(pixel.getHitCount(), pixel.getR(), fromPixel.getHitCount(), fromPixel.getR()));
                pixel.setG(mergeColors(pixel.getHitCount(), pixel.getG(), fromPixel.getHitCount(), fromPixel.getG()));
                pixel.setB(mergeColors(pixel.getHitCount(), pixel.getB(), fromPixel.getHitCount(), fromPixel.getB()));
            }
        }
    }

    public static void saveImage(Image fractalImage, String outputPath) throws IOException {
        BufferedImage image =
                new BufferedImage(fractalImage.width(), fractalImage.height(), BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < fractalImage.width(); x++) {
            for (int y = 0; y < fractalImage.height(); y++) {
                Pixel pixel = fractalImage.pixel(x, y);
                int rgbColor = (pixel.getR() << 16) | (pixel.getG() << 8) | pixel.getB();

                image.setRGB(x, y, rgbColor);
            }
        }

        Path outputFilePath = Path.of(outputPath);
        File outputFile = outputFilePath.toFile();

        ImageIO.write(image, "png", outputFile);

        LOGGER.info("Image saved successfully to {}", outputPath);
    }
}

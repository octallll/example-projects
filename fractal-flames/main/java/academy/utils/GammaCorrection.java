package academy.utils;

import academy.model.Image;
import academy.model.Pixel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GammaCorrection {
    private static final Logger LOGGER = LogManager.getLogger(GammaCorrection.class);

    public static void gammaProcess(Image image, double gamma) {
        LOGGER.info("Start post process with gamma correction {}", gamma);

        double maxNormal = 0;

        for (int x = 0; x < image.width(); x++) {
            for (int y = 0; y < image.height(); y++) {
                if (image.pixel(x, y).getHitCount() > 0) {
                    double normal = Math.log10(image.pixel(x, y).getHitCount());
                    maxNormal = Math.max(maxNormal, normal);
                }
            }
        }

        for (int x = 0; x < image.width(); x++) {
            for (int y = 0; y < image.height(); y++) {
                if (image.pixel(x, y).getHitCount() > 0) {
                    double normal = Math.log10(image.pixel(x, y).getHitCount());

                    double factor = Math.pow(normal / maxNormal, 1 / gamma);

                    Pixel pixel = image.pixel(x, y);
                    pixel.setR((int) (pixel.getR() * factor));
                    pixel.setG((int) (pixel.getG() * factor));
                    pixel.setB((int) (pixel.getB() * factor));
                }
            }
        }

        LOGGER.info("Post process with gamma end successfully");
    }
}

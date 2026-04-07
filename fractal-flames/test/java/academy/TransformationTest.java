package academy;

import static org.junit.jupiter.api.Assertions.*;

import academy.config.AffineParamConfig;
import academy.transforms.ExTransformation;
import academy.transforms.HandkerchiefTransformation;
import academy.transforms.LinearTransformation;
import academy.transforms.SphericalTransformation;
import academy.transforms.SwirlTransformation;
import academy.utils.Point;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TransformationTest {
    private static final double EPSILON = 1e-10;

    @Test
    @DisplayName("Linear transform works correct")
    void testLinearTransformation() {
        LinearTransformation transformation = new LinearTransformation();
        Point input = new Point(1.5, 2.5);
        Point output = transformation.transform(input);

        assertEquals(1.5, output.x(), EPSILON);
        assertEquals(2.5, output.y(), EPSILON);
    }

    @Test
    @DisplayName("Swirl transform works correct")
    void testSwirlTransformation() {
        SwirlTransformation transformation = new SwirlTransformation();
        Point input = new Point(1.0, 0.0);
        Point output = transformation.transform(input);

        double expectedX = Math.sin(1);
        double expectedY = Math.cos(1);

        assertEquals(expectedX, output.x(), EPSILON);
        assertEquals(expectedY, output.y(), EPSILON);
    }

    @Test
    @DisplayName("Spherical transform works correct")
    void testSphericalTransformation() {
        SphericalTransformation transformation = new SphericalTransformation();
        Point input = new Point(2.0, 3.0);
        Point output = transformation.transform(input);

        double k = 1.0 / (4.0 + 9.0);
        double expectedX = k * 2.0;
        double expectedY = k * 3.0;

        assertEquals(expectedX, output.x(), EPSILON);
        assertEquals(expectedY, output.y(), EPSILON);
    }

    @Test
    @DisplayName("Handkerchief transform works correct")
    void testHandkerchiefTransformation() {
        HandkerchiefTransformation transformation = new HandkerchiefTransformation();
        Point input = new Point(1.0, 1.0);
        Point output = transformation.transform(input);

        double r = Math.sqrt(2);
        double theta = Math.atan(1.0);
        double expectedX = r * Math.sin(theta + r);
        double expectedY = r * Math.cos(theta - r);

        assertEquals(expectedX, output.x(), EPSILON);
        assertEquals(expectedY, output.y(), EPSILON);
    }

    @Test
    @DisplayName("Ex transform works correct")
    void testExTransformation() {
        ExTransformation transformation = new ExTransformation();
        Point input = new Point(1.0, 1.0);
        Point output = transformation.transform(input);

        double r = Math.sqrt(2);
        double theta = Math.atan(1.0);
        double p0 = Math.sin(theta + r);
        double p1 = Math.cos(theta - r);
        double expectedX = r * (p0 * p0 * p0 + p1 * p1 * p1);
        double expectedY = r * (p0 * p0 * p0 - p1 * p1 * p1);

        assertEquals(expectedX, output.x(), EPSILON);
        assertEquals(expectedY, output.y(), EPSILON);
    }

    @Test
    @DisplayName("Affine transform works correct")
    void testAffineTransformation() {
        AffineParamConfig transformation = new AffineParamConfig(0.5, 0.2, 1.0, -0.1, 0.8, 2.0);

        Point input = new Point(3.0, 4.0);
        Point output = transformation.transform(input);

        double expectedX = 0.5 * 3.0 + 0.2 * 4.0 + 1.0;
        double expectedY = -0.1 * 3.0 + 0.8 * 4.0 + 2.0;

        assertEquals(expectedX, output.x(), EPSILON);
        assertEquals(expectedY, output.y(), EPSILON);
    }
}

package academy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import academy.config.Config;
import academy.utils.ConfigUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ConfigUtilsTest {
    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Json config from file uploads correctly")
    void testGetConfigFromJsonFileCompleteConfig() throws IOException {
        File tempFile = getTempFile(
                """
            {
                "size": {
                    "width": 800,
                    "height": 600
                },
                "iteration_count": 5000,
                "output_path": "test_output.png",
                "threads": 4,
                "seed": 12345,
                "functions": [
                    {
                        "name": "linear",
                        "weight": 1.0
                    },
                    {
                        "name": "swirl",
                        "weight": 0.5
                    }
                ],
                "affine_params": [
                    {
                        "a": 1.0,
                        "b": 0.0,
                        "c": 0.0,
                        "d": 0.0,
                        "e": 1.0,
                        "f": 0.0
                    },
                    {
                        "a": 0.5,
                        "b": 0.0,
                        "c": 0.5,
                        "d": 0.0,
                        "e": 0.5,
                        "f": 0.5
                    }
                ],
                "gamma_correction": true,
                "gamma": 2.2,
                "symmetry_level": 3
            }
            """,
                "config.json");

        Config config =
                ConfigUtils.getConfig(tempFile, null, null, null, null, null, null, null, null, null, null, null);

        assertNotNull(config);
        assertEquals(800, config.getWidth());
        assertEquals(600, config.getHeight());
        assertEquals(5000, config.getIterationCount());
        assertEquals("test_output.png", config.getOutputPath());
        assertEquals(4, config.getThreads());
        assertEquals(12345, config.getSeed());
        assertTrue(config.isGammaCorrection());
        assertEquals(2.2, config.getGamma(), 1e-10);
        assertEquals(3, config.getSymmetryLevel());

        assertNotNull(config.getFunctions());
        assertEquals(2, config.getFunctions().size());
        assertEquals("linear", config.getFunctions().get(0).getName());
        assertEquals(1.0, config.getFunctions().get(0).getWeight(), 1e-10);
        assertEquals("swirl", config.getFunctions().get(1).getName());
        assertEquals(0.5, config.getFunctions().get(1).getWeight(), 1e-10);

        String functionsStr = config.getFunctionsStr();
        assertEquals("linear:1.0,swirl:0.5", functionsStr);

        assertNotNull(config.getAffineParams());
        assertEquals(2, config.getAffineParams().size());
        assertEquals(1.0, config.getAffineParams().getFirst().getA(), 1e-10);
        assertEquals(0.0, config.getAffineParams().getFirst().getB(), 1e-10);
        assertEquals(0.0, config.getAffineParams().getFirst().getC(), 1e-10);
        assertEquals(0.0, config.getAffineParams().getFirst().getD(), 1e-10);
        assertEquals(1.0, config.getAffineParams().getFirst().getE(), 1e-10);
        assertEquals(0.0, config.getAffineParams().getFirst().getF(), 1e-10);

        String affineParamsStr = config.getAffineParamsStr();
        assertEquals("1.0,0.0,0.0,0.0,1.0,0.0/0.5,0.0,0.5,0.0,0.5,0.5", affineParamsStr);
    }

    @Test
    @DisplayName("Overriding of arguments works correctly")
    void testGetConfigCommandLineOverridesJson() throws IOException {
        String jsonContent =
                """
            {
                "width": 800,
                "height": 600,
                "iteration_count": 5000,
                "threads": 2
            }
            """;

        Path tempFilePath = Files.createTempFile(tempDir, "config", ".json");

        try (BufferedWriter writer = Files.newBufferedWriter(tempFilePath)) {
            writer.write(jsonContent);
        }

        Config config = ConfigUtils.getConfig(
                tempFilePath.toFile(), 1920, null, null, 10000, null, 8, null, null, null, null, null);

        assertEquals(1920, config.getWidth());
        assertEquals(600, config.getHeight());
        assertEquals(10000, config.getIterationCount());
        assertEquals(8, config.getThreads());
    }

    @Test
    @DisplayName("Default values sets correctly")
    void testGetConfigDefaultValues() throws IOException {
        Config config = ConfigUtils.getConfig(null, null, null, null, null, null, null, null, null, null, null, null);

        assertNotNull(config);
        assertNotNull(config.getOutputPath());
        assertTrue(config.getWidth() > 0);
        assertTrue(config.getHeight() > 0);
        assertTrue(config.getIterationCount() > 0);
        assertTrue(config.getThreads() > 0);
    }

    private File getTempFile(String jsonContent, String name) throws IOException {
        Path configPath = tempDir.resolve(name);
        Files.writeString(configPath, jsonContent);

        return configPath.toFile();
    }
}

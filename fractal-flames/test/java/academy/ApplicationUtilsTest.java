package academy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class ApplicationUtilsTest {
    @Test
    @DisplayName("Throws when invalid affine")
    void testInvalidAffineParameters() {
        String[] args = {
            "-ap", "1,2,3,4,5",
            "-f", "linear:1.0"
        };

        int exitCode = new CommandLine(new Application()).execute(args);
        assertEquals(2, exitCode);
    }

    @Test
    @DisplayName("Throws when invalid variation")
    void testInvalidVariationName() {
        String[] args = {
            "-ap", "1,2,3,4,5,6",
            "-f", "unknown:1.0"
        };

        int exitCode = new CommandLine(new Application()).execute(args);
        assertEquals(2, exitCode);
    }
}

package academy;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import org.junit.jupiter.api.Test;

public class CommandErrorsTest {
    @Test
    void testGenerateCommandWithNegativeDimensions() {
        Application.GenerateCommand command = new Application.GenerateCommand();
        command.width = -5;
        command.height = -3;
        command.algorithm = "dfs";

        assertThrows(IllegalArgumentException.class, command::run);
    }

    @Test
    void testGenerateCommandWithZeroDimensions() {
        Application.GenerateCommand command = new Application.GenerateCommand();
        command.width = 0;
        command.height = 10;
        command.algorithm = "dfs";

        assertThrows(IllegalArgumentException.class, command::run);
    }

    @Test
    void testGenerateCommandWithInvalidAlgorithm() {
        Application.GenerateCommand command = new Application.GenerateCommand();
        command.width = 10;
        command.height = 10;
        command.algorithm = "invalid_algorithm";

        assertThrows(IllegalArgumentException.class, command::run);
    }

    @Test
    void testSolveCommandWithNonExistentFile() {
        Application.SolveCommand command = new Application.SolveCommand();
        command.inputFile = new File("nonexistent_file.txt");
        command.start = "1,1";
        command.end = "3,3";
        command.algorithm = "dijkstra";

        assertThrows(RuntimeException.class, command::run);
    }
}

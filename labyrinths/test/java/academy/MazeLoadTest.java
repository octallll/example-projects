package academy;

import static academy.utils.MazeUtils.loadMazeFromFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import academy.maze.dto.CellType;
import academy.maze.dto.Maze;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class MazeLoadTest {
    @TempDir
    File tempDir;

    @Test
    @DisplayName("Maze load works correctly with all types of cells")
    void loadMazeFromFileShouldLoadAllCellTypes() throws IOException {
        String mazeContent = """
            #OC#
            # D#
            # X#
            ####
            """;

        File mazeFile = new File(tempDir, "test_maze.txt");
        Files.writeString(mazeFile.toPath(), mazeContent);

        Maze maze = loadMazeFromFile(mazeFile);

        assertEquals(CellType.WALL, maze.getAt(0, 0));
        assertEquals(CellType.START, maze.getAt(0, 1));
        assertEquals(CellType.COIN, maze.getAt(0, 2));
        assertEquals(CellType.WALL, maze.getAt(0, 3));

        assertEquals(CellType.WALL, maze.getAt(1, 0));
        assertEquals(CellType.PATH, maze.getAt(1, 1));
        assertEquals(CellType.DESERT, maze.getAt(1, 2));
        assertEquals(CellType.WALL, maze.getAt(1, 3));

        assertEquals(CellType.WALL, maze.getAt(2, 0));
        assertEquals(CellType.PATH, maze.getAt(2, 1));
        assertEquals(CellType.END, maze.getAt(2, 2));
        assertEquals(CellType.WALL, maze.getAt(2, 3));

        assertEquals(CellType.WALL, maze.getAt(3, 0));
        assertEquals(CellType.WALL, maze.getAt(3, 1));
        assertEquals(CellType.WALL, maze.getAt(3, 2));
        assertEquals(CellType.WALL, maze.getAt(3, 3));
    }

    @Test
    @DisplayName("Load maze throws with incorrect type of cell")
    void loadMazeFromFileShouldThrowExceptionForInvalidCharacter() throws IOException {
        String mazeContent = """
            #A#
            ###
            """;

        File mazeFile = new File(tempDir, "invalid_maze.txt");
        Files.writeString(mazeFile.toPath(), mazeContent);

        assertThrows(IllegalStateException.class, () -> loadMazeFromFile(mazeFile));
    }
}

package academy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import academy.maze.dto.CellType;
import academy.maze.dto.Maze;
import academy.utils.MazeUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MazeDisplayTest {
    private final Maze smallMaze = new Maze(
            new CellType[][] {
                {CellType.WALL, CellType.WALL, CellType.WALL},
                {CellType.PATH, CellType.COIN, CellType.WALL},
                {CellType.WALL, CellType.PATH, CellType.DESERT}
            },
            3,
            3);

    private final Maze mediumMaze = new Maze(
            new CellType[][] {
                {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL},
                {CellType.DESERT, CellType.PATH, CellType.PATH, CellType.PATH, CellType.WALL},
                {CellType.WALL, CellType.WALL, CellType.WALL, CellType.PATH, CellType.WALL},
                {CellType.WALL, CellType.COIN, CellType.PATH, CellType.PATH, CellType.PATH},
                {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL}
            },
            5,
            5);

    @Test
    @DisplayName("Unicode displays correctly on small mazes")
    public void unicodeSmallDisplayTest() {
        String mazeView = MazeUtils.mazeToString(smallMaze, true);

        String smallMazeUnicode = """
            ███
             ○█
            █ ░
            """
                .replace("\n", System.lineSeparator());

        assertEquals(smallMazeUnicode, mazeView);
    }

    @Test
    @DisplayName("ASCII displays correctly on small mazes")
    public void asciiSmallDisplayTest() {
        String mazeView = MazeUtils.mazeToString(smallMaze, false);

        String smallMazeAscii = """
            ###
             $#
            # D
            """
                .replace("\n", System.lineSeparator());

        assertEquals(smallMazeAscii, mazeView);
    }

    @Test
    @DisplayName("Unicode displays correctly on medium mazes")
    public void unicodeMediumDisplayTest() {
        String mazeView = MazeUtils.mazeToString(mediumMaze, true);

        String mediumMazeUnicode =
                """
            █████
            ░   █
            ███ █
            █○  \s
            █████
            """
                        .replace("\n", System.lineSeparator());

        assertEquals(mediumMazeUnicode, mazeView);
    }

    @Test
    @DisplayName("ASCII displays correctly on medium mazes")
    public void asciiMediumDisplayTest() {
        String mazeView = MazeUtils.mazeToString(mediumMaze, false);

        String mediumMazeAscii =
                """
            #####
            D   #
            ### #
            #$  \s
            #####
            """
                        .replace("\n", System.lineSeparator());

        assertEquals(mediumMazeAscii, mazeView);
    }
}

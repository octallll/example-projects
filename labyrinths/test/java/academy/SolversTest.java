package academy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import academy.maze.Solver;
import academy.maze.dto.CellType;
import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;
import academy.maze.solvers.AStarSolver;
import academy.maze.solvers.DijkstraSolver;
import academy.maze.solvers.FordBellmanSolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SolversTest {
    private final Maze smallMaze = new Maze(
            new CellType[][] {
                {CellType.WALL, CellType.WALL, CellType.WALL},
                {CellType.PATH, CellType.COIN, CellType.WALL},
                {CellType.WALL, CellType.PATH, CellType.DESERT}
            },
            3,
            3);

    private final Path expectedSmallPath =
            new Path(new Point[] {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 2)});

    private final Point smallStart = new Point(0, 1);
    private final Point smallEnd = new Point(2, 2);

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

    private final Path expectedMediumPath = new Path(new Point[] {
        new Point(0, 1),
        new Point(1, 1),
        new Point(1, 2),
        new Point(1, 3),
        new Point(2, 3),
        new Point(3, 3),
        new Point(3, 4)
    });

    private final Point mediumStart = new Point(0, 1);
    private final Point mediumEnd = new Point(3, 4);

    private final Maze multiplePathsMaze = new Maze(
            new CellType[][] {
                {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL},
                {CellType.PATH, CellType.PATH, CellType.PATH, CellType.PATH, CellType.WALL},
                {CellType.WALL, CellType.WALL, CellType.WALL, CellType.PATH, CellType.WALL},
                {CellType.WALL, CellType.PATH, CellType.COIN, CellType.PATH, CellType.DESERT},
                {CellType.WALL, CellType.WALL, CellType.WALL, CellType.DESERT, CellType.WALL}
            },
            5,
            5);

    private final Path[] expectedMultiplePaths = new Path[] {
        new Path(new Point[] {
            new Point(0, 1),
            new Point(1, 1),
            new Point(2, 1),
            new Point(3, 1),
            new Point(3, 2),
            new Point(3, 3),
            new Point(4, 3)
        }),
        new Path(new Point[] {
            new Point(0, 1),
            new Point(1, 1),
            new Point(1, 2),
            new Point(1, 3),
            new Point(2, 3),
            new Point(3, 3),
            new Point(4, 3)
        })
    };

    private final Point multipleStart = new Point(0, 1);
    private final Point multipleEnd = new Point(4, 3);

    @Test
    @DisplayName("Dijkstra solver works correctly on small mazes")
    public void testSmallMazeDijkstra() {
        assertTrue(
                testMazeSolve(new DijkstraSolver(), smallMaze, smallStart, smallEnd, new Path[] {expectedSmallPath}));
    }

    @Test
    @DisplayName("A* solver works correctly on small mazes")
    public void testSmallMazeAStar() {
        assertTrue(testMazeSolve(new AStarSolver(), smallMaze, smallStart, smallEnd, new Path[] {expectedSmallPath}));
    }

    @Test
    @DisplayName("Ford-Bellman solver works correctly on small mazes")
    public void testSmallMazeFordBellman() {
        assertTrue(testMazeSolve(
                new FordBellmanSolver(), smallMaze, smallStart, smallEnd, new Path[] {expectedSmallPath}));
    }

    @Test
    @DisplayName("Dijkstra solver works correctly on medium mazes")
    public void testMediumMazeDijkstra() {
        assertTrue(testMazeSolve(
                new DijkstraSolver(), mediumMaze, mediumStart, mediumEnd, new Path[] {expectedMediumPath}));
    }

    @Test
    @DisplayName("A* solver works correctly on medium mazes")
    public void testMediumMazeAStar() {
        assertTrue(
                testMazeSolve(new AStarSolver(), mediumMaze, mediumStart, mediumEnd, new Path[] {expectedMediumPath}));
    }

    @Test
    @DisplayName("Ford-Bellman solver works correctly on medium mazes")
    public void testMediumMazeFordBellman() {
        assertTrue(testMazeSolve(
                new FordBellmanSolver(), smallMaze, smallStart, smallEnd, new Path[] {expectedSmallPath}));
    }

    @Test
    @DisplayName("Dijkstra solver works correctly on multiple paths mazes")
    public void testMultipleMazeDijkstra() {
        assertTrue(testMazeSolve(
                new DijkstraSolver(), multiplePathsMaze, multipleStart, multipleEnd, expectedMultiplePaths));
    }

    @Test
    @DisplayName("A* solver works correctly on multiple paths mazes")
    public void testMultipleMazeAStar() {
        assertTrue(
                testMazeSolve(new AStarSolver(), multiplePathsMaze, multipleStart, multipleEnd, expectedMultiplePaths));
    }

    @Test
    @DisplayName("Ford-Bellman solver works correctly on multiple paths mazes")
    public void testMultipleMazeFordBellman() {
        assertTrue(testMazeSolve(
                new FordBellmanSolver(), multiplePathsMaze, multipleStart, multipleEnd, expectedMultiplePaths));
    }

    private boolean testMazeSolve(Solver solver, Maze maze, Point start, Point end, Path[] expectedPaths) {
        Path path = solver.solve(maze, start, end);

        for (Path possiblePath : expectedPaths) {
            if (possiblePath.points().length == path.points().length) {
                boolean equals = true;

                for (int i = 0; i < path.points().length; i++) {
                    if (!path.points()[i].equals(possiblePath.points()[i])) {
                        equals = false;
                        break;
                    }
                }

                if (equals) {
                    return true;
                }
            }
        }

        return false;
    }
}

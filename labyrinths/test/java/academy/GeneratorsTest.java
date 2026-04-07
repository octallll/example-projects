package academy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import academy.maze.Generator;
import academy.maze.dto.CellType;
import academy.maze.dto.Maze;
import academy.maze.dto.Point;
import academy.maze.generators.BfsGenerator;
import academy.maze.generators.DfsGenerator;
import academy.maze.generators.PrimeGenerator;
import java.util.ArrayDeque;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GeneratorsTest {
    private final Random random = new Random();

    @Test
    @DisplayName("Maze generated from Prime generator connected")
    public void mazeConnectionTestPrimeGenerator() {
        mazeConnectionTest(new PrimeGenerator());
    }

    @Test
    @DisplayName("Maze generated from DFS generator connected")
    public void mazeConnectionTestDFSGenerator() {
        mazeConnectionTest(new DfsGenerator());
    }

    @Test
    @DisplayName("Maze generated from BFS generator connected")
    public void mazeConnectionTestBFSGenerator() {
        mazeConnectionTest(new BfsGenerator());
    }

    private void mazeConnectionTest(Generator generator) {
        final int NUMBER_OF_ITERATIONS = 5;

        for (int iteration = 0; iteration < NUMBER_OF_ITERATIONS; iteration++) {
            int width = (Math.abs(random.nextInt()) % 5 + 2) * 2 + 1;
            int height = (Math.abs(random.nextInt()) % 5 + 2) * 2 + 1;

            Maze maze = generator.generate(width, height);

            int startX = -1;
            int startY = -1;
            int needUsed = 0;

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (maze.getAt(i, j) != CellType.WALL) {
                        startX = i;
                        startY = j;
                        needUsed++;
                    }
                }
            }

            ArrayDeque<Point> stack = new ArrayDeque<>();
            boolean[][] used = new boolean[maze.width()][maze.height()];

            for (int i = 0; i < maze.width(); i++) {
                for (int j = 0; j < maze.height(); j++) {
                    used[i][j] = false;
                }
            }

            stack.add(new Point(startX, startY));
            used[startX][startY] = true;

            final int COUNT_OF_DIRECTIONS = 4;
            final int[] dx = {0, 0, 1, -1};
            final int[] dy = {1, -1, 0, 0};

            int usedCount = 1;

            while (!stack.isEmpty()) {
                Point current = stack.pollLast();

                for (int dir = 0; dir < COUNT_OF_DIRECTIONS; dir++) {
                    Point next = new Point(current.x() + dx[dir], current.y() + dy[dir]);

                    if (next.x() >= 0
                            && next.y() >= 0
                            && next.x() < maze.width()
                            && next.y() < maze.height()
                            && maze.isClearAt(next.x(), next.y())
                            && !used[next.x()][next.y()]) {
                        used[next.x()][next.y()] = true;
                        stack.add(next);
                        usedCount++;
                    }
                }
            }

            assertEquals(usedCount, needUsed);
        }
    }

    @Test
    @DisplayName("DFS generator generates walls on the boards")
    public void testWallDFS() {
        mazeWallTest(new DfsGenerator());
    }

    @Test
    @DisplayName("BFS generator generates walls on the boards")
    public void testWallBFS() {
        mazeWallTest(new BfsGenerator());
    }

    @Test
    @DisplayName("Prime generator generates walls on the boards")
    public void testWallPrime() {
        mazeWallTest(new PrimeGenerator());
    }

    private void mazeWallTest(Generator generator) {
        final int NUMBER_OF_ITERATIONS = 5;

        for (int iteration = 0; iteration < NUMBER_OF_ITERATIONS; iteration++) {
            int width = (Math.abs(random.nextInt()) % 5 + 2) * 2 + 1;
            int height = (Math.abs(random.nextInt()) % 5 + 2) * 2 + 1;

            Maze maze = generator.generate(width, height);

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (i == 0 || j == 0 || i == width - 1 || j == height - 1) {
                        assertSame(CellType.WALL, maze.getAt(i, j));
                    }
                }
            }
        }
    }
}

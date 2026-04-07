package academy.maze.generators;

import academy.maze.Generator;
import academy.maze.dto.CellType;
import academy.maze.dto.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public abstract class AbstractGenerator implements Generator {
    protected Random random = new Random();
    protected static final int COUNT_OF_DIRECTIONS = 4;
    static final int[] dx = {2, -2, 0, 0};
    static final int[] dy = {0, 0, 2, -2};
    static final int[] dxWall = {1, -1, 0, 0};
    static final int[] dyWall = {0, 0, 1, -1};

    /**
     * Initializes the maze grid with walls and sets up the starting point. Fills the entire grid with walls, and adds
     * neighboring cells to the queue for processing.
     *
     * @param cells the 2D array representing the maze cells
     * @param queue the list of points to be processed
     * @param width the width of the maze
     * @param height the height of the maze
     */
    protected void fillInitialize(CellType[][] cells, ArrayList<Point> queue, int width, int height) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                cells[i][j] = CellType.WALL;
            }
        }

        int startX = 1;
        int startY = 1;
        cells[startX][startY] = CellType.PATH;
        addQueue(startX, startY, width, height, cells, queue);
    }

    /**
     * Validates that the maze dimensions are appropriate for generation. Width and height must be positive odd numbers.
     *
     * @param width the width to validate
     * @param height the height to validate
     * @throws IllegalArgumentException if dimensions are invalid
     */
    protected void checkBoards(int width, int height) {
        if (width <= 2 || height <= 2) {
            throw new IllegalArgumentException("Incorrect width or height: " + width + ", " + height);
        }
    }

    /**
     * Processes a point from the queue by attempting to create paths in random directions. For each direction, checks
     * if moving 2 steps creates a valid path, and if so, converts the intermediate wall to either a path, coin (10%
     * chance), or desert (10% chance).
     *
     * @param cells the 2D array representing the maze cells
     * @param out the current point being processed
     * @param queue the list of points to be processed
     * @param width the width of the maze
     * @param height the height of the maze
     */
    protected void walkFromPoint(CellType[][] cells, Point out, ArrayList<Point> queue, int width, int height) {
        int x = out.x();
        int y = out.y();

        ArrayList<Integer> dirs = new ArrayList<>();
        for (int i = 0; i < COUNT_OF_DIRECTIONS; i++) {
            dirs.add(i);
        }
        Collections.shuffle(dirs, random);

        for (int dir : dirs) {
            int next_x = x + dx[dir];
            int next_y = y + dy[dir];

            if (isFieldCorrect(next_x, next_y, width, height) && cells[next_x][next_y] != CellType.WALL) {
                cells[x][y] = CellType.PATH;

                cells[x + dxWall[dir]][y + dyWall[dir]] = switch (random.nextInt() % 10) {
                    case 0 -> CellType.COIN;
                    case 1 -> CellType.DESERT;
                    default -> CellType.PATH;
                };

                addQueue(x, y, width, height, cells, queue);

                break;
            }
        }
    }

    /**
     * Adds valid neighboring wall cells to the processing queue. Checks all four directions from the current position
     * and adds any valid wall cells that are within bounds and haven't been processed.
     *
     * @param x the x-coordinate of the current position
     * @param y the y-coordinate of the current position
     * @param width the width of the maze
     * @param height the height of the maze
     * @param cells the 2D array representing the maze cells
     * @param queue the list of points to be processed
     */
    protected void addQueue(int x, int y, int width, int height, CellType[][] cells, ArrayList<Point> queue) {
        for (int dir = 0; dir < COUNT_OF_DIRECTIONS; dir++) {
            int nextX = x + dx[dir];
            int nextY = y + dy[dir];

            if (isFieldCorrect(nextX, nextY, width, height) && cells[nextX][nextY] == CellType.WALL) {
                queue.add(new Point(nextX, nextY));
            }
        }
    }

    /**
     * Checks if a coordinate is within the bounds of the maze.
     *
     * @param x the x-coordinate to check
     * @param y the y-coordinate to check
     * @param width the width of the maze
     * @param height the height of the maze
     * @return true if the coordinate is within bounds, false otherwise
     */
    protected boolean isFieldCorrect(int x, int y, int width, int height) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}

package academy.maze.solvers;

import academy.maze.Solver;
import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSolver implements Solver {
    protected int INF = Integer.MAX_VALUE;
    protected Point DEFAULT_POINT = new Point(-1, -1);

    protected int COUNT_OF_DIRECTIONS = 4;

    protected int[] dx = {0, 0, 1, -1};
    protected int[] dy = {1, -1, 0, 0};

    /**
     * Initializes the dist and path grid with default values. and set the dist from the start point to 0.
     *
     * @param dist the 2D array representing the distance from start point
     * @param path the 2D array representing the parent of points in the founded path
     * @param width the width of the maze
     * @param height the height of the maze
     * @param start the start point in the path
     */
    protected void fillInitialize(int[][] dist, Point[][] path, int width, int height, Point start) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                dist[i][j] = INF;
                path[i][j] = DEFAULT_POINT;
            }
        }

        dist[start.x()][start.y()] = 0;
    }

    /**
     * Find the result path from 2D array of parent on the path
     *
     * @param path the 2D array representing the parent of points in the founded path
     * @param end the end point in the path
     * @return result path
     */
    protected Path getPath(Point[][] path, Point end) {
        ArrayList<Point> pathPoints = new ArrayList<>();

        Point current = end;

        while (path[current.x()][current.y()] != DEFAULT_POINT) {
            pathPoints.add(current);
            current = path[current.x()][current.y()];
        }
        pathPoints.add(current);

        List<Point> reversed = pathPoints.reversed();
        Point[] toPath = new Point[reversed.size()];
        for (int i = 0; i < reversed.size(); i++) {
            toPath[i] = reversed.get(i);
        }

        return new Path(toPath);
    }

    /**
     * Checks if a coordinate is within the bounds of the maze.
     *
     * @param nextX the x-coordinate to check
     * @param nextY the y-coordinate to check
     * @param maze the maze
     * @return true if the coordinate is within bounds and not represent a wall, false otherwise
     */
    protected boolean isPointCorrect(int nextX, int nextY, Maze maze) {
        return nextX >= 0
                && nextY >= 0
                && nextX < maze.width()
                && nextY < maze.height()
                && maze.isClearAt(nextX, nextY);
    }
}

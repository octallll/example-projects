package academy.maze.solvers;

import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;
import academy.utils.Pair;
import java.util.ArrayList;

public class FordBellmanSolver extends AbstractSolver {
    @Override
    public Path solve(Maze maze, Point start, Point end) {
        ArrayList<Pair<Point, Point>> edges = new ArrayList<>();

        for (int x = 0; x < maze.width(); x++) {
            for (int y = 0; y < maze.height(); y++) {
                for (int dir = 0; dir < COUNT_OF_DIRECTIONS; dir++) {
                    int nextX = x + dx[dir];
                    int nextY = y + dy[dir];

                    if (isPointCorrect(nextX, nextY, maze)) {
                        edges.add(new Pair<>(new Point(x, y), new Point(nextX, nextY)));
                    }
                }
            }
        }

        int[][] dist = new int[maze.width()][maze.height()];
        Point[][] path = new Point[maze.width()][maze.height()];

        fillInitialize(dist, path, maze.width(), maze.height(), start);

        for (int x = 0; x < maze.width(); x++) {
            for (int y = 0; y < maze.height(); y++) {
                for (Pair<Point, Point> edge : edges) {
                    Point from = edge.first();
                    Point to = edge.second();
                    int cost = maze.getAt(to.x(), to.y()).cost;

                    if (dist[from.x()][from.y()] != INF && dist[from.x()][from.y()] + cost < dist[to.x()][to.y()]) {
                        dist[to.x()][to.y()] = dist[from.x()][from.y()] + cost;
                        path[to.x()][to.y()] = from;
                    }
                }
            }
        }

        return getPath(path, end);
    }
}

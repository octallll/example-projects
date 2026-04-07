package academy.maze.solvers;

import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;
import academy.utils.Pair;
import java.util.PriorityQueue;

public class DijkstraSolver extends AbstractSolver {
    @Override
    public Path solve(Maze maze, Point start, Point end) {
        PriorityQueue<Pair<Integer, Point>> queue = new PriorityQueue<>();

        queue.add(new Pair<>(0, start));

        int[][] dist = new int[maze.width()][maze.height()];
        Point[][] path = new Point[maze.width()][maze.height()];

        fillInitialize(dist, path, maze.width(), maze.height(), start);

        while (!queue.isEmpty()) {
            Pair<Integer, Point> current = queue.poll();

            int x = current.second().x();
            int y = current.second().y();

            for (int dir = 0; dir < COUNT_OF_DIRECTIONS; dir++) {
                int nextX = x + dx[dir];
                int nextY = y + dy[dir];

                if (isPointCorrect(nextX, nextY, maze)) {
                    int cost = maze.getAt(nextX, nextY).cost;

                    if (dist[nextX][nextY] > dist[x][y] + cost) {
                        queue.remove(new Pair<>(dist[nextX][nextY], new Point(nextX, nextY)));
                        dist[nextX][nextY] = dist[x][y] + cost;
                        path[nextX][nextY] = new Point(x, y);
                        queue.add(new Pair<>(dist[nextX][nextY], new Point(nextX, nextY)));
                    }
                }
            }
        }

        if (dist[end.x()][end.y()] == INF) {
            return new Path(new Point[0]);
        }

        return getPath(path, end);
    }
}

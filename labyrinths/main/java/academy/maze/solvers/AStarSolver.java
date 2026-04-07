package academy.maze.solvers;

import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;
import academy.utils.Pair;
import java.util.PriorityQueue;

public class AStarSolver extends AbstractSolver {
    @Override
    public Path solve(Maze maze, Point start, Point end) {
        PriorityQueue<Pair<Integer, Point>> queue = new PriorityQueue<>();
        queue.add(new Pair<>(h(start, end), start));

        int[][] dist = new int[maze.width()][maze.height()];
        Point[][] path = new Point[maze.width()][maze.height()];
        boolean[][] visited = new boolean[maze.width()][maze.height()];

        fillInitialize(dist, path, maze.width(), maze.height(), start);

        dist[start.x()][start.y()] = 0;

        while (!queue.isEmpty()) {
            Point out = queue.poll().second();
            int x = out.x();
            int y = out.y();

            if (visited[x][y]) {
                continue;
            }

            visited[x][y] = true;

            if (x == end.x() && y == end.y()) {
                break;
            }

            for (int dir = 0; dir < COUNT_OF_DIRECTIONS; dir++) {
                int nextX = x + dx[dir];
                int nextY = y + dy[dir];

                if (isPointCorrect(nextX, nextY, maze)) {
                    int cost = dist[x][y] + maze.getAt(nextX, nextY).cost;

                    if (cost < dist[nextX][nextY]) {
                        dist[nextX][nextY] = cost;
                        path[nextX][nextY] = new Point(x, y);

                        int fScore = cost + h(new Point(nextX, nextY), end);
                        queue.add(new Pair<>(fScore, new Point(nextX, nextY)));
                    }
                }
            }
        }

        if (!visited[end.x()][end.y()]) {
            return new Path(new Point[0]);
        }

        return getPath(path, end);
    }

    private int h(Point from, Point to) {
        return Math.abs(from.x() - to.x()) + Math.abs(from.y() - to.y());
    }
}

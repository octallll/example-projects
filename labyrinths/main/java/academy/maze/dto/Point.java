package academy.maze.dto;

/**
 * Координаты точки
 *
 * @param x
 * @param y
 */
public record Point(int x, int y) implements Comparable<Point> {
    @Override
    public int compareTo(Point point) {
        return x == point.x ? Integer.compare(y, point.y) : Integer.compare(x, point.x);
    }
}

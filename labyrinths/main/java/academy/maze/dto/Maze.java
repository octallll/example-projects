package academy.maze.dto;

/**
 * Лабиринт.
 *
 * @param cells Массив ячеек лабиринта.
 */
public record Maze(CellType[][] cells, int width, int height) {
    public boolean isClearAt(int x, int y) {
        return cells[x][y] != CellType.WALL;
    }

    public CellType getAt(int x, int y) {
        return cells[x][y];
    }
}

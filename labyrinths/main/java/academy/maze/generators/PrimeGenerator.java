package academy.maze.generators;

import academy.maze.dto.CellType;
import academy.maze.dto.Maze;
import academy.maze.dto.Point;
import java.util.ArrayList;

public class PrimeGenerator extends AbstractGenerator {
    @Override
    public Maze generate(int width, int height) {
        checkBoards(width, height);

        CellType[][] cells = new CellType[width][height];
        ArrayList<Point> queue = new ArrayList<>();
        fillInitialize(cells, queue, width, height);

        while (!queue.isEmpty()) {
            Point out = queue.remove(random.nextInt(queue.size()));

            walkFromPoint(cells, out, queue, width, height);
        }

        return new Maze(cells, width, height);
    }
}

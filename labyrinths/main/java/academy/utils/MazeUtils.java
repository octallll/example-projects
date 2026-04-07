package academy.utils;

import academy.maze.dto.CellType;
import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Utility class for maze operations including visualization, file I/O, and point parsing. Provides methods for
 * converting mazes to string representations with optional path visualization.
 */
public class MazeUtils {
    /**
     * Merge maze with path to string
     *
     * @param maze maze to represent into string
     * @param path path to represent into string
     * @param startPoint start point of path
     * @param endPoint end point of path
     * @param useUnicode use unicode displaying if available
     * @return string representation of the maze with the path
     */
    public static String mazeWithPathToString(
            Maze maze, Path path, Point startPoint, Point endPoint, boolean useUnicode) {
        char[][] display = new char[maze.width()][maze.height()];

        for (int x = 0; x < maze.width(); x++) {
            for (int y = 0; y < maze.height(); y++) {
                if (new Point(x, y).equals(startPoint)) {
                    display[x][y] = getCellSymbol(CellType.START, useUnicode);
                } else if (new Point(x, y).equals(endPoint)) {
                    display[x][y] = getCellSymbol(CellType.END, useUnicode);
                } else {
                    display[x][y] = getCellSymbol(maze.getAt(x, y), useUnicode);
                }
            }
        }

        for (Point point : path.points()) {
            if (!point.equals(startPoint) && !point.equals(endPoint)) {
                display[point.x()][point.y()] = useUnicode ? '●' : '.';
            }
        }

        StringBuilder mazeView = new StringBuilder();

        for (int x = 0; x < maze.width(); x++) {
            for (int y = 0; y < maze.height(); y++) {
                mazeView.append(display[x][y]);
            }

            mazeView.append(System.lineSeparator());
        }

        return mazeView.toString();
    }

    /**
     * @param type type of the cell
     * @param useUnicode used unicode symbols if available
     * @return char representation of the cell
     */
    private static char getCellSymbol(CellType type, boolean useUnicode) {
        if (useUnicode) {
            return switch (type) {
                case WALL -> '█';
                case PATH -> ' ';
                case COIN -> '○';
                case DESERT -> '░';
                case START -> 'O';
                case END -> 'X';
            };
        } else {
            return switch (type) {
                case WALL -> '#';
                case PATH -> ' ';
                case COIN -> '$';
                case DESERT -> 'D';
                case START -> 'O';
                case END -> 'X';
            };
        }
    }

    /**
     * @param maze the maze
     * @param useUnicode used unicode symbols if available
     * @return string representation of the maze
     */
    public static String mazeToString(Maze maze, boolean useUnicode) {
        StringBuilder mazeView = new StringBuilder();

        for (int x = 0; x < maze.width(); x++) {
            for (int y = 0; y < maze.height(); y++) {
                mazeView.append(getCellSymbol(maze.getAt(x, y), useUnicode));
            }

            mazeView.append(System.lineSeparator());
        }

        return mazeView.toString();
    }

    /**
     * Parsing maze from the input file
     *
     * @param file input file with the maze
     * @return pared maze from file
     * @throws RuntimeException if file contains unknown cells
     */
    public static Maze loadMazeFromFile(File file) {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            ArrayList<String> mazeView = new ArrayList<>(reader.lines().toList());

            int width = mazeView.size();
            int height = mazeView.getFirst().length();

            CellType[][] cells = new CellType[width][height];

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    cells[x][y] = switch (mazeView.get(x).charAt(y)) {
                        case '#' -> CellType.WALL;
                        case ' ' -> CellType.PATH;
                        case 'O' -> CellType.START;
                        case 'X' -> CellType.END;
                        case 'C' -> CellType.COIN;
                        case 'D' -> CellType.DESERT;
                        default ->
                            throw new IllegalStateException("Unexpected type in maze: "
                                    + mazeView.get(x).charAt(y));
                    };
                }
            }

            return new Maze(cells, width, height);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading maze from file: " + e.getMessage());
        }
    }

    /**
     * Parse point from string view
     *
     * @param pointView string view of point
     * @param width width of the maze
     * @param height height of the maze
     * @return point representation from string
     */
    public static Point parsePoint(String pointView, int width, int height) {
        String[] parts = pointView.split(",");

        if (parts.length != 2) {
            System.err.println("Invalid point format: " + pointView + ", expected format: x,y");
            System.exit(-1);
        }

        try {
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);

            if (x >= 1 && y >= 1 && x <= width && y <= height) {
                return new Point(x, y);
            } else {
                System.err.println("Invalid point format: " + pointView + ", expected format: x,y");
                System.exit(-1);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid point format: " + pointView + ", expected format: x,y");
            System.exit(-1);
        }

        return null;
    }
}

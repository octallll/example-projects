package academy;

import static academy.utils.MazeUtils.loadMazeFromFile;
import static academy.utils.MazeUtils.mazeToString;
import static academy.utils.MazeUtils.mazeWithPathToString;
import static academy.utils.MazeUtils.parsePoint;

import academy.maze.Generator;
import academy.maze.Solver;
import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;
import academy.maze.generators.BfsGenerator;
import academy.maze.generators.DfsGenerator;
import academy.maze.generators.PrimeGenerator;
import academy.maze.solvers.AStarSolver;
import academy.maze.solvers.DijkstraSolver;
import academy.maze.solvers.FordBellmanSolver;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "maze-app",
        version = "1.0",
        mixinStandardHelpOptions = true,
        description = "Maze generator and solver CLI application.",
        subcommands = {Application.GenerateCommand.class, Application.SolveCommand.class})
public class Application {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    @Command(name = "generate", description = "Generate a maze with specified algorithm and dimensions.")
    static class GenerateCommand implements Runnable {
        @Option(
                names = {"--algorithm", "-a"},
                description = "Algorithm for maze generation (dfs, prim, bfs)",
                defaultValue = "dfs")
        String algorithm;

        @Option(
                names = {"--width", "-w"},
                description = "Width of the maze",
                required = true)
        int width;

        @Option(
                names = {"--height", "-h"},
                description = "Height of the maze",
                required = true)
        int height;

        @Option(
                names = {"--output", "-o"},
                description = "Output file for the maze")
        File outputFile;

        @Option(
                names = {"--unicode"},
                description = "Use Unicode symbols for display")
        boolean useUnicode;

        @Override
        public void run() {
            Generator generator =
                    switch (algorithm.toLowerCase()) {
                        case "dfs" -> new DfsGenerator();
                        case "prim" -> new PrimeGenerator();
                        case "bfs" -> new BfsGenerator();
                        default -> throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
                    };

            Maze maze = generator.generate(width + 2, height + 2);
            String mazeString = mazeToString(maze, useUnicode);

            if (outputFile != null) {
                try (BufferedWriter writer = Files.newBufferedWriter(outputFile.toPath())) {
                    writer.write(mazeString);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to write to file: " + outputFile + " :" + e.getMessage());
                }
            } else {
                System.out.println(mazeString);
            }
        }
    }

    @Command(name = "solve", description = "Solve a maze with specified algorithm and points.")
    static class SolveCommand implements Runnable {
        @Option(
                names = {"--algorithm", "-a"},
                description = "Solver algorithm (Dijkstra, AStar, Ford-Bellman)",
                defaultValue = "dijkstra")
        String algorithm;

        @Option(names = "--file", description = "Input file for maze")
        File inputFile;

        @Option(names = "--start", description = "Start point for maze solution", required = true)
        String start;

        @Option(names = "--end", description = "End point for maze solution", required = true)
        String end;

        @Option(
                names = {"--output"},
                description = "Output file for maze solution")
        private File outputFile;

        @Option(
                names = {"--unicode"},
                description = "Use Unicode symbols for display")
        private boolean useUnicode;

        @Override
        public void run() {
            Maze maze = loadMazeFromFile(inputFile);

            Point startPoint = parsePoint(start, maze.width(), maze.height());
            Point endPoint = parsePoint(end, maze.width(), maze.height());

            Path path = getPath(maze, startPoint, endPoint);
            String result = mazeWithPathToString(maze, path, startPoint, endPoint, useUnicode);

            if (outputFile != null) {
                try (BufferedWriter writer = Files.newBufferedWriter(outputFile.toPath())) {
                    writer.write(result);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to write to file: " + outputFile, e);
                }
            } else {
                System.out.println(result);
            }
        }

        /**
         * Get the shortest path from start to end in the maze
         *
         * @param maze the maze
         * @param startPoint start point in the path
         * @param endPoint end point in the path
         * @return shortest path from start to end
         */
        private Path getPath(Maze maze, Point startPoint, Point endPoint) {
            if (!maze.isClearAt(startPoint.x(), startPoint.y()) || !maze.isClearAt(endPoint.x(), endPoint.y())) {
                throw new IllegalArgumentException("Start or end point is not a clear cell");
            }

            Solver solver =
                    switch (algorithm.toLowerCase()) {
                        case "astar" -> new AStarSolver();
                        case "dijkstra" -> new DijkstraSolver();
                        case "ford-bellman" -> new FordBellmanSolver();
                        default -> throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
                    };

            return solver.solve(maze, startPoint, endPoint);
        }
    }
}

package academy.maze.dto;

/** Тип ячейки в лабиринте. WALL - стена, PATH - свободная ячейка. */
public enum CellType {
    WALL(Integer.MAX_VALUE),
    PATH(2),
    COIN(1),
    DESERT(3),
    START(0),
    END(0);

    public final int cost;

    CellType(int cost) {
        this.cost = cost;
    }

    //    public static final Map<CellType, Integer> cost = Map.of(
    //            DESERT, 3,
    //            PATH, 2,
    //            COIN, 1,
    //            START, 0,
    //            END, 0,
    //            WALL, Integer.MAX_VALUE);
}

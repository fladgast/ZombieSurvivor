import java.util.ArrayList;

public class Maze {
    ArrayList<Position> blockPositions = new ArrayList<>();
    final char block = '\u2588';
    public Maze() {

    }

    public void buildOuterWalls() {
        for (int i = 0; i <= 50; i++) {
            blockPositions.add(new Position(i, 0));
            blockPositions.add(new Position(i, 20));
        }
        for (int i = 0; i <= 20; i++) {
            blockPositions.add(new Position(0, i));
            blockPositions.add(new Position(49, i));
        }
    }

    public void buildMaze() {
        for (int i = 0; i < 20; i++) {
            blockPositions.add(new Position(15 + i, 10));
        }
    }
}

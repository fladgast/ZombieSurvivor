import java.util.ArrayList;
import java.util.Random;

public class Zombie {
    final char zombie = '\u2620';
    Position position;

    public Zombie(Position position) {
        this.position = position;
    }
    public void chasePlayer(Position position, ArrayList<Position> blockPositions) {
        int oldX = this.position.getX();
        int oldY = this.position.getY();
        Random random = new Random();
        boolean coinFlip = random.nextBoolean();

        if (coinFlip) {
            if (this.position.getX() < position.getX()) {
                this.position.setX(this.position.getX() + 1);
            } else if (this.position.getX() > position.getX()) {
                this.position.setX(this.position.getX() - 1);
            } else if (this.position.getY() < position.getY()) {
                this.position.setY(this.position.getY() + 1);
            } else if (this.position.getY() > position.getY()) {
                this.position.setY(this.position.getY() - 1);
            }
        } else {
            if (this.position.getY() < position.getY()) {
                this.position.setY(this.position.getY() + 1);
            } else if (this.position.getY() > position.getY()) {
                this.position.setY(this.position.getY() - 1);
            } else if (this.position.getX() < position.getX()) {
                this.position.setX(this.position.getX() + 1);
            } else if (this.position.getX() > position.getX()) {
                this.position.setX(this.position.getX() - 1);
            }
        }
        for (Position p : blockPositions) {
            if (this.position.getX() == p.getX() && this.position.getY() == p.getY()) {
                this.position.setX(oldX);
                this.position.setY(oldY);
                break;
            }
        }

    }
}

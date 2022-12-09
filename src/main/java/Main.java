import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        TerminalSize ts = new TerminalSize(50, 20);
        DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
        defaultTerminalFactory.setInitialTerminalSize(ts);
        Terminal terminal = defaultTerminalFactory.createTerminal();


        ArrayList<Zombie> zombies = new ArrayList<>();

        Maze maze = new Maze();
        maze.buildMaze();

        for (Position blockPosition : maze.blockPositions) {
            terminal.setCursorPosition(blockPosition.getX(), blockPosition.getY());
            terminal.putCharacter(maze.block);
        }


        terminal.flush();

//        Random r = new Random();
//        Position bombPosition = new Position(r.nextInt(80), r.nextInt(24));
//        terminal.setCursorPosition(bombPosition.getX(), bombPosition.getY());
//        terminal.putCharacter('@');

        Position playerPosition = new Position(25, 11);
        final char player = '\u2606';

        terminal.setCursorPosition(playerPosition.getX(), playerPosition.getY());
        terminal.putCharacter(player);
        terminal.setCursorVisible(false);
        terminal.flush();

        spawnZombie(terminal, zombies);

        KeyType direction = null;
        int stepsTaken = 0;
        int spawnRate = 5;
        boolean isDead = false;
        int score = 0;
        while (!isDead) {
            KeyStroke keyStroke;
            String scoreString = String.format("Score: %04d", score);


            for (int i = 0; i < scoreString.length(); i++) {
                terminal.setCursorPosition(39 + i, 0);
                terminal.putCharacter(scoreString.charAt(i));
            }

            do {
                Thread.sleep(5);
                keyStroke = terminal.pollInput();
            } while (keyStroke == null);

            KeyType type = keyStroke.getKeyType();
            int oldX = playerPosition.getX(), oldY = playerPosition.getY();
            switch (type) {
                case ArrowLeft -> {
                    playerPosition.setX(playerPosition.getX() - 1);
                    direction = KeyType.ArrowLeft;
                    if (playerPosition.getX() < 0) {
                        playerPosition.setX(49);
                    }
                }
                case ArrowUp -> {
                    playerPosition.setY(playerPosition.getY() - 1);
                    direction = KeyType.ArrowUp;
                    if (playerPosition.getY() < 0) {
                        playerPosition.setY(19);
                    }
                }
                case ArrowRight -> {
                    playerPosition.setX(playerPosition.getX() + 1);
                    direction = KeyType.ArrowRight;
                    if (playerPosition.getX() > 49) {
                        playerPosition.setX(0);
                    }
                }
                case ArrowDown -> {
                    playerPosition.setY(playerPosition.getY() + 1);
                    direction = KeyType.ArrowDown;
                    if (playerPosition.getY() > 19) {
                        playerPosition.setY(0);
                    }
                }
                case Character -> {
                    if (keyStroke.getCharacter() == ' ' && direction != null) {
                        score = fireLaser(terminal, playerPosition, direction, zombies, score, maze.blockPositions);
                    }
                }
            }
            stepsTaken++;

            for (Zombie zombie : zombies) {
                int zombieOldX = zombie.position.getX();
                int zombieOldY = zombie.position.getY();
                zombie.chasePlayer(playerPosition, maze.blockPositions);
                if (zombie.position.getX() == zombieOldX && zombie.position.getY() == zombieOldY) {
                    continue;
                }
                terminal.setCursorPosition(zombie.position.getX(), zombie.position.getY());
                terminal.putCharacter(zombie.zombie);
                terminal.setCursorPosition(zombieOldX, zombieOldY);
                terminal.putCharacter(' ');
            }

            String loserText = "Mmm... Brains";
            for (Zombie zombie : zombies) {
                if (zombie.position.getX() == playerPosition.getX() && zombie.position.getY() == playerPosition.getY()) {
                    terminal.bell();
                    terminal.clearScreen();
                    int textX = 19;
                    for (int i = 0; i < loserText.length(); i++) {
                        terminal.setCursorPosition(textX + i, 10);
                        terminal.putCharacter(loserText.charAt(i));
                    }
                    for (int i = 0; i < scoreString.length(); i++) {
                        terminal.setCursorPosition(textX + i, 12);
                        terminal.putCharacter(scoreString.charAt(i));
                    }
                    Thread.sleep(5000);
                    terminal.close();
                    isDead = true;
                    break;
                }
            }

            if (stepsTaken % 15 == 0) {
                spawnRate--;
                if (spawnRate < 1) {
                    spawnRate = 1;
                }
            }

            if (stepsTaken % spawnRate == 0) {
                spawnZombie(terminal, zombies);
            }

            for (int i = 0; i < maze.blockPositions.size(); i++) {
                if (playerPosition.getX() == maze.blockPositions.get(i).getX() && playerPosition.getY() == maze.blockPositions.get(i).getY()) {
                    playerPosition.setX(oldX);
                    playerPosition.setY(oldY);
                    break;
                }
            }

            terminal.setCursorPosition(oldX, oldY);
            terminal.putCharacter(' ');

            terminal.setCursorPosition(playerPosition.getX(), playerPosition.getY());
//            if (x == bombPosition.getX() && y == bombPosition.getY()) {
//                terminal.bell();
//                Thread.sleep(300);
//                terminal.close();
//                break;
//            }
            terminal.putCharacter(player);
            terminal.flush();
        }
    }
    static int fireLaser(Terminal terminal, Position playerPosition, KeyType direction, ArrayList<Zombie> zombiePositions, int score, ArrayList<Position> blockPositions) throws IOException, InterruptedException {
        int x = playerPosition.getX();
        int y = playerPosition.getY();
        final char horizontalLine = '\u23AF';
        final char verticalLine = '\u007C';
        switch (direction) {
            case ArrowLeft -> {
                while (true) {
                    if (zombieHit(x, y, terminal, zombiePositions)) {
                        score++;
                        break;
                    }
                    if (wallHit(x, y, terminal, blockPositions)) {
                        break;
                    }
                    x--;
                    if (x < 0) {
                        break;
                    }
                    terminal.setCursorPosition(x, y);
                    terminal.putCharacter(horizontalLine);
                    terminal.flush();
                }
                Thread.sleep(50);
                for (int i = x; i < playerPosition.getX(); i++) {
                    terminal.setCursorPosition(i, playerPosition.getY());
                    terminal.putCharacter(' ');
                    terminal.flush();
                }
            }
            case ArrowRight -> {
                while (true) {
                    if (zombieHit(x, y, terminal, zombiePositions)) {
                        score++;
                        break;
                    }
                    if (wallHit(x, y, terminal, blockPositions)) {
                        break;
                    }
                    x++;
                    if (x > 100) {
                        break;
                    }
                    terminal.setCursorPosition(x, y);
                    terminal.putCharacter(horizontalLine);
                    terminal.flush();
                }
                Thread.sleep(50);
                for (int i = x; i > playerPosition.getX(); i--) {
                    terminal.setCursorPosition(i, playerPosition.getY());
                    terminal.putCharacter(' ');
                    terminal.flush();
                }
            }
            case ArrowUp -> {
                while (true) {
                    if (zombieHit(x, y, terminal, zombiePositions)) {
                        score++;
                        break;
                    }
                    if (wallHit(x, y, terminal, blockPositions)) {
                        break;
                    }
                    y--;
                    if (y < 0) {
                        break;
                    }
                    terminal.setCursorPosition(x, y);
                    terminal.putCharacter(verticalLine);
                    terminal.flush();
                }
                Thread.sleep(50);
                for (int i = y; i < playerPosition.getY(); i++) {
                    terminal.setCursorPosition(playerPosition.getX(), i);
                    terminal.putCharacter(' ');
                    terminal.flush();
                }
            }
            case ArrowDown -> {
                while (true) {
                    if (zombieHit(x, y, terminal, zombiePositions)) {
                        score++;
                        break;
                    }
                    if (wallHit(x, y, terminal, blockPositions)) {
                        break;
                    }
                    y++;
                    if (y > 50) {
                        break;
                    }
                    terminal.setCursorPosition(x, y);
                    terminal.putCharacter(verticalLine);
                    terminal.flush();
                }
                Thread.sleep(50);
                for (int i = y; i > playerPosition.getY(); i--) {
                    terminal.setCursorPosition(playerPosition.getX(), i);
                    terminal.putCharacter(' ');
                    terminal.flush();
                }
            }
        }
        return score;
    }
    static boolean zombieHit(int x, int y, Terminal terminal, ArrayList<Zombie> zombies) throws IOException {
        for (int i = 0; i < zombies.size(); i++) {
            if (x == zombies.get(i).position.getX() && y == zombies.get(i).position.getY()) {
                terminal.setCursorPosition(x, y);
                terminal.putCharacter(' ');
                zombies.remove(i);
                return true;
            }
        }
        return false;
    }
    static void spawnZombie(Terminal terminal, List<Zombie> zombies) throws IOException {
        Random r = new Random();
        Zombie zombie = new Zombie(new Position(r.nextInt(0, 40), 0));
        zombies.add(zombie);
        terminal.setCursorPosition(zombie.position.getX(), zombie.position.getY());
        terminal.putCharacter(zombie.zombie);
    }
    static boolean wallHit(int x, int y, Terminal terminal, ArrayList<Position> blockPositions) throws IOException {
        for (int i = 0; i < blockPositions.size(); i++) {
            if (x == blockPositions.get(i).getX() && y == blockPositions.get(i).getY()) {
                terminal.setCursorPosition(x, y);
                terminal.putCharacter(' ');
                blockPositions.remove(i);
                return true;
            }
        }
        return false;
    }
}

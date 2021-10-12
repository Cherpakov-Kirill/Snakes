package nsu.networks.snakes.model.gamecore;

import nsu.networks.snakes.model.SnakesProto;
import nsu.networks.snakes.model.SnakesProto.GameState.Coord;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GameCore implements SnakeListener, FoodListener {
    private final GameCoreListener listener;
    private final SnakesProto.GameConfig config;
    private final char[][] field;
    private final int width;
    private final int height;
    private final int nodePlayerId;
    private Map<Integer, Snake> snakeMap;
    private Food food;

    public GameCore(GameCoreListener listener, SnakesProto.GameConfig config, int nodePlayerId) {
        this.listener = listener;
        this.config = config;
        this.width = config.getWidth();
        this.height = config.getHeight();
        this.field = new char[height][width];
        clearField();
        this.snakeMap = new HashMap<>();
        this.nodePlayerId = nodePlayerId;
        this.food = new Food(this, width, height, config.getFoodStatic(), config.getFoodPerPlayer());
    }

    public int getNodeSnakeLength(){
        return snakeMap.get(nodePlayerId).getLength();
    }

    public void addNewPlayer(int playerId) {
        createNewSnake(playerId);
        food.updateNumberOfFood(snakeMap.size());
        food.addAllNotAddedFood();
    }

    public SnakesProto.Direction getSnakeDirection(int playerId) {
        return snakeMap.get(playerId).getSnakeProto().getHeadDirection();
    }

    private void createNewSnake(int playerId) {
        Snake newSnake = new Snake(this, width, height, playerId, getRandomDirection(), getStartCoordinate());
        snakeMap.put(playerId, newSnake);
    }

    public void updateGameState(SnakesProto.GameState gameState) {
        clearField();
        List<SnakesProto.GameState.Snake> snakesList = gameState.getSnakesList();
        snakeMap.clear();
        for (SnakesProto.GameState.Snake snake : snakesList) {
            snakeMap.put(snake.getPlayerId(), new Snake(this, snake, width, height));
        }
        food.updateFood(gameState.getFoodsList());
        updateField();
    }

    private List<SnakesProto.GameState.Snake> getListWithSnakesProto() {
        List<SnakesProto.GameState.Snake> snakesProto = new LinkedList<>();
        List<Snake> snakesObjects = new LinkedList<>(snakeMap.values());
        for (Snake snake : snakesObjects) {
            snakesProto.add(snake.getSnakeProto());
        }
        return snakesProto;
    }

    public SnakesProto.GameState.Builder getGameData() {
        return SnakesProto.GameState.newBuilder().addAllSnakes(getListWithSnakesProto()).addAllFoods(food.getListOfFoodCoordinates());
    }

    public void makeRightMove(int playerId) {
        snakeMap.get(playerId).makeRightMove();
    }

    public void makeLeftMove(int playerId) {
        snakeMap.get(playerId).makeLeftMove();
    }

    public void makeUpMove(int playerId) {
        snakeMap.get(playerId).makeUpMove();
    }

    public void makeDownMove(int playerId) {
        snakeMap.get(playerId).makeDownMove();
    }

    private void clearField() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                field[i][j] = '-';
            }
        }
    }

    public String getField() {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                char sym = field[i][j];
                string.append(sym);
            }
        }
        return string.toString();
    }

    @Override
    public int addX(int x, int delta) {
        int newX = x + delta;
        return newX < width ? newX : newX - width;
    }

    @Override
    public int subtractX(int x, int delta) {
        int newX = x - delta;
        return newX >= 0 ? newX : newX + width;
    }

    @Override
    public int addY(int y, int delta) {
        int newY = y + delta;
        return newY < height ? newY : newY - height;
    }

    @Override
    public int subtractY(int y, int delta) {
        int newY = y - delta;
        return newY >= 0 ? newY : newY + height;
    }

    public static Coord buildCoordinate(int x, int y) {
        return Coord.newBuilder().setX(x).setY(y).build();
    }

    public static SnakesProto.Direction getRandomDirection() {
        int dir = 1 + (int) (Math.random() * 4);
        return SnakesProto.Direction.forNumber(dir);

    }

    private Coord getStartCoordinate() {
        boolean success = false;
        int x = getField().indexOf('-') % width;
        int y = getField().indexOf('-') / height;
        int iter = 0;
        while (!success) {
            int randX = (int) (Math.random() * width);
            int randY = (int) (Math.random() * height);
            if (field[randY][randX] == '-') {
                x = randX;
                y = randY;
                success = true;
            }
            iter++;
            if (iter == 20) break;
        }
        return buildCoordinate(x, y);
    }

    @Override
    public PointType checkCoordinate(Coord coordinate) {
        switch (field[coordinate.getY()][coordinate.getX()]) {
            case '-', '.' -> {
                return PointType.EMPTY;
            }
            case '#' -> {
                return PointType.SNAKE;
            }
            case '*' -> {
                return PointType.FOOD;
            }
        }
        return null;
    }

    private void setSymInSquare(char sym, int xCenter, int yCenter) {
        int startX = subtractX(xCenter, 2);
        int startY = subtractY(yCenter, 2);
        for (int i = 0; i < 5; i++) {
            int newX = addX(startX, i);
            for (int j = 0; j < 5; j++) {
                int newY = addY(startY, j);
                if (field[newY][newX] != '#' && field[newY][newX] != '*') {
                    field[newY][newX] = sym;
                }
            }
        }
    }

    @Override
    public void setSnakePoint(Coord coordinate) {
        int x = coordinate.getX();
        int y = coordinate.getY();
        if (field[y][x] == '*') food.foodWasEaten(x, y);
        field[y][x] = '#';
        setSymInSquare('.', x, y);
    }

    @Override
    public void deleteSnakePoint(Coord coordinate) {
        int x = coordinate.getX();
        int y = coordinate.getY();
        int prevX = 0;
        int prevY = 0;
        if (field[subtractY(y, 1)][x] == '#') {
            prevX = x;
            prevY = subtractY(y, 1);
        } else {
            if (field[addY(y, 1)][x] == '#') {
                prevX = x;
                prevY = addY(y, 1);
            } else {
                if (field[y][subtractX(x, 1)] == '#') {
                    prevX = subtractX(x, 1);
                    prevY = y;
                } else {
                    if (field[y][addX(x, 1)] == '#') {
                        prevX = addX(x, 1);
                        prevY = y;
                    }
                }
            }
        }
        setSymInSquare('-', x, y);
        setSymInSquare('.', prevX, prevY);
        field[y][x] = '.';
    }

    @Override
    public void snakeIsDead(int snakePlayerId) {
        snakeMap.remove(snakePlayerId);
        System.out.println("Snake " + snakePlayerId + " is dead!");
        //TODO: send signal to food class for make dead points in food
    }

    @Override
    public void updateField() {
        listener.updateField(getField());
    }

    @Override
    public char getPointFormField(int x, int y) {
        return field[y][x];
    }

    @Override
    public String getFieldString() {
        return getField();
    }

    @Override
    public void setFoodPoint(Coord coordinate) {
        int x = coordinate.getX();
        int y = coordinate.getY();
        field[y][x] = '*';
    }
}

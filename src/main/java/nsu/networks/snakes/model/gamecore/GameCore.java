package nsu.networks.snakes.model.gamecore;

import nsu.networks.snakes.model.SnakesProto;
import nsu.networks.snakes.model.SnakesProto.GameState.Coord;
import nsu.networks.snakes.model.actionUpdater.GameCoreForActionUpdater;
import nsu.networks.snakes.model.players.FieldPoint;

import java.util.*;

public class GameCore implements SnakeListener, FoodListener, GameCoreForActionUpdater {
    private final GameCoreListener listener;
    private final char[][] field;
    private final int width;
    private final int height;
    private final int nodePlayerId;
    private final Map<Integer, Snake> snakeMap;
    private final Map<Integer, Coord> idOfSnakeForDeleteCoords;
    private final Map<Coord, List<Integer>> idListOfNewCoords;
    private String prevField;
    private final Food food;

    public GameCore(GameCoreListener listener, SnakesProto.GameConfig config, int nodePlayerId) {
        this.listener = listener;
        this.width = config.getWidth();
        this.height = config.getHeight();
        this.field = new char[height][width];
        clearField();
        this.snakeMap = new HashMap<>();
        this.nodePlayerId = nodePlayerId;
        this.food = new Food(this, width, height, config.getFoodStatic(), config.getFoodPerPlayer(), config.getDeadFoodProb());
        this.idOfSnakeForDeleteCoords = new HashMap<>();
        this.idListOfNewCoords = new HashMap<>();
        prevField = "-".repeat(height * width);
    }

    public boolean getOpportunityToJoin() {
        return getFieldString().indexOf('-') != -1;
    }

    //Functions for adding new player
    public SnakesProto.Direction getSnakeDirection(int playerId) {
        return snakeMap.get(playerId).getSnakeProto().getHeadDirection();
    }

    public boolean addNewPlayer(int playerId) {
        if (!createNewSnake(playerId)) return false;
        food.updateNumberOfFood(snakeMap.size());
        food.addAllNotAddedFood();
        return true;
    }

    //Create new snake
    private static SnakesProto.Direction getRandomDirection() {
        int dir = 1 + (int) (Math.random() * 4);
        return SnakesProto.Direction.forNumber(dir);

    }

    private Coord findTailCoordination(int xHead, int yHead) {
        boolean success = false;
        SnakesProto.Direction direction;
        int xTail = 0;
        int yTail = 0;
        Set<SnakesProto.Direction> directionSet = new HashSet<>();
        while (!success) {
            if (directionSet.size() == 4) return null;
            direction = getRandomDirection();
            switch (direction) {
                case LEFT -> {
                    directionSet.add(SnakesProto.Direction.LEFT);
                    xTail = addX(xHead, 1);
                    yTail = yHead;
                    if (field[yTail][xTail] == '-') success = true;
                }
                case RIGHT -> {
                    directionSet.add(SnakesProto.Direction.RIGHT);
                    xTail = subtractX(xHead, 1);
                    yTail = yHead;
                    if (field[yTail][xTail] == '-') success = true;
                }
                case UP -> {
                    directionSet.add(SnakesProto.Direction.UP);
                    xTail = xHead;
                    yTail = addY(yHead, 1);
                    if (field[yTail][xTail] == '-') success = true;
                }
                case DOWN -> {
                    directionSet.add(SnakesProto.Direction.DOWN);
                    xTail = xHead;
                    yTail = subtractY(yHead, 1);
                    if (field[yTail][xTail] == '-') success = true;
                }
            }
        }
        return buildCoordinate(xTail, yTail);
    }

    private boolean createNewSnake(int playerId) {
        int indexOfEmpty = getFieldString().indexOf('-');
        if (indexOfEmpty == -1) {
            return false;
        }
        int xHead = indexOfEmpty % width;
        int yHead = indexOfEmpty / height;
        Coord head = buildCoordinate(xHead, yHead);
        Coord tail = findTailCoordination(xHead, yHead);
        int iter = 0;
        do {
            int randX = (int) (Math.random() * width);
            int randY = (int) (Math.random() * height);
            synchronized (field) {
                if (field[randY][randX] == '-') {
                    Coord randHead = buildCoordinate(randX, randY);
                    Coord randTail = findTailCoordination(randX, randY);
                    if (randTail != null) {
                        head = randHead;
                        tail = randTail;
                        break;
                    }
                }
            }
            iter++;
        } while (iter != 20);
        if (tail == null) return false;
        Snake newSnake = new Snake(this, width, height, playerId, head, tail);
        snakeMap.put(playerId, newSnake);
        return true;
    }


    //Receive game state message
    public void updateGameState(SnakesProto.GameState gameState) {
        clearField();
        List<SnakesProto.GameState.Snake> snakesList = gameState.getSnakesList();
        snakeMap.clear();
        for (SnakesProto.GameState.Snake snake : snakesList) {
            snakeMap.put(snake.getPlayerId(), new Snake(this, snake, width, height));
        }
        food.updateFood(gameState.getFoodsList());
        listener.updateField(getFieldPoints());
    }

    private List<FieldPoint> getFieldPoints() {
        List<FieldPoint> fieldPoints = new LinkedList<>();
        int index = -1;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                char sym = field[i][j];
                index++;
                if (sym == prevField.charAt(index)) continue;
                Coord coordinate = buildCoordinate(j, i);
                fieldPoints.add(new FieldPoint(sym, coordinate));
            }
        }
        prevField = getFieldString();
        return fieldPoints;
    }

    //Get game data for send
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

    //Player's actions
    public void makeAction(int idPlayer, SnakesProto.Direction direction) {
        switch (direction) {
            case LEFT -> snakeMap.get(idPlayer).makeLeftMove();
            case RIGHT -> snakeMap.get(idPlayer).makeRightMove();
            case DOWN -> snakeMap.get(idPlayer).makeDownMove();
            case UP -> snakeMap.get(idPlayer).makeUpMove();
        }
    }

    //Field
    private void clearField() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                field[i][j] = '-';
            }
        }
    }

    //Discrete THOR arithmetic functions
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

    @Override
    public PointType checkCoordinate(int x, int y) {
        switch (field[y][x]) {
            case '-', '.' -> {
                return PointType.EMPTY;
            }
            case '#', '&' -> {
                return PointType.SNAKE;
            }
            case '*' -> {
                return PointType.FOOD;
            }
        }
        return null;
    }

    private void setSymInSquare(char sym, int xCenter, int yCenter) {
        int startX = subtractX(xCenter, (5 - 1) / 2);
        int startY = subtractY(yCenter, (5 - 1) / 2);
        for (int i = 0; i < 5; i++) {
            int newX = addX(startX, i);
            for (int j = 0; j < 5; j++) {
                int newY = addY(startY, j);
                if (field[newY][newX] != '#' && field[newY][newX] != '&' && field[newY][newX] != '*') {
                    field[newY][newX] = sym;
                }
            }
        }
    }

    @Override
    public void setFoodPoint(Coord coordinate) {
        int x = coordinate.getX();
        int y = coordinate.getY();
        field[y][x] = '*';
    }

    private void addOnePoint(int nodePlayerId) {
        listener.addOnePoint(nodePlayerId);
    }

    @Override
    public void addNewCoordinateOnStep(int nodePlayerId, Coord newCoordinate, Coord delCoordinate) {
        if (checkCoordinate(newCoordinate.getX(), newCoordinate.getY()) != PointType.FOOD) {
            idOfSnakeForDeleteCoords.put(nodePlayerId, delCoordinate);
        } else addOnePoint(nodePlayerId);
        if (idListOfNewCoords.containsKey(newCoordinate)) {
            idListOfNewCoords.get(newCoordinate).add(nodePlayerId);
        } else {
            List<Integer> list = new LinkedList<>();
            list.add(nodePlayerId);
            idListOfNewCoords.put(newCoordinate, list);
        }
    }

    @Override
    public void setSnakePoint(Coord coordinate, int playerId) {
        int x = coordinate.getX();
        int y = coordinate.getY();
        if (field[y][x] == '*') food.foodWasEaten(coordinate);
        if (nodePlayerId == playerId) field[y][x] = '&';
        else field[y][x] = '#';
        setSymInSquare('.', x, y);
    }

    @Override
    public void clearSnakePoint(Coord coordinate) {
        int x = coordinate.getX();
        int y = coordinate.getY();
        field[y][x] = '-';
        setSymInSquare('-', x, y);
    }

    public void deleteSnakePoint(Coord coordinate) {
        int x = coordinate.getX();
        int y = coordinate.getY();
        int prevX = 0;
        int prevY = 0;
        char sym = field[subtractY(y, 1)][x];
        if (sym == '#' || sym == '&') {
            prevX = x;
            prevY = subtractY(y, 1);
        } else {
            sym = field[addY(y, 1)][x];
            if (sym == '#' || sym == '&') {
                prevX = x;
                prevY = addY(y, 1);
            } else {
                sym = field[y][subtractX(x, 1)];
                if (sym == '#' || sym == '&') {
                    prevX = subtractX(x, 1);
                    prevY = y;
                } else {
                    sym = field[y][addX(x, 1)];
                    if (sym == '#' || sym == '&') {
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
    public void snakeIsDead(int snakePlayerId, List<Coord> snakeCoordinates) {
        food.turnSnakeIntoFood(snakeCoordinates);
        snakeMap.remove(snakePlayerId);
        food.updateNumberOfFood(snakeMap.size());
        listener.nodeSnakeIsDead(snakePlayerId);
        System.out.println("Snake " + snakePlayerId + " is dead!");
    }

    @Override
    public String getFieldString() {
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
    public List<Integer> getListOfSnakesId() {
        return new LinkedList<>(snakeMap.keySet());
    }

    @Override
    public void updateField() {
        for (int id : idOfSnakeForDeleteCoords.keySet()) {
            snakeMap.get(id).deleteTail();
        }
        List<Integer> deadSnakes = new LinkedList<>();
        for (Coord coordinate : idListOfNewCoords.keySet()) {
            List<Integer> idList = idListOfNewCoords.get(coordinate);
            if (idList.size() == 1) {
                int playerId = idList.get(0);
                if (checkCoordinate(coordinate.getX(), coordinate.getY()) != PointType.SNAKE) {
                    Snake currSnake = snakeMap.get(playerId);
                    currSnake.addHead(coordinate);
                } else {
                    for (int id : snakeMap.keySet()) {
                        if (snakeMap.get(id).containsCoordinate(coordinate)) {
                            addOnePoint(id);
                            break;
                        }
                    }
                    deadSnakes.add(playerId);
                }
            } else {
                deadSnakes.addAll(idList);
            }
        }
        boolean isMasterSnakeDead = false;
        for (int id : deadSnakes) {
            if (id == nodePlayerId) isMasterSnakeDead = true;
            else snakeMap.get(id).deleteSnake();
        }
        if (isMasterSnakeDead) snakeMap.get(nodePlayerId).deleteSnake();
        idOfSnakeForDeleteCoords.clear();
        idListOfNewCoords.clear();
        listener.updateField(getFieldPoints());
    }
}

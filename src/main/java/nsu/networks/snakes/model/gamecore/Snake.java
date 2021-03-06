package nsu.networks.snakes.model.gamecore;

import nsu.networks.snakes.model.SnakesProto.GameState.Coord;
import nsu.networks.snakes.model.SnakesProto.*;

import java.util.LinkedList;
import java.util.List;

public class Snake {
    private final int playerId;
    private final int width;
    private final int height;
    private GameState.Snake snake;
    private final SnakeListener listener;
    private final List<Coord> snakeCoordinates;

    public Snake(SnakeListener listener, int width, int height, int playerId, Coord coordinateOfHead, Coord coordinateOfTail) {
        this.listener = listener;
        this.playerId = playerId;
        this.width = width;
        this.height = height;
        this.snakeCoordinates = new LinkedList<>();

        GameState.Snake.Builder builder = GameState.Snake.newBuilder();
        builder.setState(GameState.Snake.SnakeState.ALIVE);
        builder.setPlayerId(playerId);
        builder.setHeadDirection(getDirection(coordinateOfTail, coordinateOfHead));
        builder.addPoints(coordinateOfHead);
        builder.addPoints(coordinateOfTail);
        snake = builder.build();
        listener.setSnakePoint(coordinateOfHead, playerId);
        listener.setSnakePoint(coordinateOfTail, playerId);
        snakeCoordinates.add(coordinateOfHead);
        snakeCoordinates.add(coordinateOfTail);
    }

    public Snake(SnakeListener listener, GameState.Snake snake, int width, int height) {
        this.listener = listener;
        this.width = width;
        this.height = height;
        this.snakeCoordinates = new LinkedList<>();
        this.snake = snake;
        this.playerId = snake.getPlayerId();
        parseSnakeCoordinatesFromProto();
    }

    //Update snake coordinates
    private void parseSnakeCoordinatesFromProto() {
        snakeCoordinates.clear();
        Coord prevCoordinate = null;
        for (Coord coordinate : snake.getPointsList()) {
            if (snakeCoordinates.isEmpty()) {
                snakeCoordinates.add(coordinate);
                prevCoordinate = coordinate;
                listener.setSnakePoint(prevCoordinate, playerId);
                continue;
            }
            int offsetX = coordinate.getX();
            int offsetY = coordinate.getY();
            if (offsetX != 0) {
                if (offsetX > 0) {
                    for (int i = 1; i <= offsetX; i++) {
                        prevCoordinate = GameCore.buildCoordinate(listener.addX(prevCoordinate.getX(), 1), prevCoordinate.getY());
                        snakeCoordinates.add(prevCoordinate);
                        listener.setSnakePoint(prevCoordinate, playerId);
                    }
                } else {
                    for (int i = -1; i >= offsetX; i--) {
                        prevCoordinate = GameCore.buildCoordinate(listener.subtractX(prevCoordinate.getX(), 1), prevCoordinate.getY());
                        snakeCoordinates.add(prevCoordinate);
                        listener.setSnakePoint(prevCoordinate, playerId);
                    }
                }
            } else {
                if (offsetY > 0) {
                    for (int i = 1; i <= offsetY; i++) {
                        prevCoordinate = GameCore.buildCoordinate(prevCoordinate.getX(), listener.addY(prevCoordinate.getY(), 1));
                        snakeCoordinates.add(prevCoordinate);
                        listener.setSnakePoint(prevCoordinate, playerId);
                    }
                } else {
                    for (int i = -1; i >= offsetY; i--) {
                        prevCoordinate = GameCore.buildCoordinate(prevCoordinate.getX(), listener.subtractY(prevCoordinate.getY(), 1));
                        snakeCoordinates.add(prevCoordinate);
                        listener.setSnakePoint(prevCoordinate, playerId);
                    }
                }
            }
        }
    }

    //Get snake coordinates for send
    private Direction getDirection(Coord from, Coord to) {
        int deltaX = to.getX() - from.getX();
        if (Math.abs(deltaX) > 1) {
            if (deltaX < 0) deltaX = 1;
            else if (deltaX > 0) deltaX = -1;
        }
        int deltaY = to.getY() - from.getY();
        if (Math.abs(deltaY) > 1) {
            if (deltaY < 0) deltaY = 1;
            else if (deltaY > 0) deltaY = -1;
        }

        if (deltaY == 1) return Direction.DOWN;
        else if (deltaY == -1) return Direction.UP;
        else if (deltaX == 1) return Direction.RIGHT;
        else if (deltaX == -1) return Direction.LEFT;
        return null;
    }

    private int getDeltaX(int currentCoordinate, int previousCoordinate, Direction direction) {
        int delta = currentCoordinate - previousCoordinate;
        switch (direction) {
            case LEFT -> {
                if (delta < 0) {
                    return delta;
                } else return delta - width;
            }
            case RIGHT -> {
                if (delta > 0) {
                    return delta;
                } else return delta + width;
            }
        }
        return 0;
    }

    private int getDeltaY(int currentCoordinate, int previousCoordinate, Direction direction) {
        int delta = currentCoordinate - previousCoordinate;
        switch (direction) {
            case UP -> {
                if (delta < 0) {
                    return delta;
                } else return delta - height;
            }
            case DOWN -> {
                if (delta > 0) {
                    return delta;
                } else return delta + height;
            }
        }
        return 0;
    }

    private List<Coord> getCoordinatesForProtocol() {
        List<Coord> coordinatesForProtocol = new LinkedList<>();
        Direction prevDirectionToTail = null;
        switch (snake.getHeadDirection()) {
            case UP -> prevDirectionToTail = Direction.DOWN;
            case DOWN -> prevDirectionToTail = Direction.UP;
            case LEFT -> prevDirectionToTail = Direction.RIGHT;
            case RIGHT -> prevDirectionToTail = Direction.LEFT;
        }

        Coord prevPoint = snakeCoordinates.get(0);
        int coordinationNum = snakeCoordinates.size();
        int iterCounter = 0;
        Coord lastAddedCoordinate = null;
        for (Coord currPoint : snakeCoordinates) {
            iterCounter++;
            if (coordinatesForProtocol.isEmpty()) {
                coordinatesForProtocol.add(currPoint);
                lastAddedCoordinate = currPoint;
                prevPoint = currPoint;
                continue;
            }
            Direction currDirectionToTail = getDirection(prevPoint, currPoint);

            if (prevDirectionToTail != currDirectionToTail) {
                assert currDirectionToTail != null;
                coordinatesForProtocol.add(GameCore.buildCoordinate(getDeltaX(prevPoint.getX(), lastAddedCoordinate.getX(), prevDirectionToTail),
                        getDeltaY(prevPoint.getY(), lastAddedCoordinate.getY(), prevDirectionToTail)));
                lastAddedCoordinate = prevPoint;
            }
            if (coordinationNum == iterCounter) {
                coordinatesForProtocol.add(GameCore.buildCoordinate(getDeltaX(currPoint.getX(), lastAddedCoordinate.getX(), currDirectionToTail),
                        getDeltaY(currPoint.getY(), lastAddedCoordinate.getY(), currDirectionToTail)));
            }
            prevDirectionToTail = currDirectionToTail;
            prevPoint = currPoint;
        }
        return coordinatesForProtocol;
    }

    public GameState.Snake getSnakeProto() {
        snake = snake.toBuilder().clearPoints().addAllPoints(getCoordinatesForProtocol()).build();
        return snake;
    }


    //Make new step
    public void addHead(Coord coordinate) {
        snakeCoordinates.add(0, coordinate);
        listener.setSnakePoint(coordinate,playerId);
    }

    public void deleteTail() {
        Coord tail = getTailCoordinate();
        snakeCoordinates.remove(snakeCoordinates.size() - 1);
        listener.deleteSnakePoint(tail);
    }

    public void deleteSnake(){
        for(Coord c : snakeCoordinates){
            listener.clearSnakePoint(c);
        }
        listener.snakeIsDead(snake.getPlayerId(),snakeCoordinates);
    }

    private Coord getHeadCoordinate() {
        return snakeCoordinates.get(0);
    }

    private Coord getTailCoordinate() {
        return snakeCoordinates.get(snakeCoordinates.size() - 1);
    }

    public boolean containsCoordinate(Coord point){
        return snakeCoordinates.contains(point);
    }

    /*private void setMove(Coord newPoint) {
        PointType type = listener.checkCoordinate(newPoint.getX(), newPoint.getY());
        switch (type) {
            case EMPTY -> {
                listener.setSnakePoint(newPoint, playerId);
                addHead(newPoint);
                listener.deleteSnakePoint(getTailCoordinate());
                deleteTail();
            }
            case FOOD -> {
                listener.setSnakePoint(newPoint, playerId);
                snakeCoordinates.add(0, newPoint);
                listener.addOnePoint(playerId);
            }
            case SNAKE -> {
                listener.deleteSnakePoint(getTailCoordinate());
                deleteTail();
                if(listener.checkCoordinate(newPoint.getX(), newPoint.getY()) == PointType.EMPTY){
                    listener.setSnakePoint(newPoint, playerId);
                    addHead(newPoint);
                    break;
                }
                if(!containsCoordinate(newPoint)) listener.addOnePointToOtherSnake(playerId, newPoint);
                for(Coord c : snakeCoordinates){
                    listener.clearSnakePoint(c);
                }
                listener.snakeIsDead(snake.getPlayerId(),snakeCoordinates);
            }
        }
    }*/

    private void repeatLastStep() {
        switch (snake.getHeadDirection()) {
            case LEFT -> makeLeftMove();
            case RIGHT -> makeRightMove();
            case DOWN -> makeDownMove();
            case UP -> makeUpMove();
        }
    }

    public void makeRightMove() {
        if (snake.getHeadDirection() != Direction.LEFT || snakeCoordinates.size() == 1) {
            Coord head = getHeadCoordinate();
            Coord newPoint;
            if (head.getX() == width - 1) {
                newPoint = GameCore.buildCoordinate(0, head.getY());
            } else {
                newPoint = GameCore.buildCoordinate(head.getX() + 1, head.getY());
            }
            snake = snake.toBuilder().setHeadDirection(Direction.RIGHT).build();
            listener.addNewCoordinateOnStep(playerId,newPoint,getTailCoordinate());
        } else {
            repeatLastStep();
        }
    }

    public void makeLeftMove() {
        if (snake.getHeadDirection() != Direction.RIGHT || snakeCoordinates.size() == 1) {
            Coord head = getHeadCoordinate();
            Coord newPoint;
            if (head.getX() == 0) {
                newPoint = GameCore.buildCoordinate(width - 1, head.getY());
            } else {
                newPoint = GameCore.buildCoordinate(head.getX() - 1, head.getY());
            }
            snake = snake.toBuilder().setHeadDirection(Direction.LEFT).build();
            listener.addNewCoordinateOnStep(playerId,newPoint,getTailCoordinate());
        } else {
            repeatLastStep();
        }
    }

    public void makeUpMove() {
        if (snake.getHeadDirection() != Direction.DOWN || snakeCoordinates.size() == 1) {
            Coord head = getHeadCoordinate();
            Coord newPoint;
            if (head.getY() == 0) {
                newPoint = GameCore.buildCoordinate(head.getX(), height - 1);
            } else {
                newPoint = GameCore.buildCoordinate(head.getX(), head.getY() - 1);
            }
            snake = snake.toBuilder().setHeadDirection(Direction.UP).build();
            listener.addNewCoordinateOnStep(playerId,newPoint,getTailCoordinate());
        } else {
            repeatLastStep();
        }
    }

    public void makeDownMove() {
        if (snake.getHeadDirection() != Direction.UP || snakeCoordinates.size() == 1) {
            Coord head = getHeadCoordinate();
            Coord newPoint;
            if (head.getY() == height - 1) {
                newPoint = GameCore.buildCoordinate(head.getX(), 0);
            } else {
                newPoint = GameCore.buildCoordinate(head.getX(), head.getY() + 1);
            }
            snake = snake.toBuilder().setHeadDirection(Direction.DOWN).build();
            listener.addNewCoordinateOnStep(playerId,newPoint,getTailCoordinate());
        } else {
            repeatLastStep();
        }
    }

    public Direction getHeadDirection() {
        return snake.getHeadDirection();
    }
}

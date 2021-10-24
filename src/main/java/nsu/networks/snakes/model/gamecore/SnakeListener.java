package nsu.networks.snakes.model.gamecore;
import java.util.List;

import static nsu.networks.snakes.model.SnakesProto.GameState.*;

public interface SnakeListener {
    PointType checkCoordinate(int x, int y);
    void addOnePoint(int nodePlayerId);
    void addOnePointToOtherSnake(int nodePlayerId,Coord point);
    void setSnakePoint(Coord coordinate, int playerId);
    void clearSnakePoint(Coord coordinate);
    void deleteSnakePoint(Coord coordinate);
    void snakeIsDead (int snakePlayerId, List<Coord> snakeCoordinates);
    int addX(int x, int delta);
    int subtractX(int x, int delta);
    int addY(int y, int delta);
    int subtractY(int y, int delta);
}

package nsu.networks.snakes.model.gamecore;
import static nsu.networks.snakes.model.SnakesProto.GameState.*;

public interface SnakeListener {
    PointType checkCoordinate(int x, int y);
    void setSnakePoint(Coord coordinate, int playerId);
    void deleteSnakePoint(Coord coordinate);
    void snakeIsDead (int snakePlayerId);
    int addX(int x, int delta);
    int subtractX(int x, int delta);
    int addY(int y, int delta);
    int subtractY(int y, int delta);
}

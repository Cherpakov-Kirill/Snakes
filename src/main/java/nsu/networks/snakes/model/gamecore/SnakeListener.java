package nsu.networks.snakes.model.gamecore;
import static nsu.networks.snakes.model.SnakesProto.GameState.*;

public interface SnakeListener {
    PointType checkCoordinate(Coord coordinate);
    void setSnakePoint(Coord coordinate);
    void deleteSnakePoint(Coord coordinate);
    void snakeIsDead (int snakePlayerId);
    int addX(int x, int delta);
    int subtractX(int x, int delta);
    int addY(int y, int delta);
    int subtractY(int y, int delta);

    void updateField();
}

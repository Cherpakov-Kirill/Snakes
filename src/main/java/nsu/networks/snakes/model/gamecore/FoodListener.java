package nsu.networks.snakes.model.gamecore;

import nsu.networks.snakes.model.SnakesProto;

public interface FoodListener {
    PointType checkCoordinate(int x, int y);
    String getFieldString();
    void setFoodPoint(SnakesProto.GameState.Coord coordinate);
}

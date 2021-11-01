package nsu.networks.snakes.model.gamecore;

import nsu.networks.snakes.model.players.FieldPoint;

import java.util.List;

public interface GameCoreListener {
    void updateField(List<FieldPoint> field);
    void nodeSnakeIsDead(int playerId);
    void addOnePoint(int nodePlayerId);
}

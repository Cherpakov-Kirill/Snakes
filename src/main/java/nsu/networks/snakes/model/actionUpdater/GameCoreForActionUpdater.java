package nsu.networks.snakes.model.actionUpdater;

import nsu.networks.snakes.model.SnakesProto;

public interface GameCoreForActionUpdater {
    void updateField();
    void makeAction(int idPlayer, SnakesProto.Direction direction);
    SnakesProto.Direction getSnakeDirection(int playerId);
}

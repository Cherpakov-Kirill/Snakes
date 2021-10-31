package nsu.networks.snakes.model.inet;

import nsu.networks.snakes.model.SnakesProto;

public interface PlayersForInet {
    void disconnectPlayer(int playerId);
    SnakesProto.GamePlayer getGamePLayerById(int id);
    void sendPing(int playerId);
}

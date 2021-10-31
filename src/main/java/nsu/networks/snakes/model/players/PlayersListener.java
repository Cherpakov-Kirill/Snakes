package nsu.networks.snakes.model.players;

import nsu.networks.snakes.model.SnakesProto;

public interface PlayersListener {
    boolean addPlayerInGame(int newPLayer);
    SnakesProto.GameState.Builder getGameStateData();
    void changeThisNodeRole(SnakesProto.NodeRole role, boolean requestFromPlayer);
}

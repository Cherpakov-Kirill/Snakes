package nsu.networks.snakes.model.messages;

import nsu.networks.snakes.model.SnakesProto;

public interface MessageAcceptorListener {
    void launchGameCore(int playerId);
    SnakesProto.GamePlayer getGamePlayerById(int id);
    void sendAckMessage(SnakesProto.GamePlayer player, SnakesProto.GameMessage message);
}

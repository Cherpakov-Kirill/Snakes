package nsu.networks.snakes.model.players;

import nsu.networks.snakes.model.SnakesProto;

public interface InetForPlayers {
    void sendMessage(SnakesProto.GamePlayer player, SnakesProto.GameMessage message);
    void removePlayerFromPing(int playerId);
}

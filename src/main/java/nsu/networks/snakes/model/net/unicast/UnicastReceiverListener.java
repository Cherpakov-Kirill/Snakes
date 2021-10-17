package nsu.networks.snakes.model.net.unicast;

import nsu.networks.snakes.model.SnakesProto;

public interface UnicastReceiverListener {
    void acceptMessage(int playerId, long messageSequence);
    void receiveAckMsg(int id);
    void receiveGameStateMsg(SnakesProto.GameState gameState, String masterIp);
    void receiveSteerMsg(SnakesProto.Direction direction, int playerId);
    int receiveJoinMsg(String name, String ip, int port, SnakesProto.NodeRole role, SnakesProto.PlayerType type);
}

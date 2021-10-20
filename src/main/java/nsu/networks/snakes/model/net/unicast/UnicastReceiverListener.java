package nsu.networks.snakes.model.net.unicast;

import nsu.networks.snakes.model.SnakesProto;

public interface UnicastReceiverListener {
    void acceptMessage(int playerId, long messageSequence);
    void receiveAckMsg(int playerId, long messageSequence);
    void receiveGameStateMsg(SnakesProto.GameState gameState, String masterIp);
    void receiveRoleChangeMsg(SnakesProto.GameMessage.RoleChangeMsg roleChangeMsg);
    void receiveSteerMsg(SnakesProto.Direction direction, int playerId);
    int receiveJoinMsg(String name, String ip, int port, SnakesProto.NodeRole role, SnakesProto.PlayerType type);
}

package nsu.networks.snakes.model.inet.unicast;

import nsu.networks.snakes.model.SnakesProto;

public interface UnicastReceiverListener {
    void receiveGameStateMsg(SnakesProto.GameState gameState, String masterIp);
    void receiveRoleChangeMsg(SnakesProto.GameMessage.RoleChangeMsg roleChangeMsg, int senderId);
    void receiveSteerMsg(SnakesProto.Direction direction, int playerId);
    int receiveJoinMsg(String name, String ip, int port, SnakesProto.NodeRole role, SnakesProto.PlayerType type);
}

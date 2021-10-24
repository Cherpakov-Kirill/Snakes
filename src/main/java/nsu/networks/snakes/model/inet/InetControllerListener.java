package nsu.networks.snakes.model.inet;

import nsu.networks.snakes.model.SnakesProto;

import java.util.List;

public interface InetControllerListener {
    void updateFindGameList(List<String> games);
    boolean getOpportunityToJoin();

    void launchGameCore(int playerId);

    void receiveGameStateMsg(SnakesProto.GameState gameState, String masterIp);
    void receiveRoleChangeMsg(SnakesProto.GameMessage.RoleChangeMsg roleChangeMsg);
    void receiveSteerMsg(SnakesProto.Direction direction, int playerId);
    int receiveJoinMsg(String name, String ip, int port, SnakesProto.NodeRole role, SnakesProto.PlayerType type);
}

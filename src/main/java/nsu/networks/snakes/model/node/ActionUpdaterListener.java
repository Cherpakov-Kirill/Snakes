package nsu.networks.snakes.model.node;

import nsu.networks.snakes.model.SnakesProto;

public interface ActionUpdaterListener {
    void makeAllChangesOnField();
    void sendChangesToAllPlayers();
    void initiateDeputyPLayer();
    void changeThisNodeMasterRoleOnViewer();
    int getStateOrder();
    long getMessageSequence();
}

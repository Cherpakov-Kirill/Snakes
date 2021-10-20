package nsu.networks.snakes.model.net.unicast;

public interface UnicastSenderListener {
    void disconnectPlayer(int playerId);
    boolean checkAcceptedMessage(long seqNumber);
}

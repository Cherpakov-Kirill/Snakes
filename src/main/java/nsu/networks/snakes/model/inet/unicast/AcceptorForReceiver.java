package nsu.networks.snakes.model.inet.unicast;

public interface AcceptorForReceiver {
    void acceptMessage(int playerId, long messageSequence);
    void receiveAckMsg(int playerId, long messageSequence);
}

package nsu.networks.snakes.model.inet.unicast;

public interface AcceptorForSender {
    boolean checkAcceptedMessage(long seqNumber);
}

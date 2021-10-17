package nsu.networks.snakes.model.net.multicast;

public interface MulticastPublisherListener {
    long getMessageSequence();
    boolean getOpportunityToJoin();
}

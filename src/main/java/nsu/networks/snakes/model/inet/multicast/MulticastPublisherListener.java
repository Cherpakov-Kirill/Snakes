package nsu.networks.snakes.model.inet.multicast;

public interface MulticastPublisherListener {
    long getMessageSequence();
    boolean getOpportunityToJoin();
}

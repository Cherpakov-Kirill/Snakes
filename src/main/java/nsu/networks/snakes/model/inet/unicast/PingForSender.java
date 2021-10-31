package nsu.networks.snakes.model.inet.unicast;

public interface PingForSender {
    boolean isAlivePlayer(int playerId);
    void setTimeOfSentMessage(int playerId);
}

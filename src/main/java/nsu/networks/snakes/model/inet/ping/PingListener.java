package nsu.networks.snakes.model.inet.ping;

public interface PingListener {
    void disconnectPlayer(int playerId);
    void sendPing(int playerId);
}

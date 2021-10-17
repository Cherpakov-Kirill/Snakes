package nsu.networks.snakes.model.gamecore;

public interface GameCoreListener {
    void updateField(String field);
    void nodeSnakeIsDead(int playerId);
}

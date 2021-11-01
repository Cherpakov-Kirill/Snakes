package nsu.networks.snakes.model;

public class Configuration {
    public static int width = 40;
    public static int height = 30;

    public static SnakesProto.GameConfig defaultConfigBuilder(){
        return configBuilder(width,height,1,1,1000,(float)0.1,100,800);
    }

    public static SnakesProto.GameConfig configBuilder(int width, int height, int foodStatic, float foodPerPlayer, int stateDelay, float deadFoodProb, int pingDelay, int nodeTimeout){
        return SnakesProto.GameConfig.newBuilder()
                .setWidth(width)
                .setHeight(height)
                .setFoodStatic(foodStatic)
                .setFoodPerPlayer(foodPerPlayer)
                .setStateDelayMs(stateDelay)
                .setDeadFoodProb(deadFoodProb)
                .setPingDelayMs(pingDelay)
                .setNodeTimeoutMs(nodeTimeout)
                .build();
    }
}

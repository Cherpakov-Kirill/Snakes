package nsu.networks.snakes.model.node;

import nsu.networks.snakes.model.SnakesProto;

public class Configuration {
    public static int width = 10;
    public static int height = 10;

    public static SnakesProto.GameConfig defaultConfigBuilder(){
        return configBuilder(width,height,1,1,1000,(float)0.1,100,800);
        //todo 1000 ms delay
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

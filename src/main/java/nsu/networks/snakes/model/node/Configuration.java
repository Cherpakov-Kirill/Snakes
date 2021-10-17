package nsu.networks.snakes.model.node;

import nsu.networks.snakes.model.SnakesProto;

public class Configuration {
    public static SnakesProto.GameConfig defaultConfigBuilder(){
        return configBuilder(40,30,1,1,400,(float)0.1,100,800);
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

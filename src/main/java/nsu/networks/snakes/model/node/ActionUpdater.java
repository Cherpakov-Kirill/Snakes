package nsu.networks.snakes.model.node;

import nsu.networks.snakes.model.SnakesProto;
import nsu.networks.snakes.model.gamecore.GameCore;
import nsu.networks.snakes.model.net.messages.MessageBuilder;
import nsu.networks.snakes.model.net.unicast.UnicastSender;

import java.util.List;
import java.util.Map;

public class ActionUpdater extends Thread {
    private final ActionUpdaterListener listener;
    private final SnakesProto.GameConfig config;
    private final GameCore gameCore;
    private final UnicastSender sender;
    private final int nodeId;
    private final Map<Integer, SnakesProto.Direction> playersActions;
    private final List<SnakesProto.GamePlayer> players;

    public ActionUpdater(ActionUpdaterListener listener,
                         SnakesProto.GameConfig config,
                         GameCore gameCore,
                         UnicastSender sender,
                         int nodeId,
                         Map<Integer, SnakesProto.Direction> playersActions,
                         List<SnakesProto.GamePlayer> players) {
        this.listener = listener;
        this.config = config;
        this.gameCore = gameCore;
        this.sender = sender;
        this.nodeId = nodeId;
        this.playersActions = playersActions;
        this.players = players;
    }

    @Override
    public void run() {
        gameCore.updateField();
        try {
            Thread.sleep(config.getStateDelayMs());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (!isInterrupted()) {
            synchronized (playersActions) {
                for (int idPLayer : playersActions.keySet()) {
                    gameCore.makeAction(idPLayer, playersActions.get(idPLayer));
                }
            }
            SnakesProto.GamePlayers gamePlayers = SnakesProto.GamePlayers.newBuilder().addAllPlayers(players).build();
            SnakesProto.GameState state = gameCore.getGameData().setStateOrder(listener.getStateOrder()).setPlayers(gamePlayers).setConfig(config).build();
            gameCore.updateField();
            synchronized (players) {
                for (SnakesProto.GamePlayer player : players) {
                    if (player.getId() != nodeId) {
                        sender.sendMessage(player, MessageBuilder.stateMsgBuilder(state, listener.getMessageSequence(), nodeId, player.getId()));
                    }
                }
            }
            try {
                Thread.sleep(config.getStateDelayMs());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

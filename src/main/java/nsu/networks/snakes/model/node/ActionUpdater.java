package nsu.networks.snakes.model.node;

import nsu.networks.snakes.model.SnakesProto;
import nsu.networks.snakes.model.gamecore.GameCore;
import nsu.networks.snakes.model.net.messages.MessageBuilder;
import nsu.networks.snakes.model.net.unicast.UnicastSender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

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
        int sleepMs = config.getStateDelayMs();
        Timer timer = new Timer(true);
        TimerTask timerTask = new Updater(listener, config, gameCore, sender, nodeId, playersActions, players);
        timer.scheduleAtFixedRate(timerTask, 0, sleepMs);
        while (!isInterrupted()) {
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        timer.cancel();
    }

    static class Updater extends TimerTask {
        private final ActionUpdaterListener listener;
        private final SnakesProto.GameConfig config;
        private final GameCore gameCore;
        private final UnicastSender sender;
        private final int nodeId;
        private final Map<Integer, SnakesProto.Direction> playersActions;
        private final List<SnakesProto.GamePlayer> players;

        Updater(ActionUpdaterListener listener,
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
            synchronized (playersActions) {
                for (int idPLayer : playersActions.keySet()) {
                    gameCore.makeAction(idPLayer, playersActions.get(idPLayer));
                }
            }
            gameCore.updateField();
            SnakesProto.GamePlayers gamePlayers = SnakesProto.GamePlayers.newBuilder().addAllPlayers(players).build();
            SnakesProto.GameState state = gameCore.getGameData().setStateOrder(listener.getStateOrder()).setPlayers(gamePlayers).setConfig(config).build();
            synchronized (players) {
                for (SnakesProto.GamePlayer player : players) {
                    if (player.getId() != nodeId) {
                        sender.sendMessage(player, MessageBuilder.stateMsgBuilder(state, listener.getMessageSequence(), nodeId, player.getId()));
                    }
                }
            }
        }
    }
}

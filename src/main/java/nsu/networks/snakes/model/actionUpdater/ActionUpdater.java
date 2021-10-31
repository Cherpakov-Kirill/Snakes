package nsu.networks.snakes.model.actionUpdater;

import nsu.networks.snakes.model.SnakesProto;

import java.io.IOException;
import java.util.*;

public class ActionUpdater extends Thread {
    private final GameCoreForActionUpdater gameCore;
    private final PlayersForActionUpdater players;
    private final int stateDelay;
    private boolean isAlive;
    private final Map<Integer, SnakesProto.Direction> playersActions;

    public ActionUpdater(GameCoreForActionUpdater gameCore,
                         PlayersForActionUpdater players,
                         int stateDelay) {
        this.gameCore = gameCore;
        this.players = players;
        this.stateDelay = stateDelay;
        this.isAlive = true;
        this.playersActions = new HashMap<>();
    }

    public void setChangeMasterBreakPoint() {
        System.out.println("Action Updater: exit breakpoint was set");
        isAlive = false;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(stateDelay);
            while (isAlive) {
                makeActions();
                players.initiateDeputyPLayer();
                players.sendGameStateToAllPlayers();
                Thread.sleep(stateDelay);
            }
        } catch (/*IOException | */InterruptedException e) {
            System.out.println("Action Updater: " + e.getMessage());
        } finally {
            System.out.println("Action Updater finished");
            players.transferRoleOfTheMaster();
        }
    }

    public void addAction(int playerId, SnakesProto.Direction direction) {
        //synchronized (playersActions) {
            playersActions.put(playerId, direction);
        //}
    }

    public void addNewPlayer(int newPlayerId) {
        //synchronized (playersActions) {
            SnakesProto.Direction playerSnakeDirection = gameCore.getSnakeDirection(newPlayerId);
            playersActions.put(newPlayerId, playerSnakeDirection);
        //}
    }

    public void removePlayer(int playerId) {
        //synchronized (playersActions) {
            playersActions.remove(playerId);
        //}
    }

    private void makeActions() {
        //synchronized (playersActions) {
            for (int idPLayer : new LinkedList<>(playersActions.keySet())) {
                gameCore.makeAction(idPLayer, playersActions.get(idPLayer));
            }
        //}
        gameCore.updateField();
    }
}

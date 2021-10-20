package nsu.networks.snakes.model.node;

import nsu.networks.snakes.model.SnakesProto;
import nsu.networks.snakes.model.gamecore.GameCore;
import nsu.networks.snakes.model.net.unicast.UnicastSender;

import java.util.*;

public class ActionUpdater extends Thread {
    private final ActionUpdaterListener listener;
    private final SnakesProto.GameConfig config;
    private boolean isAlive;

    public ActionUpdater(ActionUpdaterListener listener,
                         SnakesProto.GameConfig config) {
        this.listener = listener;
        this.config = config;
        this.isAlive = true;
    }

    public void setChangeMasterBreakPoint(){
        isAlive = false;
    }

    @Override
    public void run() {
        int sleepMs = config.getStateDelayMs();
        Timer timer = new Timer(true);
        TimerTask timerTask = new Updater(listener);
        timer.scheduleAtFixedRate(timerTask, 0, sleepMs);
        try {
            while (isAlive) {
                Thread.sleep(sleepMs);
            }
        } catch (InterruptedException e) {
            System.out.println("Action Updater: " + e.getMessage());
        } finally {
            timer.cancel();
            listener.changeThisNodeMasterRoleOnViewer();
        }
    }

    static class Updater extends TimerTask {
        private final ActionUpdaterListener listener;

        Updater(ActionUpdaterListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            listener.makeAllChangesOnField();
            listener.initiateDeputyPLayer();
            listener.sendChangesToAllPlayers();
        }
    }
}

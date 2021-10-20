package nsu.networks.snakes.model.node;

import nsu.networks.snakes.model.SnakesProto;
import nsu.networks.snakes.model.gamecore.GameCore;
import nsu.networks.snakes.model.net.unicast.UnicastSender;

import java.util.*;

public class ActionUpdater extends Thread {
    private final ActionUpdaterListener listener;
    private final int stateDelay;
    private boolean isAlive;

    public ActionUpdater(ActionUpdaterListener listener,
                         int stateDelay) {
        this.listener = listener;
        this.stateDelay = stateDelay;
        this.isAlive = true;
    }

    public void setChangeMasterBreakPoint(){
        isAlive = false;
    }

    @Override
    public void run() {
        Timer timer = new Timer(true);
        TimerTask timerTask = new Updater(listener);
        timer.scheduleAtFixedRate(timerTask, 0, stateDelay);
        try {
            while (isAlive) {
                Thread.sleep(stateDelay);
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

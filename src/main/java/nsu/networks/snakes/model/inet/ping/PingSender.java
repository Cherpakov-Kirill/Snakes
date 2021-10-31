package nsu.networks.snakes.model.inet.ping;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

class PingSender extends Thread {
    private final PingListener listener;
    private final Map<Integer, Long> playersPingTime;
    private final int pingDelay;

    PingSender(PingListener listener, int pingDelay) {
        this.listener = listener;
        this.playersPingTime = new HashMap<>();
        this.pingDelay = pingDelay;
    }

    public void removePlayer(int playerId) {
        synchronized (this) {
            playersPingTime.remove(playerId);
        }
    }

    public void setPlayerPingTime(int playerId) {
        //System.out.println("Ping Sender setPlayerPingTime player id = " + playerId);
        playersPingTime.put(playerId, (new Date()).getTime());
    }

    @Override
    public void run() {
        try {
            int delay = pingDelay / 4 > 0 ? pingDelay / 4 : 1;
            while (!isInterrupted()) {
                for (int id : new LinkedList<>(playersPingTime.keySet())) {
                    long now = (new Date()).getTime();
                    if (now - playersPingTime.get(id) > pingDelay) {
                        System.out.println("Ping Sender id = " + id + " Delay = " + (now - playersPingTime.get(id)));
                        listener.sendPing(id);
                    }
                }
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
            System.out.println("Ping Sender: " + e.getMessage());
        } finally {
            playersPingTime.clear();
            System.out.println("Ping Sender finished");

        }
    }
}

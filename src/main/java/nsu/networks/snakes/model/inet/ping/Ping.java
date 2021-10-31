package nsu.networks.snakes.model.inet.ping;

import nsu.networks.snakes.model.inet.unicast.PingForSender;

import java.util.*;

public class Ping extends Thread implements PingForSender {
    private final PingListener listener;
    private final PingSender pingSender;
    private final Map<Integer, Long> lastMessageTimeFromNode;
    private final int nodeTimeOut;

    public Ping(PingListener listener, int pingDelay, int nodeTimeOut) {
        this.listener = listener;
        this.nodeTimeOut = nodeTimeOut;
        this.lastMessageTimeFromNode = new HashMap<>();
        this.pingSender = new PingSender(listener, pingDelay);
    }

    public void setTimeOfReceivedMessage(int playerId) {
        //System.out.println("Ping setTimeOfReceivedMessage player id = " + playerId);
        lastMessageTimeFromNode.put(playerId, (new Date()).getTime());
    }

    @Override
    public void setTimeOfSentMessage(int playerId) {
        pingSender.setPlayerPingTime(playerId);
    }

    @Override
    public boolean isAlivePlayer(int playerId) {
        return lastMessageTimeFromNode.containsKey(playerId);
    }

    public void removePlayer(int playerId) {
        lastMessageTimeFromNode.remove(playerId);
        pingSender.removePlayer(playerId);
        System.out.println("Ping: delete dead node = " + playerId);
    }

    private void checkNodes() {
        synchronized (lastMessageTimeFromNode) {
            for (int idPLayer : new LinkedList<>(lastMessageTimeFromNode.keySet())) {
                long now = (new Date()).getTime();
                if (now - lastMessageTimeFromNode.get(idPLayer) > nodeTimeOut) {
                    listener.disconnectPlayer(idPLayer);
                    lastMessageTimeFromNode.remove(idPLayer);
                    pingSender.removePlayer(idPLayer);
                }
            }
        }
    }

    @Override
    public void run() {
        int delay = nodeTimeOut / 4 > 0 ? nodeTimeOut / 4 : 1;
        pingSender.start();
        try {
            Thread.sleep(delay);
            while (!isInterrupted()) {
                checkNodes();
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
            System.out.println("Ping: " + e.getMessage());
        } finally {
            lastMessageTimeFromNode.clear();
            System.out.println("Ping finished");
            pingSender.interrupt();
        }
    }
}

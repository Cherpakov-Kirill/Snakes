package nsu.networks.snakes.model.net.unicast;

import nsu.networks.snakes.model.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class UnicastSender {
    private final UnicastSenderListener listener;
    private final int pingDelay;
    private final int nodeTimeout;

    private final DatagramSocket socket;

    public UnicastSender(UnicastSenderListener listener, DatagramSocket socket, int pingDelay, int nodeTimeout) {
        this.listener = listener;
        this.socket = socket;
        this.pingDelay = pingDelay;
        this.nodeTimeout = nodeTimeout;
    }

    public void sendMessage(SnakesProto.GamePlayer player, SnakesProto.GameMessage message) {
        if (message.getTypeCase() == SnakesProto.GameMessage.TypeCase.ACK) {
            SenderHandler sender = new SenderHandler(socket, player, message);
            sender.run();
        } else {
            Timer timer = new Timer(true);
            TimerTask timerTask = new SenderHandler(socket, player, message);
            timer.scheduleAtFixedRate(timerTask, 0, pingDelay);
            Date timeOfStart = new Date();
            while (true) {
                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (listener.checkAcceptedMessage(message.getMsgSeq())) {
                    System.out.println("Sender found accepted message. Break.");
                    break;
                } else System.out.println("Sender did not find accepted message. seq = " + message.getMsgSeq());
                if (new Date().getTime() - timeOfStart.getTime() > nodeTimeout) {
                    System.out.println("Node " + player.getIpAddress() + ":" + player.getPort() + " was disconnected");
                    listener.disconnectPlayer(player.getId());
                    //if()
                    break;
                }
            }
            timer.cancel();
        }
    }

    static class SenderHandler extends TimerTask {
        private final SnakesProto.GamePlayer player;
        private final SnakesProto.GameMessage message;
        private final DatagramSocket socket;

        SenderHandler(DatagramSocket socket, SnakesProto.GamePlayer player, SnakesProto.GameMessage message) {
            this.player = player;
            this.message = message;
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                System.out.println("Unicast Sender is sending a " + message.getTypeCase() + " message seq = " + message.getMsgSeq() + " to " + player.getIpAddress() + ":" + player.getPort());
                byte[] buffer = message.toByteArray();
                InetAddress ip = InetAddress.getByName(player.getIpAddress());
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, player.getPort());
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

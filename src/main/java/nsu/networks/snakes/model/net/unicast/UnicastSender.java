package nsu.networks.snakes.model.net.unicast;

import nsu.networks.snakes.model.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class UnicastSender {
    private final SnakesProto.GameConfig config;
    private final Set<Long> acceptedMessages;
    private final DatagramSocket socket;

    public UnicastSender(DatagramSocket socket, SnakesProto.GameConfig config, Set<Long> acceptedMessages) {
        this.config = config;
        this.acceptedMessages = acceptedMessages;
        this.socket = socket;
    }

    public void sendMessage(SnakesProto.GamePlayer player, SnakesProto.GameMessage message) {
        if (message.getTypeCase() == SnakesProto.GameMessage.TypeCase.ACK) {
            SenderHandler sender = new SenderHandler(socket, player, message);
            sender.run();
        } else {
            Timer timer = new Timer(true);
            TimerTask timerTask = new SenderHandler(socket, player, message);
            timer.scheduleAtFixedRate(timerTask, 0, config.getPingDelayMs());
            Date timeOfStart = new Date();
            while (true) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (acceptedMessages) {
                    if (acceptedMessages.contains(message.getMsgSeq())) {
                        System.out.println("Sender found accepted message. Break.");
                        break;
                    } else System.out.println("Sender did not find accepted message.");
                }
                if (new Date().getTime() - timeOfStart.getTime() > config.getNodeTimeoutMs()) {
                    System.out.println("Node " + player.getIpAddress() + ":" + player.getPort() + " was disconnected");
                    ///тут надо поменять мастера на депути и слать пакеты ему
                    //core.deleteNode(receiver);
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
                System.out.println("Unicast Sender is sending a " + message.getTypeCase() + " message to " + player.getIpAddress() + ":" + player.getPort());
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

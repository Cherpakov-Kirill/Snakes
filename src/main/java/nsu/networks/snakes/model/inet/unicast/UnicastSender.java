package nsu.networks.snakes.model.inet.unicast;

import nsu.networks.snakes.model.SnakesProto;
import nsu.networks.snakes.model.inet.ping.Ping;
import nsu.networks.snakes.model.inet.ping.PingListener;
import nsu.networks.snakes.model.messages.MessageAcceptor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class UnicastSender {
    private final DatagramSocket socket;
    private final AcceptorForSender messageAcceptor;
    private final PingForSender ping;

    private final int pingDelay;


    public UnicastSender(DatagramSocket socket, AcceptorForSender messageAcceptor, PingForSender ping, int pingDelay) {
        this.socket = socket;
        this.messageAcceptor = messageAcceptor;
        this.ping = ping;
        this.pingDelay = pingDelay;
    }

    public void sendMessage(SnakesProto.GamePlayer player, SnakesProto.GameMessage message) {
        ping.setTimeOfSentMessage(player.getId());
        if (message.getTypeCase() == SnakesProto.GameMessage.TypeCase.ACK) {
            SenderHandler sender = new SenderHandler(socket, player, message);
            sender.run();
        } else {
            Timer timer = new Timer(true);
            TimerTask timerTask = new SenderHandler(socket, player, message);
            timer.scheduleAtFixedRate(timerTask, 0, pingDelay);
            int delay = pingDelay / 4 > 0 ? pingDelay / 4 : 1;
            while (true) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    if (messageAcceptor.checkAcceptedMessage(message.getMsgSeq())) {
                        System.out.println("Sender found accepted message. Break.");
                        break;
                    } else System.out.println("Sender did not find accepted message. seq = " + message.getMsgSeq());
                    if (!ping.isAlivePlayer(player.getId())) {
                        System.out.println("Sender BREAK seq = " + message.getMsgSeq());
                        break;
                    }
                } catch (Exception e){
                    e.printStackTrace();
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

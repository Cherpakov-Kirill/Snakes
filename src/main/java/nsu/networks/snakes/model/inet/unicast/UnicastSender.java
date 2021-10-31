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
        (new SenderSchedule(socket, messageAcceptor, ping, pingDelay, player, message)).start();
    }

    static class SenderSchedule extends Thread {
        private final SnakesProto.GamePlayer player;
        private final SnakesProto.GameMessage message;
        private final DatagramSocket socket;

        private final AcceptorForSender messageAcceptor;
        private final PingForSender ping;

        private final int pingDelay;

        SenderSchedule(DatagramSocket socket, AcceptorForSender messageAcceptor, PingForSender ping, int pingDelay, SnakesProto.GamePlayer player, SnakesProto.GameMessage message) {
            this.socket = socket;
            this.messageAcceptor = messageAcceptor;
            this.ping = ping;
            this.message = message;
            this.player = player;
            this.pingDelay = pingDelay;
        }

        void send() throws IOException {
            System.out.println("Unicast Sender is sending a " + message.getTypeCase() + " message seq = " + message.getMsgSeq() + " to id=" + player.getId() + " ip:port=" + player.getIpAddress() + ":" + player.getPort());
            byte[] buffer = message.toByteArray();
            InetAddress ip = InetAddress.getByName(player.getIpAddress());
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, player.getPort());
            socket.send(packet);
        }

        @Override
        public void run() {
            ping.setTimeOfSentMessage(player.getId());
            if (message.getTypeCase() == SnakesProto.GameMessage.TypeCase.ACK) {
                try {
                    send();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                while (true) {
                    try {
                        send();
                        Thread.sleep(pingDelay);
                        if (messageAcceptor.checkAcceptedMessage(message.getMsgSeq())) {
                            System.out.println("Sender found accepted message. Break.");
                            break;
                        } else System.out.println("Sender did not find accepted message. seq = " + message.getMsgSeq());
                        if (!ping.isAlivePlayer(player.getId())) {
                            System.out.println("Sender BREAK seq = " + message.getMsgSeq());
                            break;
                        }
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}

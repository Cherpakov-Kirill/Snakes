package nsu.networks.snakes.model.net.multicast;

import nsu.networks.snakes.model.net.messages.MessageBuilder;
import nsu.networks.snakes.model.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class MulticastPublisher extends Thread {
    private DatagramSocket socket = null;

    private final MulticastPublisherListener listener;
    private final int nodeId;
    private final SnakesProto.GameConfig config;
    private final List<SnakesProto.GamePlayer> playersList;


    public MulticastPublisher(MulticastPublisherListener listener, int nodeId, SnakesProto.GameConfig config, List<SnakesProto.GamePlayer> players) {
        this.listener = listener;
        this.nodeId = nodeId;
        this.config = config;
        this.playersList = players;
    }

    private byte[] getMessageBytes() {
        SnakesProto.GameMessage message;
        SnakesProto.GamePlayers playersMessage = SnakesProto.GamePlayers.newBuilder().addAllPlayers(playersList).build();
        message = MessageBuilder.announcementMsgBuilder(playersMessage,config,listener.getOpportunityToJoin(),listener.getMessageSequence(),nodeId);
        return message.toByteArray();
    }

    public void run() {
        try {
            System.out.println("Multicast publisher started");
            InetAddress group = InetAddress.getByName("239.192.0.4");
            int port = 9192;
            this.socket = new DatagramSocket();
            while (!isInterrupted()) {
                byte[] messageBuffer = getMessageBytes();
                DatagramPacket packet = new DatagramPacket(messageBuffer, messageBuffer.length, group, port);
                socket.send(packet);
                Thread.sleep(1000);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Multicast publisher: " + e.getMessage());
        } finally {
            if (socket != null) {
                socket.close();
            }
            System.out.println("Multicast publisher finished");
        }
    }
}

package nsu.networks.snakes.model.net.multicast;

import nsu.networks.snakes.model.net.messages.MessageBuilder;
import nsu.networks.snakes.model.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MulticastPublisher extends Thread {
    private DatagramSocket socket = null;

    private final MulticastPublisherListener listener;
    private final int nodeId;
    private boolean opportunityToJoin;
    private long messageSequence;
    private final SnakesProto.GameConfig config;
    private final SnakesProto.GamePlayers players;

    public MulticastPublisher(MulticastPublisherListener listener, int nodeId, SnakesProto.GameConfig config, SnakesProto.GamePlayers players) {
        this.listener = listener;
        this.nodeId = nodeId;
        this.config = config;
        this.players = players;
    }

    private void updateData() {
        opportunityToJoin = listener.getOpportunityToJoin();
        messageSequence = listener.getMessageSequence();
    }

    private byte[] getMessage() {
        SnakesProto.GameMessage message;
        synchronized (players){
            message = MessageBuilder.announcementMsgBuilder(players,config,opportunityToJoin,messageSequence,nodeId);
        }
        return message.toByteArray();
    }

    public void run() {
        try {
            System.out.println("Multicast Publisher started");
            InetAddress group = InetAddress.getByName("239.192.0.4");
            int port = 9192;
            this.socket = new DatagramSocket();
            while (!isInterrupted()) {
                updateData();
                byte[] messageBuffer = getMessage();
                DatagramPacket packet = new DatagramPacket(messageBuffer, messageBuffer.length, group, port);
                //System.out.println("Sender sent " + packet.getLength()+" bytes");
                socket.send(packet);
                Thread.sleep(1000);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Multicast Publisher finished");
            if (socket != null) {
                socket.close();
            }
        }
    }
}

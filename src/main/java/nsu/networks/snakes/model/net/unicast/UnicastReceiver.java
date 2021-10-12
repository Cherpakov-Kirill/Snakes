package nsu.networks.snakes.model.net.unicast;

import nsu.networks.snakes.model.Node;
import nsu.networks.snakes.model.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Set;

public class UnicastReceiver extends Thread {
    private final Set<Long> acceptedMessages;
    private final DatagramSocket socket;
    private final Node node;

    public UnicastReceiver(DatagramSocket socket, Set<Long> acceptedMessages, Node node) {
        this.acceptedMessages = acceptedMessages;
        this.socket = socket;
        this.node = node;
    }

    public void messageTypeHandler(SnakesProto.GameMessage msg, InetAddress address, int port) {
        SnakesProto.GamePlayer messageSender = node.getGamePLayerById(msg.getSenderId());
        long messageSequence = msg.getMsgSeq();
        switch (msg.getTypeCase()) {
            case PING -> {
                node.acceptMessage(messageSender, messageSequence);
                System.out.println("PING");
            }
            case STEER -> {
                node.acceptMessage(messageSender, messageSequence);
                node.receiveSteerMsg(msg.getSteer().getDirection(), msg.getSenderId());
                System.out.println("STEER");
            }
            case ACK -> {
                synchronized (acceptedMessages) {
                    acceptedMessages.add(messageSequence);
                    System.out.println("ACK");
                    node.checkIdSetting(msg.getReceiverId());
                    System.out.println("Accepted: " + messageSequence);
                }
            }
            case STATE -> {
                node.acceptMessage(messageSender, messageSequence);
                node.receiveGameStateMsg(msg.getState().getState(), address.getHostAddress());
                System.out.println("STATE");
            }
            case JOIN -> {
                SnakesProto.GameMessage.JoinMsg joinMsg = msg.getJoin();
                int newPlayerId = node.addPlayer(joinMsg.getName(), address.getHostAddress(), port, joinMsg.getOnlyView() ? SnakesProto.NodeRole.VIEWER : SnakesProto.NodeRole.NORMAL, joinMsg.getPlayerType());
                node.acceptMessage(node.getGamePLayerById(newPlayerId), messageSequence);
                System.out.println("JOIN");
            }
            case ERROR -> {
                node.acceptMessage(messageSender, messageSequence);
                System.out.println("ERROR");
            }
            case ROLE_CHANGE -> {
                node.acceptMessage(messageSender, messageSequence);
                System.out.println("ROLE_CHANGE");
            }
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("Unicast Receiver started");
            while (true) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                //System.out.println("Unicast Receiver ready to receive");
                socket.receive(packet);
                //System.out.println("Receiver got " + packet.getLength() + " bytes");
                byte[] gotBytes = new byte[packet.getLength()];
                System.arraycopy(buffer, 0, gotBytes, 0, packet.getLength());
                SnakesProto.GameMessage msg = SnakesProto.GameMessage.parseFrom(gotBytes);
                //System.out.println(" RECEIVER GOT OBJECT (" + System.identityHashCode(msg) + "): " + msg);
                messageTypeHandler(msg, packet.getAddress(), packet.getPort());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Unicast Receiver finished");
        }
    }
}

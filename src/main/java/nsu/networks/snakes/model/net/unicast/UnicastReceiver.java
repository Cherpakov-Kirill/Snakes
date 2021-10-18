package nsu.networks.snakes.model.net.unicast;

import nsu.networks.snakes.model.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Set;

public class UnicastReceiver extends Thread {
    private final Set<Long> acceptedMessages;
    private final DatagramSocket socket;
    private final UnicastReceiverListener listener;

    public UnicastReceiver(DatagramSocket socket, Set<Long> acceptedMessages, UnicastReceiverListener listener) {
        this.acceptedMessages = acceptedMessages;
        this.socket = socket;
        this.listener = listener;
    }

    public void messageTypeHandler(SnakesProto.GameMessage msg, InetAddress address, int port) {
        int messageSenderId = msg.getSenderId();
        long messageSequence = msg.getMsgSeq();
        switch (msg.getTypeCase()) {
            case PING -> {
                listener.acceptMessage(messageSenderId, messageSequence);
                System.out.println("PING id:" + messageSenderId + " seq=" + messageSequence);
            }
            case STEER -> {
                listener.acceptMessage(messageSenderId, messageSequence);
                listener.receiveSteerMsg(msg.getSteer().getDirection(), messageSenderId);
                System.out.println("STEER id:" + messageSenderId + " seq=" + messageSequence);
            }
            case ACK -> {
                synchronized (acceptedMessages) {
                    listener.receiveAckMsg(msg.getReceiverId(),messageSequence);
                    System.out.println("ACK id:" + messageSenderId + " seq=" + messageSequence);
                }
            }
            case STATE -> {
                listener.acceptMessage(messageSenderId, messageSequence);
                listener.receiveGameStateMsg(msg.getState().getState(), address.getHostAddress());
                System.out.println("STATE id:" + messageSenderId + " seq=" + messageSequence);
            }
            case JOIN -> {
                SnakesProto.GameMessage.JoinMsg joinMsg = msg.getJoin();
                int newPlayerId = listener.receiveJoinMsg(joinMsg.getName(),
                        address.getHostAddress(),
                        port,
                        joinMsg.getOnlyView() ? SnakesProto.NodeRole.VIEWER : SnakesProto.NodeRole.NORMAL,
                        joinMsg.getPlayerType());
                listener.acceptMessage(newPlayerId, messageSequence);
                System.out.println("JOIN id:" + messageSenderId + " seq=" + messageSequence);
            }
            case ERROR -> {
                listener.acceptMessage(messageSenderId, messageSequence);
                System.out.println("ERROR id:" + messageSenderId + " seq=" + messageSequence);
            }
            case ROLE_CHANGE -> {
                listener.acceptMessage(messageSenderId, messageSequence);
                System.out.println("ROLE_CHANGE id:" + messageSenderId + "seq=" + messageSequence);
            }
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("Unicast Receiver started");
            while (!isInterrupted()) {
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

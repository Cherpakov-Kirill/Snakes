package nsu.networks.snakes.model.net.unicast;

import nsu.networks.snakes.model.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UnicastReceiver extends Thread {
    private final DatagramSocket socket;
    private final UnicastReceiverListener listener;

    public UnicastReceiver(UnicastReceiverListener listener, DatagramSocket socket) {
        this.listener = listener;
        this.socket = socket;
    }

    public void messageTypeHandler(SnakesProto.GameMessage msg, InetAddress address, int port) {
        int messageSenderId = msg.getSenderId();
        long messageSequence = msg.getMsgSeq();
        switch (msg.getTypeCase()) {
            case PING -> {
                System.out.println("PING id:" + messageSenderId + " seq=" + messageSequence);
                listener.acceptMessage(messageSenderId, messageSequence);
            }
            case STEER -> {
                System.out.println("STEER id:" + messageSenderId + " seq=" + messageSequence);
                listener.acceptMessage(messageSenderId, messageSequence);
                listener.receiveSteerMsg(msg.getSteer().getDirection(), messageSenderId);
            }
            case ACK -> {
                System.out.println("ACK id:" + messageSenderId + " seq=" + messageSequence);
                listener.receiveAckMsg(msg.getReceiverId(), messageSequence);
            }
            case STATE -> {
                System.out.println("STATE id:" + messageSenderId + " seq=" + messageSequence);
                listener.acceptMessage(messageSenderId, messageSequence);
                listener.receiveGameStateMsg(msg.getState().getState(), address.getHostAddress());
            }
            case JOIN -> {
                System.out.println("JOIN id:" + messageSenderId + " seq=" + messageSequence);
                SnakesProto.GameMessage.JoinMsg joinMsg = msg.getJoin();
                int newPlayerId = listener.receiveJoinMsg(joinMsg.getName(),
                        address.getHostAddress(),
                        port,
                        joinMsg.getOnlyView() ? SnakesProto.NodeRole.VIEWER : SnakesProto.NodeRole.NORMAL,
                        joinMsg.getPlayerType());
                listener.acceptMessage(newPlayerId, messageSequence);

            }
            case ERROR -> {
                System.out.println("ERROR id:" + messageSenderId + " seq=" + messageSequence);
                listener.acceptMessage(messageSenderId, messageSequence);
            }
            case ROLE_CHANGE -> {
                System.out.println("ROLE_CHANGE " + msg.getRoleChange().getReceiverRole() + " id:" + messageSenderId + " seq=" + messageSequence);
                listener.acceptMessage(messageSenderId, messageSequence);
                listener.receiveRoleChangeMsg(msg.getRoleChange());
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
                socket.receive(packet);
                byte[] gotBytes = new byte[packet.getLength()];
                System.arraycopy(buffer, 0, gotBytes, 0, packet.getLength());
                SnakesProto.GameMessage msg = SnakesProto.GameMessage.parseFrom(gotBytes);
                messageTypeHandler(msg, packet.getAddress(), packet.getPort());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Unicast Receiver finished");
        }
    }
}

package nsu.networks.snakes.model.net.multicast;

import nsu.networks.snakes.model.SnakesProto;

import java.io.IOException;
import java.net.*;

public class MulticastReceiver extends Thread {
    private MulticastSocket socket = null;
    private final MulticastReceiverListener listener;

    public MulticastReceiver(MulticastReceiverListener listener){
        this.listener = listener;
    }

    SnakesProto.GamePlayer getMasterPlayer(SnakesProto.GameMessage msg){
        for(SnakesProto.GamePlayer player : msg.getAnnouncement().getPlayers().getPlayersList()){
            if(player.getRole() == SnakesProto.NodeRole.MASTER){
                return player;
            }
        }
        return null;
    }

    @Override
    public void run() {
        InetAddress ip;
        int port;
        NetworkInterface netIf;
        InetSocketAddress group;
        try {
            System.out.println("Multicast receiver started");
            ip = InetAddress.getByName("239.192.0.4");
            port = 9192;
            socket = new MulticastSocket(port);
            netIf = NetworkInterface.getByInetAddress(ip);
            group = new InetSocketAddress(ip, port);
            socket.joinGroup(group, netIf);
            while (!isInterrupted()) {
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                if(isInterrupted()) break;
                byte[] gotBytes = new byte[packet.getLength()];
                System.arraycopy(buf, 0, gotBytes, 0, packet.getLength());
                SnakesProto.GameMessage msg = SnakesProto.GameMessage.parseFrom(gotBytes);
                //System.out.println(" RECEIVER GOT OBJECT (" + System.identityHashCode(msg) + "): " + msg);
                SnakesProto.GamePlayer master = getMasterPlayer(msg);
                master = master.toBuilder().setIpAddress(packet.getAddress().getHostAddress()).build();
                listener.receiveAnnouncementMsg(msg.getAnnouncement(),master);
            }
            socket.leaveGroup(group,netIf);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Multicast receiver finished");
            if (socket != null) {
                socket.close();
            }
        }
    }
}

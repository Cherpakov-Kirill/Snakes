package nsu.networks.snakes.model.inet.multicast;

import nsu.networks.snakes.model.SnakesProto;

import java.io.IOException;
import java.net.*;

public class MulticastReceiver extends Thread {
    private MulticastSocket socket = null;
    private final MulticastReceiverListener listener;

    public MulticastReceiver(MulticastReceiverListener listener) {
        this.listener = listener;
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
                if (isInterrupted()) break;
                byte[] gotBytes = new byte[packet.getLength()];
                System.arraycopy(buf, 0, gotBytes, 0, packet.getLength());
                SnakesProto.GameMessage msg = SnakesProto.GameMessage.parseFrom(gotBytes);
                listener.receiveAnnouncementMsg(msg.getAnnouncement(), packet.getAddress().getHostAddress());
            }
            socket.leaveGroup(group, netIf);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
            System.out.println("Multicast receiver finished");
        }
    }
}

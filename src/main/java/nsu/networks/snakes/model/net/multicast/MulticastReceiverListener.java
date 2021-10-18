package nsu.networks.snakes.model.net.multicast;

import nsu.networks.snakes.model.SnakesProto;

public interface MulticastReceiverListener {
    void receiveAnnouncementMsg(SnakesProto.GameMessage.AnnouncementMsg msg,String masterIp);
}

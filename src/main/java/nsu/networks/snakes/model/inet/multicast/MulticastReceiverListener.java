package nsu.networks.snakes.model.inet.multicast;

import nsu.networks.snakes.model.SnakesProto;

public interface MulticastReceiverListener {
    void receiveAnnouncementMsg(SnakesProto.GameMessage.AnnouncementMsg msg,String masterIp);
}

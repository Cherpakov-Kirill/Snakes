package nsu.networks.snakes.model.net.multicast;

import nsu.networks.snakes.model.SnakesProto;

public interface MulticastListener {
    void receiveAnnouncementMsg(SnakesProto.GameMessage.AnnouncementMsg msg, SnakesProto.GamePlayer masterPLayer);
}

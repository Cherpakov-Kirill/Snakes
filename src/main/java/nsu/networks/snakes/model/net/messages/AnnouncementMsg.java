package nsu.networks.snakes.model.net.messages;

import nsu.networks.snakes.model.SnakesProto;

import java.util.Date;

public class AnnouncementMsg {
    public SnakesProto.GameMessage.AnnouncementMsg gameMessage;
    public SnakesProto.GamePlayer master;
    private long timeOfLastReceivedMsg;

    public static String makeKeyForAnnouncementMsg(String address, int port) {
        return address + ":" + port;
    }

    public static String makeKeyForAnnouncementMsg(String keyGame) {
        String[] s = keyGame.split(":");
        return s[0] + ":" + s[1];
    }

    public String getKeyForMap() {
        return makeKeyForAnnouncementMsg(master.getIpAddress(), master.getPort());
    }

    public void updateTime() {
        Date now = new Date();
        this.timeOfLastReceivedMsg = now.getTime();
    }

    public boolean isActual() {
        Date now = new Date();
        return now.getTime() - timeOfLastReceivedMsg < 6000;
    }

    public AnnouncementMsg(SnakesProto.GameMessage.AnnouncementMsg gameMessage, SnakesProto.GamePlayer masterPlayer) {
        Date now = new Date();
        this.gameMessage = gameMessage;
        this.timeOfLastReceivedMsg = now.getTime();
        this.master = masterPlayer;
    }
}

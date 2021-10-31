package nsu.networks.snakes.model.inet;

import nsu.networks.snakes.model.SnakesProto;
import nsu.networks.snakes.model.inet.ping.Ping;
import nsu.networks.snakes.model.inet.ping.PingListener;
import nsu.networks.snakes.model.messages.AnnouncementMsg;
import nsu.networks.snakes.model.messages.MessageAcceptor;
import nsu.networks.snakes.model.messages.MessageAcceptorListener;
import nsu.networks.snakes.model.messages.MessageBuilder;
import nsu.networks.snakes.model.inet.multicast.MulticastPublisher;
import nsu.networks.snakes.model.inet.multicast.MulticastPublisherListener;
import nsu.networks.snakes.model.inet.multicast.MulticastReceiver;
import nsu.networks.snakes.model.inet.multicast.MulticastReceiverListener;
import nsu.networks.snakes.model.inet.unicast.*;
import nsu.networks.snakes.model.players.InetForPlayers;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

import static nsu.networks.snakes.model.messages.AnnouncementMsg.makeKeyForAnnouncementMsg;

public class InetController implements MulticastPublisherListener, MulticastReceiverListener, UnicastReceiverListener, MessageAcceptorListener, PingListener, InetForPlayers {
    private final InetControllerListener listener;
    private PlayersForInet players;
    private DatagramSocket socket;
    private final MessageAcceptor messageAcceptor;
    private final Ping ping;
    private final UnicastSender sender;
    private final UnicastReceiver receiver;
    private MulticastPublisher inviteSender;
    private MulticastReceiver inviteReceiver;
    private long messageSequence;
    private final Map<String, AnnouncementMsg> announcementMsgMap;

    public InetController(InetControllerListener listener, int port, int pingDelayMs, int nodeTimeOutMs) {
        this.listener = listener;
        try {
            socket = new DatagramSocket(port);
            System.out.println("Socket port " + port);
        } catch (SocketException e) {
            System.err.println("ERROR: Node did not started");
            e.printStackTrace();
        }
        this.messageAcceptor = new MessageAcceptor(this);
        this.ping = new Ping(this,pingDelayMs,nodeTimeOutMs);
        this.ping.start();
        this.sender = new UnicastSender(socket, messageAcceptor, ping, pingDelayMs);
        this.receiver = new UnicastReceiver(this, socket, messageAcceptor);
        this.receiver.start();
        this.messageSequence = 0;
        this.announcementMsgMap = new HashMap<>();
        startMulticastReceiver();
    }

    public void interruptUnicast(){
        ping.interrupt();
        receiver.interrupt();
    }

    public void attachPlayers(PlayersForInet players){
        this.players = players;
    }

    public void startMulticastReceiver() {
        inviteReceiver = new MulticastReceiver(this);
        announcementMsgMap.clear();
        inviteReceiver.start();
    }

    public void stopMulticastReceiver() {
        if (inviteReceiver != null) inviteReceiver.interrupt();
    }

    public void startMulticastPublisher(int nodeId, SnakesProto.GameConfig config, List<SnakesProto.GamePlayer> players) {
        inviteSender = new MulticastPublisher(this,
                nodeId,
                config,
                players);
        inviteSender.start();
    }

    public void stopMulticastPublisher() {
        if (inviteSender != null) inviteSender.interrupt();
    }

    public synchronized long getMessageSequence() {
        messageSequence++;
        return messageSequence;
    }

    public AnnouncementMsg getAnnouncementMsg(String key) {
        return announcementMsgMap.get(key);
    }


    @Override
    public void sendAckMessage(SnakesProto.GamePlayer player, SnakesProto.GameMessage message) {
        sender.sendMessage(player, message);
    }

    @Override
    public void setTimeOfReceivedMessage(int playerId) {
        ping.setTimeOfReceivedMessage(playerId);
    }

    @Override
    public void sendMessage(SnakesProto.GamePlayer player, SnakesProto.GameMessage message) {
        sender.sendMessage(player, MessageBuilder.setMessageSequence(message, getMessageSequence()));
    }


    @Override
    public void launchGameCore(int playerId) {
        listener.launchGameCore(playerId);
    }

    @Override
    public SnakesProto.GamePlayer getGamePlayerById(int id) {
        return players.getGamePLayerById(id);
    }

    @Override
    public void receiveAnnouncementMsg(SnakesProto.GameMessage.AnnouncementMsg msg, String masterIp) {
        try {
            SnakesProto.GamePlayer master = Objects.requireNonNull(AnnouncementMsg.getMasterPlayerFromGameMessage(msg)).toBuilder().setIpAddress(masterIp).build();
            synchronized (announcementMsgMap) {
                String key = makeKeyForAnnouncementMsg(master.getIpAddress(), master.getPort());
                if (!announcementMsgMap.containsKey(key)) {
                    announcementMsgMap.put(key, new AnnouncementMsg(msg, master));
                } else {
                    announcementMsgMap.get(key).updateTime();
                }
                List<String> gamesList = new LinkedList<>();
                for (AnnouncementMsg announcementMsg : new LinkedList<>(announcementMsgMap.values())) {
                    if (!announcementMsg.isActual()) {
                        announcementMsgMap.remove(announcementMsg.getKeyForMap());
                    } else {
                        if (announcementMsg.gameMessage.getCanJoin()) {
                            gamesList.add(announcementMsg.getKeyForMap() + ":" + announcementMsg.gameMessage.getPlayers().getPlayersCount());
                        }
                    }
                }
                listener.updateFindGameList(gamesList);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveGameStateMsg(SnakesProto.GameState gameState, String masterIp) {
        listener.receiveGameStateMsg(gameState, masterIp);
    }

    @Override
    public void receiveRoleChangeMsg(SnakesProto.GameMessage.RoleChangeMsg roleChangeMsg, int senderId) {
        listener.receiveRoleChangeMsg(roleChangeMsg, senderId);
    }

    @Override
    public void receiveSteerMsg(SnakesProto.Direction direction, int senderId) {
        listener.receiveSteerMsg(direction, senderId);
    }

    @Override
    public int receiveJoinMsg(String name, String ip, int port, SnakesProto.NodeRole role, SnakesProto.PlayerType type) {
        return listener.receiveJoinMsg(name, ip, port, role, type);
    }

    @Override
    public void disconnectPlayer(int playerId) {
        players.disconnectPlayer(playerId);
    }

    @Override
    public void sendPing(int playerId) {
        players.sendPing(playerId);
    }

    @Override
    public void removePlayerFromPing(int playerId){
        ping.removePlayer(playerId);
    }

    @Override
    public boolean getOpportunityToJoin() {
        return listener.getOpportunityToJoin();
    }
}

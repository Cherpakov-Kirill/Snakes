package nsu.networks.snakes.model.node;

import nsu.networks.snakes.model.*;
import nsu.networks.snakes.model.gamecore.GameCore;
import nsu.networks.snakes.model.gamecore.GameCoreListener;
import nsu.networks.snakes.model.net.messages.AnnouncementMsg;
import nsu.networks.snakes.model.net.messages.MessageBuilder;
import nsu.networks.snakes.model.net.multicast.MulticastPublisherListener;
import nsu.networks.snakes.model.net.multicast.MulticastReceiverListener;
import nsu.networks.snakes.model.net.multicast.MulticastPublisher;
import nsu.networks.snakes.model.net.multicast.MulticastReceiver;
import nsu.networks.snakes.model.net.unicast.UnicastReceiver;
import nsu.networks.snakes.model.net.unicast.UnicastReceiverListener;
import nsu.networks.snakes.model.net.unicast.UnicastSender;
import nsu.networks.snakes.model.net.unicast.UnicastSenderListener;

import static nsu.networks.snakes.model.net.messages.AnnouncementMsg.makeKeyForAnnouncementMsg;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

public class Node implements GameCoreListener, MulticastPublisherListener, MulticastReceiverListener, UnicastReceiverListener, UnicastSenderListener, ActionUpdaterListener {
    private final NodeListener listener;
    private int id;
    private final String name;
    private final int port;
    private SnakesProto.NodeRole nodeRole;
    private SnakesProto.PlayerType playerType;
    private SnakesProto.GameConfig config;
    private List<SnakesProto.GamePlayer> players;
    private SnakesProto.GamePlayer master;
    private SnakesProto.GamePlayer deputy;
    private final Map<Integer, SnakesProto.Direction> playersActions;
    private SnakesProto.Direction thisNodeAction;
    private ActionUpdater actionUpdater;

    private boolean opportunityToJoin;
    private long messageSequence;
    private int stateOrder;

    private GameCore gameCore;

    private final Set<Long> acceptedMessages;
    private final Map<String, AnnouncementMsg> announcementMsgMap;
    private DatagramSocket socket;
    private MulticastPublisher inviteSender;
    private MulticastReceiver inviteReceiver;
    private UnicastSender sender;
    private UnicastReceiver receiver;

    public Node(NodeListener listener, String name, int port, SnakesProto.PlayerType type) {
        this.listener = listener;
        this.id = -1; //not received id of this node or not started game on Master
        this.config = Configuration.defaultConfigBuilder();
        this.name = name;
        this.port = port;
        this.playerType = type;
        this.nodeRole = SnakesProto.NodeRole.VIEWER;
        this.players = new LinkedList<>();
        this.playersActions = new HashMap<>();
        this.opportunityToJoin = true;
        this.messageSequence = 1;
        this.stateOrder = 0;
        this.acceptedMessages = new HashSet<>();
        try {
            socket = new DatagramSocket(port);
            System.out.println("Socket port " + port);
        } catch (SocketException e) {
            System.err.println("ERROR: Node did not started");
            e.printStackTrace();
        }
        this.announcementMsgMap = new HashMap<>();
        startMulticastReceiver();
        sender = new UnicastSender(this,socket, config, acceptedMessages);
        receiver = new UnicastReceiver(socket, acceptedMessages, this);
        receiver.start();
    }

    //Multicast Control
    private void startMulticastReceiver() {
        if (inviteReceiver == null) inviteReceiver = new MulticastReceiver(this);
        announcementMsgMap.clear();
        inviteReceiver.start();
    }

    private void stopMulticastReceiver() {
        if (inviteReceiver != null && inviteReceiver.isAlive()) inviteReceiver.interrupt();
    }

    private void startMulticastPublisher() {
        if (inviteSender == null) inviteSender = new MulticastPublisher(this,
                id,
                config,
                players);
        inviteSender.start();
    }

    private void stopMulticastPublisher() {
        if (inviteSender != null && inviteSender.isAlive()) inviteSender.interrupt();
    }

    //Utils
    private SnakesProto.GamePlayer getGamePLayerById(int id) {
        for (SnakesProto.GamePlayer player : players) {
            if (player.getId() == id) {
                return player;
            }
        }
        return null;
    }

    private int getPlayerIdByIPAndPort(String ip, int port) {
        for (SnakesProto.GamePlayer player : players) {
            if (player.getIpAddress().equals(ip) && player.getPort() == port) return player.getId();
        }
        return 0;
    }

    private static SnakesProto.Direction reverseDirection(SnakesProto.Direction direction) {
        switch (direction) {
            case RIGHT -> {
                return SnakesProto.Direction.LEFT;
            }
            case LEFT -> {
                return SnakesProto.Direction.RIGHT;
            }
            case UP -> {
                return SnakesProto.Direction.DOWN;
            }
            case DOWN -> {
                return SnakesProto.Direction.UP;
            }
        }
        return null;
    }

    private int addPlayer(String name, String ip, int port, SnakesProto.NodeRole role, SnakesProto.PlayerType type) {
        int newPlayerId = getPlayerIdByIPAndPort(ip, port);
        if (newPlayerId == 0) {
            if (players.size() == 0) newPlayerId = 1;
            else newPlayerId = players.get(players.size() - 1).getId() + 1;
            SnakesProto.GamePlayer newPlayer = SnakesProto.GamePlayer
                    .newBuilder()
                    .setName(name)
                    .setId(newPlayerId)
                    .setIpAddress(ip)
                    .setPort(port)
                    .setRole(role)
                    .setType(type)
                    .setScore(0)
                    .build();
            players.add(newPlayer);
            if (role != SnakesProto.NodeRole.VIEWER) {
                gameCore.addNewPlayer(newPlayerId);
                synchronized (playersActions) {
                    SnakesProto.Direction playerSnakeDirection = gameCore.getSnakeDirection(newPlayerId);
                    playersActions.put(newPlayerId, playerSnakeDirection);
                    if (newPlayerId == 1) thisNodeAction = playerSnakeDirection;
                }
            }
        }
        return newPlayerId;
    }

    private void deletePlayer(int playerId){
        int index = 0;
        for(SnakesProto.GamePlayer player : players){
            if(player.getId() == playerId) break;
            index++;
        }
        players.remove(index);
    }

    @Override
    public void disconnectPlayer(int playerId){
        deletePlayer(playerId);
    }

    private void changeNodeRole(SnakesProto.NodeRole role) {
        switch (role) {
            case MASTER -> {
                stopMulticastReceiver();
                nodeRole = SnakesProto.NodeRole.MASTER;
                playersActions.clear(); //todo make parse of last actions when deputy becomes master-node
                startMulticastPublisher();
                actionUpdater = new ActionUpdater(this, config, gameCore, sender, id, playersActions, players);
                actionUpdater.start();
            }
            case NORMAL, DEPUTY -> {
                if (actionUpdater != null) actionUpdater.interrupt();
                stopMulticastReceiver();
                stopMulticastPublisher();
            }
            case VIEWER -> {
                if (actionUpdater != null) actionUpdater.interrupt();
                stopMulticastReceiver();
                stopMulticastPublisher();
                switch (nodeRole) {
                    case MASTER -> {

                    }
                }
            }
        }
        this.nodeRole = role;
    }

    @Override
    public synchronized void nodeSnakeIsDead(int playerId) {
        System.out.println("Node snake is dead id = " + playerId);
        synchronized (playersActions) {
            playersActions.remove(playerId);
        }
        //changeNodeRole(SnakesProto.NodeRole.VIEWER);//todo send to player message about change role
    }


    //Launching game
    private void launchGameCore() {
        gameCore = new GameCore(this, config, id);
    }

    public void createNewGame(SnakesProto.GameConfig config, int nodeId) {
        this.config = config;
        this.id = nodeId;
        launchGameCore();
        changeNodeRole(SnakesProto.NodeRole.MASTER);
        addPlayer(name, "", port, nodeRole, playerType);
    }

    public void joinTheGame(String keyGame, SnakesProto.NodeRole newNodeRole) {
        synchronized (announcementMsgMap) {
            stopMulticastReceiver();
            String ketMap = makeKeyForAnnouncementMsg(keyGame);
            AnnouncementMsg msg = announcementMsgMap.get(ketMap);
            if (msg != null) {
                changeNodeRole(newNodeRole);
                this.config = msg.gameMessage.getConfig();
                this.players = msg.gameMessage.getPlayers().getPlayersList();
                listener.openFieldWindow(config.getWidth(), config.getHeight());
                sender.sendMessage(msg.master, MessageBuilder.joinMsgBuilder(playerType, newNodeRole == SnakesProto.NodeRole.VIEWER, name, getMessageSequence(), 0, 0));
            }
        }
    }

    //Receive functions
    private void assignMasterIp(List<SnakesProto.GamePlayer> playersListFromMessage, String ip) {
        for (SnakesProto.GamePlayer player : playersListFromMessage) {
            switch (player.getRole()) {
                case MASTER -> {
                    master = player.toBuilder().setIpAddress(ip).build();
                }
                case DEPUTY -> {
                    deputy = player;
                }
            }
        }
    }

    @Override
    public void receiveAnnouncementMsg(SnakesProto.GameMessage.AnnouncementMsg msg, String masterIp) {
        assignMasterIp(msg.getPlayers().getPlayersList(), masterIp);
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
    }

    @Override
    public void receiveGameStateMsg(SnakesProto.GameState gameState, String masterIp) {
        if (gameState.getStateOrder() > stateOrder) {
            assignMasterIp(gameState.getPlayers().getPlayersList(), masterIp);
            stateOrder = gameState.getStateOrder();
            players = gameState.getPlayers().getPlayersList();
            //todo set IP in players for master
            for (SnakesProto.GamePlayer player : players) {
                switch (player.getRole()) {
                    case MASTER -> {
                        master = player.toBuilder().setIpAddress(masterIp).build();
                        assert getGamePLayerById(master.getId()) != null;
                        getGamePLayerById(master.getId()).toBuilder().setIpAddress(masterIp).build();
                    }
                    case DEPUTY -> deputy = player;
                }
            }
            gameCore.updateGameState(gameState);
        }
    }

    @Override
    public void receiveSteerMsg(SnakesProto.Direction direction, int playerId) {
        synchronized (playersActions) {
            playersActions.put(playerId, direction);
        }
    }

    @Override
    public int receiveJoinMsg(String name, String ip, int port, SnakesProto.NodeRole role, SnakesProto.PlayerType type) {
        return addPlayer(name, ip, port, role, type);
    }

    @Override
    public void receiveAckMsg(int playerId, long messageSequence) {
        synchronized (acceptedMessages){
            acceptedMessages.add(messageSequence);
            if(acceptedMessages.contains(messageSequence)) System.out.println("acceptedMessages.contains("+messageSequence+")");
            else System.out.println("acceptedMessages.doesNotContain("+messageSequence+")");
        }
        if (this.id == -1) {
            this.id = playerId;
            launchGameCore();
        }
    }

    //Accept message to snender
    @Override
    public void acceptMessage(int playerId, long messageSequence) {
        SnakesProto.GamePlayer player = getGamePLayerById(playerId);
        assert player != null;
        if (player.getRole() == SnakesProto.NodeRole.MASTER){
            player = master;
        }
        sender.sendMessage(player, MessageBuilder.ackMsgBuilder(messageSequence, id, playerId));
    }

    //Keyboard listener
    public void setKeyboardAction(SnakesProto.Direction direction) {
        if ((stateOrder != 0 || nodeRole == SnakesProto.NodeRole.MASTER) && nodeRole != SnakesProto.NodeRole.VIEWER && reverseDirection(gameCore.getSnakeDirection(id)) != direction) {
            switch (nodeRole) {
                case MASTER -> {
                    synchronized (playersActions) {
                        playersActions.put(id, direction);
                    }
                }
                case DEPUTY, NORMAL -> sender.sendMessage(master, MessageBuilder.steerMsgBuilder(direction, getMessageSequence(), id, master.getId()));
            }
        }
    }

    //Get updating parameters
    @Override
    public synchronized long getMessageSequence() {
        messageSequence++;
        return messageSequence;
    }

    @Override
    public synchronized int getStateOrder() {
        stateOrder++;
        return stateOrder;
    }

    @Override
    public synchronized boolean getOpportunityToJoin() {
        opportunityToJoin = gameCore.getOpportunityToJoin();
        return opportunityToJoin;
    }

    //Update UI visualization of map
    @Override
    public void updateField(String field) {
        listener.updateField(field);
    }
}

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
        this.master = null;
        this.deputy = null;
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
        sender = new UnicastSender(this, socket, config, acceptedMessages);
        receiver = new UnicastReceiver(socket, acceptedMessages, this);
        receiver.start();
    }

    //Threads Control
    private void startMulticastReceiver() {
        inviteReceiver = new MulticastReceiver(this);
        announcementMsgMap.clear();
        inviteReceiver.start();
    }

    private void stopMulticastReceiver() {
        if (inviteReceiver != null) inviteReceiver.interrupt();
    }

    private void startMulticastPublisher() {
        inviteSender = new MulticastPublisher(this,
                id,
                config,
                players);
        inviteSender.start();
    }

    private void stopMulticastPublisher() {
        if (inviteSender != null) inviteSender.interrupt();
    }

    private void startActionUpdater() {
        actionUpdater = new ActionUpdater(this, config);
        actionUpdater.start();
    }

    private void stopActionUpdater() {
        if (actionUpdater != null) actionUpdater.interrupt();
        actionUpdater = null;
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
            SnakesProto.GamePlayer newPlayer;
            SnakesProto.GamePlayer.Builder playerBuilder = SnakesProto.GamePlayer
                    .newBuilder()
                    .setName(name)
                    .setId(newPlayerId)
                    .setIpAddress(ip)
                    .setPort(port)
                    .setType(type)
                    .setScore(0);
            newPlayer = playerBuilder.setRole(role).build();
            if (role == SnakesProto.NodeRole.MASTER) master = newPlayer;
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

    private void deletePlayer(int playerId) {
        int index = 0;
        for (SnakesProto.GamePlayer player : players) {
            if (player.getId() == playerId) break;
            index++;
        }
        players.remove(index);
    }

    @Override
    public void disconnectPlayer(int playerId) {
        SnakesProto.GamePlayer player = getGamePLayerById(playerId);
        deletePlayer(playerId);
        if (player != null && player.getRole() == SnakesProto.NodeRole.DEPUTY) {
            findNewDeputy();
        }
    }

    private void changeThisNodeRole(SnakesProto.NodeRole role) {
        switch (role) {
            case MASTER -> {
                stopMulticastReceiver();
                startMulticastPublisher();
                playersActions.clear();
                if (nodeRole != SnakesProto.NodeRole.MASTER) {
                    for (SnakesProto.GamePlayer player : players) {
                        if (player.getRole() == SnakesProto.NodeRole.VIEWER) continue;
                        int newPlayerId = player.getId();
                        SnakesProto.Direction playerSnakeDirection = gameCore.getSnakeDirection(newPlayerId);
                        playersActions.put(newPlayerId, playerSnakeDirection);
                    }

                }
                startActionUpdater();
            }
            case NORMAL, DEPUTY -> {
                stopActionUpdater();
                stopMulticastReceiver();
                stopMulticastPublisher();
            }
            case VIEWER -> {
                stopActionUpdater();
                stopMulticastReceiver();
                stopMulticastPublisher();
                switch (nodeRole) {
                    case MASTER -> {
                        changePlayerRole(id, SnakesProto.NodeRole.VIEWER);
                        if (deputy == null) exit();
                        else{
                            int index = 0;
                            for (SnakesProto.GamePlayer player : players) {
                                if (player.getRole() == SnakesProto.NodeRole.DEPUTY) {
                                    master = player.toBuilder().setRole(SnakesProto.NodeRole.MASTER).build();
                                    players.set(index, master);
                                    findNewDeputy();
                                    break;
                                }
                                index++;
                            }
                        }
                    }
                    case NORMAL, DEPUTY -> {
                    }
                }
            }
        }
        System.out.println("Changed node role = " + role);
        this.nodeRole = role;
    }

    private void findNewDeputy() {
        int index = 0;
        for (SnakesProto.GamePlayer player : players) {
            if (player.getRole() == SnakesProto.NodeRole.NORMAL) {
                deputy = player.toBuilder().setRole(SnakesProto.NodeRole.DEPUTY).build();
                System.out.println("Deputy was changed: deputyId=" + deputy.getId());
                players.set(index, deputy);
                sender.sendMessage(deputy, MessageBuilder.roleChangingMsgBuilder(SnakesProto.NodeRole.MASTER, SnakesProto.NodeRole.DEPUTY, getMessageSequence(), id, deputy.getId()));
                return;
            }
            index++;
        }
        deputy = null;
    }

    @Override
    public void initiateDeputyPLayer() {
        if (deputy == null) {
            findNewDeputy();
        }
    }

    @Override
    public void changeThisNodeMasterRoleOnViewer(){
        sender.sendMessage(master, MessageBuilder.roleChangingMsgBuilder(SnakesProto.NodeRole.VIEWER, SnakesProto.NodeRole.MASTER, getMessageSequence(), id, master.getId()));
    }

    public void changePlayerRole(int playerId, SnakesProto.NodeRole role) { //todo make private
        int index = 0;
        for (SnakesProto.GamePlayer player : players) {
            if (player.getId() == playerId) {
                SnakesProto.NodeRole prevRole = player.getRole();
                players.set(index, player.toBuilder().setRole(role).build());
                if (prevRole == SnakesProto.NodeRole.DEPUTY) {
                    findNewDeputy();
                }
                if (player.getId() != id)
                    sender.sendMessage(player, MessageBuilder.roleChangingMsgBuilder(SnakesProto.NodeRole.MASTER, SnakesProto.NodeRole.VIEWER, getMessageSequence(), id, player.getId()));
                break;
            }
            index++;
        }
    }

    @Override
    public synchronized void nodeSnakeIsDead(int playerId) {
        System.out.println("Node snake is dead id = " + playerId);
        synchronized (playersActions) {
            playersActions.remove(playerId);
        }
        if (playerId == id) {
            actionUpdater.setChangeMasterBreakPoint();
            changeThisNodeRole(SnakesProto.NodeRole.VIEWER);
        } else {
            changePlayerRole(playerId, SnakesProto.NodeRole.VIEWER);
        }
    }

    private void savePlayersList(List<SnakesProto.GamePlayer> playersList, String ip) {
        int index = 0;
        players = new LinkedList<>(playersList);
        for (SnakesProto.GamePlayer player : players) {
            switch (player.getRole()) {
                case MASTER -> {
                    master = player.toBuilder().setIpAddress(ip).build();
                    players.set(index, master);
                }
                case DEPUTY -> {
                    deputy = player;
                }
            }
            index++;
        }
    }

    private SnakesProto.GameState getGameState() {
        SnakesProto.GamePlayers gamePlayers = SnakesProto.GamePlayers.newBuilder().addAllPlayers(players).build();
        return gameCore.getGameData().setStateOrder(getStateOrder()).setPlayers(gamePlayers).setConfig(config).build();
    }

    @Override
    public void sendChangesToAllPlayers() {
        synchronized (players) {
            for (SnakesProto.GamePlayer player : players) {
                if (player.getId() != id) {
                    sender.sendMessage(player, MessageBuilder.stateMsgBuilder(getGameState(), getMessageSequence(), id, player.getId()));
                }
            }
        }
    }

    @Override
    public void makeAllChangesOnField() {
        synchronized (playersActions) {
            for (int idPLayer : new LinkedList<>(playersActions.keySet())) {
                gameCore.makeAction(idPLayer, playersActions.get(idPLayer));
            }
        }
        gameCore.updateField();
    }

    //Launching game
    private void launchGameCore() {
        gameCore = new GameCore(this, config, id);
    }

    public void createNewGame(SnakesProto.GameConfig config, int nodeId) {
        this.config = config;
        this.id = nodeId;
        launchGameCore();
        changeThisNodeRole(SnakesProto.NodeRole.MASTER);
        addPlayer(name, "", port, nodeRole, playerType);
    }

    public void joinTheGame(String keyGame, SnakesProto.NodeRole newNodeRole) {
        synchronized (announcementMsgMap) {
            stopMulticastReceiver();
            String ketMap = makeKeyForAnnouncementMsg(keyGame);
            AnnouncementMsg msg = announcementMsgMap.get(ketMap);
            if (msg != null) {
                changeThisNodeRole(newNodeRole);
                this.config = msg.gameMessage.getConfig();
                List<SnakesProto.GamePlayer> playersFromMessage = msg.gameMessage.getPlayers().getPlayersList();
                savePlayersList(playersFromMessage, msg.master.getIpAddress());
                this.players = new LinkedList<>(msg.gameMessage.getPlayers().getPlayersList()); //have not accessed to unmodifiable list
                listener.openFieldWindow(config.getWidth(), config.getHeight());
                sender.sendMessage(msg.master, MessageBuilder.joinMsgBuilder(playerType, newNodeRole == SnakesProto.NodeRole.VIEWER, name, getMessageSequence(), 0, 0));
            }
        }
    }

    //Receive functions
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
        if (gameState.getStateOrder() > stateOrder && nodeRole != SnakesProto.NodeRole.MASTER) {
            savePlayersList(gameState.getPlayers().getPlayersList(), masterIp);
            stateOrder = gameState.getStateOrder();
            gameCore.updateGameState(gameState);
        } else System.out.println("I could not get state corrections!");
    }

    @Override
    public void receiveRoleChangeMsg(SnakesProto.GameMessage.RoleChangeMsg roleChangeMsg) {
        if (roleChangeMsg.getReceiverRole() != nodeRole && nodeRole != SnakesProto.NodeRole.MASTER) {
            changeThisNodeRole(roleChangeMsg.getReceiverRole());
        } else System.out.println("I could not get Role Change correction!");

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
        synchronized (acceptedMessages) {
            acceptedMessages.add(messageSequence);
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
        if (player.getRole() == SnakesProto.NodeRole.MASTER) {
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

    //Exit
    private void exit() {
        stopActionUpdater();
        stopMulticastPublisher();
        stopMulticastReceiver();
        System.out.println("EXIT");
        //todo call function from presenter exit()
    }
}

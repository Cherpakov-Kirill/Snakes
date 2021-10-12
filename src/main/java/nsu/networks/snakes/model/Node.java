package nsu.networks.snakes.model;

import nsu.networks.snakes.model.gamecore.GameCore;
import nsu.networks.snakes.model.gamecore.GameCoreListener;
import nsu.networks.snakes.model.net.messages.AnnouncementMsg;
import nsu.networks.snakes.model.net.messages.MessageBuilder;
import nsu.networks.snakes.model.net.multicast.MulticastListener;
import nsu.networks.snakes.model.net.multicast.MulticastPublisher;
import nsu.networks.snakes.model.net.multicast.MulticastReceiver;
import nsu.networks.snakes.model.net.unicast.UnicastReceiver;
import nsu.networks.snakes.model.net.unicast.UnicastSender;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

import static nsu.networks.snakes.model.net.messages.AnnouncementMsg.makeKeyForAnnouncementMsg;

public class Node implements GameCoreListener, MulticastListener {
    private final NodeListener listener;
    private int id;
    private final String name;
    private final int port;
    private SnakesProto.NodeRole nodeRole;
    private final SnakesProto.PlayerType playerType;
    private SnakesProto.GameConfig config;
    private List<SnakesProto.GamePlayer> players;
    private SnakesProto.GamePlayer master;
    private SnakesProto.GamePlayer deputy;
    private final Map<Integer, SnakesProto.Direction> playersActions;
    private SnakesProto.Direction thisNodeAction;
    private ActionUpdater actionUpdater;

    private MulticastPublisher inviteSender;
    private MulticastReceiver inviteReceiver;
    private boolean opportunityToJoin;
    private long messageSequence;
    private int stateOrder;

    private GameCore gameCore;

    private final Map<String, AnnouncementMsg> announcementMsgMap;

    private Set<Long> acceptedMessages;
    private DatagramSocket socket;
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
        this.stateOrder = 1;
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
        sender = new UnicastSender(socket, config, acceptedMessages);
        receiver = new UnicastReceiver(socket, acceptedMessages, this);
        receiver.start();
    }

    private void startMulticastReceiver() {
        if (inviteReceiver == null) inviteReceiver = new MulticastReceiver(this);
        announcementMsgMap.clear();
        inviteReceiver.start();
    }

    private void stopMulticastReceiver() {
        if (inviteReceiver != null && inviteReceiver.isAlive()) inviteReceiver.interrupt();
    }

    private void startMulticastPublisher() {
        if (inviteSender == null) inviteSender = new MulticastPublisher(this);
        inviteSender.start();
    }

    private void stopMulticastPublisher() {
        if (inviteSender != null && inviteSender.isAlive()) inviteSender.interrupt();
    }

    private void makeAction(int idPlayer, SnakesProto.Direction direction) {
        switch (direction) {
            case LEFT -> gameCore.makeLeftMove(idPlayer);
            case RIGHT -> gameCore.makeRightMove(idPlayer);
            case DOWN -> gameCore.makeDownMove(idPlayer);
            case UP -> gameCore.makeUpMove(idPlayer);
        }
    }

    public class ActionUpdater extends Thread {
        private final SnakesProto.GameConfig config;
        private final Map<Integer, SnakesProto.Direction> playersActions;
        private final List<SnakesProto.GamePlayer> players;

        public ActionUpdater(SnakesProto.GameConfig config, Map<Integer, SnakesProto.Direction> playersActions, List<SnakesProto.GamePlayer> players) {
            this.config = config;
            this.playersActions = playersActions;
            this.players = players;
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    Thread.sleep(config.getStateDelayMs());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isInterrupted()) break;
                synchronized (playersActions) {
                    for (int idPLayer : playersActions.keySet()) {
                        makeAction(idPLayer, playersActions.get(idPLayer));
                    }
                }
                stateOrder++;
                SnakesProto.GamePlayers gamePlayers = SnakesProto.GamePlayers.newBuilder().addAllPlayers(players).build();
                SnakesProto.GameState state = gameCore.getGameData().setStateOrder(stateOrder).setPlayers(gamePlayers).setConfig(config).build();
                updateField(gameCore.getField());
                synchronized (playersActions) {
                    for (SnakesProto.GamePlayer player : players) {
                        if (player.getId() != id) {
                            sender.sendMessage(player, MessageBuilder.stateMsgBuilder(state, getMessageSequence(), id, player.getId()));
                        }
                    }
                }
            }
        }
    }

    public void changeNodeRole(SnakesProto.NodeRole role) {
        switch (role) {
            case MASTER -> {
                stopMulticastReceiver();
                nodeRole = SnakesProto.NodeRole.MASTER;
                playersActions.clear();
                addPlayer(name, "", port, nodeRole, playerType);
                startMulticastPublisher();
                actionUpdater = new ActionUpdater(config, playersActions, players);
                actionUpdater.start();
            }
            case NORMAL, DEPUTY, VIEWER -> {
                if (actionUpdater != null) actionUpdater.interrupt();
                stopMulticastReceiver();
                stopMulticastPublisher();
            }
        }
        this.nodeRole = role;
    }

    public void joinTheGame(String keyGame) {
        synchronized (announcementMsgMap) {
            stopMulticastReceiver();
            String ketMap = makeKeyForAnnouncementMsg(keyGame);
            AnnouncementMsg msg = announcementMsgMap.get(ketMap);
            if (msg != null) {
                this.config = msg.gameMessage.getConfig();
                this.players = msg.gameMessage.getPlayers().getPlayersList();
                listener.openFieldWindow(config.getWidth(), config.getHeight());
                sender.sendMessage(msg.master, MessageBuilder.joinMsgBuilder(playerType, nodeRole == SnakesProto.NodeRole.VIEWER, name, getMessageSequence(), 0, 0));
            }
        }
    }

    @Override
    public void receiveAnnouncementMsg(SnakesProto.GameMessage.AnnouncementMsg msg, SnakesProto.GamePlayer masterPLayer) {
        synchronized (announcementMsgMap) {
            String key = makeKeyForAnnouncementMsg(masterPLayer.getIpAddress(), masterPLayer.getPort());
            if (!announcementMsgMap.containsKey(key)) {
                announcementMsgMap.put(key, new AnnouncementMsg(msg, masterPLayer));
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

    public void receiveGameStateMsg(SnakesProto.GameState gameState, String masterIp) {
        if (gameState.getStateOrder() > stateOrder) {
            stateOrder = gameState.getStateOrder();
            players = gameState.getPlayers().getPlayersList();
            for (SnakesProto.GamePlayer player : players) {
                switch (player.getRole()) {
                    case MASTER -> master = player.toBuilder().setIpAddress(masterIp).build();
                    case DEPUTY -> deputy = player;
                }
            }
            gameCore.updateGameState(gameState);
        }
    }

    public void receiveSteerMsg(SnakesProto.Direction direction, int playerId) {
        synchronized (playersActions) {
            playersActions.put(id, direction);
        }
    }

    public void launchGameCore() {
        gameCore = new GameCore(this, config, id);
    }

    public void checkIdSetting(int id) {
        if (this.id == -1) {
            this.id = id;
            launchGameCore();
        }
    }

    public void setStartNodeParameters(SnakesProto.GameConfig config, int nodeId) {
        this.config = config;
        this.id = nodeId;
        launchGameCore();
    }


    public int addPlayer(String name, String ip, int port, SnakesProto.NodeRole role, SnakesProto.PlayerType type) {
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
                    thisNodeAction = playerSnakeDirection;
                }
            }
        }
        //System.out.println("Count of players = " + players.size());
        return newPlayerId;
    }

    public SnakesProto.GamePlayer getGamePLayerById(int id) {
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

    public void acceptMessage(SnakesProto.GamePlayer player, long messageSequence) {
        sender.sendMessage(player, MessageBuilder.ackMsgBuilder(messageSequence, id, player.getId()));
    }

    public synchronized long getMessageSequence() {
        messageSequence++;
        return messageSequence;
    }

    public boolean getOpportunityToJoin() {
        return opportunityToJoin;
    }

    public int getId() {
        return id;
    }

    public SnakesProto.GameConfig getGameConfig() {
        return config;
    }

    public SnakesProto.GamePlayers getGamePlayers() {
        return SnakesProto.GamePlayers.newBuilder().addAllPlayers(players).build();
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

    public void setKeyboardAction(SnakesProto.Direction direction) {
        if (gameCore.getNodeSnakeLength() == 1 || reverseDirection(thisNodeAction) != direction) {
            thisNodeAction = direction;
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

    @Override
    public void updateField(String field) {
        listener.updateField(field);
    }
}

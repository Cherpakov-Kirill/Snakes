package nsu.networks.snakes.model;

import nsu.networks.snakes.model.gamecore.GameCore;
import nsu.networks.snakes.model.gamecore.GameCoreListener;
import nsu.networks.snakes.model.inet.InetController;
import nsu.networks.snakes.model.inet.InetControllerListener;
import nsu.networks.snakes.model.messages.AnnouncementMsg;
import nsu.networks.snakes.model.actionUpdater.ActionUpdater;
import nsu.networks.snakes.model.players.Players;
import nsu.networks.snakes.model.players.PlayersListener;

import static nsu.networks.snakes.model.messages.AnnouncementMsg.makeKeyForAnnouncementMsg;

import java.util.*;

public class Node implements GameCoreListener, PlayersListener, InetControllerListener {
    private final NodeListener listener;
    private int id;
    private final String name;
    private final int port;
    private SnakesProto.NodeRole nodeRole;
    private final SnakesProto.PlayerType playerType;
    private SnakesProto.GameConfig config;

    private final Players players;
    private ActionUpdater actionUpdater;
    private final InetController inetController;
    private GameCore gameCore;

    private boolean opportunityToJoin;
    private int stateOrder;

    public Node(NodeListener listener, String name, int port, SnakesProto.PlayerType type) {
        this.listener = listener;
        this.id = 0;
        this.config = Configuration.defaultConfigBuilder();
        this.name = name;
        this.port = port;
        this.playerType = type;
        this.nodeRole = SnakesProto.NodeRole.VIEWER;


        this.opportunityToJoin = true;
        this.stateOrder = 0;


        this.inetController = new InetController(this, port, config.getPingDelayMs(), config.getNodeTimeoutMs());
        this.players = new Players(this, inetController);
        inetController.attachPlayers(players);
    }


    //Action updater
    private void startActionUpdater() {
        actionUpdater = new ActionUpdater(gameCore, players, config.getStateDelayMs());
        actionUpdater.start();
    }

    private void stopActionUpdater() {
        if (actionUpdater != null) actionUpdater.setChangeMasterBreakPoint();
        actionUpdater = null;
    }

    @Override
    public SnakesProto.GameState.Builder getGameStateData() {
        return gameCore.getGameData().setStateOrder(getStateOrder()).setConfig(config);
    }

    //Roles
    @Override
    public void changeThisNodeRole(SnakesProto.NodeRole role, boolean requestFromPlayer) {
        if (nodeRole == role) return;
        switch (role) {
            case MASTER -> {
                inetController.stopMulticastReceiver();
                inetController.startMulticastPublisher(id, config, players.getPlayersList());
                startActionUpdater();
                if (nodeRole != SnakesProto.NodeRole.MASTER) {
                    if (players.getNumberOfPlayers() > 0) {
                        for (SnakesProto.GamePlayer player : players.getPlayersList()) {
                            if (player.getRole() == SnakesProto.NodeRole.VIEWER) continue;
                            int newPlayerId = player.getId();
                            actionUpdater.addNewPlayer(newPlayerId);
                        }
                        players.changePlayerRole(id, SnakesProto.NodeRole.MASTER, false);
                    }
                }
            }
            case NORMAL, DEPUTY -> {
                stopActionUpdater();
                inetController.stopMulticastReceiver();
                inetController.stopMulticastPublisher();
            }
            case VIEWER -> {
                stopActionUpdater();
                inetController.stopMulticastReceiver();
                inetController.stopMulticastPublisher();
                switch (nodeRole) {
                    case MASTER -> {
                        players.changePlayerRole(id, SnakesProto.NodeRole.VIEWER, false);
                        if (players.deputy == null) endTheGame();
                        else {
                            int index = 0;
                            for (SnakesProto.GamePlayer player : players.getPlayersList()) {
                                if (player.getRole() == SnakesProto.NodeRole.DEPUTY) {
                                    players.master = player.toBuilder().setRole(SnakesProto.NodeRole.MASTER).build();
                                    players.changePlayerInList(index, players.master);
                                    players.findNewDeputy();
                                    break;
                                }
                                index++;
                            }
                        }
                    }
                    case DEPUTY, NORMAL -> {
                        if (requestFromPlayer)
                            players.sendChangeRoleMessage(players.master, SnakesProto.NodeRole.VIEWER, SnakesProto.NodeRole.MASTER);
                    }
                }
            }
        }
        System.out.println("Changed node role = " + role);
        this.nodeRole = role;
        players.setNodeRole(nodeRole);
    }

    public void becameAViewer() {
        changeThisNodeRole(SnakesProto.NodeRole.VIEWER, true);
    }

    //Dead snake
    @Override
    public synchronized void nodeSnakeIsDead(int playerId) {
        System.out.println("Node snake is dead id = " + playerId);
        actionUpdater.removePlayer(playerId);
        if (playerId == id) {
            actionUpdater.setChangeMasterBreakPoint();
            changeThisNodeRole(SnakesProto.NodeRole.VIEWER, false);
        } else {
            players.changePlayerRole(playerId, SnakesProto.NodeRole.VIEWER, true);
        }
    }

    private void nodeBecameViewer(int playerId) {
        System.out.println("Node became a VIEWER: id = " + playerId);
        players.changePlayerRole(playerId, SnakesProto.NodeRole.VIEWER, false);
    }

    //Launching game
    @Override
    public void launchGameCore(int nodeId) {
        id = nodeId;
        players.setNodeId(id);
        gameCore = new GameCore(this, config, nodeId);
    }

    public void createNewGame(SnakesProto.GameConfig config) {
        this.config = config;
        launchGameCore(1);
        changeThisNodeRole(SnakesProto.NodeRole.MASTER, false);
        players.addPlayer(name, "", port, nodeRole, playerType);
    }

    public void joinTheGame(String keyGame, SnakesProto.NodeRole newNodeRole) {
        inetController.stopMulticastReceiver();
        AnnouncementMsg msg = inetController.getAnnouncementMsg(makeKeyForAnnouncementMsg(keyGame));
        if (msg != null) {
            changeThisNodeRole(newNodeRole, false);
            this.config = msg.gameMessage.getConfig();
            players.updatePlayersList(msg.gameMessage.getPlayers().getPlayersList(), msg.master.getIpAddress());
            listener.openFieldWindow(config.getWidth(), config.getHeight());
            players.sendJoinMessage(playerType, newNodeRole == SnakesProto.NodeRole.VIEWER, name);
        }
    }

    //Receive functions
    @Override
    public void receiveGameStateMsg(SnakesProto.GameState gameState, String masterIp) {
        if (gameState.getStateOrder() > stateOrder && nodeRole != SnakesProto.NodeRole.MASTER) {
            players.updatePlayersList(gameState.getPlayers().getPlayersList(), masterIp);
            stateOrder = gameState.getStateOrder();
            gameCore.updateGameState(gameState);
        } else System.out.println("I could not get state corrections!");
    }

    @Override
    public void receiveRoleChangeMsg(SnakesProto.GameMessage.RoleChangeMsg roleChangeMsg, int senderId) {
        if (roleChangeMsg.getReceiverRole() != nodeRole) {
            if (nodeRole != SnakesProto.NodeRole.MASTER) changeThisNodeRole(roleChangeMsg.getReceiverRole(), false);
        } else {
            if (nodeRole == SnakesProto.NodeRole.MASTER) nodeBecameViewer(senderId);
            else players.changePlayerRole(senderId, roleChangeMsg.getSenderRole(), false);
        }
    }

    @Override
    public void receiveSteerMsg(SnakesProto.Direction direction, int playerId) {
        actionUpdater.addAction(playerId, direction);
    }

    @Override
    public int receiveJoinMsg(String name, String ip, int port, SnakesProto.NodeRole role, SnakesProto.PlayerType type) {
        return players.addPlayer(name, ip, port, role, type);
    }

    //Players
    @Override
    public boolean addPlayerInGame(int newPlayer) {
        if (!gameCore.addNewPlayer(newPlayer)) return false;
        actionUpdater.addNewPlayer(newPlayer);
        return true;
    }

    @Override
    public void addOnePoint(int nodePlayerId) {
        players.addOnePoint(nodePlayerId);
    }

    //Keyboard listener
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
        if ((stateOrder != 0 || nodeRole == SnakesProto.NodeRole.MASTER) && nodeRole != SnakesProto.NodeRole.VIEWER && reverseDirection(gameCore.getSnakeDirection(id)) != direction) {
            switch (nodeRole) {
                case MASTER -> actionUpdater.addAction(id, direction);
                case DEPUTY, NORMAL -> players.sendSteerMessage(direction);
            }
        }
    }

    //Get parameters
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
        listener.updateField(field, players.getScores(), nodeRole.toString());
    }

    @Override
    public void updateFindGameList(List<String> games) {
        listener.updateFindGameList(games);
    }

    //Exit
    public void endTheGame() {
        stopActionUpdater();
        inetController.stopMulticastPublisher();
        inetController.stopMulticastReceiver();
        inetController.interruptUnicast();
    }
}

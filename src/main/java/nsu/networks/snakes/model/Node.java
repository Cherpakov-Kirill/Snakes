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
        this.id = 0; //not received id of this node or not started game on Master
        this.config = Configuration.defaultConfigBuilder();
        this.name = name;
        this.port = port;
        this.playerType = type;
        this.nodeRole = SnakesProto.NodeRole.VIEWER;


        this.opportunityToJoin = true;
        this.stateOrder = 0;


        this.inetController = new InetController(this, port,config.getPingDelayMs(),config.getNodeTimeoutMs());
        this.players = new Players(this, inetController);
        inetController.attachPlayers(players);
    }


    //Action updater
    private void startActionUpdater() {
        actionUpdater = new ActionUpdater(gameCore, players, config.getStateDelayMs());
        actionUpdater.start();
    }

    private void stopActionUpdater() {
        if (actionUpdater != null) actionUpdater.setChangeMasterBreakPoint();///actionUpdater.interrupt();
        actionUpdater = null;
    }

    @Override
    public SnakesProto.GameState.Builder getGameStateData() {
        return gameCore.getGameData().setStateOrder(getStateOrder()).setConfig(config);
    }

    //Roles
    private void changeThisNodeRole(SnakesProto.NodeRole role) {
        if(nodeRole == role) return;
        switch (role) {
            case MASTER -> {
                inetController.stopMulticastReceiver();
                inetController.startMulticastPublisher(id,config,players.getPlayersList());
                startActionUpdater();
                if (nodeRole != SnakesProto.NodeRole.MASTER) {
                    for (SnakesProto.GamePlayer player : players.getPlayersList()) {
                        if (player.getRole() == SnakesProto.NodeRole.VIEWER) continue;
                        int newPlayerId = player.getId();
                        actionUpdater.addNewPlayer(newPlayerId);
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
                        players.changePlayerRole(id, SnakesProto.NodeRole.VIEWER);
                        if (players.deputy == null) exit();
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
                    case NORMAL, DEPUTY -> {
                    }
                }
            }
        }
        System.out.println("Changed node role = " + role);
        this.nodeRole = role;
    }

    //Dead snake
    @Override
    public synchronized void nodeSnakeIsDead(int playerId) {
        System.out.println("Node snake is dead id = " + playerId);
        actionUpdater.removePlayer(playerId);
        if (playerId == id) {
            actionUpdater.setChangeMasterBreakPoint();
            changeThisNodeRole(SnakesProto.NodeRole.VIEWER);
        } else {
            players.changePlayerRole(playerId, SnakesProto.NodeRole.VIEWER);
        }
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
        changeThisNodeRole(SnakesProto.NodeRole.MASTER);
        players.addPlayer(name, "", port, nodeRole, playerType);
    }

    public void joinTheGame(String keyGame, SnakesProto.NodeRole newNodeRole) {
        inetController.stopMulticastReceiver();
        AnnouncementMsg msg = inetController.getAnnouncementMsg(makeKeyForAnnouncementMsg(keyGame));
        if (msg != null) {
            changeThisNodeRole(newNodeRole);
            this.config = msg.gameMessage.getConfig();
            players.updatePlayersList(msg.gameMessage.getPlayers().getPlayersList(), msg.master.getIpAddress());
            listener.openFieldWindow(config.getWidth(), config.getHeight());
            players.sendJoinMessage(playerType,newNodeRole == SnakesProto.NodeRole.VIEWER, name);
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
    public void receiveRoleChangeMsg(SnakesProto.GameMessage.RoleChangeMsg roleChangeMsg) {
        if (roleChangeMsg.getReceiverRole() != nodeRole && nodeRole != SnakesProto.NodeRole.MASTER) {
            changeThisNodeRole(roleChangeMsg.getReceiverRole());
        } else System.out.println("I could not get Role Change correction!");

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
    public boolean addPlayerInGame(int newPLayer) {
        if(!gameCore.addNewPlayer(newPLayer)) return false;
        actionUpdater.addNewPlayer(newPLayer);
        return true;
    }

    @Override
    public void addOnePoint(int nodePlayerId){
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
        listener.updateField(field);
    }

    @Override
    public void updateFindGameList(List<String> games) {
        listener.updateFindGameList(games);
    }

    //Exit
    private void exit() {
        stopActionUpdater();
        inetController.stopMulticastPublisher();
        inetController.stopMulticastReceiver();
        System.out.println("EXIT");
        //todo call function from presenter exit()
    }
}

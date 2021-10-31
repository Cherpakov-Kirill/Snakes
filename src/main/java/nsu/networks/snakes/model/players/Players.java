package nsu.networks.snakes.model.players;

import nsu.networks.snakes.model.SnakesProto;
import nsu.networks.snakes.model.actionUpdater.PlayersForActionUpdater;
import nsu.networks.snakes.model.inet.PlayersForInet;
import nsu.networks.snakes.model.messages.MessageBuilder;

import java.util.LinkedList;
import java.util.List;

public class Players implements PlayersForActionUpdater, PlayersForInet {
    private final PlayersListener listener;
    private final InetForPlayers inetController;
    private List<SnakesProto.GamePlayer> playerList;
    public SnakesProto.GamePlayer master;
    public SnakesProto.GamePlayer deputy;
    private int newPlayerIdCounter;
    private int nodeId;
    private SnakesProto.NodeRole nodeRole;

    public Players(PlayersListener listener, InetForPlayers inetController) {
        this.listener = listener;
        this.inetController = inetController;
        this.playerList = new LinkedList<>();
        this.master = null;
        this.deputy = null;
        this.nodeId = 0;
        this.nodeRole = SnakesProto.NodeRole.VIEWER;
        newPlayerIdCounter = 1;

    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public void setNodeRole(SnakesProto.NodeRole nodeRole) {
        this.nodeRole = nodeRole;
    }

    public int addPlayer(String name, String ip, int port, SnakesProto.NodeRole role, SnakesProto.PlayerType type) {
        int newPlayerId = getPlayerIdByIPAndPort(ip, port);
        if (newPlayerId == 0) {
            newPlayerId = newPlayerIdCounter;
            newPlayerIdCounter++;
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
            playerList.add(newPlayer);
            if (role != SnakesProto.NodeRole.VIEWER) {
                if (!listener.addPlayerInGame(newPlayerId)) {
                    sendErrorMessage(newPlayerId);
                    disconnectPlayer(newPlayerId);
                    return -1;
                }
                ;
            }
        }
        return newPlayerId;
    }

    public void addOnePoint(int nodePlayerId) {
        int index = 0;
        for (SnakesProto.GamePlayer player : playerList) {
            if (player.getId() == nodePlayerId) {
                changePlayerInList(index, player.toBuilder().setScore(player.getScore() + 1).build());
                break;
            }
            index++;
        }

    }

    public void changePlayerInList(int index, SnakesProto.GamePlayer newPlayer) {
        playerList.set(index, newPlayer);
    }

    public void updatePlayersList(List<SnakesProto.GamePlayer> playersList, String ip) {
        int index = 0;
        playerList = new LinkedList<>(playersList);
        SnakesProto.GamePlayer newMaster = master;
        deputy = null;
        for (SnakesProto.GamePlayer player : playerList) {
            switch (player.getRole()) {
                case MASTER -> {
                    if (player.getId() != nodeId) {
                        newMaster = player.toBuilder().setIpAddress(ip).build();
                        playerList.set(index, newMaster);
                        System.out.println("Players: updated master ip = " + newMaster.getIpAddress());
                    } else newMaster = player;
                }
                case DEPUTY -> deputy = player;
                case VIEWER -> {
                    if (master != null && player.getId() == master.getId()) {
                        SnakesProto.GamePlayer prevMaster = player.toBuilder().setIpAddress(ip).build();
                        playerList.set(index, prevMaster);
                        System.out.println("Players: updated prevMaster ip = " + prevMaster.getIpAddress());
                    }
                }
            }
            index++;
        }
        master = newMaster;
    }

    private void deletePlayer(int playerId) {
        int index = 0;
        for (SnakesProto.GamePlayer player : playerList) {
            if (player.getId() == playerId) break;
            index++;
        }
        playerList.remove(index);
        System.out.println("Players: delete dead node = " + playerId);
        if(nodeRole!= SnakesProto.NodeRole.MASTER){
            inetController.removePlayerFromPing(playerId);
        }
    }

    public void disconnectPlayer(int playerId) {
        SnakesProto.GamePlayer player = getGamePLayerById(playerId);
        if (player != null) {
            switch (nodeRole) {
                case MASTER -> {
                        System.out.println("Node " + player.getId() + " " + player.getIpAddress() + ":" + player.getPort() + " was disconnected");
                        deletePlayer(playerId);
                        if (player.getRole() == SnakesProto.NodeRole.DEPUTY) findNewDeputy();
                }
                case DEPUTY -> {
                    if(player.getRole() == SnakesProto.NodeRole.MASTER){
                        listener.changeThisNodeRole(SnakesProto.NodeRole.MASTER, false);
                        newPlayerIdCounter = playerList.size() + 1;
                        deletePlayer(playerId);
                        sendChangeRoleToAllPlayers(SnakesProto.NodeRole.MASTER);
                        findNewDeputy();
                    }
                }
            }
        }

    }

    public void findNewDeputy() {
        int index = 0;
        for (SnakesProto.GamePlayer player : playerList) {
            if (player.getRole() == SnakesProto.NodeRole.NORMAL) {
                deputy = player.toBuilder().setRole(SnakesProto.NodeRole.DEPUTY).build();
                System.out.println("Deputy was changed: deputyId=" + deputy.getId());
                playerList.set(index, deputy);
                sendChangeRoleMessage(deputy, SnakesProto.NodeRole.MASTER, SnakesProto.NodeRole.DEPUTY);
                return;
            }
            index++;
        }
        deputy = null;
    }

    public void initiateDeputyPLayer() {
        if (deputy == null) {
            findNewDeputy();
        }
    }

    @Override
    public void transferRoleOfTheMaster() {
        if (master.getId() != nodeId) {
            sendChangeRoleMessage(master, SnakesProto.NodeRole.VIEWER, SnakesProto.NodeRole.MASTER);
            System.out.println("Change role message was sent to nodeId=" + master.getId());
        }

    }

    public void changePlayerRole(int playerId, SnakesProto.NodeRole role, boolean toNotify) { //todo make private
        int index = 0;
        for (SnakesProto.GamePlayer player : playerList) {
            if (player.getId() == playerId) {
                SnakesProto.NodeRole prevRole = player.getRole();
                if (prevRole == role) return;
                playerList.set(index, player.toBuilder().setRole(role).build());
                if(role == SnakesProto.NodeRole.MASTER) {
                    master = playerList.get(index);
                    System.out.println("Players: changed master = " + playerId);
                }
                if (prevRole == SnakesProto.NodeRole.DEPUTY) {
                    findNewDeputy();
                }
                if (toNotify && player.getId() != nodeId) {
                    sendChangeRoleMessage(player, SnakesProto.NodeRole.MASTER, role); //when snake dead
                }
                break;
            }
            index++;
        }
    }

    public void sendChangeRoleToAllPlayers(SnakesProto.NodeRole changedRole) {
        for (SnakesProto.GamePlayer player : playerList) {
            if (player.getId() != nodeId) {
                sendChangeRoleMessage(player, changedRole, player.getRole());
            }
        }
    }


    public void sendGameStateToAllPlayers() {
        SnakesProto.GamePlayers gamePlayers = SnakesProto.GamePlayers.newBuilder().addAllPlayers(playerList).build();
        SnakesProto.GameState gameState = listener.getGameStateData().setPlayers(gamePlayers).build();
        for (SnakesProto.GamePlayer player : playerList) {
            if (player.getId() != nodeId) {
                sendGameStateMessage(player, gameState);
            }
        }
    }

    public void sendErrorMessage(int playerId) {
        inetController.sendMessage(getGamePLayerById(playerId), MessageBuilder.errorMsgBuilder("There is not enough space on the playing field for you.\nTry again later!\nHave a nice try!", nodeId, playerId));
    }

    @Override
    public void sendPing(int playerId) {
        inetController.sendMessage(getGamePLayerById(playerId), MessageBuilder.pingMsgBuilder(nodeId, playerId));
    }

    public void sendJoinMessage(SnakesProto.PlayerType playerType, boolean onlyView, String name) {
        inetController.sendMessage(master, MessageBuilder.joinMsgBuilder(playerType, onlyView, name, nodeId, master.getId()));
    }

    public void sendSteerMessage(SnakesProto.Direction direction) {
        inetController.sendMessage(master, MessageBuilder.steerMsgBuilder(direction, nodeId, master.getId()));
    }

    public void sendGameStateMessage(SnakesProto.GamePlayer receiver, SnakesProto.GameState gameState) {
        inetController.sendMessage(receiver, MessageBuilder.stateMsgBuilder(gameState, nodeId, receiver.getId()));
    }

    public void sendChangeRoleMessage(SnakesProto.GamePlayer receiver, SnakesProto.NodeRole senderRole, SnakesProto.NodeRole receiverRole) {
        inetController.sendMessage(receiver, MessageBuilder.roleChangingMsgBuilder(senderRole, receiverRole, nodeId, receiver.getId()));
    }

    public int getNumberOfPlayers(){
        return playerList.size();
    }

    public List<SnakesProto.GamePlayer> getPlayersList() {
        return playerList;
    }

    public SnakesProto.GamePlayer getGamePLayerById(int id) {
        for (SnakesProto.GamePlayer player : playerList) {
            if (player.getId() == id) {
                return player;
            }
        }
        return null;
    }

    public List<String> getScores() {
        List<String> scoresTable = new LinkedList<>();
        for (SnakesProto.GamePlayer player : playerList) {
            scoresTable.add(player.getName() + ":" + player.getScore());
        }
        return scoresTable;
    }

    public int getPlayerIdByIPAndPort(String ip, int port) {
        for (SnakesProto.GamePlayer player : playerList) {
            if (player.getIpAddress().equals(ip) && player.getPort() == port) return player.getId();
        }
        return 0;
    }
}

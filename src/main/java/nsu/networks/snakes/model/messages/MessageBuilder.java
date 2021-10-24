package nsu.networks.snakes.model.messages;

import nsu.networks.snakes.model.SnakesProto;

public class MessageBuilder {
    public static SnakesProto.GameMessage pingMsgBuilder(int senderId, int receiverId) {
        return SnakesProto.GameMessage.newBuilder()
                .setPing(SnakesProto.GameMessage.PingMsg.newBuilder().build())
                .setMsgSeq(0)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public static SnakesProto.GameMessage steerMsgBuilder(SnakesProto.Direction direction, int senderId, int receiverId) {
        return SnakesProto.GameMessage.newBuilder()
                .setSteer(SnakesProto.GameMessage.SteerMsg.newBuilder()
                        .setDirection(direction)
                        .build())
                .setMsgSeq(0)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public static SnakesProto.GameMessage ackMsgBuilder(long messageSequence, int senderId, int receiverId) {
        return SnakesProto.GameMessage.newBuilder()
                .setAck(SnakesProto.GameMessage.AckMsg.newBuilder().build())
                .setMsgSeq(messageSequence)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public static SnakesProto.GameMessage stateMsgBuilder(SnakesProto.GameState state, int senderId, int receiverId) {
        return SnakesProto.GameMessage.newBuilder()
                .setState(SnakesProto.GameMessage.StateMsg.newBuilder().setState(state).build())
                .setMsgSeq(0)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public static SnakesProto.GameMessage announcementMsgBuilder(SnakesProto.GamePlayers players, SnakesProto.GameConfig config, boolean canJoin, int senderId) {
        return SnakesProto.GameMessage.newBuilder()
                .setAnnouncement(SnakesProto.GameMessage.AnnouncementMsg.newBuilder()
                        .setPlayers(players)
                        .setConfig(config)
                        .setCanJoin(canJoin)
                        .build())
                .setMsgSeq(0)
                .setSenderId(senderId)
                .build();
    }

    public static SnakesProto.GameMessage joinMsgBuilder(SnakesProto.PlayerType type, boolean onlyView, String name, int senderId, int receiverId) {
        return SnakesProto.GameMessage.newBuilder()
                .setJoin(SnakesProto.GameMessage.JoinMsg.newBuilder()
                        .setName(name)
                        .setOnlyView(onlyView)
                        .setPlayerType(type)
                        .build())
                .setMsgSeq(0)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public static SnakesProto.GameMessage errorMsgBuilder(String errorMessage, int senderId, int receiverId) {
        return SnakesProto.GameMessage.newBuilder()
                .setError(SnakesProto.GameMessage.ErrorMsg.newBuilder()
                        .setErrorMessage(errorMessage)
                        .build())
                .setMsgSeq(0)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public static SnakesProto.GameMessage roleChangingMsgBuilder(SnakesProto.NodeRole senderRole, SnakesProto.NodeRole receiverRole, int senderId, int receiverId) {
        return SnakesProto.GameMessage.newBuilder()
                .setRoleChange(SnakesProto.GameMessage.RoleChangeMsg.newBuilder()
                        .setSenderRole(senderRole)
                        .setReceiverRole(receiverRole)
                        .build())
                .setMsgSeq(0)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public static SnakesProto.GameMessage setMessageSequence(SnakesProto.GameMessage gameMessage, long messageSeq){
        return gameMessage.toBuilder().setMsgSeq(messageSeq).build();
    }
}


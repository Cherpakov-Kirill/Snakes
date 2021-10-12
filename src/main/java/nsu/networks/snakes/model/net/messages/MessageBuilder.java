package nsu.networks.snakes.model.net.messages;

import nsu.networks.snakes.model.SnakesProto;

public class MessageBuilder {
    public static SnakesProto.GameMessage pingMsgBuilder(long messageSeq, int senderId, int receiverId) {
        return SnakesProto.GameMessage.newBuilder()
                .setPing(SnakesProto.GameMessage.PingMsg.newBuilder().build())
                .setMsgSeq(messageSeq)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public static SnakesProto.GameMessage steerMsgBuilder(SnakesProto.Direction direction, long messageSeq, int senderId, int receiverId) {
        return SnakesProto.GameMessage.newBuilder()
                .setSteer(SnakesProto.GameMessage.SteerMsg.newBuilder()
                        .setDirection(direction)
                        .build())
                .setMsgSeq(messageSeq)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public static SnakesProto.GameMessage ackMsgBuilder(long messageSeq, int senderId, int receiverId) {
        return SnakesProto.GameMessage.newBuilder()
                .setAck(SnakesProto.GameMessage.AckMsg.newBuilder().build())
                .setMsgSeq(messageSeq)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public static SnakesProto.GameMessage stateMsgBuilder(SnakesProto.GameState state, long messageSeq, int senderId, int receiverId) {
        return SnakesProto.GameMessage.newBuilder()
                .setState(SnakesProto.GameMessage.StateMsg.newBuilder().setState(state).build())
                .setMsgSeq(messageSeq)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public static SnakesProto.GameMessage announcementMsgBuilder(SnakesProto.GamePlayers players, SnakesProto.GameConfig config, boolean canJoin, long messageSeq, int senderId) {
        return SnakesProto.GameMessage.newBuilder()
                .setAnnouncement(SnakesProto.GameMessage.AnnouncementMsg.newBuilder()
                        .setPlayers(players)
                        .setConfig(config)
                        .setCanJoin(canJoin)
                        .build())
                .setMsgSeq(messageSeq)
                .setSenderId(senderId)
                .build();
    }

    public static SnakesProto.GameMessage joinMsgBuilder(SnakesProto.PlayerType type, boolean onlyView, String name, long messageSeq, int senderId, int receiverId) {
        return SnakesProto.GameMessage.newBuilder()
                .setJoin(SnakesProto.GameMessage.JoinMsg.newBuilder()
                        .setName(name)
                        .setOnlyView(onlyView)
                        .setPlayerType(type)
                        .build())
                .setMsgSeq(messageSeq)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public static SnakesProto.GameMessage errorMsgBuilder(String errorMessage, long messageSeq, int senderId, int receiverId) {
        return SnakesProto.GameMessage.newBuilder()
                .setError(SnakesProto.GameMessage.ErrorMsg.newBuilder()
                        .setErrorMessage(errorMessage)
                        .build())
                .setMsgSeq(messageSeq)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public static SnakesProto.GameMessage roleChangingMsgBuilder(SnakesProto.NodeRole senderRole, SnakesProto.NodeRole receiverRole, long messageSeq, int senderId, int receiverId) {
        return SnakesProto.GameMessage.newBuilder()
                .setRoleChange(SnakesProto.GameMessage.RoleChangeMsg.newBuilder()
                        .setSenderRole(senderRole)
                        .setReceiverRole(receiverRole)
                        .build())
                .setMsgSeq(messageSeq)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }
}


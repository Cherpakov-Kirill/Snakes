package nsu.networks.snakes.model.messages;

import nsu.networks.snakes.model.SnakesProto;
import nsu.networks.snakes.model.inet.unicast.AcceptorForReceiver;
import nsu.networks.snakes.model.inet.unicast.AcceptorForSender;

import java.util.HashSet;
import java.util.Set;

public class MessageAcceptor implements AcceptorForSender, AcceptorForReceiver {
    private final MessageAcceptorListener listener;
    private final Set<Long> acceptedMessages;
    private int thisNodeId;

    public MessageAcceptor(MessageAcceptorListener listener) {
        this.listener = listener;
        this.acceptedMessages = new HashSet<>();
        this.thisNodeId = 1;
    }

    public void acceptMessage(int playerId, long messageSequence) {
        SnakesProto.GamePlayer player = listener.getGamePlayerById(playerId);
        //assert player != null;
        /*if (player.getRole() == SnakesProto.NodeRole.MASTER) {
            player = master;
        }*/ //todo check that you have already set master ip
        if (player != null)
            listener.sendAckMessage(player, MessageBuilder.ackMsgBuilder(messageSequence, thisNodeId, playerId));
    }

    public boolean checkAcceptedMessage(long seqNumber) {
        synchronized (acceptedMessages) {
            return acceptedMessages.contains(seqNumber);
        }
    }

    public void receiveAckMsg(int playerId, long messageSequence) {
        synchronized (acceptedMessages) {
            acceptedMessages.add(messageSequence);
        }
        if (messageSequence == 1) {
            thisNodeId = playerId;
            listener.launchGameCore(playerId);
        }
    }
}

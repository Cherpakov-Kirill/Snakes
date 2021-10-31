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

    @Override
    public boolean acceptMessage(int playerId, long messageSequence) {
        listener.setTimeOfReceivedMessage(playerId);
        SnakesProto.GamePlayer player = listener.getGamePlayerById(playerId);
        if (player != null) {
            listener.sendAckMessage(player, MessageBuilder.ackMsgBuilder(messageSequence, thisNodeId, playerId));
            //System.out.println("Message acceptor sent ACK to " + playerId);
            return true;
        }
        return false;
    }

    @Override
    public boolean checkAcceptedMessage(long seqNumber) {
        synchronized (acceptedMessages) {
            return acceptedMessages.contains(seqNumber);
        }
    }

    @Override
    public void receiveAckMsg(int receiverId, int senderId, long messageSequence) {
        listener.setTimeOfReceivedMessage(senderId);
        synchronized (acceptedMessages) {
            acceptedMessages.add(messageSequence);
        }
        if (messageSequence == 1) {
            thisNodeId = receiverId;
            listener.launchGameCore(receiverId);
        }
    }
}

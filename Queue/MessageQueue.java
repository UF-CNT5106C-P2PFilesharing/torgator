package Queue;

import Metadata.MessageMetadata;
import java.util.LinkedList;
import java.util.Queue;

public class MessageQueue {
    public static Queue<MessageMetadata> messageQueue = new LinkedList<>();

    public static synchronized void addMessageToMessageQueue(MessageMetadata messageDetails)
    {
        messageQueue.add(messageDetails);
    }

    public static synchronized MessageMetadata getMessageFromQueue() {
        return messageQueue.remove();
    }

    public static synchronized boolean hasNext() {
        return !messageQueue.isEmpty();
    }
}
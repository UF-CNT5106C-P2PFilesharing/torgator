package Messages;

import java.io.RandomAccessFile;

/**
 * This class is used to process messages from message queue.
 */
public class MsgProcessor implements Runnable {

    //PeerID of the host
    private static String currentPeerID;
    //File to handle a piece
    private RandomAccessFile randomAccessFile;

    /**
     * Constructor to initialize PeerMessageProcessingHandler object with peerID from arguments
     *
     * @param peerID - peerID to be set
     */
    public MsgProcessor(String peerID) {
        currentPeerID = peerID;
    }

    /**
     * Empty constructor to initialize PeerMessageProcessingHandler object
     */
    public MsgProcessor() {
        currentPeerID = null;
    }

    @Override
    public void run() {
        // TODO: Implement message processing
    }
}
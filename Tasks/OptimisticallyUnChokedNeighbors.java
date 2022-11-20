package Tasks;
import Messages.Constants;
import Messages.Msg;
import Metadata.PeerMetadata;
import Process.peerProcess;

import static Logging.Helper.logMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Set;
import java.util.TimerTask;
import java.util.Vector;


public class OptimisticallyUnChokedNeighbors extends TimerTask {
    /**
     * This method runs asynchronously as part of timer task every 'CommonConfiguration.optimisticUnchokingInterval' interval.
     * It determines optimistically unchoked neighbor at random from all the neighbors which are choked.
     */
    @Override
    public void run() {
        peerProcess.updateOtherPeerMetaData();
        if (!peerProcess.optimisticUnChokedNeighbors.isEmpty()) peerProcess.optimisticUnChokedNeighbors.clear();

        //Collect all interested peers in a set
        Set<String> keys = peerProcess.remotePeerDetails.keySet();
        Vector<PeerMetadata> remotePeerDetailsVector = new Vector<>();
        keys.forEach(key -> {
            PeerMetadata peerMetadata = peerProcess.remotePeerDetails.get(key);
            if (!key.equals(peerProcess.peerID) && isPeerInterested(peerMetadata)) remotePeerDetailsVector.add(peerMetadata);
        });

        if(!remotePeerDetailsVector.isEmpty()) {
            //randomize the vector and get the first element from it.
            Collections.shuffle(remotePeerDetailsVector);
            PeerMetadata peerMetadata = remotePeerDetailsVector.firstElement();
            peerMetadata.setIsOptimisticallyUnChockedNeighbor(1);
            peerProcess.optimisticUnChokedNeighbors.put(peerMetadata.getId(), peerMetadata);
            logMessage(peerProcess.peerID + " chose the optimistically unChoked neighbor " + peerMetadata.getId());

            if(peerMetadata.getIsChoked() == 1) {
                //send unChoke message if choked
                peerProcess.remotePeerDetails.get(peerMetadata.getId()).setIsChoked(0);
                sendUnChokedMessage(peerProcess.peerToSocketMap.get(peerMetadata.getId()), peerMetadata.getId());
                sendHaveMessage(peerProcess.peerToSocketMap.get(peerMetadata.getId()), peerMetadata.getId());
                peerProcess.remotePeerDetails.get(peerMetadata.getId()).setPeerState(3);
            }
        }
    }

    /**
     * This method is used to determine if the peer is interested to receive pieces
     * @param peerMetadata - peer to check whether it is interested or not
     * @return true - peer interested; false - peer not interested
     */
    private boolean isPeerInterested(PeerMetadata peerMetadata) {
        return peerMetadata.getHasCompleteFile() == 0 && peerMetadata.getIsChoked() == 1 && peerMetadata.getIsInterested() == 1;
    }

    /**
     * This method is used to send UNCHOKED message to socket
     * @param socket - socket in which the message to be sent
     * @param remotePeerID - peerID to which the message should be sent
     */
    private void sendUnChokedMessage(Socket socket, String remotePeerID) {
        logMessage(peerProcess.peerID + " sending a unChoke message to peerProcess " + remotePeerID);
        Msg message = new Msg(Constants.UNCHOKE);
        byte[] messageInBytes = new byte[0];
        try {
            messageInBytes = Msg.serializeMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SendMessageToSocket(socket, messageInBytes);
    }

    /**
     * This method is used to send HAVE message to socket
     * @param socket - socket in which the message to be sent
     * @param peerID - peerID to which the message should be sent
     */
    private void sendHaveMessage(Socket socket, String peerID) {
        logMessage(peerProcess.peerID + " sending HAVE message to peerProcess " + peerID);
        byte[] bitFieldInBytes = peerProcess.bitFieldMessage.getFilePieceBytesEncoded();
        Msg message = null;
        try {
            message = new Msg(Constants.HAVE, bitFieldInBytes);
            SendMessageToSocket(socket, Msg.serializeMessage(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to write a message to socket
     * @param socket - socket in which the message to be sent
     * @param messageInBytes - message to be sent
     */
    private void SendMessageToSocket(Socket socket, byte[] messageInBytes) {
        try {
            OutputStream out = socket.getOutputStream();
            out.write(messageInBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

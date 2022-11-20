package Tasks;

import Configurations.SystemConfiguration;
import Messages.Constants;
import Messages.Msg;
import Metadata.PeerMetadata;
import Process.peerProcess;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

import static Logging.Helper.logMessage;

public class PreferredNeighbors extends TimerTask {

    public void run() {
        int numInterested = 0;
        peerProcess.updateOtherPeerMetaData();
        Set<String> remotePeerIds = peerProcess.remotePeerDetails.keySet();
        for(String key: remotePeerIds) {
            PeerMetadata peerMetaData = peerProcess.remotePeerDetails.get(key);
            if(!key.equals(peerProcess.peerID)) {
                if (peerMetaData.getHasCompleteFile() == 0 && peerMetaData.getIsInterested() == 1) numInterested++;
                else if (peerMetaData.getHasCompleteFile() == 1) peerProcess.preferredNeighbours.remove(key);
                }
            }
        updatePreferredNeighbors(numInterested);
    }

    public void updatePreferredNeighbors(int numInterestedPeers) {
        List<String> updatedPreferredNeighbors = new ArrayList<>();
        int numNeighborsToUpdate = Math.min(numInterestedPeers, SystemConfiguration.numberOfPreferredNeighbours);
        if(!peerProcess.preferredNeighbours.isEmpty() && numInterestedPeers > SystemConfiguration.numberOfPreferredNeighbours) peerProcess.preferredNeighbours.clear();
        ArrayList<PeerMetadata> preferredPeers = new ArrayList<>(peerProcess.remotePeerDetails.values());
        int isCompleteFilePresent = peerProcess.remotePeerDetails.get(peerProcess.peerID).getHasCompleteFile();
        if (isCompleteFilePresent == 1) {
            Collections.shuffle(preferredPeers);
        } else {
            preferredPeers.sort((p1, p2) -> {
                if (p1 == null && p2 == null)
                    return 0;

                if (p1 == null)
                    return 1;

                if (p2 == null)
                    return -1;

                return p1 instanceof Comparable ? p2.compareTo(p1) : p2.toString().compareTo(p1.toString());
                });
            }
        for (int i = 0; i < numNeighborsToUpdate; i++) {
            PeerMetadata preferredPeer = preferredPeers.get(i);
            // If the preferred peer is interested and does not have the complete file yet then
            // add this peer to current peer's preferred neighbors list and un-choke it if choked
            // along with communicating by sending a have message to the peer and changing state to 3
            if (preferredPeer.getIsInterested() == 1 &&
                    !preferredPeer.getId().equals(peerProcess.peerID) &&
                    peerProcess.remotePeerDetails.get(preferredPeer.getId()).getHasCompleteFile() == 0) {

                peerProcess.remotePeerDetails.get(preferredPeer.getId()).setIsPreferredNeighbor(1);
                peerProcess.preferredNeighbours.put(preferredPeer.getId(), peerProcess.remotePeerDetails.get(preferredPeer.getId()));

                updatedPreferredNeighbors.add(preferredPeer.getId());

                if (peerProcess.remotePeerDetails.get(preferredPeer.getId()).getIsChoked() == 1) {
                    sendUnChokedMessage(peerProcess.peerToSocketMap.get(preferredPeer.getId()), preferredPeer.getId());
                    peerProcess.remotePeerDetails.get(preferredPeer.getId()).setIsChoked(0);
                    sendHaveMessage(peerProcess.peerToSocketMap.get(preferredPeer.getId()));
                    peerProcess.remotePeerDetails.get(preferredPeer.getId()).setPeerState(3);
                }
            }
        }
        if (!updatedPreferredNeighbors.isEmpty()) {
            logMessage(peerProcess.peerID + " has selected the preferred neighbors - " + String.join(",", updatedPreferredNeighbors));
        }
    }

    /**
     * This method is used to send UNCHOKE message to socket
     * @param socket - socket in which the message to be sent
     * @param remotePeerID - peerID to which the message should be sent
     */
    private static void sendUnChokedMessage(Socket socket, String remotePeerID) {
        logMessage(peerProcess.peerID + " sending UNCHOKE message to peerProcess " + remotePeerID);
        Msg message = new Msg(Constants.UNCHOKE);
        try {
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
    private static void SendMessageToSocket(Socket socket, byte[] messageInBytes) {
        try {
            OutputStream out = socket.getOutputStream();
            out.write(messageInBytes);
        } catch (IOException ignored) {
        }
    }

    /**
     * This method is used to send HAVE message to socket
     * @param socket - socket in which the message to be sent
     */
    private void sendHaveMessage(Socket socket) {
        byte[] bitFieldInBytes = peerProcess.bitFieldMessage.getFilePieceBytesEncoded();
        try {
            Msg message = new Msg(Constants.HAVE, bitFieldInBytes);
            SendMessageToSocket(socket, Msg.serializeMessage(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package Tasks;

import Configurations.SystemConfiguration;
import Messages.Constants;
import Messages.Msg;
import Metadata.PeerMetadata;
import Process.Peer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

import static Logging.Helper.logMessage;

public class PreferredNeighbors extends TimerTask {

    public void run() {
        int numInterested = 0;
        Peer.updateOtherPeerMetaData();
        Set<String> remotePeerIds = Peer.remotePeerDetails.keySet();
        for(String key: remotePeerIds) {
            PeerMetadata peerMetaData = Peer.remotePeerDetails.get(key);
            if(!key.equals(Peer.peerID)) {
                if (peerMetaData.getHasCompleteFile() == 0 && peerMetaData.getIsInterested() == 1) numInterested++;
                else if (peerMetaData.getHasCompleteFile() == 1) Peer.preferredNeighbours.remove(key);
                }
            }
        updatePreferredNeighbors(numInterested);
    }

    public void updatePreferredNeighbors(int numInterestedPeers) {
        List<String> updatedPreferredNeighbors = new ArrayList<>();
        int numNeighborsToUpdate = Math.min(numInterestedPeers, SystemConfiguration.numberOfPreferredNeighbours);
        if(!Peer.preferredNeighbours.isEmpty() && numInterestedPeers > SystemConfiguration.numberOfPreferredNeighbours) Peer.preferredNeighbours.clear();
        ArrayList<PeerMetadata> preferredPeers = new ArrayList<>(Peer.remotePeerDetails.values());
        int isCompleteFilePresent = Peer.remotePeerDetails.get(Peer.peerID).getHasCompleteFile();
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
                    !preferredPeer.getId().equals(Peer.peerID) &&
                    Peer.remotePeerDetails.get(preferredPeer.getId()).getHasCompleteFile() == 0) {

                Peer.remotePeerDetails.get(preferredPeer.getId()).setIsPreferredNeighbor(1);
                Peer.preferredNeighbours.put(preferredPeer.getId(), Peer.remotePeerDetails.get(preferredPeer.getId()));

                updatedPreferredNeighbors.add(preferredPeer.getId());

                if (Peer.remotePeerDetails.get(preferredPeer.getId()).getIsChoked() == 1) {
                    sendUnChokedMessage(Peer.peerToSocketMap.get(preferredPeer.getId()), preferredPeer.getId());
                    Peer.remotePeerDetails.get(preferredPeer.getId()).setIsChoked(0);
                    sendHaveMessage(Peer.peerToSocketMap.get(preferredPeer.getId()), preferredPeer.getId());
                    Peer.remotePeerDetails.get(preferredPeer.getId()).setPeerState(3);
                }
            }
        }
        if (!updatedPreferredNeighbors.isEmpty()) {
            logMessage(Peer.peerID + " has selected the preferred neighbors - " + String.join(",", updatedPreferredNeighbors));
        }
    }

    /**
     * This method is used to send UNCHOKE message to socket
     * @param socket - socket in which the message to be sent
     * @param remotePeerID - peerID to which the message should be sent
     */
    private static void sendUnChokedMessage(Socket socket, String remotePeerID) {
        logMessage(Peer.peerID + " sending UNCHOKE message to Peer " + remotePeerID);
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
     * @param peerID - peerID to which the message should be sent
     */
    private void sendHaveMessage(Socket socket, String peerID) {
        byte[] bitFieldInBytes = Peer.bitFieldMessage.getFilePieceBytesEncoded();
        try {
            Msg message = new Msg(Constants.HAVE, bitFieldInBytes);
            SendMessageToSocket(socket, Msg.serializeMessage(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

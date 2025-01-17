package Handlers;

import Configurations.SystemConfiguration;
import Messages.BitField;
import Messages.Constants;
import Messages.FilePiece;
import Messages.Msg;
import Metadata.MessageMetadata;
import Metadata.PeerMetadata;
import Queue.MessageQueue;
import Process.peerProcess;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Set;
import static Logging.Helper.logMessage;

public class MessageProcessingHandler implements Runnable {

    private static String peerId;

    public MessageProcessingHandler() {}

    public MessageProcessingHandler(String peerID) {
        peerId = peerID;
    }

    /**
     * This method runs everytime MessageProcessingHandler thread is spawned.
     * It reads messages from message queue and processes them.
     * It sends the appropriate messages based on the type of message received.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            //Poll the message queue
            while (MessageQueue.hasNext()) {
                MessageMetadata messageDetails = MessageQueue.getMessageFromQueue();
                Msg message = messageDetails.getMsg();
                String messageType = message.getType();
                String remotePeerID = messageDetails.getSenderId();
                int peerState = peerProcess.remotePeerDetails.get(remotePeerID).getPeerState();

                if (messageType.equals(Constants.HAVE) && peerState != 14)
                    processInterestingPieces(message, messageType, peerState, remotePeerID);
                else {
                    switch (peerState) {
                        case 2:
                            processBitFieldMessage(messageType, remotePeerID);
                            break;
                        case 3:
                            processInterestsRequest(messageType, remotePeerID);
                            break;
                        case 4:
                            processFileRequest(message, messageType, remotePeerID);
                        case 8:
                            acknowledgeBitFieldMessage(message, messageType, remotePeerID);
                            break;
                        case 9:
                            processChokeUnChokeRequest(messageType, remotePeerID);
                            break;
                        case 11:
                            processReceivedFilePiece(message, messageType, remotePeerID);
                            break;
                        case 14:
                            processHaveRequest(message, messageType, remotePeerID);
                            break;
                        case 15:
                            processPeerDownloadCompleteState(remotePeerID);
                            break;

                    }
                }
            }
        }
    }

    private void processInterestingPieces(Msg message, String messageType, int peerState, String remotePeerId) {
        if (messageType.equals(Constants.HAVE) && peerState != 14) {
            logMessage(peerId + " contains interesting pieces from Peer " + remotePeerId);
            if (isPotentiallyInterestingPeer(message, remotePeerId)) {
                handleInterestedPeer(remotePeerId);
            } else {
                handleUnInterestedPeer(remotePeerId);
            }
        }
    }

    private void handleInterestedPeer(String remotePeerId) {
        sendInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerId), remotePeerId);
        peerProcess.remotePeerDetails.get(remotePeerId).setPeerState(9);
    }

    private void handleUnInterestedPeer(String remotePeerId) {
        sendNotInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerId), remotePeerId);
        peerProcess.remotePeerDetails.get(remotePeerId).setPeerState(13);
    }

    private void processBitFieldMessage(String messageType, String remotePeerID) {
        if (messageType.equals(Constants.BITFIELD)) {
            logMessage(peerId + " received a BITFIELD message from Peer " + remotePeerID);
            sendBitFieldMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
            peerProcess.remotePeerDetails.get(remotePeerID).setPeerState(3);
        }
    }

    private void chokePeer(String remotePeerID) {
        sendChokedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
        peerProcess.remotePeerDetails.get(remotePeerID).setIsChoked(1);
        peerProcess.remotePeerDetails.get(remotePeerID).setPeerState(6);
    }

    private void unChokePeer(String remotePeerID) {
        sendUnChokedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
        peerProcess.remotePeerDetails.get(remotePeerID).setIsChoked(0);
        peerProcess.remotePeerDetails.get(remotePeerID).setPeerState(4);
    }

    private void processInterestedRequestFromPeer(String remotePeerID) {
        logMessage(peerId + " received an INTERESTED message from Peer " + remotePeerID);
        peerProcess.remotePeerDetails.get(remotePeerID).setIsInterested(1);
        peerProcess.remotePeerDetails.get(remotePeerID).setIsHandShakeComplete(1);
        if (isNotPreferredAndUnchokedNeighbour(remotePeerID)) {
            chokePeer(remotePeerID);
        } else {
            unChokePeer(remotePeerID);
        }
    }

    private void processUninterestedRequestFromPeer(String remotePeerID) {
        logMessage(peerId + " received a NOT INTERESTED message from Peer " + remotePeerID);
        peerProcess.remotePeerDetails.get(remotePeerID).setIsInterested(0);
        peerProcess.remotePeerDetails.get(remotePeerID).setIsHandShakeComplete(1);
        peerProcess.remotePeerDetails.get(remotePeerID).setPeerState(5);
    }

    private void processInterestsRequest(String messageType, String remotePeerID) {
        if (messageType.equals(Constants.INTERESTED)) {
            processInterestedRequestFromPeer(remotePeerID);
        } else if (messageType.equals(Constants.NOT_INTERESTED)) {
            processUninterestedRequestFromPeer(remotePeerID);
        }
    }

    private void processFileRequest(Msg message, String messageType, String remotePeerID) {
        try {
            if (messageType.equals(Constants.REQUEST)) {
                sendFilePiece(peerProcess.peerToSocketMap.get(remotePeerID), message, remotePeerID);
                broadcastDownloadCompleteMessage();
                if (isNotPreferredAndUnchokedNeighbour(remotePeerID)) chokePeer(remotePeerID);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcastDownloadCompleteMessage() {
        Set<String> remotePeerDetailsKeys = peerProcess.remotePeerDetails.keySet();
        if (!peerProcess.isFirstPeer && peerProcess.bitFieldMessage.isFileDownloadComplete()) {
            for (String key : remotePeerDetailsKeys) {
                if (!key.equals(peerProcess.peerID)) {
                    Socket socket = peerProcess.peerToSocketMap.get(key);
                    if (socket != null) {
                        sendDownloadCompleteMessage(socket, key);
                    }
                }
            }
        }
    }

    private void acknowledgeBitFieldMessage(Msg message, String messageType, String remotePeerID) {
        if (messageType.equals(Constants.BITFIELD)) {
            if (isPotentiallyInterestingPeer(message, remotePeerID)) {
                handleInterestedPeer(remotePeerID);
            } else {
                handleUnInterestedPeer(remotePeerID);
            }
        }
    }

    private void processChokeUnChokeRequest(String messageType, String remotePeerID) {
        if (messageType.equals(Constants.CHOKE)) {
           handleChokeRequest(remotePeerID);
        } else if (messageType.equals(Constants.UNCHOKE)) {
            handleUnChokeRequest(remotePeerID);
        }
    }

    private void handleChokeRequest(String remotePeerID) {
        logMessage(peerId + " is CHOKED by Peer " + remotePeerID);
        peerProcess.remotePeerDetails.get(remotePeerID).setIsChoked(1);
        peerProcess.remotePeerDetails.get(remotePeerID).setPeerState(14);
    }

    private void handleUnChokeRequest(String remotePeerID) {
        logMessage(peerId + " is UNCHOKED by Peer " + remotePeerID);
        processFirstDifferentPiece(remotePeerID);
    }

    private void processFirstDifferentPiece(String remotePeerID) {
        int firstDifferentPieceIndex = getFirstDifferentPieceIndex(remotePeerID);
        if (firstDifferentPieceIndex == -1) {
            peerProcess.remotePeerDetails.get(remotePeerID).setPeerState(13);
        } else {
            sendRequestMessage(peerProcess.peerToSocketMap.get(remotePeerID), firstDifferentPieceIndex, remotePeerID);
            peerProcess.remotePeerDetails.get(remotePeerID).setPeerState(11);
            peerProcess.remotePeerDetails.get(remotePeerID).setStartTime(new Date());
        }
    }

    private void processReceivedFilePiece(Msg message, String messageType, String remotePeerID) {
        if (messageType.equals(Constants.CHOKE)) handleChokeRequest(remotePeerID);
        else if (messageType.equals(Constants.PIECE)) {
            //Received piece message
            byte[] payloadInBytes = message.getPayload();
            updateRemotePeerDownloadRate(payloadInBytes, remotePeerID);
            FilePiece filePiece = FilePiece.getFilePieceFromPayload(payloadInBytes);
            //update the piece information in current peer bitfield
            peerProcess.bitFieldMessage.updateBitFieldMetadata(remotePeerID, filePiece);
            processFirstDifferentPiece(remotePeerID);
            peerProcess.updateOtherPeerMetaData();
            Set<String> remotePeerDetailsKeys = peerProcess.remotePeerDetails.keySet();
            for (String key : remotePeerDetailsKeys) {
                PeerMetadata peerDetails = peerProcess.remotePeerDetails.get(key);
                //send have message to peer if its interested
                if (!key.equals(peerProcess.peerID) && isPeerInterested(peerDetails)) {
                    sendHaveMessage(peerProcess.peerToSocketMap.get(key));
                    peerProcess.remotePeerDetails.get(key).setPeerState(3);
                }
            }
            if (!peerProcess.isFirstPeer && peerProcess.bitFieldMessage.isFileDownloadComplete()) broadcastDownloadCompleteMessage();
        }
    }

    private void updateRemotePeerDownloadRate(byte[] payloadInBytes, String remotePeerID) {
        //compute the peer data downloading rate
        peerProcess.remotePeerDetails.get(remotePeerID).setEndTime(new Date());
        long endTime = peerProcess.remotePeerDetails.get(remotePeerID).getEndTime().getTime();
        long startTime = peerProcess.remotePeerDetails.get(remotePeerID).getStartTime().getTime();
        long totalTime = endTime - startTime;
        double dataRate = ((double) (payloadInBytes.length + Constants.MESSAGE_LENGTH + Constants.MESSAGE_TYPE) / (double) totalTime) * 100;
        peerProcess.remotePeerDetails.get(remotePeerID).setDataRate(dataRate);
    }

    private void processHaveRequest(Msg message, String messageType, String remotePeerID) {
        if (messageType.equals(Constants.HAVE)) {
            //Received contains interesting pieces
            logMessage(peerId + " contains interesting pieces from Peer " + remotePeerID);
            if (isPotentiallyInterestingPeer(message, remotePeerID)) {
                handleInterestedPeer(remotePeerID);
            } else {
                handleUnInterestedPeer(remotePeerID);
            }
        } else if (messageType.equals(Constants.UNCHOKE)) {
            //Received unChoked message
            handleUnChokeRequest(remotePeerID);
        }
    }

    private void processPeerDownloadCompleteState(String remotePeerID) {
//        try {
            //update neighbor details after it gets file completely
//            peerProcess.remotePeerDetails.get(peerProcess.peerID).updatePeerMetadata(remotePeerID, 1);
            logMessage(remotePeerID + " has downloaded the complete file");
            int previousState = peerProcess.remotePeerDetails.get(remotePeerID).getPreviousPeerState();
            peerProcess.remotePeerDetails.get(remotePeerID).setPeerState(previousState);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * This method is used to check remote peer is interested to receive messages
     *
     * @param remotePeerDetails - peerProcess to be checked
     * @return true - peer interested; false peer not interested
     */
    private boolean isPeerInterested(PeerMetadata remotePeerDetails) {
        return remotePeerDetails.getHasCompleteFile() == 0 && remotePeerDetails.getIsChoked() == 0 && remotePeerDetails.getIsInterested() == 1;
    }

    private int getFirstDifferentPieceIndex(String peerID) {
        return peerProcess.bitFieldMessage.getFirstDifferentPieceIndex(peerProcess.remotePeerDetails.get(peerID).getBitFieldMessage());
    }

    /**
     * This method is used if remote peer is not a preferred neighbor or optimistically unchoked neighbor.
     *
     * @param remotePeerId - peerID to be checked
     * @return true - remote peer is not preferred neighbor or optimistically unchoked neighbor;
     * false - remote peer is preferred neighbor or optimistically unchoked neighbor
     */
    private boolean isNotPreferredAndUnchokedNeighbour(String remotePeerId) {
        return !peerProcess.preferredNeighbours.containsKey(remotePeerId) && !peerProcess.optimisticUnChokedNeighbors.containsKey(remotePeerId);
    }


    /**
     * This method is used to send HAVE message to socket
     *
     * @param socket - socket in which the message to be sent
     */
    private void sendHaveMessage(Socket socket) {
        byte[] bitFieldInBytes = peerProcess.bitFieldMessage.getFilePieceBytesEncoded();
        try {
            Msg message = new Msg(Constants.HAVE, bitFieldInBytes);
            sendMessageToSocket(socket, Msg.serializeMessage(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to write a message to socket
     *
     * @param socket         - socket in which the message to be sent
     * @param messageInBytes - message to be sent
     */
    private void sendMessageToSocket(Socket socket, byte[] messageInBytes) {
        try {
            OutputStream out = socket.getOutputStream();
            out.write(messageInBytes);
        } catch (IOException ignored) {
        }
    }


    /**
     * This method is used to send DOWNLOAD COMPLETE message to socket
     *
     * @param socket - socket in which the message to be sent
     * @param remotePeerID - peerID to which the message should be sent
     */
    private void sendDownloadCompleteMessage(Socket socket, String remotePeerID) {
        logMessage(peerProcess.peerID + " sending a DOWNLOAD COMPLETE message to Peer " + remotePeerID);
        Msg message = new Msg(Constants.MESSAGE_DOWNLOADED);
        byte[] messageInBytes = new byte[0];
        try {
            messageInBytes = Msg.serializeMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendMessageToSocket(socket, messageInBytes);
    }

    /**
     * This method is used to send REQUEST message to socket
     *
     * @param socket       - socket in which the message to be sent
     * @param pieceIndex   - index of the piece to be requested
     * @param remotePeerID - peerID to which the message should be sent
     */
    private void sendRequestMessage(Socket socket, int pieceIndex, String remotePeerID) {
        logMessage(peerProcess.peerID + " sending REQUEST message to Peer " + remotePeerID + " for piece " + pieceIndex);
        byte[] pieceIndexInBytes = ByteBuffer.allocate(4).putInt(pieceIndex).array();
        try {
            Msg message = new Msg(Constants.REQUEST, pieceIndexInBytes);
            sendMessageToSocket(socket, Msg.serializeMessage(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to send File piece to socket
     *
     * @param socket       - socket in which the message to be sent
     * @param message      - message to be sent
     * @param remotePeerID - peerID to which the message should be sent
     */
    private void sendFilePiece(Socket socket, Msg message, String remotePeerID) {
        byte[] pieceIndexInBytes = message.getPayload();
        int pieceIndex = ByteBuffer.wrap(pieceIndexInBytes).getInt();
        int pieceSize = SystemConfiguration.pieceSize;
        logMessage(peerId + " sending a PIECE message for piece " + pieceIndex + " to Peer " + remotePeerID);

        byte[] bytesRead = new byte[pieceSize];
        int numberOfBytesRead;
        File file = new File(peerProcess.peerFolder, SystemConfiguration.fileName);
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            randomAccessFile.seek(pieceIndex * pieceSize);
            numberOfBytesRead = randomAccessFile.read(bytesRead, 0, pieceSize);

            byte[] buffer = new byte[numberOfBytesRead + Constants.PIECE_INDEX_LENGTH];
            System.arraycopy(pieceIndexInBytes, 0, buffer, 0, Constants.PIECE_INDEX_LENGTH);
            System.arraycopy(bytesRead, 0, buffer, Constants.PIECE_INDEX_LENGTH, numberOfBytesRead);

            Msg messageToBeSent = new Msg(Constants.PIECE, buffer);
            sendMessageToSocket(socket, Msg.serializeMessage(messageToBeSent));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to check if a peer is interested to receive messages.
     *
     * @param message      - message to be checked
     * @param remotePeerID - peerID to which the message should be sent
     * @return true - peer interested; false - peer not interested
     */
    private boolean isPotentiallyInterestingPeer(Msg message, String remotePeerID) {
        boolean peerInterested = false;
        BitField bitField = BitField.decodedFilePieceBytes(message.getPayload());
        peerProcess.remotePeerDetails.get(remotePeerID).setBitFieldMessage(bitField);
        int pieceIndex = peerProcess.bitFieldMessage.getInterestingPieceIndex(bitField);
        if (pieceIndex != -1) {
            if (message.getType().equals(Constants.HAVE))
                logMessage(peerId + " received HAVE message from Peer " + remotePeerID + " for piece " + pieceIndex);
            peerInterested = true;
        }

        return peerInterested;
    }

    /**
     * This method is used to send CHOKE message to socket
     *
     * @param socket       - socket in which the message to be sent
     * @param remotePeerID - peerID to which the message should be sent
     */
    private void sendChokedMessage(Socket socket, String remotePeerID) {
        logMessage(peerId + " sending a CHOKE message to Peer " + remotePeerID);
        Msg message = new Msg(Constants.CHOKE);
        byte[] messageInBytes = new byte[0];
        try {
            messageInBytes = Msg.serializeMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendMessageToSocket(socket, messageInBytes);
    }

    /**
     * This method is used to send UNCHOKE message to socket
     *
     * @param socket       - socket in which the message to be sent
     * @param remotePeerID - peerID to which the message should be sent
     */
    private void sendUnChokedMessage(Socket socket, String remotePeerID) {
        logMessage(peerId + " sending a UNCHOKE message to Peer " + remotePeerID);
        Msg message = new Msg(Constants.UNCHOKE);
        byte[] messageInBytes = new byte[0];
        try {
            messageInBytes = Msg.serializeMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendMessageToSocket(socket, messageInBytes);
    }

    /**
     * This method is used to send NOT INTERESTED message to socket
     *
     * @param socket       - socket in which the message to be sent
     * @param remotePeerID - peerID to which the message should be sent
     */
    private void sendNotInterestedMessage(Socket socket, String remotePeerID) {
        logMessage(peerId + " sending a NOT INTERESTED message to Peer " + remotePeerID);
        Msg message = new Msg(Constants.NOT_INTERESTED);
        byte[] messageInBytes = new byte[0];
        try {
            messageInBytes = Msg.serializeMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendMessageToSocket(socket, messageInBytes);
    }

    /**
     * This method is used to send INTERESTED message to socket
     *
     * @param socket       - socket in which the message to be sent
     * @param remotePeerID - peerID to which the message should be sent
     */
    private void sendInterestedMessage(Socket socket, String remotePeerID) {
        logMessage(peerId + " sending an INTERESTED message to Peer " + remotePeerID);
        Msg message = new Msg(Constants.INTERESTED);
        byte[] messageInBytes = new byte[0];
        try {
            messageInBytes = Msg.serializeMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendMessageToSocket(socket, messageInBytes);
    }

    /**
     * This method is used to send BITFIELD message to socket
     *
     * @param socket       - socket in which the message to be sent
     * @param remotePeerID - peerID to which the message should be sent
     */
    private void sendBitFieldMessage(Socket socket, String remotePeerID) {
        logMessage(peerId + " sending a BITFIELD message to Peer " + remotePeerID);
        byte[] bitFieldMessageInByteArray = peerProcess.bitFieldMessage.getFilePieceBytesEncoded();
        try {
            Msg message = new Msg(Constants.BITFIELD, bitFieldMessageInByteArray);
            byte[] messageInBytes = Msg.serializeMessage(message);
            sendMessageToSocket(socket, messageInBytes);
        }
        catch (Exception ignored) {
        }
    }

}

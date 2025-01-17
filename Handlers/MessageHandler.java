package Handlers;

import Logging.Helper;
import Messages.Constants;
import Messages.HandShakeMsg;
import Metadata.MessageMetadata;
import Messages.Msg;
import Queue.MessageQueue;
import Process.peerProcess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class MessageHandler implements Runnable {
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private HandShakeMsg handShakeMsg;
    public String id;
    public String remotePeerId;
    private final int connectionType;

    public MessageHandler(String id, int connectionType, String address, int port) throws IOException {
        this.id = id;
        this.connectionType = connectionType;
        this.socket = new Socket(address, port);
        this.inputStream = this.socket.getInputStream();
        this.outputStream = this.socket.getOutputStream();
    }

    public MessageHandler(String id, int connectionType, Socket socket) throws IOException {
        this.socket = socket;
        this.connectionType = connectionType;
        this.id = id;
        this.inputStream = this.socket.getInputStream();
        this.outputStream = this.socket.getOutputStream();
    }

    public boolean initiateHandshake() throws IOException {
        HandShakeMsg handShakeMsg = new HandShakeMsg(Constants.HANDSHAKE_HEADER, this.id);
        this.outputStream.write(handShakeMsg.serializeHandShakeMsg());
        return true;
    }

    public void exchangeBitfield() throws Exception {
        byte[] handShakeMessageBytes = new byte[32];
        while (true) {
            if (inputStream.read(handShakeMessageBytes) > 0) {
                handShakeMsg = HandShakeMsg.deserializeHandShakeMsg(handShakeMessageBytes);
                if (handShakeMsg.getHeader().equals(Constants.HANDSHAKE_HEADER)) {
                    remotePeerId = handShakeMsg.getPeerID();
                    Helper.logMessage(id + " established a connection to " + remotePeerId);
                    Helper.logMessage(id + " Received a HANDSHAKE message from " + remotePeerId);
                    // populate peerID to socket mapping
                    peerProcess.peerToSocketMap.put(remotePeerId, this.socket);
                    break;
                }
            }
        }
        // Sending BitField...
        Msg m = new Msg(Constants.BITFIELD, peerProcess.bitFieldMessage.getFilePieceBytesEncoded());
        byte[] b = Msg.serializeMessage(m);
        outputStream.write(b);
        // set remote peer state
        peerProcess.remotePeerDetails.get(remotePeerId).setPeerState(8);
    }

    public void processPassiveConnection() throws Exception {
        byte[] messageBytes = new byte[32];
        while (true) {
            if (inputStream.read(messageBytes) > 0) {
                handShakeMsg = HandShakeMsg.deserializeHandShakeMsg(messageBytes);
                if (handShakeMsg.getHeader().equals(Constants.HANDSHAKE_HEADER)) {
                    remotePeerId = handShakeMsg.getPeerID();
                    Helper.logMessage(id + " is connected from Peer " + remotePeerId);
                    Helper.logMessage(id + " Received a HANDSHAKE message from Peer " + remotePeerId);

                    // populate peerID to socket mapping
                    peerProcess.peerToSocketMap.put(remotePeerId, this.socket);
                    break;
                }
            }
        }
        if (initiateHandshake()) {
            Helper.logMessage(id + " HANDSHAKE message has been sent successfully.");

        } else {
            Helper.logMessage(id + " HANDSHAKE message sending failed.");
            System.exit(-1);
        }
         // set remote peer state
        peerProcess.remotePeerDetails.get(remotePeerId).setPeerState(2);
    }

    public void processMessages() throws IOException {
        byte[] messageLength;
        byte[] messageType;
        byte[] dataBufferWithoutPayload = new byte[Constants.MESSAGE_LENGTH + Constants.MESSAGE_TYPE];
        MessageMetadata metadata = new MessageMetadata();
        while (!Thread.currentThread().isInterrupted()) {
            int headerBytes = inputStream.read(dataBufferWithoutPayload);
            if (headerBytes == -1)
                break;
            messageLength = new byte[Constants.MESSAGE_LENGTH];
            messageType = new byte[Constants.MESSAGE_TYPE];
            System.arraycopy(dataBufferWithoutPayload, 0, messageLength, 0, Constants.MESSAGE_LENGTH);
            System.arraycopy(dataBufferWithoutPayload, Constants.MESSAGE_LENGTH, messageType, 0, Constants.MESSAGE_TYPE);
            Msg message = new Msg();
            message.setMessageLength(messageLength);
            message.setMessageType(messageType);
            switch(message.getType()) {
                case Constants.INTERESTED:
                case Constants.NOT_INTERESTED:
                case Constants.CHOKE:
                case Constants.UNCHOKE:
                    metadata.setMsg(message);
                    metadata.setSenderId(remotePeerId);
                    MessageQueue.addMessageToMessageQueue(metadata);
                    break;
                case Constants.MESSAGE_DOWNLOADED:
                    metadata.setMsg(message);
                    metadata.setSenderId(remotePeerId);
                    int peerState = peerProcess.remotePeerDetails.get(remotePeerId).getPeerState();
                    peerProcess.remotePeerDetails.get(remotePeerId).setPreviousPeerState(peerState);
                    peerProcess.remotePeerDetails.get(remotePeerId).setPeerState(15);
                    MessageQueue.addMessageToMessageQueue(metadata);
                    break;
                default:
                    int bytesAlreadyRead = 0;
                    int bytesRead;
                    byte[] dataBuffPayload = new byte[message.getDataLength() - 1];
                    while (bytesAlreadyRead < message.getDataLength() - 1) {
                        bytesRead = inputStream.read(dataBuffPayload, bytesAlreadyRead, message.getDataLength() - 1 - bytesAlreadyRead);
                        if (bytesRead == -1)
                            return;
                        bytesAlreadyRead += bytesRead;
                    }

                    byte[] dataBuffWithPayload = new byte[message.getDataLength() + Constants.MESSAGE_LENGTH];
                    System.arraycopy(dataBufferWithoutPayload, 0, dataBuffWithPayload, 0, Constants.MESSAGE_LENGTH + Constants.MESSAGE_TYPE);
                    System.arraycopy(dataBuffPayload, 0, dataBuffWithPayload, Constants.MESSAGE_LENGTH + Constants.MESSAGE_TYPE, dataBuffPayload.length);

                    Msg dataMsgWithPayload = Msg.deserializeMessage(dataBuffWithPayload);
                    metadata.setMsg(dataMsgWithPayload);
                    metadata.setSenderId(remotePeerId);
                    MessageQueue.addMessageToMessageQueue(metadata);
                    break;
            }
        }
    }

    /**
     * This method is run everytime MessageHandler thread is started.
     * It supports 2 types of connection
     * Active Connection : It performs initial handshake and bitfield messages sending to socket.
     * Passive Connection : It reads messages from socket and adds them to message queue.
     */
    @Override
    public void run() {
        try {
            if (connectionType == Constants.ACTIVE_CONNECTION) {
                if (initiateHandshake()) {
                    Helper.logMessage(this.id + "Handshake Message sent.");
                    exchangeBitfield();
                } else {
                    Helper.logMessage(this.id + " Handshake message sending failed.");
                    System.exit(-1);
                }
            } else {
                processPassiveConnection();
            }
            processMessages();
        }
        catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()) + ": " + e.toString());
        }
    }
}

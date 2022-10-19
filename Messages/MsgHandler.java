package Messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * This class is used to write/read messages from socket
 */
public class MsgHandler implements Runnable {
    //socket from where message are read/written
    private Socket peerSocket = null;
    //The type of connection established
    private int connType;
    //The peerID of the current host
    String ownPeerId;
    //The peerID of the remote host
    String remotePeerId;
    //The input stream of the socket
    private InputStream socketInputStream;
    //The output stream of the socket
    private OutputStream socketOutputStream;
    //The handshake message received
    private HandShakeMsg handshakeMessage;

    /**
     * This constructor initializes the PeerMessage Handler object setting up the required fields
     *
     * @param address        - address of the remote host to be connected to
     * @param port           - port of the remote host
     * @param connectionType - type of connection established
     * @param serverPeerID   - peer ID of the remote host
     */
    public MsgHandler(String address, int port, int connectionType, String serverPeerID) {
        try {
            connType = connectionType;
            ownPeerId = serverPeerID;
            peerSocket = new Socket(address, port);
            socketInputStream = peerSocket.getInputStream();
            socketOutputStream = peerSocket.getOutputStream();
        } catch (IOException e) {
        }
    }

    /**
     * This constructor initializes the PeerMessage Handler object setting up the required fields
     *
     * @param socket         - the socket connection created for the remote host
     * @param connectionType - type of connection established
     * @param serverPeerID   - peer ID of the remote host
     */
    public MsgHandler(Socket socket, int connectionType, String serverPeerID) {
        try {
            peerSocket = socket;
            connType = connectionType;
            ownPeerId = serverPeerID;
            socketInputStream = peerSocket.getInputStream();
            socketOutputStream = peerSocket.getOutputStream();
        } catch (IOException e) {

        }
    }

    /**
     * This method is used to get the socket instance
     *
     * @return socket
     */
    public Socket getPeerSocket() {
        return peerSocket;
    }

    /**
     * This method is used to set socket instance
     *
     * @param peerSocket - socket to be set
     */
    public void setPeerSocket(Socket peerSocket) {
        this.peerSocket = peerSocket;
    }

    /**
     * This method is used to get the connection type established
     *
     * @return type of connection established
     */
    public int getConnType() {
        return connType;
    }

    /**
     * This method is used to set the connection type established
     *
     * @param connType - connection type to be set
     */
    public void setConnType(int connType) {
        this.connType = connType;
    }

    /**
     * This method is used to get the current host peerID
     *
     * @return current host peerID
     */
    public String getOwnPeerId() {
        return ownPeerId;
    }

    /**
     * This method is used to set the current host peerID
     *
     * @param ownPeerId - current host peerID
     */
    public void setOwnPeerId(String ownPeerId) {
        this.ownPeerId = ownPeerId;
    }

    /**
     * This method is used to get the remote host peerID
     *
     * @return remote host peerID
     */
    public String getRemotePeerId() {
        return remotePeerId;
    }

    /**
     * This method is used to set the remote host peerID
     *
     * @param remotePeerId - remote host peerID
     */
    public void setRemotePeerId(String remotePeerId) {
        this.remotePeerId = remotePeerId;
    }

    /**
     * This method is used to get the socket input stream
     *
     * @return socket input stream
     */
    public InputStream getSocketInputStream() {
        return socketInputStream;
    }

    /**
     * This method is used to set the socket input stream
     *
     * @param socketInputStream - socket input stream
     */
    public void setSocketInputStream(InputStream socketInputStream) {
        this.socketInputStream = socketInputStream;
    }

    /**
     * This method is used to get the socket output stream
     *
     * @return socket output stream
     */
    public OutputStream getSocketOutputStream() {
        return socketOutputStream;
    }

    /**
     * This method is used to set the socket output stream
     *
     * @param socketOutputStream - socket output stream
     */
    public void setSocketOutputStream(OutputStream socketOutputStream) {
        this.socketOutputStream = socketOutputStream;
    }

    /**
     * This method is used to get the handshake message
     *
     * @return handshake message
     */
    public HandShakeMsg getHandshakeMessage() {
        return handshakeMessage;
    }

    /**
     * This method is used to set the handshake message
     *
     * @param handshakeMessage - handshake message
     */
    public void setHandshakeMessage(HandShakeMsg handshakeMessage) {
        this.handshakeMessage = handshakeMessage;
    }

    @Override
    public void run() {
        // TODO: Implement run
    }
}
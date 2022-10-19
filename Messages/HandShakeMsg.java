package Messages;

import java.io.UnsupportedEncodingException;

public class HandShakeMsg {
    private byte[] headerInBytes = new byte[Constants.HANDSHAKE_HEADER_LENGTH];
    private byte[] peerIDInBytes = new byte[Constants.HANDSHAKE_PEERID_LENGTH];
    private byte[] zeroBits = new byte[Constants.HANDSHAKE_ZEROBITS_LENGTH];
    private String header;
    private String peerID;

    public HandShakeMsg() {};

    public HandShakeMsg(String header, String peerID) throws UnsupportedEncodingException {
            this.header = header;
            this.headerInBytes = header.getBytes(Constants.DEFAULT_CHARSET);
            this.peerID = peerID;
            this.peerIDInBytes = peerID.getBytes(Constants.DEFAULT_CHARSET);
            this.zeroBits = "0000000000".getBytes(Constants.DEFAULT_CHARSET);
    }

    public void setHeaderFromBytes(byte[] headerBytes) throws UnsupportedEncodingException {
        this.header = new String(headerBytes, Constants.DEFAULT_CHARSET);
        this.headerInBytes = headerBytes;
    }

    public void setPeerIdFromBytes(byte[] peerIDBytes) throws UnsupportedEncodingException {
        this.peerID = new String(peerIDBytes, Constants.DEFAULT_CHARSET);
        this.peerIDInBytes = peerIDBytes;
    }

    public byte[] serializeHandShakeMsg() throws ArrayStoreException {
        byte[] handShakeMessageInBytes = new byte[Constants.HANDSHAKE_MESSAGE_LENGTH];
            System.arraycopy(this.headerInBytes, 0, handShakeMessageInBytes, 0, this.headerInBytes.length);
            System.arraycopy(this.zeroBits, 0, handShakeMessageInBytes, Constants.HANDSHAKE_HEADER_LENGTH, Constants.HANDSHAKE_ZEROBITS_LENGTH - 1);
            System.arraycopy(this.peerIDInBytes, 0,  handShakeMessageInBytes, Constants.HANDSHAKE_HEADER_LENGTH + Constants.HANDSHAKE_ZEROBITS_LENGTH, this.peerIDInBytes.length);
        return handShakeMessageInBytes;
    }

    public static HandShakeMsg deserializeHandShakeMsg(byte[] handShakeMessageInBytes) throws ArrayStoreException, UnsupportedEncodingException {
        HandShakeMsg message = null;
        byte[] header = new byte[Constants.HANDSHAKE_HEADER_LENGTH];
        byte[] peerId = new byte[Constants.HANDSHAKE_PEERID_LENGTH];

        System.arraycopy(handShakeMessageInBytes, 0, header, 0, Constants.HANDSHAKE_HEADER_LENGTH);
        System.arraycopy(handShakeMessageInBytes, Constants.HANDSHAKE_HEADER_LENGTH + Constants.HANDSHAKE_ZEROBITS_LENGTH, peerId, 0, Constants.HANDSHAKE_PEERID_LENGTH);
        message = new HandShakeMsg();
        message.setHeaderFromBytes(header);
        message.setPeerIdFromBytes(peerId);
        return message;
    }

    public String getHeader() {
        return header;
    }

    public String getPeerID() {
        return peerID;
    }

    public void setPeerID(String peerID) {
        this.peerID = peerID;
    }
}

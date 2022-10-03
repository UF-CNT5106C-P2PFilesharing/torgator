package Messages;

import java.io.UnsupportedEncodingException;

public class HandShakeMsg {
    private byte[] headerInBytes = new byte[Constants.HANDSHAKE_HEADER_LENGTH];
    private byte[] peerIDInBytes = new byte[Constants.HANDSHAKE_PEERID_LENGTH];
    private byte[] zeroBits = new byte[Constants.HANDSHAKE_ZEROBITS_LENGTH];
    private String header;
    private String peerID;

    public HandShakeMsg(String header, String peerID) throws UnsupportedEncodingException {
            this.header = header;
            this.headerInBytes = header.getBytes(Constants.DEFAULT_CHARSET);
            this.peerID = peerID;
            this.peerIDInBytes = peerID.getBytes(Constants.DEFAULT_CHARSET);
            this.zeroBits = "0000000000".getBytes(Constants.DEFAULT_CHARSET);
    }

    public byte[] serializeHandShakeMsg() throws ArrayStoreException {
        byte[] handShakeMessageInBytes = new byte[Constants.HANDSHAKE_MESSAGE_LENGTH];
            System.arraycopy(this.headerInBytes, 0, handShakeMessageInBytes, 0, this.headerInBytes.length);
            System.arraycopy(this.zeroBits, 0, handShakeMessageInBytes, Constants.HANDSHAKE_HEADER_LENGTH, Constants.HANDSHAKE_ZEROBITS_LENGTH - 1);
            System.arraycopy(this.peerIDInBytes, 0,  handShakeMessageInBytes, Constants.HANDSHAKE_HEADER_LENGTH + Constants.HANDSHAKE_ZEROBITS_LENGTH, this.peerIDInBytes.length);
        return handShakeMessageInBytes;
    }
}

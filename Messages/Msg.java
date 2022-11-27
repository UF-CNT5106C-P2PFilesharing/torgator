package Messages;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import static Logging.Helper.logMessage;

public class Msg {
    private String lengthInString;
    private String type;
    private int dataLength;
    private byte[] payload = null;
    private byte[] typeInBytes = null;
    private byte[] lengthInBytes = null;

    public Msg() {};

    public Msg(String messageType) {
        try {
            if (messageType.equals(Constants.INTERESTED) || messageType.equals(Constants.NOT_INTERESTED) || messageType.equals(Constants.CHOKE) || messageType.equals(Constants.UNCHOKE) || messageType.equals(Constants.MESSAGE_DOWNLOADED)) {
               setMessageLength(Constants.MESSAGE_TYPE);
               this.type = messageType.trim();
               this.typeInBytes = messageType.trim().getBytes(Constants.DEFAULT_CHARSET);
               this.payload = null;
            } else {
                throw new Exception("Invalid message type.");
            }
        } catch (Exception e) {
            logMessage(e.getMessage());
        }
    }

    public Msg(String messageType, byte[] payload) throws UnsupportedEncodingException {
        if (payload != null) {
            setMessageLength(payload.length + 1);
            setPayload(payload);
        }
        else {
            if (messageType.equals(Constants.INTERESTED) || messageType.equals(Constants.NOT_INTERESTED) || messageType.equals(Constants.CHOKE) || messageType.equals(Constants.UNCHOKE) || messageType.equals(Constants.MESSAGE_DOWNLOADED)) {
                setMessageLength(Constants.MESSAGE_TYPE);
                setPayload(null);
            }
        }
        this.type = messageType.trim();
        this.typeInBytes = messageType.trim().getBytes(Constants.DEFAULT_CHARSET);
    }

    public String getType() {
        return this.type;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public byte[] getLengthAsBytes() {
        return this.lengthInBytes;
    }

    public byte[] getTypeAsBytes() {
        return this.typeInBytes;
    }

    public void setMessageLength(int length){
        this.dataLength = length;
        this.lengthInString = ((Integer) length).toString();
        this.lengthInBytes = ByteBuffer.allocate(4).putInt(length).array();
    }

    public void setMessageLength(byte[] length) throws UnsupportedEncodingException {
        int len = ByteBuffer.wrap(length).getInt();
        this.dataLength = len;
        this.lengthInString = Integer.toString(len);
        this.lengthInBytes = length;
    }

    public void setMessageType(byte[] type) throws UnsupportedEncodingException {
            this.type = new String(type, Constants.DEFAULT_CHARSET);
            this.typeInBytes = type;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public static byte[] serializeMessage(Msg message) throws Exception {
        byte[] serializedMessage;
        int messageType = Integer.parseInt(message.getType());
        if (0 < messageType && messageType <= 8) {
            if (message.getPayload() != null) {
                serializedMessage = new byte[Constants.MESSAGE_LENGTH + Constants.MESSAGE_TYPE + message.getPayload().length];
                System.arraycopy(message.getLengthAsBytes(), 0, serializedMessage, 0, message.getLengthAsBytes().length);
                System.arraycopy(message.getTypeAsBytes(), 0, serializedMessage, Constants.MESSAGE_LENGTH, Constants.MESSAGE_TYPE);
                System.arraycopy(message.getPayload(), 0, serializedMessage, Constants.MESSAGE_LENGTH + Constants.MESSAGE_TYPE, message.getPayload().length);
            } else {
                serializedMessage = new byte[Constants.MESSAGE_LENGTH + Constants.MESSAGE_TYPE];
                System.arraycopy(message.getLengthAsBytes(), 0, serializedMessage, 0, message.getLengthAsBytes().length);
                System.arraycopy(message.getTypeAsBytes(), 0, serializedMessage, Constants.MESSAGE_LENGTH, Constants.MESSAGE_TYPE);
            }
        }
        else {
            System.out.println("Message Type: " + messageType);
            throw new Exception("Invalid message Type");
        }
        return serializedMessage;
    }

    public static Msg deserializeMessage(byte[] message) throws ArrayStoreException, UnsupportedEncodingException {
        Msg msg = new Msg();
        byte[] msgLength = new byte[Constants.MESSAGE_LENGTH];
        byte[] msgType = new byte[Constants.MESSAGE_TYPE];
        byte[] payLoad = null;

        System.arraycopy(message, 0, msgLength, 0, Constants.MESSAGE_LENGTH);
        System.arraycopy(message, Constants.MESSAGE_LENGTH, msgType, 0, Constants.MESSAGE_TYPE);

        msg.setMessageLength(msgLength);
        msg.setMessageType(msgType);
        int messageLength = ByteBuffer.wrap(msgLength).getInt();
        if (messageLength != 1) {
            payLoad = new byte[messageLength - 1];
            System.arraycopy(message, Constants.MESSAGE_LENGTH + Constants.MESSAGE_TYPE, payLoad, 0, message.length - Constants.MESSAGE_LENGTH - Constants.MESSAGE_TYPE);
            msg.setPayload(payLoad);
        }
        return msg;

    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }
}


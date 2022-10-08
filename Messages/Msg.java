package Messages;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import static Logging.Helper.logMessage;

public class Msg {
    private String lengthInString;
    private String type;
    private int message_type_length = Constants.MESSAGE_TYPE;
    private byte[] payload = null;
    private byte[] typeInBytes = null;
    private byte[] lengthInBytes = null;

    public Msg() {};

    public Msg(String messageType) {
        try {
            if (messageType.equals(Constants.INTERESTED) || messageType.equals(Constants.NOT_INTERESTED) || messageType.equals(Constants.CHOKE) || messageType.equals(Constants.UNCHOKE) || messageType.equals(Constants.MESSAGE_DOWNLOADED)) {
                this.message_type_length = Constants.MESSAGE_TYPE;
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
            this.message_type_length = payload.length + 1;
            this.payload = payload;
        }
        else {
            if (messageType.equals(Constants.INTERESTED) || messageType.equals(Constants.NOT_INTERESTED) || messageType.equals(Constants.CHOKE) || messageType.equals(Constants.UNCHOKE) || messageType.equals(Constants.MESSAGE_DOWNLOADED)) {
                this.message_type_length = Constants.MESSAGE_TYPE;
                this.payload = null;
            }
        }
        this.type = messageType;
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

    public void setMessageLength(int length) throws UnsupportedEncodingException {
        this.message_type_length = length;
        this.lengthInString = ((Integer) length).toString();
        this.lengthInBytes = this.lengthInString.getBytes(Constants.DEFAULT_CHARSET);
    }

    public void setMessageLength(byte[] length) throws UnsupportedEncodingException {
        int len = ByteBuffer.wrap(length).getInt();
        this.message_type_length = len;
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
        byte[] serializedMessage = null;
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
            else throw new Exception("Invalid message Type");
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
}


package Messages;

/**
 * This class is used to handle message and its metadata
 */
public class MsgDetail {

    //Message sent/received
    private Msg message;
    //peerID of sender
    private String fromPeerID;

    /**
     * Constructor creating Message Details instance and setting required fields
     */
    public MsgDetail() {
        message = new Msg();
        fromPeerID = null;
    }

    /**
     * This method is used get the message
     * @return message
     */
    public Msg getMessage() {
        return message;
    }

    /**
     * This method is used to set the message
     * @param message
     */
    public void setMessage(Msg message) {
        this.message = message;
    }

    /**
     * This method is used get the peerID of the sender
     * @return peerID
     */
    public String getFromPeerID() {
        return fromPeerID;
    }

    /**
     * This method is used set the peerID of the sender
     * @param fromPeerID
     */
    public void setFromPeerID(String fromPeerID) {
        this.fromPeerID = fromPeerID;
    }

}
package Messages;

public class MsgDetails {

    private Msg message;
    private String fromPeerID;
    public MsgDetails() {
        message = new Msg();
        fromPeerID = null;
    }

    
    public Msg getMessage() {
        return message;
    }

    public void setMessage(Msg message) {
        this.message = message;
    }

    public String getFromPeerID() {
        return fromPeerID;
    }

    public void setFromPeerID(String fromPeerID) {
        this.fromPeerID = fromPeerID;
    }

}


public class MessageDetails {

    private Message message;
    private String fromPeerID;
    public MessageDetails() {
        message = new Message();
        fromPeerID = null;
    }

    
    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getFromPeerID() {
        return fromPeerID;
    }

    public void setFromPeerID(String fromPeerID) {
        this.fromPeerID = fromPeerID;
    }

}

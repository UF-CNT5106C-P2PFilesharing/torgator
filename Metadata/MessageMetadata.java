package Metadata;

import Messages.Msg;

public class MessageMetadata {
    private Msg msg;
    private String senderId;

    public MessageMetadata() {
        this.msg = new Msg();
    }

    public Msg getMsg() {
        return msg;
    }

    public void setMsg(Msg msg) {
        this.msg = msg;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}


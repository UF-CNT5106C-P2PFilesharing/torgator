package Handlers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import Process.Peer;

public class ServerHandler implements Runnable {
    private final ServerSocket serverSocket;
    private final String selfPeerId;

    public ServerHandler(ServerSocket serverSocket, String peerID) {
        this.serverSocket = serverSocket;
        this.selfPeerId = peerID;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            try {
                Socket peerSocket = serverSocket.accept();
                Peer.servingThreads.execute(new MessageHandler(selfPeerId, 0, peerSocket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

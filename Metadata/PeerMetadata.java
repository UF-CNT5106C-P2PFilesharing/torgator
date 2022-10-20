package Metadata;

import Messages.BitField;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PeerMetadata {
    private String id;
    private String hostAddress;
    private String port;
    private int hasFile;
    private int index;
    private int peerState = -1;
    private int previousPeerState = -1;
    private int isPreferredNeighbor = 0;
    private BitField bitFieldMessage;
    private int isOptimisticallyUnChokedNeighbor;
    private int isInterested;
    private int isHandShaked;
    private int isChoked;
    private int hasCompleteFile;
    private Date startTime;
    private Date endTime;
    private double dataRate;

    public PeerMetadata(String id, String hostAddress, String port, int hasFile, int index) {
        this.id = id;
        this.hostAddress = hostAddress;
        this.port = port;
        this.hasFile = hasFile;
        this.index = index;
        this.dataRate = 0;
        this.isOptimisticallyUnChokedNeighbor = 0;
    }

    public void updatePeerMetadata(String peerId, int hasFile) throws IOException {
        Path path = Paths.get("Configurations","PeerInfo.cfg");
        Stream<String> lines = Files.lines(path);

        List<String> newLines = lines.map(line ->
                {
                    String newLine = line;
                    String[] tokens = line.trim().split("\\s+");
                    if (tokens[0].equals(peerId)) {
                        newLine = tokens[0] + " " + tokens[1] + " " + tokens[2] + " " + hasFile;
                    }
                    return newLine;
                }
        ).collect(Collectors.toList());
        Files.write(path, newLines);
        lines.close();
    }

    public int getIsInterested() {
        return isInterested;
    }

    public void setIsInterested(int isInterested) {
        this.isInterested = isInterested;
    }

    public int getHasCompleteFile() {
        return hasCompleteFile;
    }

    public void setHasCompleteFile(int hasCompleteFile) {
        this.hasCompleteFile = hasCompleteFile;
    }

    public int getIsChoked() {
        return isChoked;
    }

    public void setIsChoked(int isChoked) {
        this.isChoked = isChoked;
    }
}

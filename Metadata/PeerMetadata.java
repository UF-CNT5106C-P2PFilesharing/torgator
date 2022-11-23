package Metadata;

import Messages.BitField;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static Configurations.SystemConfiguration.peerInfoFile;

public class PeerMetadata {
    private final String id;
    private final String hostAddress;
    private final String port;
    private final int index;
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
    private static final File peerInfoConfigFile = new File(peerInfoFile);

    public PeerMetadata(String id, String hostAddress, String port, int hasCompleteFile, int index) {
        this.id = id;
        this.hostAddress = hostAddress;
        this.port = port;
        this.hasCompleteFile = hasCompleteFile;
        this.index = index;
        this.dataRate = 0;
        this.isOptimisticallyUnChokedNeighbor = 0;
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

    public String getId() {
        return id;
    }

    public String getPort() {
        return port;
    }

    public int getIndex() {
        return index;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setIsPreferredNeighbor(int isPreferredNeighbor) {
        this.isPreferredNeighbor = isPreferredNeighbor;
    }

    public void setPeerState(int peerState) {
        this.peerState = peerState;
    }

    public int compareTo(PeerMetadata other) {
        return Double.compare(this.dataRate, other.dataRate);
    }

    public void setIsOptimisticallyUnChockedNeighbor(int isOptimisticallyUnChokedNeighbor) {
        this.isOptimisticallyUnChokedNeighbor = isOptimisticallyUnChokedNeighbor;
    }

    public int getPeerState() {
        return peerState;
    }

    public void setPreviousPeerState(int previousPeerState) {
        this.previousPeerState = previousPeerState;
    }

    public void setIsHandShakeComplete(int isHandShaked) {
        this.isHandShaked = isHandShaked;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setDataRate(double dataRate) {
        this.dataRate = dataRate;
    }

    public int getPreviousPeerState() {
        return previousPeerState;
    }

    public BitField getBitFieldMessage() {
        return bitFieldMessage;
    }

    public void setBitFieldMessage(BitField bitField) {
        this.bitFieldMessage = bitField;
    }

    /**
     * This method is used to update peerID entry with hasFile parameter in PeerInfo.cfg file
     * @param peerId - peerID to updated
     * @param hasFile - value by which peerID should be updated
     * @throws IOException
     */
//    public void updatePeerMetadata(String peerId, int hasFile) throws IOException {
//        Path path = Paths.get(peerInfoFile);
//        Stream<String> lines = Files.lines(path);
//
//        List<String> newLines = lines.map(line ->
//                {
//                    String newLine = line;
//                    String[] tokens = line.trim().split("\\s+");
//                    if (tokens[0].equals(peerId)) {
//                        newLine = tokens[0] + " " + tokens[1] + " " + tokens[2] + " " + hasFile;
//                    }
//                    return newLine;
//                }
//        ).collect(Collectors.toList());
//        Files.write(path, newLines);
//        lines.close();
//    }

    public void updatePeerMetadata(String peerId, int hasFile) throws IOException {
        Path path = Paths.get(peerInfoFile);
        List<String> newLines;
        try(Stream<String> lines = Files.lines(path)) {

             newLines = lines.map(line ->
                    {
                        String newLine = line;
                        String[] tokens = line.trim().split("\\s+");
                        if (tokens[0].equals(peerId)) {
                            newLine = tokens[0] + " " + tokens[1] + " " + tokens[2] + " " + hasFile;
                        }
                        return newLine;
                    }
            ).collect(Collectors.toList());
        }
        synchronized (peerInfoConfigFile) {
            try (FileOutputStream fos = new FileOutputStream(peerInfoConfigFile, false)) {
                FileLock lock = fos.getChannel().lock();
                PrintWriter pw = new PrintWriter(fos);
                for(String line: newLines) {
                    pw.println(line);
                }
                pw.close();
                lock.release();
            }
        }
    }
}

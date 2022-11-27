package Process;

import Configurations.SystemConfiguration;
import Handlers.FileServerHandler;
import Handlers.MessageHandler;
import Handlers.MessageProcessingHandler;
import Logging.Helper;
import Messages.BitField;
import Metadata.PeerMetadata;
import Tasks.OptimisticallyUnChokedNeighbors;
import Tasks.PreferredNeighbors;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.*;

import static Configurations.SystemConfiguration.peerInfoFile;
import static Configurations.SystemConfiguration.systemConfigurationFile;
import static Logging.Helper.logMessage;

public class peerProcess {
    public static ExecutorService fileServerThread = Executors.newSingleThreadExecutor();
    public static ServerSocket fileServingSocket = null;
    public static String peerID;
    public static String peerFolder;
    public static int peerIndex;
    public static boolean isFirstPeer = false;
    public static int peerPort;
    public static int peerHasFile;
    public static BitField bitFieldMessage = null;
    private static final File peerInfoConfigFile = new File(peerInfoFile);
    public static ExecutorService messageProcessor = Executors.newSingleThreadExecutor();
    public static ExecutorService receivingThreads = Executors.newCachedThreadPool();
    public static ExecutorService servingThreads = Executors.newCachedThreadPool();
    public static volatile Timer preferredNeighborsTimer;
    public static volatile Timer optimisticallyUnChokedNeighborTimer;
    public static volatile ConcurrentHashMap<String, PeerMetadata> remotePeerDetails = new ConcurrentHashMap<>();
    public static volatile ConcurrentHashMap<String, PeerMetadata> preferredNeighbours = new ConcurrentHashMap<>();
    public static volatile ConcurrentHashMap<String, Socket> peerToSocketMap = new ConcurrentHashMap<>();
    public static volatile ConcurrentHashMap<String, PeerMetadata> optimisticUnChokedNeighbors = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        peerID = args[0];
        peerFolder = "peer_" + peerID;

        try {
            Helper logHelper = new Helper();
            logHelper.initializeLogger(peerID);
            logMessage(peerID + " started");
            System.out.println("Reading configurations");
            readInConfiguration();
            // check if current peer is the first peer(i.e, if it initially has the file)
            setCurrentPeerDetails();
            // initializing current peer bitfield information
            initializeBitFieldMessage();
            // starting the message processing thread
            startMessageProcessingThread();
            // starting the file exchanging threads
            startFileExchangeThreads();
            // update preferred neighbors list
            determinePreferredNeighbors();
            // update optimistically unChoked neighbor list
            determineOptimisticallyUnchockedNeighbours();
            // if all the peers have completed downloading the file i.e, all entries in PeerInfo.cfg update to 1 terminate current peer
            terminatePeerAndCleanUp();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            logMessage(peerID + " is exiting...");
            System.exit(0);
        }
    }

    public static void readInConfiguration() throws Exception {

        // read from Common.cfg
        initializeSystemConfiguration();
        // read from PeerInfo.cfg
        addOtherPeerMetadata();
        // initialize preferred neighbours
        setPreferredNeighbours();

    }

    /**
     * This method is to used to set current peer details
     */
    public static void setCurrentPeerDetails() {
        PeerMetadata peerMetadata = remotePeerDetails.get(peerID);
        peerPort = Integer.parseInt(peerMetadata.getPort());
        peerIndex = peerMetadata.getIndex();
        isFirstPeer = peerMetadata.getHasCompleteFile() == 1;
        peerHasFile = peerMetadata.getHasCompleteFile();
    }

    /**
     * This method is used to initialize bitfield for the current peer
     */
    public static void initializeBitFieldMessage() {
        bitFieldMessage = new BitField();
        bitFieldMessage.setPieceDetails(peerHasFile);
    }

    /**
     * This method is used to start message processing thread
     */
    public static void startMessageProcessingThread() {
        messageProcessor.execute(new MessageProcessingHandler(peerID));
    }

    /**
     * This method is used to start file server and file receiver threads
     */
    public static void startFileExchangeThreads() throws IOException {
        startFileServingThread();
        if (!isFirstPeer) {
            //if file not present create a new one and start serving and listening.
            createNewFile();
            startFileReceivingThreads();
        }
    }

    /**
     * This method is used to create empty file with size 'SystemConfiguration.fileSize' and set zero bits into it
     */
    public static void createNewFile() {
            File dir = new File(peerFolder);
            if(dir.mkdir()) {
                File emptyFile = new File(peerFolder, SystemConfiguration.fileName);
                try (OutputStream os = new FileOutputStream(emptyFile, true)) {
                    byte b = 0;

                    for (int i = 0; i < SystemConfiguration.fileSize; i++)
                        os.write(b);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }

    /**
     * This method is used to start file receiver threads
     */
    public static void startFileReceivingThreads() throws IOException {
       Set<String> remotePeerMetaDataKeys = remotePeerDetails.keySet();
        for (String remotePeerID : remotePeerMetaDataKeys) {
            PeerMetadata peerMetadata = remotePeerDetails.get(remotePeerID);

            if (peerIndex > peerMetadata.getIndex()) {
                receivingThreads.execute(new MessageHandler(peerID, 1, peerMetadata.getHostAddress(), Integer.parseInt(peerMetadata.getPort())));
            }
        }
    }

    /**
     * This method is used to start file server thread
     */
    public static void startFileServingThread() {
        try {
            //Start a new file serving socket inside a thread
            fileServingSocket = new ServerSocket(peerPort);
            fileServerThread.execute(new FileServerHandler(fileServingSocket, peerID));
        } catch (SocketTimeoutException e) {
            logMessage(peerID + " Socket Timed out Error - " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * This method creates a timer task to determine preferred neighbors
     */
    public static void determinePreferredNeighbors() {
        preferredNeighborsTimer = new Timer();
        preferredNeighborsTimer.schedule(new PreferredNeighbors(), 0, SystemConfiguration.unChokingInterval * 1000);
    }

    /**
     * This method creates a timer task to determine optimistically unchoked neighbors
     */
    public static void determineOptimisticallyUnchockedNeighbours() {
        optimisticallyUnChokedNeighborTimer = new Timer();
        optimisticallyUnChokedNeighborTimer.schedule(new OptimisticallyUnChokedNeighbors(), 0, SystemConfiguration.optimisticUnChokingInterval * 100);
    }

    /**
     * This method is used to terminate the peerProcess process if all the peers have downloaded the files.
     * It terminates all the threads related to the peerProcess.
     */
    private static void terminatePeerAndCleanUp() {
        while (true) {
            if (isDownloadComplete()) {
                logMessage("All peers have completed downloading the file.");
                preferredNeighborsTimer.cancel();
                optimisticallyUnChokedNeighborTimer.cancel();
                messageProcessor.shutdown();
                receivingThreads.shutdown();
                servingThreads.shutdown();
                fileServerThread.shutdown();
                return;
            }
        }
    }

    /**
     * This method is used to check if all the peers have downloaded the file
     * @return boolean
     */
    public static synchronized boolean isDownloadComplete() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(peerInfoFile));
            if (lines.size() == 0) return false;
            for (String line : lines) {
                String[] properties = line.split("\\s+");
                if (Integer.parseInt(properties[3]) == 0) return false;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void initializeSystemConfiguration() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(systemConfigurationFile));
        for (String line : lines) {
            String[] properties = line.split("\\s+");
            switch(properties[0].toLowerCase()) {
                case "numberofpreferredneighbors":
                    SystemConfiguration.numberOfPreferredNeighbours = Integer.parseInt(properties[1]);
                    break;
                case "unchokinginterval":
                    SystemConfiguration.unChokingInterval = Integer.parseInt(properties[1]);
                    break;
                case "optimisticunchokinginterval":
                    SystemConfiguration.optimisticUnChokingInterval = Integer.parseInt(properties[1]);
                    break;
                case "filesize":
                    SystemConfiguration.fileSize = Integer.parseInt(properties[1]);
                    break;
                case "filename":
                    SystemConfiguration.fileName = properties[1];
                    break;
                case "piecesize":
                    SystemConfiguration.pieceSize = Integer.parseInt(properties[1]);
                    break;
            }
        }
        System.out.println("File name: " + SystemConfiguration.fileName);
        System.out.println("File size: " + SystemConfiguration.fileSize);
        System.out.println("Piece Size: " + SystemConfiguration.pieceSize);
        System.out.println("Preferred Neighbors count: " + SystemConfiguration.numberOfPreferredNeighbours);
        System.out.println("Optimistic un-choking interval: " + SystemConfiguration.optimisticUnChokingInterval);
        System.out.println("Un-choking interval: " + SystemConfiguration.unChokingInterval);
    }

    public static void addOtherPeerMetadata() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(peerInfoFile));
        int index = 0;
        for (String line: lines) {
            String[] properties = line.split("\\s+");
            remotePeerDetails.put(properties[0], new PeerMetadata(properties[0], properties[1], properties[2], Integer.parseInt(properties[3]), index));
            index++;
        }
    }

    public synchronized static void updateOtherPeerMetaData() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(peerInfoFile));
            for (String line : lines) {
                String[] properties = line.split("\\s+");
                String peerId = properties[0];
                int isCompleteFile = Integer.parseInt(properties[3]);
                PeerMetadata remotePeer = remotePeerDetails.get(peerId);
                if (isCompleteFile == 1) {
                    remotePeer.setIsInterested(0);
                    remotePeer.setHasCompleteFile(1);
                    remotePeer.setIsChoked(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to set preferred neighbors of a peer
     */
    public static void setPreferredNeighbours() {
        Set<String> remotePeerIDs = remotePeerDetails.keySet();
        for (String otherPeerId : remotePeerIDs) {
            PeerMetadata peerMetadata = remotePeerDetails.get(peerID);
            if (peerMetadata != null && !peerID.equals(otherPeerId)) {
                preferredNeighbours.put(otherPeerId, peerMetadata);
            }
        }
    }
}

package Process;

import Configurations.SystemConfiguration;
import Handlers.MessageHandler;
import Handlers.MessageProcessingHandler;
import Handlers.ServerHandler;
import Logging.Helper;
import Messages.BitField;
import Metadata.PeerMetadata;
import Tasks.OptimisticallyUnchokedNeighbors;
import Tasks.PreferredNeighbors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import static Logging.Helper.logMessage;

public class Peer {
    public Thread fileServerThread;
    public ServerSocket serverSocket = null;
    public static String peerID;
    public static int peerIndex;
    public static boolean isFirstPeer = false;
    public static int peerPort;
    public static int peerHasFile;
    public static BitField bitFieldMessage = null;
    public static Thread messageProcessor;
    public static boolean isDownloadComplete = false;
    public static Vector<Thread> peerThreads = new Vector<>();
    public static Vector<Thread> servingThreads = new Vector<>();
    public static volatile Timer preferredNeighborsTimer;
    public static volatile Timer optimisticallyUnChokedNeighborTimer;
    public static volatile ConcurrentHashMap<String, PeerMetadata> remotePeerDetails = new ConcurrentHashMap<String, PeerMetadata>();
    public static volatile ConcurrentHashMap<String, PeerMetadata> preferredNeighboursMap = new ConcurrentHashMap<String, PeerMetadata>();
    public static volatile ConcurrentHashMap<String, Socket> peerToSocketMap = new ConcurrentHashMap<>();
    public static volatile ConcurrentHashMap<String, PeerMetadata> optimisticUnChokedNeighbors = new ConcurrentHashMap<String, PeerMetadata>();

    public static void main(String[] args) {
        Peer process = new Peer();
        peerID = args[0];

        try {
            Helper logHelper = new Helper();
            logHelper.initializeLogger(peerID);
            logMessage("Peer " + peerID + " started");
            System.out.println("Reading configurations");
            readInConfiguration();
            // TODO
            // check if current peer is the first peer(i.e, if it initially has the file)
            setCurrentPeerDetails();
            // initializing current peer bitfield information
            initializeBitFieldMessage();
            // starting the message processing thread
            startMessageProcessingThread(process);
            // starting the file exchanging threads
            startFileExchangeThreads(process);
            // update preferred neighbors list
            determinePreferredNeighbors();
            // update optimistically unchoked neighbor list
            determineOptimisticallyUnchockedNeighbours();
            // if all the peers have completed downloading the file i.e, all entries in PeerInfo.cfg update to 1 terminate current peer
            terminatePeerAndCleanUp(process);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            logMessage("Peer " + peerID + " is exiting...");
            System.exit(0);
        }
    }

    public Thread getFileServerThread() {
        return fileServerThread;
    }

    public static void readInConfiguration() throws Exception {

        //read from Common.cfg
        initializeSystemConfiguration();
        //read from Peerinfo.cfg
        addOtherPeerMetadata();
        //initialize preferred neighbours
//        setPreferredNeighbours();

    }

    /**
     * This process to used to set current peer details
     */
    public static void setCurrentPeerDetails() {
        Set<String> remotePeerIDs = remotePeerDetails.keySet();
        for (String peerID : remotePeerIDs) {
            PeerMetadata remotePeerMetadata = remotePeerDetails.get(peerID);
            if (remotePeerMetadata.getId().equals(peerID)) {
                peerPort = Integer.parseInt(remotePeerMetadata.getPort());
                peerIndex = remotePeerMetadata.getIndex();
                if (remotePeerMetadata.getHasFile() == 1) {
                    isFirstPeer = true;
                    peerHasFile = remotePeerMetadata.getHasFile();
                    break;
                }
            }
        }
    }

    /**
     * This method is used to initialize bitfield for the current peer
     */
    public static void initializeBitFieldMessage() {
        bitFieldMessage = new BitField();
        bitFieldMessage.setPieceDetails(peerID, peerHasFile);
    }

    /**
     * This method is used to start message processing thread
     * @param process - the peer process to start the thread into
     */
    public static void startMessageProcessingThread(Peer process) {
        messageProcessor = new Thread(new MessageProcessingHandler(peerID));
        messageProcessor.start();
    }

    /**
     * This method is used to start file server and file receiver threads
     * @param process - The process to start threads into
     */
    public static void startFileExchangeThreads(Peer process) throws IOException {
        if (isFirstPeer) {
            //Peer that has the file initially
            startFileServingThread(process);
        } else {
            //if file not present create a new one and start serving and listening.
            createNewFile();
            startFileReceivingThreads(process);
            startFileServingThread(process);
        }
    }

    /**
     * This method is used to create empty file with size 'SystemConfiguration.fileSize' and set zero bits into it
     */
    public static void createNewFile() {
        try {
            File dir = new File(peerID);
            dir.mkdir();

            File emptyFile = new File(peerID, SystemConfiguration.fileName);
            try (OutputStream os = new FileOutputStream(emptyFile, true)) {
                byte b = 0;

                for (int i = 0; i < SystemConfiguration.fileSize; i++)
                    os.write(b);
            }
        } catch (Exception e) {
            logMessage(peerID + " ERROR creating file : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This method is used to start file receiver threads
     * @param process - The process to start threads into
     */
    public static void startFileReceivingThreads(Peer process) throws IOException {
        Set<String> remotePeerMetaDataKeys = remotePeerDetails.keySet();
        for (String remotePeerID : remotePeerMetaDataKeys) {
            PeerMetadata peerMetadata = remotePeerDetails.get(remotePeerID);

            if (process.peerIndex > peerMetadata.getIndex()) {
                Thread tempThread = new Thread(new MessageHandler(peerID, 1, peerMetadata.getHostAddress(), Integer.parseInt(peerMetadata.getPort())));
                peerThreads.add(tempThread);
                tempThread.start();
            }
        }
    }

    /**
     * This method is used to start file server thread
     * @param process - peerprrocess to start thread into
     */
    public static void startFileServingThread(Peer process) {
        try {
            //Start a new file server thread
            process.serverSocket = new ServerSocket(peerPort);
            process.fileServerThread = new Thread(new ServerHandler(process.serverSocket, peerID));
            process.fileServerThread.start();
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
        optimisticallyUnChokedNeighborTimer.schedule(new OptimisticallyUnchokedNeighbors(), 0, SystemConfiguration.optimisticUnChokingInterval * 100);
    }

    /**
     * This method is used to terminate the Peer process if all the peers have downloaded the files.
     * It terminates all the threads related to the Peer.
     * @param process - The process to terminate
     */
    private static void terminatePeerAndCleanUp(Peer process) {
        while (true) {
            if (isDownloadComplete()) {
                logMessage("All peers have completed downloading the file.");
                preferredNeighborsTimer.cancel();
                optimisticallyUnChokedNeighborTimer.cancel();

                try {
                    Thread.currentThread();
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (process.getFileServerThread().isAlive()) {
                    process.getFileServerThread().stop();
                }

                if (messageProcessor.isAlive()) {
                    messageProcessor.stop();
                }

                for (Thread thread : peerThreads) {
                    if (thread.isAlive()) {
                        thread.stop();
                    }
                }

                for (Thread thread : servingThreads) {
                    if (thread.isAlive()) {
                        thread.stop();
                    }
                }

                break;

            } else {
                try {
                    Thread.currentThread();
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * This method is used to check if all the peers have downloaded the file
     * @return boolean
     */
    public static synchronized boolean isDownloadComplete() {
        boolean isDownloadComplete = true;
        try {
            List<String> lines = Files.readAllLines(Paths.get("/Users/anmol/IdeaProjects/torgator/Configurations/PeerInfo.cfg"));
            for (String line : lines) {
                String[] properties = line.split("\\s+");
                if (Integer.parseInt(properties[3]) == 0) {
                    isDownloadComplete = false;
                    break;
                }
            }
        } catch (IOException e) {
            isDownloadComplete = false;
        }

        return isDownloadComplete;
    }

    public static void initializeSystemConfiguration() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("/Users/anmol/IdeaProjects/torgator/Configurations/Common.cfg"));
        for (String line : lines) {
            String[] properties = line.split("\\s+");
            switch(properties[0]) {
                case "NumberOfPreferredNeighbors":
                    SystemConfiguration.numberOfPreferredNeighbours = Integer.parseInt(properties[1]);
                    System.out.println("entered: " + SystemConfiguration.numberOfPreferredNeighbours);
                    break;
                case "UnchokingInterval":
                    SystemConfiguration.unChokingInterval = Integer.parseInt(properties[1]);
                    break;
                case "OptimisticUnchokingInterval":
                    SystemConfiguration.optimisticUnChokingInterval = Integer.parseInt(properties[1]);
                    break;
                case "FileSize":
                    SystemConfiguration.fileSize = Integer.parseInt(properties[1]);
                    break;
                case "FileName":
                    SystemConfiguration.fileName = properties[1];
                    break;
                case "PieceSize":
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
        List<String> lines = Files.readAllLines(Paths.get("/Users/anmol/IdeaProjects/torgator/Configurations/PeerInfo.cfg"));
        for (int i = 0; i < lines.size(); i++) {
            String[] properties = lines.get(i).split("\\s+");
            remotePeerDetails.put(properties[0], new PeerMetadata(properties[0], properties[1], properties[2], Integer.parseInt(properties[3]), i));
        }
    }
}

package Process;

import Configurations.SystemConfiguration;
import Logging.Helper;
import Messages.BitField;
import Metadata.PeerMetadata;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

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
    public static Vector<Thread> serverThreads = new Vector<>();
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
            Helper.logMessage("Peer " + peerID + " started");
            System.out.println("Reading configurations");
            readInConfiguration();
            // TODO
            /*
            check if current peer is first peer(i.e, it initially has file)
            setCurrentPeerDetails();
            initializing current peer bitfield information
            initializeBitFieldMessage();
            starting the message processing thread
            startMessageProcessingThread(process);
            starting the file server thread and file threads
            startFileServerReceiverThreads(process);
            update preferred neighbors list
            determinePreferredNeighbors();
            update optimistically unchoked neighbor list
            determineOptimisticallyUnchockedNeighbours();
            if all the peers have completed downloading the file i.e, all entries in peerinfo.cfg update to 1 terminate current peer
            terminatePeer(process);process
            */

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Helper.logMessage("Peer " + peerID + " is exiting...");
            System.exit(0);
        }
    }

    public static void readInConfiguration() throws Exception {

        //read from Common.cfg
        initializeSystemConfiguration();
        //read from Peerinfo.cfg
        addOtherPeerMetadata();
        //initialize preferred neighbours
//        setPreferredNeighbours();

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

   public Thread getFileServerThread() {
        return fileServerThread;
   }

   public void setFileServerThread(Thread fileServerThread) {
        this.fileServerThread = fileServerThread;
   }
}

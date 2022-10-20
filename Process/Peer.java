package Process;

import Logging.Helper;
import Messages.BitField;
import Metadata.PeerMetadata;

import java.net.ServerSocket;
import java.net.Socket;
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

//            TODO
//            initialize peer, its neighbors, its preferred neighbors configuration configuration
//            initializeConfiguration();
//            check if current peer is first peer(i.e, it initially has file)
//            setCurrentPeerDetails();
//            initializing current peer bitfield information
//            initializeBitFieldMessage();
//            starting the message processing thread
//            startMessageProcessingThread(process);
//            starting the file server thread and file threads
//            startFileServerReceiverThreads(process);
//            update preferred neighbors list
//            determinePreferredNeighbors();
//            update optimistically unchoked neighbor list
//            determineOptimisticallyUnchockedNeighbours();
//            if all the peers have completed downloading the file i.e, all entries in peerinfo.cfg update to 1 terminate current peer
//            terminatePeer(process);

        } catch (Exception ignored) {
        } finally {
            Helper.logMessage("Peer " + peerID + " is exiting...");
            System.exit(0);
        }
    }
}

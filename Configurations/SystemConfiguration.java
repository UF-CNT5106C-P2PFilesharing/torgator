package Configurations;

public class SystemConfiguration {
    public static int numberOfPreferredNeighbours; //preferred number of neighbors for a peer
    public static int unChokingInterval; //The interval for determining preferred neighbors
    public static int optimisticUnChokingInterval; //The interval for determining optimistically unChoked neighbors
    public static String fileName; //Name of the file being transferred
    public static int fileSize; //The size of the file being transferred
    public static int pieceSize; //The size of each piece the file needs to be divided into
}

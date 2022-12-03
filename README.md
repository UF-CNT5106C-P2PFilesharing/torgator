# torgator
A peer to peer file sharing software focusing on choking and unchoking mechanism between peers.

## Usage:
Extract the zip file **torgator.zip**

## Files
1. Process/peerProcess.java - This class is used to implement the P2P process to transfer file from peer to peer.
2. Process/StartRemotePeers.java - This class is used as a driver process for launching peers on different hosts.
3. Configurations/SystemConfiguration.java - This class contains configuration to be set for a peer.
4. Configurations/Common.cfg - This class contains configuration to be set for a peer.
5. Configurations/PeerInfo.cfg - This class contains the peer details.
6. Handlers/MessageHandler.java - This class is used to handle to appropriate messages such as HANDSHAKE, BITFIELD, PIECE, REQUEST, INTERESTED, etc.
7. Handlers/MessageProcessingHandler.java - This class is used to manage the peer states such as choked, unchoked, etc.
8. Handlers/FileServerHandler.java - This class is used to run the message handler thread.
9. Logging/Helper.java - This class is used to generate log files to write messages into.
10. Logging/LogFormatter.java - This class handles the formatting of messages in log files.
11. Messages/BitField.java - This class serves the bitfiled functionality.
12. Messages/Constants.java - This helper class contains message specific constants.
13. Messages/FilePiece.java - This class serves the FilePiece functionality i.e chunks of the original file.
14. Messages/HandShakeMsg.java - This class aims to handle handshake message information for successful TCP connections.
15. Messages/Msg.java - This class represents the general message body used across the system.
16. Metadata/MessageMetadata.java - This class is used to store the message information or metadata.
17. Metadata/PeerMetadata.java - This class is used to store remote peer details information
18. Queue/MessageQueue.java - This class creates message queue which is used to process messages received from socket
19. Tasks/OptimisticallyUnChokedNeighbors.java - Class which facilitates the peer to periodically select an optimistically-unchoked neighbor.
20. Tasks/PreferredNeighbors.java - Class which facilitates the peer to periodically select k preferred neighbors.
21. jsch.jar - jar that provides functionality that allows you to connect to an sshd server and use port forwarding.
22. Makefile - to compile and run the project.

## How to compile
Run make command to compile the code

Steps:
1. make clean
2. make StartRemotePeers.class
3. make StartRemotePeers

## Log files
Log files are available as log_peer_<peer_id>.log, once the program starts.

## Project Details
Project Members: 
> Anmol Lingan Gouda Patil (UFID: 1967 - 3150) \
> Prathika Gonchigar (UFID: 5820 - 7815) \
> Sandesh Ramesh (UFID: 3791 - 2162)

## System requirements:
`java version "1.8.0_271"`
  
`Java(TM) SE Runtime Environment (build 1.8.0_271-b09)`
  
`Java HotSpot(TM) 64-Bit Server VM (build 25.271-b09, mixed mode)`

## System functionality description:
The project implements a TCP protocol to establish connection between peers. These connections are basically used to share files
amongst themselves. The first peer to start has the complete file, hence it acts as a server to other peers who request the file
to be shared. To achieve this each peer establish a TCP connection to the file serving peer's streaming socket. This mechanism
includes sending a Handshake message to acknowledge the connection from both parties. Later a series of messages such as bitfield,
filepiece, request, interested, not interested and piece are shared based on the requirement of the requesting peer.

PeerInfo.cfg provides the order in which the peers have to be started. The main class peerProcess owns the responsibility to 
start every peer with a set of configurations provided in the Common.cfg. The peers continue to progress through the file sharing
process by following the protocol rules. Choking and unchoking mechanisms are used to penalize/reward the peers so that there is
a fair share of resources among the peers in the overlay network, Choking and unchoking avoid free riding and helps maintain
balanced network. Choking and unchoking is achieved by periodically selecting the *k* (configuration parameter) preferred neighbors
by a peer. Optimistically unchoked neighbor mechanism ensures that newer peers entering the system are not left out or unecessarily
penalized. This is achieved by periodically choosing one random peer from the network. As observed these mechanisms are equally
important for the system to progress.

Finally, the peers that have completely downloaded the file, will now start serving other peers in the network. Also these
peers will broadcast the download complete message to all other peers. Then the entry corresponding to the complete file
availability in PeerInfo.cfg for this particular peer is updated to 1. When all the peers have downloaded the complete file, every
peer will have broadcast the download complete message to every other peer there by reaching a consensus to terminate successfully.

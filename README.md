# torgator
A peer to peer file sharing software.

## Usage:
Extract the zip file **torgator.zip**

## Version of java used:
`java version "1.8.0_271"`
  
`Java(TM) SE Runtime Environment (build 1.8.0_271-b09)`
  
`Java HotSpot(TM) 64-Bit Server VM (build 25.271-b09, mixed mode)`

## Files
1. Process/Peer.java - This class is used to implement the P2P process to transfer file from peer to peer.
2. HandShakeMsg.java - This class is used to handle handshake message information
3. Metadata/PeerMetadata.java - This class is used to store remote peer details information
4. Handlers/MessageHandler.java - This class is used to write/read messages from socket
5. Queue/MessageQueue.java - This class creates message queue which is used to process messages received from socket
6. Metadata/MessageMetadata.java - This class is used to handle message and its metadata
7. Messages/Constants.java - This class is used to store various message constants.
8. Messages/Msg.java - Class which handles messages except handshake, bitfield and piece messages
9. Logging/Helper.java - This class is used to generate log files to write messages into
10. Logging/LogFormatter.java - This class handles the formatting of messages in log files
11. FilePiece.java - This class is used to handle file piece information
12. SystemConfiguration.java - This class contains configuration to be set for a peer
13. Messages/BitField.java - This class is used to store bitfield message of peers

## How to compile
Run make command to compile the code

Steps:
1. make clean
2. make
3. make Peer

## Project Details
Project Members: 
> Anmol Lingan Gouda Patil (UFID: 1967 - 3150)
> Prathika Gonchigar (UFID: 5820 - 7815)
> Sandesh Ramesh (UFID: 3791 - 2162)

JCC = javac
JAVA = java
JFLAGS = -g
REMOTE_PEER_OPTS = -cp .:jsch.jar

default: peerProcess.class

peerProcess.class: Process/peerProcess.java
	$(JCC) $(JFLAGS) Process/peerProcess.java

SystemConfiguration.class: Configurations/SystemConfiguration.java
	$(JCC) $(JFLAGS) SystemConfiguration.java

MessageHandler.class: Handlers/MessageHandler.java
	$(JCC) $(JFLAGS) Handlers/MessageHandler.java

FileServerHandler: Handlers/FileServerHandler.java
	$(JCC) $(JFLAGS) Handlers/FileServerHandler.java

MessageProcessingHandler: Handlers/MessageProcessingHandler.java
	$(JCC) $(JFLAGS) Handlers/MessageProcessingHandler.java

Helper.class: Logging/Helper.java
	$(JCC) $(JFLAGS) Logging/Helper.java

LogFormatter.class: Logging/LogFormatter.java
	$(JCC) $(JFLAGS) Logging/LogFormatter.java

BitField.class: Messages/BitField.java
	$(JCC) $(JFLAGS) Messages/BitField.java

Constants.class: Messages/Constants.java
	$(JCC) $(JFLAGS) Messages/Constants.java

FilePiece.class: Messages/FilePiece.java
	$(JCC) $(JFLAGS) FilePiece.java

HandShakeMsg.class: Messages/HandShakeMsg.java
	$(JCC) $(JFLAGS) HandShakeMsg.java

Msg.class: Messages/Msg.java
	$(JCC) $(JFLAGS) Messages/Msg.java

MessageMetadata.class: Metadata/MessageMetadata.java
	$(JCC) $(JFLAGS) Metadata/MessageMetadata.java

PeerMetadata.class: Metadata/PeerMetadata.java
	$(JCC) $(JFLAGS) Metadata/PeerMetadata.java

MessageQueue.class: Queue/MessageQueue.java
	$(JCC) $(JFLAGS) Queue/MessageQueue.java

OptimisticallyUnChokedNeighbors.class: Tasks/OptimisticallyUnChokedNeighbors.java
	$(JCC) $(JFLAGS) Tasks/OptimisticallyUnChokedNeighbors.java

PreferredNeighbors.class: Tasks/PreferredNeighbors.java
	$(JCC) $(JFLAGS) Tasks/PreferredNeighbors.java

peerProcess: Process/peerProcess.class
	$(JAVA) Process/peerProcess 1001

StartRemotePeers.class: Process/StartRemotePeers.java
	$(JCC) $(JFLAGS) $(REMOTE_PEER_OPTS) Process/StartRemotePeers.java

StartRemotePeers: Process/StartRemotePeers.class
	$(JAVA) $(REMOTE_PEER_OPTS) Process/StartRemotePeers

clean:
	$(RM) *.class Configurations/*.class Messages/*.class Logging/*.class Metadata/*.class Queue/*.class Handlers/*.class Process/*.class Tasks/*.class log_*
	$(RM) -r peer_100[2-9]*
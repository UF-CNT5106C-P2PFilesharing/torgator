JCC = javac
JAVA = java
JFLAGS = -g

default: Peer.class

Peer.class: Process/Peer.java
	$(JCC) $(JFLAGS) Process/Peer.java

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

Peer: Process/Peer.class
	$(JAVA) Process/Peer 1001

MessageQueue.class: Queue/MessageQueue.java
	$(JCC) $(JFLAGS) Queue/MessageQueue.java

OptimisticallyUnChokedNeighbors.class: Tasks/OptimisticallyUnChokedNeighbors.java
	$(JCC) $(JFLAGS) Tasks/OptimisticallyUnChokedNeighbors.java

PreferredNeighbors.class: Tasks/PreferredNeighbors.java
	$(JCC) $(JFLAGS) Tasks/PreferredNeighbors.java

clean:
	$(RM) *.class Configurations/*.class Messages/*.class Logging/*.class Metadata/*.class Queue/*.class Handlers/*.class Process/*.class Tasks/*.class log_*
	$(RM) -r peer_100[2-9]*
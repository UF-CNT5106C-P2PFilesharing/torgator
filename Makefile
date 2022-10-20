JCC = javac
JAVA = java
JFLAGS = -g

default: Peer.class

Peer.class: Process/Peer.java
	$(JCC) $(JFLAGS) Process/Peer.java

HandShakeMsg.class: Messages/HandShakeMsg.java
	$(JCC) $(JFLAGS) HandShakeMsg.java

PeerMetadata.class: Metadata/PeerMetadata.java
	$(JCC) $(JFLAGS) Metadata/PeerMetadata.java

MessageHandler.class: Handlers/MessageHandler.java
	$(JCC) $(JFLAGS) Handlers/MessageHandler.java

MessageQueue.class: Queue/MessageQueue.java
	$(JCC) $(JFLAGS) Queue/MessageQueue.java

MessageMetadata.class: Metadata/MessageMetadata.java
	$(JCC) $(JFLAGS) Metadata/MessageMetadata.java

Constants.class: Messages/Constants.java
	$(JCC) $(JFLAGS) Messages/Constants.java

Msg.class: Messages/Msg.java
	$(JCC) $(JFLAGS) Messages/Msg.java

Helper.class: Logging/Helper.java
	$(JCC) $(JFLAGS) Logging/Helper.java

LogFormatter.class: Logging/LogFormatter.java
	$(JCC) $(JFLAGS) Logging/LogFormatter.java

FilePiece.class: Messages/FilePiece.java
	$(JCC) $(JFLAGS) FilePiece.java

SystemConfiguration.class: Configurations/SystemConfiguration.java
	$(JCC) $(JFLAGS) SystemConfiguration.java

BitField.class: Messages/BitField.java
	$(JCC) $(JFLAGS) Messages/BitField.java

Peer: Process/Peer.class
	$(JAVA) Process/Peer 1001

clean:
	$(RM) *.class Configurations/*.class Messages/*.class Logging/*.class Metadata/*.class Queue/*.class Handlers/*.class Process/*.class
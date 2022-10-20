package Messages;

import Configurations.SystemConfiguration;
import Logging.Helper;
import Process.Peer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class BitField {
    private FilePiece[] filePieces;
    private int numPieces;

    public BitField() {
        int fileSize = SystemConfiguration.fileSize;
        int pieceSize = SystemConfiguration.pieceSize;
        this.numPieces = (int) Math.ceil((double) fileSize / (double) pieceSize);
        this.filePieces = new FilePiece[this.numPieces];
        Arrays.setAll(filePieces, index -> new FilePiece());
    }

    public void setPieceDetails(String peerId, int hasFile) {
        for (FilePiece filePiece : filePieces) {
            filePiece.setIsPieceAvailable(hasFile == 1 ? 1 : 0);
            filePiece.setFromPeer(peerId);
        }
    }

    public byte[] getFilePieceBytesEncoded() {
        int numBytes = (int) Math.ceil((double)numPieces / 8);
        byte[] filePieceByteEncoded = new byte[numBytes];
        int[] bits = new int[numBytes * 8];
        for(int i = 0; i < numPieces; i++) {
               bits[i] = (filePieces[i].getIsPieceAvailable() == 1) ? 1 : 0;
        }
        for(int byte_n = 0; byte_n < numBytes; byte_n++) {
            int num = 0;
            for (int b = 0; b < 8; b++) {
                num = num << 1;
                num += (bits[byte_n * 8 + b] == 1) ? 1 : 0;
            }
            filePieceByteEncoded[byte_n] = (byte) num;
        }
        return filePieceByteEncoded;
    }

    public static BitField decodedFilePieceBytes(byte[] bitField) {
        BitField bitFieldMessage = new BitField();
        for (int byte_n = 0; byte_n < bitField.length; byte_n++) {
            for(int bit_n = 0; bit_n < 8; bit_n++) {
                if(byte_n * 8 + bit_n < bitFieldMessage.getNumPieces())
                    bitFieldMessage.getFilePieces()[byte_n * 8 + bit_n].setIsPieceAvailable(((bitField[byte_n] & (1 << bit_n)) == 0) ? 0 : 1);
                else break;
            }
        }
        return bitFieldMessage;
    }

    public int getNumberOfAvailablePieces() {
        int count = 0;
        for (FilePiece filePiece : filePieces) {
            if (filePiece.getIsPieceAvailable() == 1) {
                count++;
            }
        }
        return count;
    }

    public boolean isFileDownloadComplete() {
        boolean isFileDownloaded = true;
        for (FilePiece filePiece : filePieces) {
            if (filePiece.getIsPieceAvailable() == 0) {
                isFileDownloaded = false;
                break;
            }
        }
        return isFileDownloaded;
    }

    public synchronized int getFirstInterestingPieceIndex(BitField bitFieldMessage) {
        int numberOfPieces = bitFieldMessage.getNumberOfAvailablePieces();
        int interestingPiece = -1;

        for (int i = 0; i < numberOfPieces; i++) {
            if (bitFieldMessage.getFilePieces()[i].getIsPieceAvailable() == 1
                    && this.getFilePieces()[i].getIsPieceAvailable() == 0) {
                interestingPiece = i;
                break;
            }
        }

        return interestingPiece;
    }

    public synchronized int getFirstDifferentPieceIndex(BitField bitFieldMessage) {
        int peerPieces = this.numPieces;
        int remotePeerPieces = bitFieldMessage.getNumPieces();
        int pieceIndex = -1;

        if (remotePeerPieces >= peerPieces) {
            for (int i = 0; i < peerPieces; i++) {
                if (filePieces[i].getIsPieceAvailable() == 0 && bitFieldMessage.getFilePieces()[i].getIsPieceAvailable() == 1) {
                    pieceIndex = i;
                    break;
                }
            }
        } else {
            for (int i = 0; i < remotePeerPieces; i++) {
                if (filePieces[i].getIsPieceAvailable() == 0 && bitFieldMessage.getFilePieces()[i].getIsPieceAvailable() == 1) {
                    pieceIndex = i;
                    break;
                }
            }
        }
        return pieceIndex;
    }

    public void updateBitFieldMetadata(String peerID, FilePiece filePiece) {
        int pieceIndex = filePiece.getPieceIndex();
        try {
           if(!isDuplicatePiece(pieceIndex)){
                String fileName = SystemConfiguration.fileName;

                File file = new File(Peer.peerID, fileName);
                int offSet = pieceIndex * SystemConfiguration.pieceSize;
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                byte[] pieceToWrite = filePiece.getContent();
                randomAccessFile.seek(offSet);
                randomAccessFile.write(pieceToWrite);

                filePieces[pieceIndex].setIsPieceAvailable(1);
                filePieces[pieceIndex].setFromPeer(peerID);
                randomAccessFile.close();
                Helper.logMessage(Peer.peerID + " received the PIECE " + pieceIndex
                        + " from Peer " + peerID + ". Current piece count: "
                        + Peer.bitFieldMessage.getNumberOfAvailablePieces());

                if (Peer.bitFieldMessage.isFileDownloadComplete()) {
                    //update file download details
                    Peer.remotePeerDetails.get(peerID).setIsInterested(0);
                    Peer.remotePeerDetails.get(peerID).setHasCompleteFile(1);
                    Peer.remotePeerDetails.get(peerID).setIsChoked(0);
                    Peer.remotePeerDetails.get(peerID).updatePeerMetadata(Peer.peerID, 1);
                    Helper.logMessage(Peer.peerID + " finished DOWNLOADING the entire file.");
                }
            }
        } catch (IOException e) {
            Helper.logMessage(Peer.peerID + " bitfield update error " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isDuplicatePiece(int pieceIndex) {
        return Peer.bitFieldMessage.getFilePieces()[pieceIndex].getIsPieceAvailable() == 1;
    }

    public FilePiece[] getFilePieces() {
        return filePieces;
    }

    public void setFilePieces(FilePiece[] filePieces) {
        this.filePieces = filePieces;
    }

    public int getNumPieces() {
        return numPieces;
    }

    public void setNumPieces(int numPieces) {
        this.numPieces = numPieces;
    }
}

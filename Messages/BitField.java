package Messages;

import Configurations.SystemConfiguration;
import Logging.Helper;
import Process.peerProcess;

import java.io.File;
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
        Arrays.setAll(this.filePieces, index -> new FilePiece());
    }

    public void setPieceDetails(int hasFile) {
        for (int i = 0; i < filePieces.length; i++) {
            filePieces[i].setIsPieceAvailable(hasFile == 1 ? 1 : 0);
            filePieces[i].setPieceIndex(i);
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
                num |= (bits[byte_n * 8 + b] == 1) ? 1 : 0;
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
                    bitFieldMessage.getFilePieces()[byte_n * 8 + bit_n].setIsPieceAvailable((((0xff & bitField[byte_n]) & (1 << (8 - bit_n - 1))) == 0) ? 0 : 1);
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

    public synchronized int getFirstDifferentPieceIndex(BitField bitFieldMessage) {
        int peerPieces = numPieces;
        int remotePeerPieces = bitFieldMessage.getNumPieces();
        int pieceIndex = -1;
        for (int i = 0; i < Math.min(remotePeerPieces, peerPieces); i++) {
            if (filePieces[i].getIsPieceAvailable() == 0 && bitFieldMessage.getFilePieces()[i].getIsPieceAvailable() == 1) {
                pieceIndex = i;
                break;
            }
        }
        return pieceIndex;
    }

    public void updateBitFieldMetadata(String peerID, FilePiece filePiece) {
        int pieceIndex = filePiece.getPieceIndex();
        try {
           if(!isDuplicatePiece(pieceIndex)){
                String fileName = SystemConfiguration.fileName;

                File file = new File(peerProcess.peerFolder, fileName);
                int offSet = pieceIndex * SystemConfiguration.pieceSize;
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                byte[] pieceToWrite = filePiece.getContent();
                randomAccessFile.seek(offSet);
                randomAccessFile.write(pieceToWrite);

                filePieces[pieceIndex].setIsPieceAvailable(1);
                randomAccessFile.close();
                Helper.logMessage(peerProcess.peerID + " received the PIECE " + pieceIndex
                        + " from Peer " + peerID + ". Current piece count: "
                        + peerProcess.bitFieldMessage.getNumberOfAvailablePieces());

                if (peerProcess.bitFieldMessage.isFileDownloadComplete()) {
                    //update file download details
                    peerProcess.remotePeerDetails.get(peerID).setIsInterested(0);
                    peerProcess.remotePeerDetails.get(peerID).setHasCompleteFile(1);
                    peerProcess.remotePeerDetails.get(peerID).setIsChoked(0);
                    peerProcess.remotePeerDetails.get(peerID).updatePeerMetadata(peerProcess.peerID, 1);
                    Helper.logMessage(peerProcess.peerID + " finished DOWNLOADING the entire file.");
                }
            }
        } catch (IOException e) {
            Helper.logMessage(peerProcess.peerID + " bitfield update error " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isDuplicatePiece(int pieceIndex) {
        return peerProcess.bitFieldMessage.getFilePieces()[pieceIndex].getIsPieceAvailable() == 1;
    }

    public synchronized int getInterestingPieceIndex(BitField bitFieldMessage) {
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


    public FilePiece[] getFilePieces() {
        return filePieces;
    }

    public int getNumPieces() {
        return numPieces;
    }
}

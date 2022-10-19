package Messages;

import Configurations.SystemConfiguration;

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

    /**
     * This method is used to set if pieces are present of that file or not.
     * It accepts peerID which is set for a particular file piece and hasfile whether the file is present or not.
     * Used for initializing a bitfield message
     * @param peerId - ID of the peer from where the piece is found
     * @param hasFile - whether the file is present or not
     */
    public void setPieceDetails(String peerId, int hasFile) {
        for (FilePiece filePiece : filePieces) {
            filePiece.setIsPieceAvailable(hasFile == 1 ? 1 : 0);
            filePiece.setFromPeer(peerId);
        }
    }

    /**
     * This method is used to convert bitfield message to byte array.
     * @return bitfield message converted into byte array
     */
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

    /**
     * This method is used to convert a byte array to bitfield message
     * @param bitField - bitfield message in byte array
     * @return - bitfield message object
     */
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

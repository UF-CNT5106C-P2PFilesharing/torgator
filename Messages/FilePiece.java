package Messages;
import Configurations.SystemConfiguration;
import java.nio.ByteBuffer;

public class FilePiece {
    private int pieceIndex;
    private byte[] content;
    private String fromPeer;
    private int isPieceAvailable;


    public FilePiece() {
        this.pieceIndex = -1;
        this.content = new byte[SystemConfiguration.pieceSize];
        this.fromPeer = null;
        this.isPieceAvailable = 0;
    }

    public int getPieceIndex() {
        return pieceIndex;
    }

    public void setPieceIndex(int pieceIndex) {
        this.pieceIndex = pieceIndex;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setFromPeer(String fromPeer) {
        this.fromPeer = fromPeer;
    }

    public int getIsPieceAvailable() {
        return isPieceAvailable;
    }

    public void setIsPieceAvailable(int isPieceAvailable) {
        this.isPieceAvailable = isPieceAvailable;
    }

    public static FilePiece getFilePieceFromPayload(byte[] payload) {
        byte[] indexBytes = new byte[Constants.PIECE_INDEX_LENGTH];
        FilePiece filePiece = new FilePiece();
        System.arraycopy(payload, 0, indexBytes, 0, Constants.PIECE_INDEX_LENGTH);
        filePiece.setPieceIndex(ByteBuffer.wrap(indexBytes).getInt());
        filePiece.setContent(new byte[payload.length - Constants.PIECE_INDEX_LENGTH]);
        System.arraycopy(payload, Constants.PIECE_INDEX_LENGTH, filePiece.getContent(), 0,  payload.length - Constants.PIECE_INDEX_LENGTH);
        return filePiece;
    }
}

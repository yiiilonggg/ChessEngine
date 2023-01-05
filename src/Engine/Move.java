package Engine;

public class Move {
    private char pieceMoving;
    private char capturedPiece = ' ';
    private boolean isNullMove;
    private boolean isCapture = false;
    private boolean isPromotion = false;
    private boolean isCastle = false;
    private long move;
    private long capturedPiecePosition = 0L;

    public Move(char pieceMoving, long move, boolean isNullMove) {
        this.pieceMoving = pieceMoving;
        this.move = move;
        this.isNullMove = isNullMove;
    }

    public void setCapture(char capturedPiece, long capturedPiecePosition) {
        this.isCapture = true;
        this.capturedPiece = capturedPiece;
        this.capturedPiecePosition = capturedPiecePosition;
    }


    public char getMovingPiece() {
        return this.pieceMoving;
    }

    public long getMove() {
        return this.move;
    }

    public boolean getIsCapture() {
        return this.isCapture;
    }
    public char getCapturedPiece() {
        return this.capturedPiece;
    }

    public long getCapturedPiecePosition() {
        return this.capturedPiecePosition;
    }

    public boolean getIsNullMove() {
        return this.isNullMove;
    }
}

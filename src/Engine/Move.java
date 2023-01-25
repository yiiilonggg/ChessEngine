package Engine;

public class Move {
    private char pieceMoving;
    private char capturedPiece = ' ';
    private char rookCode = 'R';
    private boolean isNullMove;
    private boolean isCapture = false;
    private boolean isPromotion = false;
    private boolean isCastle = false;
    private long move;
    private long capturedPiecePosition = 0L;
    private long enPassantFlag = 0L;
    private long rookMove = 0L;

    /**
     * constructor
     * @param pieceMoving   char of main moving piece
     * @param move          long of move (starting position | ending position)
     * @param isNullMove    boolean of whether move is a null (dummy) move
     */
    public Move(char pieceMoving, long move, boolean isNullMove) {
        this.pieceMoving = pieceMoving;
        this.move = move;
        this.isNullMove = isNullMove;
    }

    /**
     * set capture information of piece is captured (or en-passant-ed) on move
     * @param capturedPiece             char of captured piece code
     * @param capturedPiecePosition     long of captured piece code's position
     */
    public void setCapture(char capturedPiece, long capturedPiecePosition) {
        this.isCapture = true;
        this.capturedPiece = capturedPiece;
        this.capturedPiecePosition = capturedPiecePosition;
    }

    public void setCastle(char rookCode, long rookMove) {
        this.isCastle = true;
        this.rookCode = rookCode;
        this.rookMove = rookMove;
    }

    /**
     * @param enPassant long of pawn susceptible to en passant
     */
    public void setEnPassantFlag(long enPassant) { this.enPassantFlag = enPassant; }

    /**
     * @return  long of en passant flag at end of current move
     */
    public long getEnPassantFlag() { return this.enPassantFlag; }

    /**
     * @return  char of piece that moved
     */
    public char getMovingPiece() { return this.pieceMoving; }

    /**
     * @return  long of move in bitboard format
     */
    public long getMove() { return this.move; }

    /**
     * @return  boolean of if move involved a capture
     */
    public boolean getIsCapture() { return this.isCapture; }

    /**
     * @return  char of captured piece
     */
    public char getCapturedPiece() { return this.capturedPiece; }

    /**
     * @return  long of captured piece position
     */
    public long getCapturedPiecePosition() { return this.capturedPiecePosition; }

    /**
     * @return  boolean of if move was a dummy move
     */
    public boolean getIsNullMove() { return this.isNullMove; }

    public boolean getIsCastle() { return this.isCastle; }

    public char getRookCode() { return this.rookCode; }

    public long getRookMove() { return this.rookMove; }
}

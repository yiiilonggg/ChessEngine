package Engine;
import java.util.*;

public class Chessboard {
    
    /**
     * 
     * Chessboard bitboard representation
     * bit 0  -> A1 (bottom left)
     * bit 7  -> H1 (bottom right)
     * bit 56 -> A8 (top left)
     * bit 63 -> H8 (top right)
     * 
     * bitboard representing white pieces: K, Q, R, N, B, P
     * bitboard representing black pieces: k, q, r, n, b, p
     * 
     * bitboard representing all white pieces: W
     * bitboard representing all black pieces: w
     * 
     * bitboard representing all pieces: A
     * 
     */
    private Map<Character, Long> bitboards;

    /**
     * 
     * attributes for chessboard position
     * 
     * isWhiteTurn: true for white to move, false for black to move
     * 
     * castling rights (whiteKingSideCastle, whiteQueenSideCastle, blackKingSideCastle, blackQueenSideCastle): true for (potential) castling availability, false otherwise
     * (note): not necessary that the king can castle in this move, only that the king or rook has yet to move
     * 
     * enPassantFlag: the bitboard position of the pawn that is susceptible to en passant. if no pawn susceptible, set to 0
     * 
     * move clocks: track the move number of the game, important for move rule
     * 
     * game state: tracks if the game is still in progress, import so that the scanner still waits for user input
     * 
     */
    private boolean isWhiteTurn;
    private boolean kingInCheck;
    private boolean kingInDoubleCheck;
    private long criticalAttacksOnKing;
    private long criticalAttackers;
    private boolean whiteKingSideCastle = false, whiteQueenSideCastle = false, blackKingSideCastle = false, blackQueenSideCastle = false;
    private long enPassantFlag;
    private int halfMoveClock, fullMoveClock;
    private boolean gameState;
    public Stack<Move> moveHistory;

    // default FEN String
    public static final String DEFAULT_FEN_STRING = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    /**
     * 
     * constructor of the chessboard class. default chess position is the starting position
     * fenstring format: (pieces position) (whose turn) (castling rights) (enpassant flag) (halfmove clock) (fullmove clock)
     * pieces position: character corresponds to a piece, number represents the number of empty cells, / represents next row. from rank 8 to 1, from A file to H, on chessboard
     * castling rights: upper case for white, lower case for black. K for king side castle, Q for queen side castle availability
     * enpassant flag: - if no enpassant possible, else will be in the form of (rank)(file) of the pawn that can be enpassant-ed
     * halfmove/fullmove clocks: counts of moves
     * 
     * @param   fenstring   FEN string of starting position. if empty string passed, default starting position is used
     * @return              returns a Chessboard object with the initialised chess position
     * 
     */
    public Chessboard(String fenString) {

        // create chessboard map
        this.bitboards = new HashMap<>();
        this.bitboards.put('K', 0L);
        this.bitboards.put('Q', 0L);
        this.bitboards.put('R', 0L);
        this.bitboards.put('N', 0L);
        this.bitboards.put('B', 0L);
        this.bitboards.put('P', 0L);
        this.bitboards.put('k', 0L);
        this.bitboards.put('q', 0L);
        this.bitboards.put('r', 0L);
        this.bitboards.put('n', 0L);
        this.bitboards.put('b', 0L);
        this.bitboards.put('p', 0L);

        // if empty string passed, set fenstring as default starting position
        if (fenString.equals("")) fenString = DEFAULT_FEN_STRING;

        // split fenstring into its various components
        String[] fenStringDeconstructed = fenString.split(" ");

        // fill individual chessboard pieces into bitboard
        String pieces = fenStringDeconstructed[0];
        int piecesN = pieces.length(), rank = 7, file = 0;
        for (int i = 0; i < piecesN; i++) {
            if (Character.isAlphabetic(pieces.charAt(i))) {
                char currPiece = pieces.charAt(i);
                int idx = rank * 8 + file;
                bitboards.put(currPiece, bitboards.get(currPiece) | PCMBB.INDEX_TO_BIN_MAP.get(idx));
                file++;
            } else if (Character.isDigit(pieces.charAt(i))) {
                file += pieces.charAt(i) - '0';
            } else {
                rank--;
                file = 0;
            }
        }

        // set turn to move
        this.isWhiteTurn = (fenStringDeconstructed[1].equals("w"));

        // set castling rights
        String castlingRights = fenStringDeconstructed[2];
        int castlingRightsN = castlingRights.length();
        for (int i = 0; i < castlingRightsN; i++) {
            if (castlingRights.charAt(i) == '-') break;
            switch (castlingRights.charAt(i)){
                case 'K':
                    this.whiteKingSideCastle = true;
                    break;
                case 'Q':
                    this.whiteQueenSideCastle = true;
                    break;
                case 'k':
                    this.blackKingSideCastle = true;
                    break;
                case 'q':
                    this.blackQueenSideCastle = true;
                    break;
                default:
                    break;
            }
        }

        // set enPassantFlag
        String enPassantString = fenStringDeconstructed[3];
        if (enPassantString.equals("-")) {
            this.enPassantFlag = 0L;
        } else {
            int enPassantIdx = (((enPassantString.charAt(0) - '1') * 8) + enPassantString.charAt(1) - 'A');
            this.enPassantFlag = PCMBB.INDEX_TO_BIN_MAP.get(enPassantIdx);
        }

        // set move clocks
        this.halfMoveClock = Integer.parseInt(fenStringDeconstructed[4]);
        this.fullMoveClock = Integer.parseInt(fenStringDeconstructed[5]);

        // set game state
        // implement a check to check the input position if the position is already a checkmate position
        this.gameState = true;
        updateCheckInformation();
        this.moveHistory = new Stack<>();

        // print board to console
        System.out.println(printBoard());
    }

    /**
     * 
     * @return      bitboard of all white pieces
     * 
     */
    public long getWhiteBitboard() {
        return bitboards.get('K') | bitboards.get('Q') | bitboards.get('R') | bitboards.get('N') | bitboards.get('B') | bitboards.get('P');
    }

    /**
     * 
     * @return      bitboard of all black pieces
     * 
     */
    public long getBlackBitboard() {
        return bitboards.get('k') | bitboards.get('q') | bitboards.get('r') | bitboards.get('n') | bitboards.get('b') | bitboards.get('p');
    }

    /**
     * 
     * @return      bitboard of all pieces
     * 
     */
    public long getFullBitboard() {
        return getWhiteBitboard() | getBlackBitboard();
    }
    
    /**
     * 
     * @param   isWhitePiece    boolean of piece color where true is white
     * @return                  long of same colored board
     * 
     */
    public long getSameColouredBoard(boolean isWhitePiece) {
        return (isWhitePiece) ? getWhiteBitboard() : getBlackBitboard();
    }

    /**
     * 
     * @param   isWhitePiece    boolean of piece color where true is white
     * @return                  long of different colored board
     * 
     */
    public long getDiffColouredBoard(boolean isWhitePiece) {
        return (!isWhitePiece) ? getWhiteBitboard() : getBlackBitboard();
    }

    /**
     * prints the board state onto the console.
     * '.' marks the empty cell
     * 
     * @return      string representation of the board and board states
     */
    public String printBoard() {
        // print board
        char[][] board = new char[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = '.';
            }
        }
        for (Map.Entry<Character, Long> pair : this.bitboards.entrySet()) {
            long bitboardPosition = pair.getValue();
            for (int i = 0; i < 64; i++) {
                if ((bitboardPosition & PCMBB.INDEX_TO_BIN_MAP.get(i)) != 0) {
                    int rank = i / 8, file = i % 8;
                    board[rank][file] = pair.getKey();
                }
            }
        }
        StringBuilder str = new StringBuilder();
        char rankCount = '8';
        for (int i = 0; i < 8; i++) {
            str.append("---------------------------------------------------------\n");
            str.append(" |  ".concat(Character.toString(rankCount)).concat("  | "));
            for (int j = 0; j < 8; j++) {
                str.append(" ".concat(Character.toString(board[7 - i][j])).concat("  | "));
            }
            str.append("\n");
            rankCount -= 1;
        }
        str.append("---------------------------------------------------------\n");
        str.append(" |     |  A  |  B  |  C  |  D  |  E  |  F  |  G  |  H  | \n");
        str.append("---------------------------------------------------------\n");

        // display board details
        String toMove = this.isWhiteTurn ? "White to Move\n" : "Black to Move\n";
        String enPassant = this.enPassantFlag == 0 ? "Potential En Passant: -\n" : "Potential En Passant ".concat(findPositionName(this.enPassantFlag)).concat("\n");
        str.append(toMove);
        str.append(enPassant);
        str.append("Castling Legality: ");
        if (this.whiteKingSideCastle) str.append("White king side  |  ");
        if (this.whiteQueenSideCastle) str.append("White queen side   |  ");
        if (this.blackKingSideCastle) str.append("Black king side  |  ");
        if (this.blackQueenSideCastle) str.append("Black queen side  |  ");
        str.append("\n");
        str.append("Half-move clock: ".concat(Integer.toString(this.halfMoveClock)).concat("; Full-move clock: ").concat(Integer.toString(this.fullMoveClock)).concat("\n"));
        if (this.isWhiteTurn && this.kingInCheck) str.append("Black king in check.\n");
        if (!this.isWhiteTurn && this.kingInCheck) str.append("White king in check.\n");
        return str.toString();
    }

    /**
     * 
     * @param   position    takes in a long that represents a position       
     * @return              returns a String of the board position of the given long
     * 
     */
    private String findPositionName(long position) {
        int rank = PCMBB.RANK_COORDINATES_MAP.get(position);
        char file = PCMBB.FILE_COORDINATES_MAP.get(position);
        return Integer.toString(rank).concat(Character.toString(file));
    }

    /**
     * 
     * @return  getter to return the game state
     * 
     */
    public boolean getGameState() {
        return this.gameState;
    }

    /**
     * 
     * @return  getter to return the turn of the game
     * 
     */
    public boolean getIsWhiteTurn() {
        return this.isWhiteTurn;
    }

    public long getPiecesPosition(char pieceCode) {
        return this.bitboards.get(pieceCode);
    }

    public long getCriticalAttackMap() {
        return this.criticalAttackers | this.criticalAttacksOnKing;
    }

    public boolean isKingInCheck() {
        return this.kingInCheck;
    }

    public boolean isKingInDoubleCheck() {
        return this.kingInDoubleCheck;
    }

    /**
     * 
     * @param pieceCode         piece user has entered to move. case sensitivity handled at input
     * @param polledPosition    long of the position entered
     * @return                  true if piece entered is at position, false otherwise
     * 
     */
    public boolean checkPieceLocation(char pieceCode, long polledPosition) {
        if ((bitboards.get(pieceCode) & polledPosition) == 0) {
            System.out.println("Piece selected not found in starting position entered.");
            return false;
        }
        return true;
    }

    /**
     * 
     * @param pieceCode     piece to move
     * @param moveLong      move identity (starting and ending position included)
     * 
     */
    public void performMove(char pieceCode, long moveLong) {
        Move move = new Move(pieceCode, moveLong, false);

        // perform move on piece
        bitboards.put(pieceCode, bitboards.get(pieceCode) ^ moveLong);

        // check for pawn promotion
        // check for king castling

        // check for captures
        for (Map.Entry<Character, Long> pair : this.bitboards.entrySet()) {
            if (pieceCode == pair.getKey()) continue;
            if ((pair.getValue() & moveLong) == 0L) continue;
            move.setCapture(pair.getKey(), pair.getValue() & moveLong);
            this.bitboards.put(pair.getKey(), pair.getValue() & (~moveLong));
        }

        // update game states
        this.moveHistory.push(move);
        this.isWhiteTurn = !this.isWhiteTurn;
        updateCheckInformation();
    }

    public void performNullMove() {
        Move move = new Move(' ', 0L, true);
        this.moveHistory.push(move);
        this.isWhiteTurn = !this.isWhiteTurn;
        updateCheckInformation();
    }

    public void undoMove() {
        Move previousMove = this.moveHistory.pop();

        // undo the move itself
        if (!previousMove.getIsNullMove()) {
            bitboards.put(previousMove.getMovingPiece(), bitboards.get(previousMove.getMovingPiece()) ^ previousMove.getMove());
        }

        // undo capture
        if (previousMove.getIsCapture()) {
            bitboards.put(previousMove.getCapturedPiece(), bitboards.get(previousMove.getCapturedPiece()) | previousMove.getCapturedPiecePosition());
        }

        // update game states
        this.isWhiteTurn = !this.isWhiteTurn;
        updateCheckInformation();
    }

    private void updateCheckInformation() {
        long[] attackInformation = MoveHandler.generateAllAttackingSquares(this, true);
        this.kingInCheck = attackInformation[1] != 0L;
        this.kingInDoubleCheck = attackInformation[3] > 1L;
        this.criticalAttacksOnKing = attackInformation[1];
        this.criticalAttackers = attackInformation[2];
    }
}

package Engine;
public class MoveHandler {

    /**
     *  string pattern for user input format
     */
    public static final String USER_STRING_PATTERN = "[kqrnbp] [a-h][1-8] [a-h][1-8]";

    /**
     * 
     *  move compasses of the various pieces
     * 
     */
    public static final int[] horizontalCompass = new int[] { -1, 1 };
    public static final int[] verticalCompass = new int[] { -8, 8 };
    public static final int[] rookCompass = new int[] { -1, 1, -8, 8 };
    public static final int[] bishopCompass = new int[] { -7, 7, -9, 9 };
    public static final int[] queenCompass = new int[] { -1, 1, -8, 8, -7, 7, -9, 9 };

    /**
     * 
     * @param chessboard    Chessboard object of current chessboard
     * @param userString    String that user has entered that represents the intended move
     * @return              boolean of whether the move is successful or not. successful moves are updated onto the passed chessboard object
     * 
     */
    public static boolean performUserMove(Chessboard chessboard, String userString) {
        // 1. check if user stirng is in correct format
        if (!verifyUserString(userString)) return false;

        userString = userString.toLowerCase();
        String[] components = userString.split(" ");
        String pieceToMove = chessboard.getIsWhiteTurn() ? components[0].toUpperCase() : components[0];
        char pieceCode = pieceToMove.charAt(0);
        String startingPosition = components[1];
        String endingPosition = components[2];

        // 2. check if starting position is correct
        long startingPositonLong = PCMBB.positionCoordinatesToBitboard(startingPosition);
        if (!chessboard.checkPieceLocation(pieceCode, startingPositonLong)) return false;

        // 3. check if ending position is valid
        // to do with move generation of pieces
        // to do with checking for checkmate, checks, pins, etc
        long endingPositionLong = PCMBB.positionCoordinatesToBitboard(endingPosition);
        if (!verifyMove(chessboard, pieceCode, startingPositonLong, endingPositionLong)) return false;

        // 4. generate move
        long moveLong = startingPositonLong | endingPositionLong;

        // 5. perform move
        chessboard.performMove(pieceCode, moveLong);
        return true;
    }

    /**
     * 
     * @param userString    String of user's intended move
     * @return              boolean of whether the user has keyed the mvoe in the correct format
     * 
     */
    private static boolean verifyUserString(String userString) {
        if (!userString.matches(USER_STRING_PATTERN)) {
            System.out.println("The string input is not in the correct format");
            System.out.println("Please ensure it is in the form [piece code] [starting position] [ending position].");
            System.out.println("Accepted piece codes are k, q, r, n, b, p.");
            return false;
        }
        return true;
    }

    /**
     * 
     * @param chessboard        Chessboard object of where move is to be made
     * @param pieceCode         char piece code for function to funnel into correct function for checking
     * @param endingPosition    long of final piece position
     * @return                  boolean flag of whether valid move or not
     * 
     */
    private static boolean verifyMove(Chessboard chessboard, char pieceCode, long startingPosition, long endingPosition) {
        // include initial check for checkmate
        // include initial check for king in check
        boolean kingInCheck = false;

        boolean isWhitePiece = Character.isUpperCase(pieceCode);
        switch (Character.toLowerCase(pieceCode)) {
            case 'k':
                return verifyKingMove(chessboard, startingPosition, endingPosition, isWhitePiece, kingInCheck);
            case 'q':
                return verifySlidingPieceMove(chessboard, startingPosition, endingPosition, isWhitePiece, kingInCheck, pieceCode);
            case 'r':
                return verifySlidingPieceMove(chessboard, startingPosition, endingPosition, isWhitePiece, kingInCheck, pieceCode);
            case 'b':
                return verifySlidingPieceMove(chessboard, startingPosition, endingPosition, isWhitePiece, kingInCheck, pieceCode);
            case 'n':
                return verifyKnightMove(chessboard, startingPosition, endingPosition, isWhitePiece, kingInCheck);
            case 'p':
                return verifyPawnMove(chessboard, startingPosition, endingPosition, isWhitePiece, kingInCheck);
            default:
                System.out.println("Piece code not recognised");
                return false;
        }
    }

    private static boolean verifyKingMove(Chessboard chessboard, long startingPosition, long endingPosition, boolean isWhitePiece, boolean kingInCheck) {
        // consider castling, in check, checkmate
        return true;
    }

    /**
     * Checks if the given move is a valid move for the knight. First, it checks if the move will clash with other same-coloured pieces.
     * Next, it will check if the move is part of the valid moves for the given position
     * 
     * @param chessboard        Chessboard object of chessboard in play
     * @param startingPosition  long of piece's starting position
     * @param endingPosition    long of piece's target position
     * @param isWhitePiece      boolean for if piece is white
     * @param kingInCheck       boolean for if king is in check
     * @return                  boolean if knight move is valid
     * 
     */
    private static boolean verifyKnightMove(Chessboard chessboard, long startingPosition, long endingPosition, boolean isWhitePiece, boolean kingInCheck) {
        long sameColourBoard = (isWhitePiece) ? chessboard.getWhiteBitboard() : chessboard.getBlackBitboard();
        if ((sameColourBoard & endingPosition) != 0L) {
            System.out.println("Knight move invalid. Ending position clashes with a same-coloured piece.");
            return false;
        }
        long potentialMoves = PCMBB.KNIGHT_MOVE_MAP.get(startingPosition);
        if ((potentialMoves & endingPosition) == 0L) {
            System.out.println("Knight move invalid. Ending position is not part of the valid knight moves at starting position");
            return false;
        }
        return true;
    }

    /**
     * Checks if the given move is a valid move for the piece. First, it checks if the move will clash with other same-coloured pieces.
     * Next, it will generate all potential moves (inclusive of captures) and treats all pieces as opposition pieces. Finally,
     * the legal moveset is derived by taking all potential moves and performing a bitwiseAND with the bitwiseNOT of the same-coloured board.
     * 
     * @param chessboard        Chessboard object of chessboard in play
     * @param startingPosition  long of piece's starting position
     * @param endingPosition    long of piece's target position
     * @param isWhitePiece      boolean for if piece is white
     * @param kingInCheck       boolean for if king is in check
     * @param pieceCode         char representing which sliding piece it is; q - queen, r - rook, b - bishop
     * @return                  boolean if knight move is valid
     * 
     */
    private static boolean verifySlidingPieceMove(Chessboard chessboard, long startingPosition, long endingPosition, boolean isWhitePiece, boolean kingInCheck, char pieceCode) {
        long sameColourBoard = (isWhitePiece) ? chessboard.getWhiteBitboard() : chessboard.getBlackBitboard();
        if ((sameColourBoard & endingPosition) != 0L) {
            System.out.println("Sliding move invalid. Ending position clashes with a same-coloured piece.");
            return false;
        }
        long fullBoard = chessboard.getFullBitboard();
        long allMoves;
        switch (pieceCode) {
            case 'b':
                allMoves = PCMBB.getBishopAttacks(PCMBB.BIN_TO_INDEX_MAP.get(startingPosition), fullBoard);
                break;
            case 'r':
                allMoves = PCMBB.getRookAttacks(PCMBB.BIN_TO_INDEX_MAP.get(startingPosition), fullBoard);
                break;
            default:
                allMoves = PCMBB.getQueenAttacks(PCMBB.BIN_TO_INDEX_MAP.get(startingPosition), fullBoard);
                break;
        } 
        allMoves &= ~sameColourBoard;
        if ((allMoves & endingPosition) == 0L) {
            System.out.println("Sliding move invalid. Ending position is not part of valid bishop moves from starting position.");
            return false;
        }
        return true;
    }

    /**
     * Checks if the given move is a valid move for the pawn. First, it checks if the move will clash with other same-coloured pieces.
     * Next, it will check if the move is part of the valid moves for the given position, inclusive of pawn attacks.
     * 
     * @param chessboard        Chessboard object of chessboard in play
     * @param startingPosition  long of piece's starting position
     * @param endingPosition    long of piece's target position
     * @param isWhitePiece      boolean for if piece is white
     * @param kingInCheck       boolean for if king is in check
     * @return                  boolean if knight move is valid
     * 
     */
    private static boolean verifyPawnMove(Chessboard chessboard, long startingPosition, long endingPosition, boolean isWhitePiece, boolean kingInCheck) {
        long sameColourBoard = (isWhitePiece) ? chessboard.getWhiteBitboard() : chessboard.getBlackBitboard();
        if ((sameColourBoard & endingPosition) != 0L) {
            System.out.println("Pawn move invlid. Ending position clashes with a same-coloured piece.");
            return false;
        }
        long potentialMoves = (isWhitePiece) ? PCMBB.WHITE_PAWN_MOVE_MAP.get(startingPosition) : PCMBB.BLACK_PAWN_MOVE_MAP.get(startingPosition);
        long potentialAttacks = (isWhitePiece) ? PCMBB.WHITE_PAWN_ATTACK_MAP.get(startingPosition) : PCMBB.BLACK_PAWN_ATTACK_MAP.get(startingPosition);
        long diffColourBoard = (!isWhitePiece) ? chessboard.getWhiteBitboard() : chessboard.getBlackBitboard();
        potentialAttacks &= diffColourBoard;
        potentialMoves |= potentialAttacks;
        if ((potentialMoves & endingPosition) == 0L) {
            System.out.println("Pawn move invalid. Ending position is not part of the valid pawn moves at starting position");
            return false;
        }
        return true;
    }
}

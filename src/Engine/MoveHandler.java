package Engine;
import java.util.*;

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

    public static long generateKingMoves(Chessboard chessboard, boolean isWhitePiece, long startingPosition) {
        long moves = generateKingAttacks(chessboard, isWhitePiece, startingPosition);

        // add function in chessboard to generate if king/queen castling is possible
        // castling is possible, add castling moves

        return moves;
    }

    public static long generateKingAttacks(Chessboard chessboard, boolean isWhitePiece, long startingPosition) {
        return PCMBB.getKingMoves(startingPosition);
    }

    public static long generateKnightMoves(Chessboard chessboard, boolean isWhitePiece, long startingPosition) {
        return PCMBB.KNIGHT_MOVE_MAP.get(startingPosition);
    }

    public static long generatePawnMoves(Chessboard chessboard, boolean isWhitePiece, long startingPosition) {
        // if the there is a same-coloured piece one square ahead, then the pawn cannot move forward at all
        long sameColourBoard = chessboard.getSameColouredBoard(isWhitePiece);
        long oneSquareForward = (isWhitePiece) ? startingPosition << 8 : startingPosition >>> 8;
        long potentialMoves = ((sameColourBoard & oneSquareForward) == 0L) ? PCMBB.getPawnMoves(startingPosition, isWhitePiece) : 0L;

        // check if there are pieces to capture in diagonally-opposite positions
        long potentialAttacks = generatePawnAttacks(chessboard, isWhitePiece, startingPosition);
        potentialMoves |= potentialAttacks;

        // check for en passant

        return potentialMoves;
    }

    public static long generatePawnAttacks(Chessboard chessboard, boolean isWhitePiece, long startingPosition) {
        long potentialAttacks = PCMBB.getPawnAttacks(startingPosition, isWhitePiece);
        long diffColourBoard = chessboard.getDiffColouredBoard(isWhitePiece);
        return potentialAttacks & diffColourBoard;
    }

    public static long generateSlidingMoves(Chessboard chessboard, boolean isWhitePiece, long startingPosition, char pieceCode) {
        long fullBoard = chessboard.getFullBitboard();
        switch (pieceCode) {
            case 'b':
                return PCMBB.getBishopAttacks(PCMBB.BIN_TO_INDEX_MAP.get(startingPosition), fullBoard);
            case 'r':
                return PCMBB.getRookAttacks(PCMBB.BIN_TO_INDEX_MAP.get(startingPosition), fullBoard);
            default:
                return PCMBB.getQueenAttacks(PCMBB.BIN_TO_INDEX_MAP.get(startingPosition), fullBoard);
        }
    }

    public static long generatePieceAttackingSquares(Chessboard chessboard, boolean isWhitePiece, long startingPosition, char pieceCode) {
        long potentialAttacks;
        switch (Character.toLowerCase(pieceCode)) {
            case 'k':
                potentialAttacks = generateKingAttacks(chessboard, isWhitePiece, startingPosition);
                break;
            case 'n':
                potentialAttacks = generateKnightMoves(chessboard, isWhitePiece, startingPosition);
                break;
            case 'p':
                potentialAttacks = generatePawnAttacks(chessboard, isWhitePiece, startingPosition);
                break;
            default:
                potentialAttacks = generateSlidingMoves(chessboard, isWhitePiece, startingPosition, pieceCode);
                break;
        }
        return potentialAttacks;
    }

    public static long[] generateAllAttackingSquares(Chessboard chessboard, boolean isOpposition) {
        // check which color to generate attacking squares of
        // e.g. if it is white's turn and we are to generate the opposition attacking squares, then we generate for black pieces
        boolean isWhitePiece = (chessboard.getIsWhiteTurn() ^ isOpposition);
        char[] pieceCodes = (isWhitePiece) ? PCMBB.whitePieceCodes : PCMBB.blackPieceCodes;
        long allAttacks = 0L, criticalAttacks = 0L, criticalAttackers = 0L;

        // obtain king information to check for checks
        long kingPosition = (isWhitePiece) ? chessboard.getPiecesPosition('k') : chessboard.getPiecesPosition('K');
        int kingRank = PCMBB.RANK_COORDINATES_MAP.get(kingPosition) - 1, kingFile = PCMBB.FILE_COORDINATES_MAP.get(kingPosition) - 'A';
        long rankMask = PCMBB.getRankMask(kingRank), fileMask = PCMBB.getFileMask(kingFile);
        long topLeftDiagonal = PCMBB.getTopLeftDiagonal(kingRank, kingFile), topRightDiagonal = PCMBB.getTopRightDiagonal(kingRank, kingFile);
        long[] masks = new long[] { rankMask, fileMask, topLeftDiagonal, topRightDiagonal };

        // for checking if double check
        int count = 0;

        for (char pieceCode : pieceCodes) {
            long pieces = chessboard.getPiecesPosition(pieceCode);
            List<Long> piecesPositions = PCMBB.getIndividualPositions(pieces);
            for (long piece : piecesPositions) {
                long attack = generatePieceAttackingSquares(chessboard, isWhitePiece, piece, pieceCode);
                allAttacks |= attack;

                // if piece is a king, skip, kings cannot give checks
                if (Character.toLowerCase(pieceCode) == 'k') continue;
                
                // if the attack does not hit the king, it is not a check
                if ((attack & kingPosition) == 0L) continue;

                // if a piece's attack hits the king, it is considered a critical attacker
                criticalAttackers |= piece;
                count++;
                // knight and pawn attacks that are checks only hit one square, which is the king square
                if (Character.toLowerCase(pieceCode) == 'p' || Character.toLowerCase(pieceCode) == 'n') continue;

                // mask is essentially a long that represents the row / file / diagonal
                for (long mask : masks) {
                    // for sliding pieces, we check if the piece sees the king on the mask
                    if ((mask & piece) == 0L) continue;
                    criticalAttacks |= (mask & attack);
                    break;
                }
            }
        }
        // if there is a critical attack, it may be a knight / pawn so capture king square to be safe as attack square
        if (criticalAttackers != 0L) criticalAttacks |= kingPosition;

        // attacking squares cannot hit the same color pieces
        long sameColourBoard = chessboard.getSameColouredBoard(isWhitePiece);
        allAttacks &= ~sameColourBoard;

        return new long[] { allAttacks, criticalAttacks, criticalAttackers, (long) count };
    }

    public static long generateLegalMoves(Chessboard chessboard, boolean isWhitePiece, long startingPosition, char pieceCode) {
        // check for double check
        // if king under double check, the king has to move
        if (chessboard.isKingInDoubleCheck() && Character.toLowerCase(pieceCode) != 'k') return 0L;

        long potentialMoves;
        switch (Character.toLowerCase(pieceCode)) {
            case 'k':
                potentialMoves = generateKingMoves(chessboard, isWhitePiece, startingPosition);
                break;
            case 'n':
                potentialMoves = generateKnightMoves(chessboard, isWhitePiece, startingPosition);
                break;
            case 'p':
                potentialMoves = generatePawnMoves(chessboard, isWhitePiece, startingPosition);
                break;
            default:
                potentialMoves = generateSlidingMoves(chessboard, isWhitePiece, startingPosition, pieceCode);
                break;
        }
        
        // potential moves generated cannot clash with same-coloured pieces
        long sameColourBoard = chessboard.getSameColouredBoard(isWhitePiece);
        potentialMoves &= ~sameColourBoard;

        // check if king is underattack, moves can only be in the line of attack or a capture of the attacking piece

        // check for pinned piece
        // remove the piece and perform a null move check if the king is still in check
        chessboard.performMove(pieceCode, startingPosition);
        chessboard.performNullMove();
        boolean pinnedPiece = chessboard.isKingInCheck();

        // if not pinned piece, then it can move anywhere else, so -1 which has a binary string of 111...111
        long blockingPositions = (pinnedPiece) ? chessboard.getCriticalAttackMap() : -1;

        // undo two moves as we need to also undo the nullMove
        chessboard.undoMove();
        chessboard.undoMove();
        potentialMoves &= blockingPositions;

        return potentialMoves;
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
        // check if piece ending position clashes with a same-coloured piece.
        boolean isWhitePiece = Character.isUpperCase(pieceCode);
        long sameColourBoard = chessboard.getSameColouredBoard(isWhitePiece);
        if ((sameColourBoard & endingPosition) != 0L) {
            System.out.println("Move invalid. Ending position clashes with a same-coloured piece.");
            return false;
        }
        
        // check for king in check
        boolean kingInCheck = chessboard.isKingInCheck();

        if (kingInCheck) {
            // check that if the move is performed, it will block the check
            // i.e. if i generate all the attacking squares again after the move is made, the king is not under attack
            long pseudoMove = startingPosition | endingPosition;
            chessboard.performMove(pieceCode, pseudoMove);
            chessboard.performNullMove();
            boolean kingStillInCheck = chessboard.isKingInCheck();
            chessboard.undoMove();
            chessboard.undoMove();
            // fails if double check or piece attacking king is a knight or pawn and it is not captured
            if (kingStillInCheck) {
                System.out.println("Move invalid. King is in check and intended move does not resolve the check");
                return false;
            }
        }

        long moves = generateLegalMoves(chessboard, isWhitePiece, startingPosition, pieceCode);
        if ((moves & endingPosition) == 0L) {
            System.out.println("Move invalid. Ending position is not part of the valid moves at starting position");
            return false;
        }
        return true;
    }
}

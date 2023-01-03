package Engine;
import java.util.*;

public class PCMBB {

    /**
     * 
     * PCMBB = Pre-Computed (Magic) BitBoards
     * helper maps
     * all helper maps are view only (of unmodifiableMap class)
     * 
     * the idea is to get a list of maps with all move and attack information pre-computed
     * initialised once at start-up of app
     * when required to obtain the movelist of a piece in a position, instead of computing, we just poll the piece's position and the move list is retrieved
     * 
     */
    public static Map<Long, Integer> RANK_COORDINATES_MAP;
    public static Map<Long, Character> FILE_COORDINATES_MAP;
    public static Map<Integer, Long> INDEX_TO_BIN_MAP;
    public static Map<Long, Integer> BIN_TO_INDEX_MAP;
    public static Map<Long, Long> KING_MOVE_MAP;
    public static Map<Long, Long> KNIGHT_MOVE_MAP;
    public static Map<Long, Long> WHITE_PAWN_MOVE_MAP;
    public static Map<Long, Long> WHITE_PAWN_ATTACK_MAP;
    public static Map<Long, Long> BLACK_PAWN_MOVE_MAP;
    public static Map<Long, Long> BLACK_PAWN_ATTACK_MAP;

    public static final int[][] KING_MOVES = new int[][] { { -1, 0 }, { -1, 1 }, { 0, 1 }, { 1, 1 }, { 1, 0 }, { 1, -1}, { 0, -1 }, { -1, -1 } };
    public static final int[][] KNIGHT_MOVES = new int[][] { { -1, 2 }, { -2, 1 }, { -2, -1 }, { -1, -2 }, { 1, 2 }, { 2, 1 }, { 2, -1 }, { 1, -2 } };

    /**
     * 
     * information for magic bitboards
     * for rooks (horizontal and vertical sliding moves) and bishops (diagonal)
     * queen moves are derived by generating both rook and bishop moves, and taking the bitwiseOR of the results
     * the idea is that there is a series of transformations that you can modify to an occupancy mask, which then returns you all possible moves for the given position
     * transformation: ((occupancy & mask) * magicNumber) >>> bitshift
     * when we want to retrieve all moves, we only need two information: square the piece is on, and the occupancy of other pieces around it
     * 
     */
    public static final int[] BIT_TABLE = new int[] {
        63, 0, 58, 1, 59, 47, 53, 2,
        60, 39, 48, 27, 54, 33, 42, 3,
        61, 51, 37, 40, 49, 18, 28, 20,
        55, 30, 34, 11, 43, 14, 22, 4,
        62, 57, 46, 52, 38, 26, 32, 41,
        50, 36, 17, 19, 29, 10, 13, 21,
        56, 45, 25, 31, 35, 16, 9, 12,
        44, 24, 15, 8, 23, 7, 6, 5
    };
    public static long[] ROOK_MASK = new long[64];
    public static long[] BISHOP_MASK = new long[64];
    public static long[] ROOK_MAGIC = {
        0x0080001020400080L, 0x0040001000200040L, 0x0080081000200080L, 0x0080040800100080L, 0x0080020400080080L, 0x0080010200040080L, 0x0080008001000200L, 0x0080002040800100L,
        0x0000800020400080L, 0x0000400020005000L, 0x0000801000200080L, 0x0000800800100080L, 0x0000800400080080L, 0x0000800200040080L, 0x0000800100020080L, 0x0000800040800100L,
        0x0000208000400080L, 0x0000404000201000L, 0x0000808010002000L, 0x0000808008001000L, 0x0000808004000800L, 0x0000808002000400L, 0x0000010100020004L, 0x0000020000408104L,
        0x0000208080004000L, 0x0000200040005000L, 0x0000100080200080L, 0x0000080080100080L, 0x0000040080080080L, 0x0000020080040080L, 0x0000010080800200L, 0x0000800080004100L,
        0x0000204000800080L, 0x0000200040401000L, 0x0000100080802000L, 0x0000080080801000L, 0x0000040080800800L, 0x0000020080800400L, 0x0000020001010004L, 0x0000800040800100L,
        0x0000204000808000L, 0x0000200040008080L, 0x0000100020008080L, 0x0000080010008080L, 0x0000040008008080L, 0x0000020004008080L, 0x0000010002008080L, 0x0000004081020004L,
        0x0000204000800080L, 0x0000200040008080L, 0x0000100020008080L, 0x0000080010008080L, 0x0000040008008080L, 0x0000020004008080L, 0x0000800100020080L, 0x0000800041000080L,
        0x0000102040800101L, 0x0000102040008101L, 0x0000081020004101L, 0x0000040810002101L, 0x0001000204080011L, 0x0001000204000801L, 0x0001000082000401L, 0x0000002040810402L
    };
    public static long[] BISHOP_MAGIC = {
        0x0002020202020200L, 0x0002020202020000L, 0x0004010202000000L, 0x0004040080000000L, 0x0001104000000000L, 0x0000821040000000L, 0x0000410410400000L, 0x0000104104104000L,
        0x0000040404040400L, 0x0000020202020200L, 0x0000040102020000L, 0x0000040400800000L, 0x0000011040000000L, 0x0000008210400000L, 0x0000004104104000L, 0x0000002082082000L,
        0x0004000808080800L, 0x0002000404040400L, 0x0001000202020200L, 0x0000800802004000L, 0x0000800400A00000L, 0x0000200100884000L, 0x0000400082082000L, 0x0000200041041000L,
        0x0002080010101000L, 0x0001040008080800L, 0x0000208004010400L, 0x0000404004010200L, 0x0000840000802000L, 0x0000404002011000L, 0x0000808001041000L, 0x0000404000820800L,
        0x0001041000202000L, 0x0000820800101000L, 0x0000104400080800L, 0x0000020080080080L, 0x0000404040040100L, 0x0000808100020100L, 0x0001010100020800L, 0x0000808080010400L,
        0x0000820820004000L, 0x0000410410002000L, 0x0000082088001000L, 0x0000002011000800L, 0x0000080100400400L, 0x0001010101000200L, 0x0002020202000400L, 0x0001010101000200L,
        0x0000410410400000L, 0x0000208208200000L, 0x0000002084100000L, 0x0000000020880000L, 0x0000001002020000L, 0x0000040408020000L, 0x0004040404040000L, 0x0002020202020000L,
        0x0000104104104000L, 0x0000002082082000L, 0x0000000020841000L, 0x0000000000208800L, 0x0000000010020200L, 0x0000000404080200L, 0x0000040404040400L, 0x0002020202020200L
    };
    public static long[][] ROOK_ATTACK_BOARD;
    public static long[][] BISHOP_ATTACK_BOARD;
    /**
     * 
     * initialises static variables of the class
     * 
     */
    static {

        // maps of useful information for quick retrieval
        Map<Long, Integer> rankCoordinates = new HashMap<>();
        Map<Long, Character> fileCoordinates = new HashMap<>();
        Map<Integer, Long> indexToBin = new HashMap<>();
        Map<Long, Integer> binToIndex = new HashMap<>();
        char file = 'A';
        for (int i = 0; i < 64; i++) {
            rankCoordinates.put(1L << i, (i / 8) + 1);

            fileCoordinates.put(1L << i, file);
            file++;
            if (file == 'I') file = 'A';
            
            indexToBin.put(i, 1L << i);

            binToIndex.put(1L << i, i);
        }
        RANK_COORDINATES_MAP = Collections.unmodifiableMap(rankCoordinates);
        FILE_COORDINATES_MAP = Collections.unmodifiableMap(fileCoordinates);
        INDEX_TO_BIN_MAP = Collections.unmodifiableMap(indexToBin);
        BIN_TO_INDEX_MAP = Collections.unmodifiableMap(binToIndex);

        // king and knight moves are trivial. excludes castling for king
        KING_MOVE_MAP = Collections.unmodifiableMap(fillStaticPieceMoves('k'));
        KNIGHT_MOVE_MAP = Collections.unmodifiableMap(fillStaticPieceMoves('n'));

        // pawns are the only pieces where moves are not attacks
        List<Map<Long, Long>> whitePawnMaps = fillPawnPieceMoves(true);
        List<Map<Long, Long>> blackPawnMaps = fillPawnPieceMoves(false);
        WHITE_PAWN_MOVE_MAP = Collections.unmodifiableMap(whitePawnMaps.get(0));
        WHITE_PAWN_ATTACK_MAP = Collections.unmodifiableMap(whitePawnMaps.get(1));
        BLACK_PAWN_MOVE_MAP = Collections.unmodifiableMap(blackPawnMaps.get(0));
        BLACK_PAWN_ATTACK_MAP = Collections.unmodifiableMap(blackPawnMaps.get(1));

        // generate the masks for rooks and bishops
        // masks are essentially all possible moves when piece is at a square, excluding the edge rows and files
        // edge rows and files are excluded as if there is no piece on squares just before the edges, the piece can travel to the edge
        ROOK_MASK = findRookMask();
        BISHOP_MASK = findBishopMask();

        // attack boards are of different permuations
        // rooks have 4096 permutations of occupancies for each square, 512 for bishops
        ROOK_ATTACK_BOARD = fillAttackBoards(false);
        BISHOP_ATTACK_BOARD = fillAttackBoards(true);
    }

    /**
     * 
     * @param pieceCode char that is either 'k' or 'n'
     * @return          Map<Long, Long> of all possible positions and corresponding moves
     * 
     */
    private static Map<Long, Long> fillStaticPieceMoves(char pieceCode) {
        int[][] moveset = (pieceCode) == 'k' ? KING_MOVES : KNIGHT_MOVES;
        Map<Long, Long> moveMap = new HashMap<>();
        for (int i = 0; i < 64; i++) {
            long currPosition = 1L << i;
            long potentialMoves = 0L;
            int rank = i / 8, file = i % 8;
            for (int j = 0; j < 8; j++) {
                int newFile = file + moveset[j][0], newRank = rank + moveset[j][1];
                if (newFile < 0 || newRank < 0 || newFile > 7 || newRank > 7) continue;
                potentialMoves |= (1L << (newRank * 8 + newFile));
            }
            moveMap.put(currPosition, potentialMoves);
        }
        return moveMap;
    }

    /**
     * 
     * @param isWhitePawn   boolean flag for whether the pawn moves are for white or black
     * @return              2 Map<Long, Long>, where index 0 is for moves, index 1 is for attacks
     * 
     */
    private static List<Map<Long, Long>> fillPawnPieceMoves(boolean isWhitePawn) {
        Map<Long, Long> pawnMoves = new HashMap<>(), pawnAttacks = new HashMap<>();
        for (int i = 0; i < 8; i++) {
            long start = (isWhitePawn) ? 1L << (8 + i) : 1L << (48 + i);
            long nextMoves = (isWhitePawn) ? ((start << 8) | (start << 16)) : ((start >>> 8) | (start >>> 16));
            long nextAttacks = getPawnAttacks(start, i, isWhitePawn);
            pawnMoves.put(start, nextMoves);
            pawnAttacks.put(start, nextAttacks);
            
            for (int j = 1; j < 7; j++) {
                long currPosition = (isWhitePawn) ? (start << (8 * j)) : (start >>> (8 * j));
                nextMoves = (isWhitePawn) ? (currPosition << 8) : (currPosition >>> 8);
                nextAttacks = getPawnAttacks(currPosition, i, isWhitePawn);
                pawnMoves.put(currPosition, nextMoves);
                pawnAttacks.put(currPosition, nextAttacks);
            }
        }
        return new ArrayList<>(List.of(pawnMoves, pawnAttacks));
    }

    /**
     * 
     * @param position      long that represents the position of the pawn on the chessboard
     * @param file          int that represents the file the pawn is on
     * @param isWhitePawn   boolean flag for whether it is a white or black pawn
     * @return              long of pawn attacks
     * 
     */
    private static long getPawnAttacks(long position, int file, boolean isWhitePawn) {
        if (file == 0) return (isWhitePawn) ? (position << 9) : (position >>> 9);
        if (file == 7) return (isWhitePawn) ? (position << 7) : (position >>> 7);
        return (isWhitePawn) ? ((position << 9) | (position << 7)) : ((position >>> 9) | (position >>> 7));
    }

    /**
     * 
     * @return  long[64] of rook masks for every position on the board
     * 
     */
    private static long[] findRookMask() {
        long[] masks = new long[64];
        for (int i = 0; i < 64; i++) {
            long mask = 0L;
            int rank = i / 8, file = i % 8;
            for (int r = rank + 1; r < 7; r++) mask |= INDEX_TO_BIN_MAP.get(r * 8 + file);
            for (int r = rank - 1; r > 0; r--) mask |= INDEX_TO_BIN_MAP.get(r * 8 + file);
            for (int f = file + 1; f < 7; f++) mask |= INDEX_TO_BIN_MAP.get(rank * 8 + f);
            for (int f = file - 1; f > 0; f--) mask |= INDEX_TO_BIN_MAP.get(rank * 8 + f);
            masks[i] = mask;
        }
        return masks;
    }

    /**
     * 
     * @return  long[64] of bishop masks for every position on the board
     * 
     */
    private static long[] findBishopMask() {
        long[] masks = new long[64];
        for (int i = 0; i < 64; i++) {
            long mask = 0L;
            int rank = i / 8, file = i % 8;
            for (int r = rank + 1, f = file + 1; r < 7 && f < 7; r++, f++) mask |= INDEX_TO_BIN_MAP.get(r * 8 + f);
            for (int r = rank + 1, f = file - 1; r < 7 && f > 0; r++, f--) mask |= INDEX_TO_BIN_MAP.get(r * 8 + f);
            for (int r = rank - 1, f = file + 1; r > 0 && f < 7; r--, f++) mask |= INDEX_TO_BIN_MAP.get(r * 8 + f);
            for (int r = rank - 1, f = file - 1; r > 0 && f > 0; r--, f--) mask |= INDEX_TO_BIN_MAP.get(r * 8 + f);
            masks[i] = mask;
        }
        return masks;
    }

    /**
     * 
     * @param isBishop  boolean of whether we are generating the bishop attack board
     * @return          long[64][] attack board
     * 
     */
    private static long[][] fillAttackBoards(boolean isBishop) {
        long[][] attackBoard = (isBishop) ? new long[64][512] : new long[64][4096];
        int[] squares = new int[64];
        int numSquares;
        for (int i = 0; i < 64; ++i) {
            numSquares = 0;
            long temp = (isBishop) ? BISHOP_MASK[i] : ROOK_MASK[i];
            // essentially trialing for every permutation of bits in temp
            // based on some masking sequence, set the number of important bits for each square
            while (temp != 0) {
                final long bit = (temp & -temp);
                squares[numSquares++] = BIT_TABLE[(int) ((bit * 0x07EDD5E59A4E28C2L) >>> 58)];
                temp ^= bit;
            }
            // when temp == 0, 1 << numSquares is the number of permutations of different occupancies since 1 bit is popped off temp each round
            for (temp = 0; temp < (1L << numSquares); ++temp) {
                final long tempOccupancy = setOccupancy(squares, numSquares, temp);
                if (isBishop) {
                    attackBoard[i][(int) ((tempOccupancy * BISHOP_MAGIC[i]) >>> 55)] = findBishopMove(i, tempOccupancy);
                } else {
                    attackBoard[i][(int) ((tempOccupancy * ROOK_MAGIC[i]) >>> 52)] = findRookMove(i, tempOccupancy);
                }
            }
        }
        return attackBoard;
    }
    /**
     * 
     * @param square        int index of piece coordinate
     * @param occupancy     long of the board
     * @return              long of all bishop attacks for bishop at square and occupancy
     * 
     */
    public static long getBishopAttacks(int square, long occupancy) {
        return BISHOP_ATTACK_BOARD[square][(int) (((occupancy & BISHOP_MASK[square]) * BISHOP_MAGIC[square]) >>> 55)];
    }

    /**
     * 
     * @param square        int index of piece coordinate
     * @param occupancy     long of the board
     * @return              long of all rook attacks for rook at square and occupancy
     * 
     */
    public static long getRookAttacks(int square, long occupancy) {
        return ROOK_ATTACK_BOARD[square][(int) (((occupancy & ROOK_MASK[square]) * ROOK_MAGIC[square]) >>> 52)];
    }

    /**
     * 
     * @param square        int index of piece coordinate
     * @param occupancy     long of the board
     * @return              long of all queen attacks for queen at square and occupancy
     * 
     */
    public static long getQueenAttacks(int square, long occupancy) {
        return getBishopAttacks(square, occupancy) | getRookAttacks(square, occupancy);
    }

    /**
     * 
     * @param squares       int[] of important bits at each square
     * @param squareNumber  int of total number of important squares
     * @param mask          long some permutation of the board occupancy
     * @return              long of the important bits based on the occupancy and important squares
     * 
     */
    private static long setOccupancy(int[] squares, int squareNumber, long mask) {
        long ret = 0L;
        for (int i = 0; i < squareNumber; ++i)
            if ((mask & (1L << i)) != 0)
                ret |= 1L << squares[i];
        return ret;
    }

    /**
     * 
     * @param idx       int square of rook position
     * @param block     long of board occupancies
     * @return          long of all posible moves. obtains this by brute force
     * 
     */
    private static long findRookMove(int idx, long block) {
        long move = 0L;
        final long rowbits = 0xFFL << (8 * (idx / 8));

        long bit = 1L << idx;
        while (bit > 0 && (bit & block) == 0) {
            bit <<= 8;
            move |= bit;
        }
        bit = 1L << idx;
        while (bit != 0 && (bit & block) == 0) {
            bit >>>= 8;
            move |= bit;
        }
        bit = 1L << idx;
        while ((bit & block) == 0) {
            bit <<= 1;
            if ((bit & rowbits) == 0) break;
            move |= bit;
        }
        bit = 1L << idx;
        while ((bit & block) == 0) {
            bit >>>= 1;
            if ((bit & rowbits) == 0) break;
            move |= bit;
        }
        return move;
    }

    /**
     * 
     * @param idx       int square of bishop position
     * @param block     long of board occupancies
     * @return          long of all posible moves. obtains this by brute force
     * 
     */
    private static long findBishopMove(int idx, long block) {
        long ret = 0L;
        final long rowbits = 0xFFL << (8 * (idx / 8));
        long bit = 1L << idx;
        long bit2 = bit;
        while (bit != 0 && (bit & block) == 0) {
            bit <<= 8 - 1;
            bit2 >>>= 1;
            if ((bit2 & rowbits) == 0) break;
            ret |= bit;
        }
        bit = 1L << idx;
        bit2 = bit;
        while (bit != 0 && (bit & block) == 0) {
            bit <<= 8 + 1;
            bit2 <<= 1;
            if ((bit2 & rowbits) == 0) break;
            ret |= bit;
        }
        bit = 1L << idx;
        bit2 = bit;
        while (bit != 0 && (bit & block) == 0) {
            bit >>>= 8 - 1;
            bit2 <<= 1;
            if ((bit2 & rowbits) == 0) break;
            ret |= bit;
        }
        bit = 1L << idx;
        bit2 = bit;
        while (bit > 0 && (bit & block) == 0) {
            bit >>>= 8 + 1;
            bit2 >>= 1;
            if ((bit2 & rowbits) == 0) break;
            ret |= bit;
        }
        return ret;
    }
    
    /**
     * 
     * @param position  String of the position of the piece in the format [file][rank]
     * @return          long of the input position
     */
    public static long positionCoordinatesToBitboard(String position) {
        int file = position.charAt(0) - 'a', rank = position.charAt(1) - '1';
        int idx = rank * 8 + file;
        return PCMBB.INDEX_TO_BIN_MAP.get(idx);
    }

    /**
     * 
     * @param position  String of position of piece in the format [file][rank]
     * @return          int index of the piece's position
     */
    public static int positionCoordinatesToIndex(String position) {
        int file = position.charAt(0) - 'a', rank = position.charAt(1) - '1';
        return rank * 8 + file;
    }

    /**
     * 
     * @param occupancies   List<String> of positions on the board with pieces
     * @return              long of all positions in list combined
     * 
     */
    public static long createOccupancy(List<String> occupancies) {
        long bitboard = 0L;
        for (String occupancy : occupancies) {
            bitboard |= positionCoordinatesToBitboard(occupancy);
        }
        return bitboard;
    }

    /**
     * 
     * @param bitboard  long of a bitboard to be printed
     * 
     */
    public static void printBoard(long bitboard) {
        // print board
        char[][] board = new char[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = ((bitboard & INDEX_TO_BIN_MAP.get(i * 8 + j)) != 0L) ? '1' : '.';
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
        System.out.println(str.toString());
    }
}

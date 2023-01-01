public class MoveHandler {
    public static final String USER_STRING_PATTERN = "[kqrnbp] [a-h][1-8] [a-h][1-8]";

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
        long startingPositonLong = positionCoordinatesToBitboard(startingPosition);
        if (!chessboard.checkPieceLocation(pieceCode, startingPositonLong)) return false;

        // 3. check if ending position is valid
        // to do with move generation of pieces
        // to do with checking for checkmate, checks, pins, etc
        long endingPositionLong = positionCoordinatesToBitboard(endingPosition);

        // 4. generate move
        long moveLong = startingPositonLong | endingPositionLong;

        // 5. perform move
        chessboard.performMove(pieceCode, moveLong);
        return true;
    }

    public static boolean verifyUserString(String userString) {
        if (!userString.matches(USER_STRING_PATTERN)) {
            System.out.println("The string input is not in the correct format");
            System.out.println("Please ensure it is in the form [piece code] [starting position] [ending position].");
            System.out.println("Accepted piece codes are k, q, r, n, b, p.");
            return false;
        }
        return true;
    }

    public static long positionCoordinatesToBitboard(String position) {
        int file = position.charAt(0) - 'a', rank = position.charAt(1) - '1';
        int idx = rank * 8 + file;
        return ((long) 1 << idx);
    }
}

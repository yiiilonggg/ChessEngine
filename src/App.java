import java.util.Scanner;

import Engine.Chessboard;
import Engine.MoveHandler;
import Tests.PCMBBTests;

public class App {
    private static void runTests() {
        PCMBBTests.testMagicBitboards();
    }

    private static void startGame() {
        Scanner sc = new Scanner(System.in);
        Chessboard chessboard = new Chessboard("");
        while (chessboard.getGameState()) {
            System.out.println("Please make your move: ");
            String userMoveString = sc.nextLine();
            if (MoveHandler.performUserMove(chessboard, userMoveString)) {
                System.out.println(chessboard.printBoard());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        startGame();
    }
}

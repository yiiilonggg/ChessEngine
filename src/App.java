import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        Chessboard chessboard = new Chessboard("");
        while (chessboard.getGameState()) {
            System.out.println("Please make your move: ");
            sc.next();
        }
    }
}

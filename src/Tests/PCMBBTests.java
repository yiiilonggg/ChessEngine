package Tests;

import java.util.*;
import Engine.PCMBB;

public class PCMBBTests {
    public static void testMagicBitboards() {
        long bishopTrial = PCMBB.createOccupancy(new ArrayList<>(List.of("g7", "f6", "c5", "b2", "g1")));
        PCMBB.printBoard(bishopTrial);
        PCMBB.printBoard(PCMBB.getBishopAttacks(PCMBB.positionCoordinatesToIndex("d4"), bishopTrial));

    }
}

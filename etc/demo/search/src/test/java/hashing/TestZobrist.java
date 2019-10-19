package hashing;

import org.junit.Test;

import domains.MNK;

import static org.junit.Assert.*;

public class TestZobrist {

    @Test
    public void testHashing() {
        ZobristMNK zobrist = new ZobristMNK(3, 3);
        MNK originalX = new MNK(3, 3, 3, MNK.X);
        MNK originalO = new MNK(3, 3, 3, MNK.O);

        assertEquals(0, zobrist.getHash(originalX));
        assertEquals(0, zobrist.getHash(originalO));

        int move1 = originalX.getMoves().get(1);
        int move2 = originalX.getMoves().get(2);
        int move3 = originalX.getMoves().get(3);

        MNK board1 = (MNK) originalX.makeMove(move1);
        MNK board2 = (MNK) board1.makeMove(move2);
        MNK board3 = (MNK) board2.makeMove(move3);

        MNK move1board = board1;
        MNK move2board = (MNK) originalO.makeMove(move2);
        MNK move3board = (MNK) originalX.makeMove(move3);

        assertEquals(zobrist.getHash(board3),
                zobrist.getHash(originalX) ^ zobrist.getHash(move1board) ^ zobrist.getHash(move2board) ^ zobrist.getHash(move3board));
    }
}

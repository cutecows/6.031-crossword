package crossword;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class WordTest {

    /**
     * Testing Partitions:
     *      getWord
     *      getHint
     *      getNum
     *      getRow
     *      getCol
     *      getPos
     *      getEnd
     *      getDir
     *      size
     *      validGuess
     *           true and false
     *      hashCode
     *      equals
     */
    static final Word WACROSS = new Word("hi", "a greeting", 1, new Position(1, 2), Board.Direction.ACROSS);
    static final Word WACROSS2 = new Word("hi", "a greeting", 1, new Position(1, 2), Board.Direction.ACROSS);
    static final Word WDOWN = new Word("hi", "a greeting", 1, new Position(1, 2), Board.Direction.DOWN);

    // this test covers correctness of getWord
    @Test
    public void getWordTest() {
        assertEquals("hi", WACROSS.getWord());
    }

    // this test covers correctness of getHint
    @Test
    public void getHintTest() {
        assertEquals("a greeting", WACROSS.getHint());
    }

    // this test covers correctness of getNum
    @Test
    public void getNumTest() {
        assertEquals(1, WACROSS.getNum());
    }

    // this test covers correctness of getRow
    @Test
    public void getRowTest() {
        assertEquals(1, WACROSS.getRow());
    }

    // this test covers correctness of getCol
    @Test
    public void getColTest() {
        assertEquals(2, WACROSS.getCol());
    }

    // this test covers correctness of getPos
    @Test
    public void getPosTest() {
        assertEquals(new Position(1,2), WACROSS.getPos());
    }

    // this test covers correctness of getEnd for ACROSS
    @Test
    public void getEndAcrossTest() {
        assertEquals(new Position(1,4), WACROSS.getEnd());
    }

    // this test covers correctness of of getEnd for DOWN
    @Test
    public void getEndDownTest() {
        assertEquals(new Position(3,2), WDOWN.getEnd());
    }

    // this test covers correctness of getDir
    @Test
    public void getDirTest() {
        assertEquals(Board.Direction.ACROSS, WACROSS.getDir());
        assertEquals(Board.Direction.DOWN, WDOWN.getDir());
    }

    // this test covers correctness of size
    @Test
    public void sizeTest() {
        assertEquals(2, WACROSS.size());
    }

    // this test covers correctness of validGuess
    @Test
    public void validGuessTest() {
        assert(WACROSS.validGuess("IH"));
    }
    
    // this test covers the case where something is not a valid test
    @Test
    public void invalidGuessTest() {
        assert(!WACROSS.validGuess("q"));
    }

    // this test covers correctness of hashCode and equals
    @Test
    public void equalityTest() {
        assert(WACROSS.equals(WACROSS2));
        assertEquals(WACROSS.hashCode(),WACROSS2.hashCode());
    }
}

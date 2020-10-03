package crossword;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PositionTest {

    /**
     * Testing Partitions:
     *      getRow
     *      getCol
     *      toString
     *      equals
     *      hashCode
     */
    
    // This covers the toString correctness test
    @Test
    public void toStringCorrect() {
        Position pos = new Position(1,2);
        assertEquals("(1,2)",pos.toString());
    }
    
    // This covers the getRow and getCol correctness test
    @Test
    public void getMethodsTest() {
        Position pos = new Position(1,2);
        assertEquals(1,pos.getRow());
        assertEquals(2,pos.getCol());
    }
    
    // This covers the equals and hashCode correctness test
    @Test
    public void equalityTest() {
        Position pos1 = new Position(1,2);
        Position pos2 = new Position(1,2);
        assert(pos1.equals(pos2));
        assertEquals(pos1.hashCode(),pos2.hashCode());
    }
}

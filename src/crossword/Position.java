package crossword;

/**
 * Immutable Position class to represent instances of positions within a crossword puzzle board
 *
 */
public class Position {
    private final int row, col;
    
    /**
     * Rep Invariant: row >= 0, col >= 0
     * AF(row, col) = represents the position in a crossword in row row and column col
     * Thread Safety: All fields are final and private (immutable).
     */
    
    /**
     * Constructor for the Position tcolpe
     * @param row the row coordinate of the position we are creating
     * @param col the col coordinate of the position we are creating
     */
    public Position (int row, int col) {
        this.row = row;
        this.col = col;
        assert(this.checkRep());
    }
    
    /**
     * @return true if this instance is a valid rep, false otherwise
     */
    private boolean checkRep() {
        return this.row >= 0 && this.col >= 0;
    }
    
    /**
     * @return the row coordinate of this position
     */
    public int getRow () {
        return this.row;
    }
    
    /**
     * @return the col coordinate of this position
     */
    public int getCol () {
        return this.col;
    }
    
    @Override
    public boolean equals(Object other) {
        return other instanceof Position && 
                ((Position)other).getRow() == this.getRow() && 
                ((Position)other).getCol() == this.getCol();
    }
    
    @Override
    public int hashCode() {
        return this.getRow() + this.getCol();
    }
    
    @Override
    public String toString() {
        return "(" + this.getRow() + "," + this.getCol() + ")";
    }
    
}

package crossword;

import crossword.Board.Direction;

/**
 * Immutable Word class for representing a specific word in a Crossword Puzzle
 *
 */
public class Word implements Crossword{
    /**
     * Rep Invariant: num > 0
     *        word can only contain word characters as defined by regular expressions
     *        word.length > 0
     *        hint.length > 0
     *        pos needs to be valid
     * AF(word, hint, number, pos, dir) = represents the word in the crossword with text word, the hint hint, the number number, the position pos, the direction dir
     * Thread Safety: All fields are private and final (immutable)
     */

    private final String word;
    private final String hint;
    private final int number;
    private final Position pos;
    private final Board.Direction dir;
    /**
     * Constructor for the Word type
     * @param word the string that this word represents
     * @param hint the hint associated with this word
     * @param number the number associated with the hint for this word
     * @param pos the position of the word in the board
     * @param dir whether this word is a horizontal word or vertical word
     */
    public Word (String word, String hint, int number, Position pos, Board.Direction dir) {
        this.word = word;
        this.hint = hint;
        this.number = number;
        this.pos = pos;
        this.dir = dir;
        assert(this.checkRep());
    }
    
   
    public String returnLetter(Position pos) {
        if (this.dir== Direction.ACROSS) {
            int index= pos.getCol()- this.pos.getCol();
            return word.substring(index, index+1);
        } else if (this.dir==Direction.DOWN){
            int index= pos.getRow()- this.pos.getRow();
            return word.substring(index, index+1);
        }else {
            throw new UnsupportedOperationException("unsupported");
        }
    }

    /**
     * @return true if this instance is valid and false otherwise
     */
    private boolean checkRep() {
        return this.word.matches("\\w+") &&
                this.word.length() > 0 &&
                this.number > 0;
    }

    /**
     * @return the word that this Word object represents
     */
    public String getWord() {
        return this.word;
    }
    /**
     * @return hint for client
     */
    
    public String getClientString() {
        return hint;
    }

    /**
     * @return the hint associated with this word
     */
    public String getHint() {
        return this.hint;
    }

    /**
     * @return the number associated with the hint for this word
     */
    public int getNum() {
        return this.number;
    }

    /**
     * @return the row coordinate of this word in the crossword
     */
    public int getRow() {
        return this.pos.getRow();
    }

    /**
     * @return the col coordinate of this word in the crossword
     */
    public int getCol() {
        return this.pos.getCol();
    }

    /**
     * @return the position of the beginning of the word on the board
     */
    public Position getPos() {
        return this.pos;
    }

    /**
     * @return the first position after the end of the word on the board
     */
    public Position getEnd() {
        return (this.dir == Board.Direction.ACROSS) ? 
                new Position(this.getRow(), this.getCol() + this.size()) :
                new Position(this.getRow() + this.size(), this.getCol());
    }

    /**
     * @return the direction of this word in the crossword
     */
    public Board.Direction getDir(){
        return this.dir;
    }

    /**
     * @return the size of the word this represents
     */
    public int size() {
        return this.word.length();
    }

    /**
     * a valid guess is determined by the two words being for the same number and having the same size
     * @param guess a String that is a guess for this word
     * @return if the other word is a valid guess
     */
    public boolean validGuess(String guess) {
        return this.size() == guess.length();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Word && 
                ((Word)other).getWord() == this.getWord() &&
                ((Word)other).getNum() == this.getNum() &&
                ((Word)other).getDir() == this.getDir() &&
                ((Word)other).getRow() == this.getRow() &&
                ((Word)other).getCol() == this.getCol();
    }

    @Override
    public int hashCode() {
        return this.getRow() + this.getCol() + this.getNum() + this.size();
    }
}

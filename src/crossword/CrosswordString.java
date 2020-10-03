package crossword;

/**
 * 
 * Variant of the crossword used to match the initial text in the crossword files when parsing.
 *
 */
public class CrosswordString implements Crossword {
    
    private final String text;
    
    /**
     * constructor for this variant of crossword
     * @param text the text that this object represents
     */
    public CrosswordString(String text) {
        this.text = text;
    }
    
    /**
     * @return the text this object represents
     */
    public String extractText() {
        return text;
    }
    
    @Override
    public String getClientString() {
        return text;
    }

}

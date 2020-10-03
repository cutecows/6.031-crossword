package crossword;
import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.mit.eecs.parserlib.ParseTree;
import java.util.*;
import edu.mit.eecs.parserlib.Parser;
import edu.mit.eecs.parserlib.UnableToParseException;

/**
 * Parser to create Crossword Puzzle Boards from files
 *
 */
public class CrosswordParser {
    
    
 // the nonterminals of the grammar
    public static enum CrosswordGrammar {
        FILE, ENTRY, NAME, DESCRIPTION, STRING, STRINGINDENT, WORDNAME, CLUE, DIRECTION, ROW, COL, INT, WHITESPACE, WHITESPACEEXCLUDINGNEWLINE, JAVACOMMENT;
    }
    
    private static Parser<CrosswordGrammar> parser = makeParser();
    
    private static Parser<CrosswordGrammar> makeParser() {
        try {
            // read the grammar as a file, relative to the project root.
            final File grammarFile = new File("src/crossword/Crossword.g");
            return Parser.compile(grammarFile, CrosswordGrammar.FILE);
            
        // Parser.compile() throws two checked exceptions.
        // Translate these checked exceptions into unchecked RuntimeExceptions,
        // because these failures indicate internal bugs rather than client errors
        } catch (IOException e) {
            throw new RuntimeException("can't read the grammar file", e);
        } catch (UnableToParseException e) {
            throw new RuntimeException("the grammar has a syntax error", e);
        }
    }
    
    /**
     * 
     * @param string to be parsed
     * @return the crosswordPuzzle parsed from string
     * @throws UnableToParseException in the case that the puzzle is mal-formatted
     */
    public static Crossword parse(final String string) throws UnableToParseException {
        final ParseTree<CrosswordGrammar> parseTree = parser.parse(string);
        //System.out.println("hi");

        // display the parse tree in various ways, for debugging only
        // System.out.println("parse tree " + parseTree);
        // Visualizer.showInBrowser(parseTree);

        // make an AST from the parse tree
        //Visualizer.showInBrowser(parseTree);
        final Crossword crossword = makeAbstractSyntaxTree(parseTree, 1);
        // System.out.println("AST " + expression);
        
        return crossword;
    }
    
    /**
     * parses a test puzzle
     * @param args default parameter to main method
     * @throws UnableToParseException puzzle is not formatted correctly
     */
    public static void main (String[] args) throws UnableToParseException{
        CrosswordParser.parse("puzzles/simple.puzzle");
    }
    
    private static Crossword makeAbstractSyntaxTree(final ParseTree<CrosswordGrammar> parseTree, int clueNumber) {
        switch(parseTree.name()) {
            case FILE: 
                {
                    final List<ParseTree<CrosswordGrammar>> children = parseTree.children();
                    CrosswordString name = (CrosswordString)makeAbstractSyntaxTree(children.get(0), clueNumber);
                    CrosswordString description = (CrosswordString)makeAbstractSyntaxTree(children.get(1), clueNumber);
                    List<Word> entries = new ArrayList<Word>();
                    for(int i = 2; i < children.size(); i++) {
                        entries.add((Word)(makeAbstractSyntaxTree(children.get(i), i-1)));
                    }
                    try {
                        System.out.println(entries);
                        return new Board(name.extractText(), description.extractText(), entries);
                    } catch (Exception e) {
                        // Auto-generated catch block
                        e.printStackTrace();
                        Boolean val = false;
                        assert(val); // board inconsistent TODO fix somehow
                    }
                }
            case NAME:
            {
                return new CrosswordString(parseTree.text());
            }
            case DESCRIPTION:
            {
                return new CrosswordString(parseTree.text());
            }
            case ENTRY:
            {
                final List<ParseTree<CrosswordGrammar>> children = parseTree.children();
                String name = children.get(0).text();
                String hint = children.get(1).text();
                String directionStr = children.get(2).text();
                Board.Direction direction = Board.Direction.DOWN;
                if(directionStr.equals("ACROSS")) {
                    direction = Board.Direction.ACROSS;
                }
                int row = Integer.valueOf(children.get(3).text());
                int col = Integer.valueOf(children.get(4).text());
                Position position = new Position(row, col);
                Word word =  new Word(name, hint, clueNumber, position, direction);
                return word;
                
            }
            default:
            {
                throw new AssertionError("should never get here");
            }            
        }
    }

}

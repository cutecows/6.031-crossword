package crossword;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import edu.mit.eecs.parserlib.UnableToParseException;




public class CrosswordParserTest {
    /**
     * Testing Partitions:
     *      valid crossword
     *      invalid crossword
     */
    @Test
    public void testSimple() {
        try {
            File simplePuzzle = new File("puzzles/simple.puzzle");
            String puzzleStr = Files.readString(simplePuzzle.toPath());
            //System.out.println(puzzleStr);
            Crossword cw = CrosswordParser.parse(puzzleStr);
            Board board = (Board)cw;
            System.out.println(board.getCellString());
        } catch (IOException e) {
            System.out.println("couldn't read file");
        } catch (UnableToParseException e) {
            e.printStackTrace();;
        }
    }
    
    @Test
    public void testInconsistent() {
        Boolean val = false;
        try {
            File puzzle = new File("puzzles/inconsistent.puzzle");
            String puzzleStr = Files.readString(puzzle.toPath());
            //System.out.println(puzzleStr);
            Crossword cw = CrosswordParser.parse(puzzleStr);
            Board board = (Board)cw;
            //System.out.println(board.getCharacterString());
        } catch (IOException e) {
            System.out.println("couldn't read file");
        } catch (UnableToParseException e) {
            e.printStackTrace();;
        } catch (Exception e) {
            val = true;
        }
        assert(val); // will only be true of the correct assertion is thrown
    }
}

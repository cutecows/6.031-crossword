package crossword;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import edu.mit.eecs.parserlib.UnableToParseException;
import static org.junit.jupiter.api.Assertions.*;
public class BoardTest {

    /**
     * test partitions:
     *     getClientString- comprised of below four strings
     *     cellString- never changes
     *     characterString
     *     numString-never changes
     *     hints-never changes
     *     tryWord
     *        only has effect on character string if at all
     *        valid guess
     *             one intersection
     *             multiple intersections
     *        invalid guess
     *             by length
     *     challengeWord
     *        only has effect on character string if at all
     *        valid challenge
     *             original word correct
     *             new word correct
     *             neither correct
     *        invalid challenge
     *             by length
     *             by intersection
     *             by same word
     *     getScore
     *        after correct challenge
     *       after incorrect challenge, where existing word was incorrect
     *       after incorrect challenge, where existing word was correct
     *     addPlayer
     *     -- string is >1, string is length 1
     */
    
    // This test covers the case that multiple unconfirmed words can be removed by a try request
    @Test
    public void testMultipleRemove() {
        try {
            File simplePuzzle = new File("puzzles/threeWords.puzzle");
            String puzzleStr = Files.readString(simplePuzzle.toPath());
            Crossword cw = CrosswordParser.parse(puzzleStr);
            Board board = (Board)cw;
            cw = CrosswordParser.parse(puzzleStr);
            Board board2 = (Board)cw;
            
            board.addPlayer("Test");
            board.addPlayer("Test2");
            board2.addPlayer("Test");
            board2.addPlayer("Test2");
            board.tryWord(1, "bob", "Test");
            
            board2.tryWord(2, "cat", "Test");
            board2.tryWord(4, "tab", "Test");
            
            assertEquals(board.getCellString(), board2.getCellString());
            assertTrue(board.getNumString().equals(board2.getNumString()));
            
            board2.tryWord(1, "bob", "Test");
            
            assertEquals(board.getCharacterString(), board2.getCharacterString());
            
        } catch (IOException e) {
            System.out.println("couldn't read file");
        } catch (UnableToParseException e) {
            e.printStackTrace();
        }
    }
    
  //This test case covers the partitions for getScore
    @Test
        public void score() throws IOException, UnableToParseException{
            File simplePuzzle = new File("puzzles/threeWords.puzzle");
            String puzzleStr = Files.readString(simplePuzzle.toPath());
            Crossword cw = CrosswordParser.parse(puzzleStr);
            Board board = (Board)cw;
            board.addPlayer("Test");
            board.addPlayer("Test2");
            board.tryWord(1, "cat", "Test");
            board.challengeWord(1, "bye", "Test2");
            assert board.getScore("Test2")==-1; //incorrect challenge, existing word right
            board.tryWord(2, "bat", "Test");
            board.challengeWord(2, "mat", "Test2");
            assert board.getScore("Test2")==1; //correct challenge
            board.tryWord(4, "hax", "Test");
            board.challengeWord(4, "bax", "Test2");
            assert board.getScore("Test2")==0;
            
        }

    // This test covers the case that confirmed words should be immutable by challenge
    @Test
    public void testChallengeImm() {
        try {
            File simplePuzzle = new File("puzzles/twoWords.puzzle");
            String puzzleStr = Files.readString(simplePuzzle.toPath());
            Crossword cw = CrosswordParser.parse(puzzleStr);
            Board board = (Board)cw;
            cw = CrosswordParser.parse(puzzleStr);
            Board board2 = (Board)cw;
            board.addPlayer("Test");
            board.addPlayer("Test2");
            board2.addPlayer("Test");
            board2.addPlayer("Test2");
            board.tryWord(1, "test", "Test");
            board2.tryWord(1, "test", "Test");
            
            assertEquals(board.getCellString(), board2.getCellString());
            assertTrue(board.getNumString().equals(board2.getNumString()));
            
            board2.challengeWord(1, "bugs", "Test2"); // Confirmed Word 1
            
            board2.tryWord(2, "save", "Test"); // Add incorrect but consistent word
            
            board2.challengeWord(2, "sack", "Test2"); // challenge with wrong word
            
            assertEquals(board.getCharacterString(), board2.getCharacterString());
            
        } catch (IOException e) {
            System.out.println("couldn't read file");
        } catch (UnableToParseException e) {
            e.printStackTrace();
        }
    }
    
    
    private boolean isDone(String ans) {
         return ans.contains("DONE") | ans.contains("INCOMPLETE");
    }
            
           // This test covers the board sending a message when it is complete
           @Test
            public void puzzleDone() {
                try {
                    File simplePuzzle = new File("puzzles/threeWords.puzzle");
                    String puzzleStr = Files.readString(simplePuzzle.toPath());
                    Crossword cw = CrosswordParser.parse(puzzleStr);
                    Board board = (Board)cw;
                    board.addPlayer("Test");
                    board.addPlayer("Test2");
                    assert isDone(board.tryWord(1, "cat", "Test"));
                    assert isDone(board.tryWord(2, "mat", "Test"));
                    assert isDone(board.challengeWord(2, "gat", "Test2"));
                    assert isDone(board.tryWord(4, "tax", "Test"));
                    assert isDone(board.challengeWord(4, "tap", "Test2"));
                    assert board.tryWord(3, "car", "Test2").contains("DONE");
                    
        
                } catch (IOException e) {
                    System.out.println("couldn't read file");
                } catch (UnableToParseException e) {
                    e.printStackTrace();
                }
           }
                
                
              
    // This test covers the case that confirmed words should be immutable by try
    @Test
    public void testTryImm() {
        try {
            File simplePuzzle = new File("puzzles/twoWords.puzzle");
            String puzzleStr = Files.readString(simplePuzzle.toPath());
            Crossword cw = CrosswordParser.parse(puzzleStr);
            Board board = (Board)cw;
            cw = CrosswordParser.parse(puzzleStr);
            Board board2 = (Board)cw;
            board.addPlayer("Test");
            board.addPlayer("Test2");
            board2.addPlayer("Test");
            board2.addPlayer("Test2");
            board.tryWord(1, "test", "Test");
            board2.tryWord(1, "test", "Test");
            
            assertEquals(board.getCellString(), board2.getCellString());
            assertTrue(board.getNumString().equals(board2.getNumString()));
            
            board2.challengeWord(1, "bugs", "Test2");
            assertEquals(board.getCharacterString(), board2.getCharacterString());
            
            board2.tryWord(1, "bugs", "Test2");
            assertEquals(board.getCharacterString(), board2.getCharacterString());
            
            board2.tryWord(2, "bugs", "Test2");
            assertEquals(board.getCharacterString(), board2.getCharacterString());
            
        } catch (IOException e) {
            System.out.println("couldn't read file");
        } catch (UnableToParseException e) {
            e.printStackTrace();
        }
    }
    
    // This test covers the challenge case of a correct word with an incorrect word
    //         and the resulting changes to scores
    @Test
    public void testChallengeCorIncor() {
        try {
            File simplePuzzle = new File("puzzles/twoWords.puzzle");
            String puzzleStr = Files.readString(simplePuzzle.toPath());
            Crossword cw = CrosswordParser.parse(puzzleStr);
            Board board = (Board)cw;
            cw = CrosswordParser.parse(puzzleStr);
            Board board2 = (Board)cw;
            board.addPlayer("Test");
            board.addPlayer("Test2");
            board2.addPlayer("Test");
            board2.addPlayer("Test2");
            board.tryWord(1, "test", "Test");
            board2.tryWord(1, "test", "Test");
            
            assertEquals(board.getCellString(), board2.getCellString());
            assertTrue(board.getNumString().equals(board2.getNumString()));
            
            board2.challengeWord(1, "bugs", "Test2");
            assertEquals(board.getCharacterString(), board2.getCharacterString());
            assertEquals(0, board2.getScore("Test"));
            assertEquals(-1, board2.getScore("Test2"));
        } catch (IOException e) {
            System.out.println("couldn't read file");
        } catch (UnableToParseException e) {
            e.printStackTrace();
        }
    }
    
    // This test covers the challenge case of an incorrect word with a correct word
    @Test
    public void testChallengeIncorCor() {
        try {
            File simplePuzzle = new File("puzzles/twoWords.puzzle");
            String puzzleStr = Files.readString(simplePuzzle.toPath());
            Crossword cw = CrosswordParser.parse(puzzleStr);
            Board board = (Board)cw;
            cw = CrosswordParser.parse(puzzleStr);
            Board board2 = (Board)cw;
            board.addPlayer("Test");
            board.addPlayer("Test2");
            board2.addPlayer("Test");
            board2.addPlayer("Test2");
            board.tryWord(1, "test", "Test");
            board2.tryWord(1, "bugs", "Test");
            
            assertEquals(board.getCellString(), board2.getCellString());
            assertTrue(board.getNumString().equals(board2.getNumString()));
            
            board2.challengeWord(1, "test", "Test2");
            assertEquals(board.getCharacterString(), board2.getCharacterString());
            assertEquals(0, board2.getScore("Test"));
            assertEquals(2, board2.getScore("Test2"));
        } catch (IOException e) {
            System.out.println("couldn't read file");
        } catch (UnableToParseException e) {
            e.printStackTrace();
        }
    }
    
    // This test covers the challenge case of an incorrect word with a correct word
    @Test
    public void testChallengeIncorIncor() {
        try {
            File simplePuzzle = new File("puzzles/twoWords.puzzle");
            String puzzleStr = Files.readString(simplePuzzle.toPath());
            Crossword cw = CrosswordParser.parse(puzzleStr);
            Board board = (Board)cw;
            cw = CrosswordParser.parse(puzzleStr);
            Board board2 = (Board)cw;
            board.addPlayer("Test");
            board.addPlayer("Test2");
            board2.addPlayer("Test");
            board2.addPlayer("Test2");
            board2.tryWord(1, "bugs", "Test");
            
            assertEquals(board.getCellString(), board2.getCellString());
            assertTrue(board.getNumString().equals(board2.getNumString()));
            
            board2.challengeWord(1, "save", "Test2");
            assertEquals(board.getCharacterString(), board2.getCharacterString());
            assertEquals(0, board2.getScore("Test"));
            assertEquals(-1, board2.getScore("Test2"));
        } catch (IOException e) {
            System.out.println("couldn't read file");
        } catch (UnableToParseException e) {
            e.printStackTrace();
        }
    }
    
    // This test covers guessing a word with intersection, (original word is correct but unconfirmed)
    @Test
    public void testTrySuccessIntersect() {
        try {
            File simplePuzzle = new File("puzzles/twoWords.puzzle");
            String puzzleStr = Files.readString(simplePuzzle.toPath());
            Crossword cw = CrosswordParser.parse(puzzleStr);
            Board board = (Board)cw;
            cw = CrosswordParser.parse(puzzleStr);
            Board board2 = (Board)cw;
            board2.addPlayer("Test");
            board.addPlayer("Test");
            board2.tryWord(1, "test", "Test");
            assertEquals(board.getCellString(), board2.getCellString());
            assertTrue(board.getNumString().equals(board2.getNumString()));
            
            board.tryWord(2, "bugs", "Test");
            board2.tryWord(2, "bugs", "Test");
            assertEquals(board.getCharacterString(), board2.getCharacterString());
            
        } catch (IOException e) {
            System.out.println("couldn't read file");
        } catch (UnableToParseException e) {
            e.printStackTrace();
        }
    }
    
    
    // This test covers the successful tryWord case and that this only changes character string
    //        and the single word success case
    @Test
    public void testTrySuccess() {
        try {
            File simplePuzzle = new File("puzzles/oneWord.puzzle");
            String puzzleStr = Files.readString(simplePuzzle.toPath());
            Crossword cw = CrosswordParser.parse(puzzleStr);
            Board board = (Board)cw;
            cw = CrosswordParser.parse(puzzleStr);
            Board board2 = (Board)cw;
            board2.addPlayer("Test");
            board2.tryWord(1, "test", "Test");
            assertEquals(board.getCellString(), board2.getCellString());
            assertEquals(board.getNumString(), board2.getNumString());
            assert(!board.getCharacterString().equals(board2.getCharacterString()));
            
        } catch (IOException e) {
            System.out.println("couldn't read file");
        } catch (UnableToParseException e) {
            e.printStackTrace();
        }
    }
    
    // This test covers the successful tryWord case with an incorrect word
    @Test
    public void testTrySuccessWrongWord() {
        try {
            File simplePuzzle = new File("puzzles/oneWord.puzzle");
            String puzzleStr = Files.readString(simplePuzzle.toPath());
            Crossword cw = CrosswordParser.parse(puzzleStr);
            Board board = (Board)cw;
            cw = CrosswordParser.parse(puzzleStr);
            Board board2 = (Board)cw;
            board2.addPlayer("Test");
            board2.tryWord(1, "base", "Test");
            assertEquals(board.getCellString(), board2.getCellString());
            assertEquals(board.getNumString(), board2.getNumString());
            assert(!board.getCharacterString().equals(board2.getCharacterString()));
            
        } catch (IOException e) {
            System.out.println("couldn't read file");
        } catch (UnableToParseException e) {
            e.printStackTrace();
        }
    }
    
    // This test covers the unsuccessful tryWord case and that it has no effect on any strings
    //         and the case that try should fail by length
    @Test
    public void testTryFailLength() {
        try {
            File simplePuzzle = new File("puzzles/oneWord.puzzle");
            String puzzleStr = Files.readString(simplePuzzle.toPath());
            Crossword cw = CrosswordParser.parse(puzzleStr);
            Board board = (Board)cw;
            cw = CrosswordParser.parse(puzzleStr);
            Board board2 = (Board)cw;
            board2.addPlayer("Test");
            board2.tryWord(1, "veryLongWordToForceAFailureByLength", "Test");
            assertEquals(board.getCellString(), board2.getCellString());
            assertEquals(board.getNumString(), board2.getNumString());
            assertEquals(board.getCharacterString(), board2.getCharacterString());
            
        } catch (IOException e) {
            System.out.println("couldn't read file");
        } catch (UnableToParseException e) {
            e.printStackTrace();
        }
    }
    
    // This covers the accuracy of the cell string
    @Test
    public void testCells() {
        try {
            File simplePuzzle = new File("puzzles/simple.puzzle");
            String puzzleStr = Files.readString(simplePuzzle.toPath());
            Crossword cw = CrosswordParser.parse(puzzleStr);
            Board board = (Board)cw;
            assertEquals("Cells # (1,0) # (1,1) # (1,2) # (1,3) # (0,2) # (1,2) # (2,2) # (3,2) # (4,2) # (5,2) # (3,2) # (3,3) # (3,4) # (3,5) # (3,6) # (3,7) # (1,5) # (2,5) # (3,5) # (4,5) # (5,5) # (4,0) # (4,1) # (4,2) # (5,2) # (5,3) # (5,4) # (5,5) # (5,6) # (5,7) # (5,8) # (5,9) # (4,4) # (4,5) # (4,6) # (4,7) # (4,8) # (3,6) # (4,6) # (5,6) # (6,6) ",
                    board.getCellString());
        } catch (IOException e) {
            System.out.println("couldn't read file");
        } catch (UnableToParseException e) {
            e.printStackTrace();
        }
    }
    
    // This covers the accuracy of the character string
    @Test
    public void testCharacters() {
        try {
            File simplePuzzle = new File("puzzles/simple.puzzle");
            String puzzleStr = Files.readString(simplePuzzle.toPath());
            Crossword cw = CrosswordParser.parse(puzzleStr);
            Board board = (Board)cw;
            assertEquals("Characters ",board.getCharacterString());
        } catch (IOException e) {
            System.out.println("couldn't read file");
        } catch (UnableToParseException e) {
            e.printStackTrace();
        }
    }
    
    // This covers the accuracy of the num string
    @Test
    public void testNums() {
        try {
            File simplePuzzle = new File("puzzles/simple.puzzle");
            String puzzleStr = Files.readString(simplePuzzle.toPath());
            Crossword cw = CrosswordParser.parse(puzzleStr);
            Board board = (Board)cw;
            assertEquals("Numbers num1DirACROSSCoor (1,0) num2DirDOWNCoor (0,2) num3DirACROSSCoor (3,2) num4DirDOWNCoor (1,5) num5DirACROSSCoor (4,0) num6DirACROSSCoor (5,2) num7DirACROSSCoor (4,4) num8DirDOWNCoor (3,6) ",
                    board.getNumString());
        } catch (IOException e) {
            System.out.println("couldn't read file");
        } catch (UnableToParseException e) {
            e.printStackTrace();
        }
    }
    
    // This covers the accuracy of the hints string
    @Test
    public void testHints() {
        try {
            File simplePuzzle = new File("puzzles/simple.puzzle");
            String puzzleStr = Files.readString(simplePuzzle.toPath());
            Crossword cw = CrosswordParser.parse(puzzleStr);
            Board board = (Board)cw;
            assertEquals("Hints 1 ACROSS \"twinkle twinkle\" 2 DOWN \"Farmers ______\" 3 ACROSS \"It's tea time!\" 4 DOWN \"more\" 5 ACROSS \"Everyone loves honey\" 6 ACROSS \"Every pirate's dream\" 7 ACROSS \"Everyone's favorite twitter pastime\" 8 DOWN \"This is not a gain\" ",
                    board.getHints());
        } catch (IOException e) {
            System.out.println("couldn't read file");
        } catch (UnableToParseException e) {
            e.printStackTrace();
        }
    }
    
    // This covers the accuracy of the client string
    @Test
    public void testClient() {
        try {
            File simplePuzzle = new File("puzzles/simple.puzzle");
            String puzzleStr = Files.readString(simplePuzzle.toPath());
            Crossword cw = CrosswordParser.parse(puzzleStr);
            Board board = (Board)cw;
            assertEquals("\"Easy\" | \"An easy puzzle to get started\" | Hints 1 ACROSS \"twinkle twinkle\" 2 DOWN \"Farmers ______\" 3 ACROSS \"It's tea time!\" 4 DOWN \"more\" 5 ACROSS \"Everyone loves honey\" 6 ACROSS \"Every pirate's dream\" 7 ACROSS \"Everyone's favorite twitter pastime\" 8 DOWN \"This is not a gain\" |Numbers num1DirACROSSCoor (1,0) num2DirDOWNCoor (0,2) num3DirACROSSCoor (3,2) num4DirDOWNCoor (1,5) num5DirACROSSCoor (4,0) num6DirACROSSCoor (5,2) num7DirACROSSCoor (4,4) num8DirDOWNCoor (3,6) |Cells # (1,0) # (1,1) # (1,2) # (1,3) # (0,2) # (1,2) # (2,2) # (3,2) # (4,2) # (5,2) # (3,2) # (3,3) # (3,4) # (3,5) # (3,6) # (3,7) # (1,5) # (2,5) # (3,5) # (4,5) # (5,5) # (4,0) # (4,1) # (4,2) # (5,2) # (5,3) # (5,4) # (5,5) # (5,6) # (5,7) # (5,8) # (5,9) # (4,4) # (4,5) # (4,6) # (4,7) # (4,8) # (3,6) # (4,6) # (5,6) # (6,6) |Characters ",
                    board.getClientString());
        } catch (IOException e) {
            System.out.println("couldn't read file");
        } catch (UnableToParseException e) {
            e.printStackTrace();
        }
    }
}

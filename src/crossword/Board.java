package crossword;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.mit.eecs.parserlib.UnableToParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;



/**
 * Threadsafe Mutable class to represent a board on which to play crossword puzzles
 *     Variant of the Crossword interface used for parsing crossword files.
 *
 */
public class Board implements Crossword{

    /**
     * AF(answers, board) = the crossword puzzle that has the answers, hints, and numbering as defined by each word in answers
     *      and the current state as defined by board (the character at board[i][j] is at row i col j on the crossword)
     * Rep Invariant:
     *      rows, cols > 0
     *      answers.size > 0
     *      hints.size = answers.size
     *      board.size = rows
     *      board[i].size = cols for all i < rows
     *      allCells.size > answers.size
     *      players <= 2
     *      posToWordDown.keys().equals(posToWordDown.keys())
     *      posToWordDown.values() consists of distinct elements
     *      posToWordAcross.values().equals(posToWordAcross.values())
     *      posToWordAcross.values() consists of distinct elements
     *      userGuess.keys().equals(answers.keys())
     *      userGuess.values().equals(playerScores.keys())
     *      blockOwn.keys().equals(new HashSet(allCells))
     *      All sets in blockOwn.values() have length < 3
     * Thread Safety Argument:
     *      The only functions permitted to modify the board are synchronized on the board or are only called within a different synchronized function
     *              and thus the board can only be modified by one thread at a time.
     * Rep Exposure:
     *      All data taken in is immutable except for in the constructor, 
     *              but the mutable list is not saved and rather we extract the immutable data inside.
     *
     **/
    public enum Direction {ACROSS, DOWN, UNASSIGNED}
    public enum State {CONFIRMED, CORRECT, INCORRECT, UNASSIGNED}
    private final String name;
    private final String description;
    private final Map<Position, Word> posToWordAcross;
    private final Map<Position,Word> posToWordDown;
    private Map<Integer, Word> answers;
    private Map<Integer, String> hints;
    private Map<Integer, String> userGuess;
    private List<Word> confirmedWord;
    private List<List<Character>> board = new ArrayList<>();
    private List<Position> allCells = new ArrayList<>();
    private  Map<String, Integer> playerScores = new HashMap<>();
    private Set<String> players = new HashSet<>();
    private Map<Position, Set<Word>> blockOwn = new HashMap<Position, Set<Word>>();
    private int rows, cols;

    /**
     * Constructor for the board class
     * @param name the name of the game
     * @param description the description of the theme
     * @param allWords the list of words to be placed on the board
     * @throws Exception if the board is inconsistent
     */
    public Board(String name, String description, List<Word> allWords) throws Exception {
        this.name = name;
        this.description = description;
        this.answers = new HashMap<>();
        this.hints = new HashMap<>();
        this.userGuess= new HashMap<Integer,String>();
        this.confirmedWord= new ArrayList<Word>();
        int rowMax = 0, colMax = 0;
 
        for (Word elt : allWords) {
            this.answers.put(elt.getNum(), elt);
            this.hints.put(elt.getNum(), elt.getHint());

            for (int i = 0; i < elt.size(); i++) {
                if (elt.getDir() == Direction.DOWN) {
                    Position newPos = new Position(elt.getRow() + i,elt.getCol());
                    allCells.add(newPos);
                    blockOwn.put(newPos, new HashSet<Word>());
                } else if (elt.getDir() == Direction.ACROSS) {
                    Position newPos = new Position(elt.getRow(),elt.getCol() + i);
                    allCells.add(newPos);
                    blockOwn.put(newPos, new HashSet<Word>());
                }
            }

            if (elt.getEnd().getRow() > rowMax) {
                rowMax = elt.getEnd().getRow();
            }
            if (elt.getEnd().getCol() > colMax) {
                colMax = elt.getEnd().getCol();
            }

        }

        this.rows = rowMax + 1;
        this.cols = colMax + 1;

        for (int i = 0; i < rows; i++) {
            List<Character> newRow = new ArrayList<>();
            for (int j = 0; j < cols; j++) {
                newRow.add('#');
            }
            this.board.add(newRow);
        }
        List<Map<Position, Word>> dictList= returnPosWordDict();
        this.posToWordAcross= dictList.get(0);
        this.posToWordDown=dictList.get(1);
        if (!this.isConsistent()) throw new Exception("Inconsistent Puzzle");
        assert(this.checkRep());
    }

    /**
     * @return true if this instance is valid according to the rep invariant
     */
    private boolean checkRep() {
        /*
         *      rows, cols > 0
         *      answers.size > 0
         *      hints.size = answers.size
         *      board.size = rows
         *      board[i].size = cols for all i < rows
         *      allcells > answers.size
         *      players <= 2
         */

        boolean rowSize = true;
        for (List<Character> list : this.board) {
            if (list.size() != this.cols) rowSize = false;
        }
        boolean boardSize = this.rows > 0 && this.cols > 0 && this.board.size() == this.rows && rowSize;
        boolean wordsSize = this.hints.size() > 0 && this.hints.size() == this.answers.size() && this.allCells.size() > this.answers.size();
        return boardSize && wordsSize && this.playerScores.keySet().size() <= 2;
    }

    /**
     * @return true if this instance of crossword is consistent by the definition in the handout.
     *        (if words overlap perpendicularly the location that they overlap must share the same letter in each word,
     *         words should not overlap in the same direction)
     */
    private boolean isConsistent() {
        List<List<Character>> testBoard = new ArrayList<>();
        for (int i = 0; i < this.rows; i++) {
            List<Character> newRow = new ArrayList<>();
            for (int j = 0; j < this.cols; j++) {
                newRow.add('#');
            }
            testBoard.add(newRow);
        }

        List<Word> acrossWords = new ArrayList<>();
        List<Word> downWords = new ArrayList<>();
        for (Word elt: this.answers.values()) {
            String word = elt.getWord();
            Direction dir = elt.getDir();
            
            if (dir == Direction.ACROSS) {
                if (sameDirOverlap(elt, acrossWords)){
                    return false;
                } else {
                    acrossWords.add(elt);
                }
            }
            else if (dir == Direction.DOWN) {
                if (sameDirOverlap(elt, downWords)){
                    return false;
                } else {
                    downWords.add(elt);
                }
            }
            
            for (int i = 0; i<elt.size(); i++) {
                if (dir == Direction.ACROSS) {
                    // add to col
                    if (testBoard.get(elt.getRow()).get(elt.getCol() + i) == '#') {
                        testBoard.get(elt.getRow()).set(elt.getCol()+i, word.charAt(i));
                    } else if (testBoard.get(elt.getRow()).get(elt.getCol() + i) != word.charAt(i)) {
                        return false;
                    }

                } else if (dir == Direction.DOWN) {
                    // add to row
                    if (testBoard.get(elt.getRow() + i).get(elt.getCol()) == '#') {
                        testBoard.get(elt.getRow() + i).set(elt.getCol(), word.charAt(i));
                    } else if (testBoard.get(elt.getRow() + i).get(elt.getCol()) != word.charAt(i)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    /**
     * Modifies blockOwn so that each position occupied by answer contains 
     * answer in its values
     * @param answer Word with the same wordID as the guess
     * 
     */
    private void updateBlockOwn(Word answer) {
        Position start= answer.getPos();
        Position end= answer.getEnd();
        if (answer.getDir()==Direction.ACROSS) {
            for (int i=start.getCol(); i<end.getCol(); i++) {
                Position query= new Position(start.getRow(),i);
                Set<Word> currentList= this.blockOwn.get(query);
                currentList.add(answer);
                this.blockOwn.put(query, currentList);
            }
        }
        else if (answer.getDir()==Direction.DOWN) {
            for (int i=start.getRow(); i<end.getRow(); i++) {
                Position query= new Position(i,start.getCol());
                Set<Word> currentList= this.blockOwn.get(query);
                currentList.add(answer);
                this.blockOwn.put(query, currentList);
                
                
            }
        }

    }
    /**
     * Modifies blockOwn so that none of its values contain answer
     * @param answer Word with the same wordID as the guess
     * 
     */
    private void removeGuess(Word answer) {
        for (Position i: blockOwn.keySet()) {
            blockOwn.get(i).remove(answer);
        }
    }
    /**
     * Updates board after guesses are removed, and sets spaces to be blank
     * in accordance with pset logic
     * 
     */
    private synchronized void updateBoard() {
        for (Position i: blockOwn.keySet()) {
            if (blockOwn.get(i).isEmpty()) {
                board.get(i.getRow()).set(i.getCol(), '#');
            }
        }
         List<Integer> removal= new ArrayList<Integer>();
         for (Integer i: this.userGuess.keySet()) {
                    String ans=this.returnGuess(this.answers.get(i));
                    if (ans.contains("#")) {
                        removal.add(i);
                    }
        
         }
          for (Integer i: removal) {
                    userGuess.remove(i);
                }

    }
    
    /**
         * Returns string with all information client needs for live player updates
         * @return String containing confirmed words, player scores, and player ids of guesses
         */
        public String tryOrChallengeString() {
            String playerPoints=this.playerScores.toString();
            String words="";
            for (Word i: this.confirmedWord) {
                words=words+i.getNum()+" ";
            }
            String guesses="";
            for (Integer i: this.userGuess.keySet()) {
                guesses=guesses+i+":" +this.userGuess.get(i)+" ";
            }
            return "%"+"Confirmed"+words+ "&" + "PlayerPoints:"+playerPoints + "&"+ guesses;
        }
        
        
    /**
     * Returns a list of words that the guess conflicts with
     * @param wordIndex wordID
     * @param guess the guess
     * @return list of word conflicts. For each word on the board the guess conflicts with, 
     * that word's answer Word object is part of the list.
     * 
     */
    private List<Word> returnConflict(Integer wordIndex, String guess){
        List<Word> finalList= new ArrayList<Word>();
        Word answer= this.answers.get(wordIndex);
        Position start= answer.getPos();
        Position end = answer.getEnd();
        if (answer.getDir()==Direction.ACROSS) {
            for (int i=start.getCol(); i<end.getCol(); i++) {
                Position query= new Position(start.getRow(),i);
                Word conflict= posToWordDown.get(query);
                int indexLetter= i- start.getCol();
                String guessLetter= guess.substring(indexLetter,indexLetter+1);
                if (!(conflict==null)) {
                if (!conflict.returnLetter(query).equals(guessLetter)) {
                    finalList.add(conflict);
                }
                }
            }
        }
        else if (answer.getDir()==Direction.DOWN) {
            for (int i=start.getRow(); i<end.getRow(); i++) {
                Position query= new Position(i,start.getCol());
                Word conflict= posToWordAcross.get(query);
                int indexLetter= i - start.getRow();
                String guessLetter= guess.substring(indexLetter, indexLetter+1);
                if (!(conflict==null)) {
                if (! conflict.returnLetter(query).equals(guessLetter)) {
                    finalList.add(conflict);
                }
                }
                
                
            }
        }
        return finalList;
        
        
        
    }
    
    /**
     * Returns a list of dictionaries that map a position to the word occupied by that position
     * @return list of dictionaries. First element of the list maps each position to the horizontal word occupied
     * by that position. Second element of the list maps each position to the vertical word occupied by that position
     * 
     */
    private List<Map<Position, Word>> returnPosWordDict() {
//        List<List<Character>> testBoard = new ArrayList<>();
//        for (int i = 0; i < this.rows; i++) {
//            List<Character> newRow = new ArrayList<>();
//            for (int j = 0; j < this.cols; j++) {
//                newRow.add('#');
//            }
//            testBoard.add(newRow);
//        }
        
        Map<Position,Word> posToWordAcross= new HashMap<Position,Word>();
        Map<Position,Word> posToWordDown= new HashMap<Position,Word>();
        
        for (Word elt: this.answers.values()) {
            Direction dir = elt.getDir();
            for (int i = 0; i<elt.size(); i++) {
                if (dir == Direction.ACROSS) {
                    posToWordAcross.put(new Position(elt.getRow(), elt.getCol()+i), elt);
                } else if (dir == Direction.DOWN) {
                    posToWordDown.put(new Position(elt.getRow()+i, elt.getCol()), elt);

                }
            }
        }
        List<Map<Position,Word>> ansList= new ArrayList<Map<Position, Word>>();
        ansList.add(posToWordAcross);
        ansList.add(posToWordDown);
        return ansList;
    }

    /**
     * @param elt the word to be checked
     * @param others the list of other words in the same direction
     * @return true if the new word overlaps with the previous words provided
     */
    private static boolean sameDirOverlap(Word elt, List<Word> others) {
        int eltStart = 0, eltEnd = 0;
        int eltVal = 0;
        Direction dir = elt.getDir();

        if (dir == Direction.ACROSS) {
            eltStart = elt.getCol();
            eltEnd = elt.getEnd().getCol()-1;
            eltVal = elt.getRow();
        } else if (dir == Direction.DOWN) {
            eltStart = elt.getRow();
            eltEnd = elt.getEnd().getRow()-1;
            eltVal = elt.getCol();
        }

        for(Word other : others) {
            int otherStart = 0, otherEnd = 0;
            int otherVal = 0;
            if (dir == Direction.ACROSS) {
                otherStart = other.getCol();
                otherEnd = other.getEnd().getCol()-1;
                otherVal = other.getRow();
            } else if (dir == Direction.DOWN) {
                otherStart = other.getRow();
                otherEnd = other.getEnd().getRow()-1;
                otherVal = other.getCol();
            }
            if (eltVal == otherVal &&
                    ((otherStart <= eltStart && eltStart <= otherEnd) ||
                    (eltStart <= otherStart && otherStart <= eltEnd))) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return a string of the numbers and where they are on the board to be displayed
     */
    public String getNumString() {
        StringBuilder str = new StringBuilder();
        str.append("Numbers ");
        for (Word elt : answers.values()) {
            String dir = (elt.getDir() == Board.Direction.ACROSS)? "ACROSS": "DOWN";
            str.append("num" + elt.getNum() + "Dir" + dir +"Coor"+ " (" + elt.getRow() + "," + elt.getCol() + ") ");
        }
        return str.toString();
    }

    /**
     * @return a string of all the cells on the board and where they are to be displayed
     */
    public String getCellString() {
        StringBuilder str = new StringBuilder();
        str.append("Cells ");
        for (Position pos : allCells) {
            str.append("#" + " (" + pos.getRow() + "," + pos.getCol() + ") ");
        }
        return str.toString();
    }

    /**
     * @return a string of all of the characters on the board and where they are to be displayed
     */
    public String getCharacterString() {
        StringBuilder str = new StringBuilder();
        str.append("Characters ");
        for (Position pos : allCells) {
            if (this.board.get(pos.getRow()).get(pos.getCol()) != '#') {
                str.append("char" +this.board.get(pos.getRow()).get(pos.getCol()) + " (" + pos.getRow() + "," + pos.getCol() + ") ");
            }
        }
        return str.toString();
    }

    /**
     * @return a string that represents the response to the client that contains all information about the board
     */
    public String getClientString() {
        return this.name + " | " + this.description + " | " +  this.getHints() + "|" + this.getNumString() + "|"+ this.getCellString() +"|"+ this.getCharacterString();
    }

    /**
     * @return a string that contains all hints and their numbers
     */
    public String getHints() {
        StringBuilder str = new StringBuilder();
        str.append("Hints ");
        for (Integer num : this.hints.keySet()) {
            String dir = (this.answers.get(num).getDir() == Direction.ACROSS)? "ACROSS" : "DOWN";
            str.append(num + " " + dir + " " + this.hints.get(num) + " ");
        }
        return str.toString();
    }
    
    /**
     * Adds a player with the identifier playerId to the game
     * @param playerId the id to be added
     */
    public void addPlayer(String playerId) {
        this.playerScores.put(playerId, 0);
    }
    
    /**
     * Returns players on the board
     * @return the set of player scores
     */
    public Set<String> getPlayerIds() {
        Set<String> safePlayerIds = new HashSet<>();
        for(String playerID: playerScores.keySet()) {
            safePlayerIds.add(playerID);
        }
        return safePlayerIds;
    }
    
    /**
     * Gets a player's score
     * @param playerId player to get score
     * @return the player's score
     */
    public int getScore(String playerId) {
        return playerScores.get(playerId);
    }
    /**
     * Updates board with guess
     * @param answer the correct answer for the guess
     * @param guess the guess
     */
    private void guessBoard(Word answer, String guess) { //sets board to guess
        Position start= answer.getPos();
        Position end = answer.getEnd();
        if (answer.getDir()==Direction.ACROSS) {
            for (int i=start.getCol(); i<end.getCol(); i++) {
                Position query= new Position(start.getRow(),i);
                Integer index= i- start.getCol();
                Character guessLetter= guess.charAt(index);

                    this.board.get(query.getRow()).set(query.getCol(), guessLetter);

                
            }
        }
        else if (answer.getDir()==Direction.DOWN) {
            for (int i=start.getRow(); i<end.getRow(); i++) {
                Position query= new Position(i,start.getCol());
                Integer index= i- start.getRow();
                Character guessLetter= guess.charAt(index);
                this.board.get(query.getRow()).set(query.getCol(), guessLetter);

                
                
            }
        }
        
        
        
    }
    
    
    private void removeBoardGuess(Word answer, String guess) { //sets board to guess
        Position start= answer.getPos();
        Position end = answer.getEnd();
        if (answer.getDir()==Direction.ACROSS) {
            for (int i=start.getCol(); i<end.getCol(); i++) {
                Position query= new Position(start.getRow(),i);
                Integer index= i- start.getCol();
                Character guessLetter= guess.charAt(index);
                
                if (!blockOwn.get(query).isEmpty()) {
                    this.board.get(query.getRow()).set(query.getCol(), guessLetter);
                }
                
            }
        }
        else if (answer.getDir()==Direction.DOWN) {
            for (int i=start.getRow(); i<end.getRow(); i++) {
                Position query= new Position(i,start.getCol());
                Integer index= i- start.getRow();
                Character guessLetter= guess.charAt(index);
                if (blockOwn.get(query).isEmpty()) {
                this.board.get(query.getRow()).set(query.getCol(), guessLetter);
                }
                
                
            }
        }
        
        
        
    }
    /**
         * Returns whether or not board is completed
         * @return boolean representing whether or not board is done
         * 
         */
   public synchronized boolean isDone() {
       for (Word i: this.answers.values()) {
           if (!(i.getWord().equals(returnGuess(i)))) {
               return false;
               
           }
           
       }
       return true;
   }
    
    
    /**
     * Returns current guess
     * @param answer
     * @return the current guess for a given answer
     */
    private String returnGuess(Word answer) { //returns current guess for board
        Position start= answer.getPos();
        Position end = answer.getEnd();
        String finalGuess="";
        if (answer.getDir()==Direction.ACROSS) {
            for (int i=start.getCol(); i<end.getCol(); i++) {
                Position query= new Position(start.getRow(),i);
                finalGuess+=this.board.get(query.getRow()).get(query.getCol());
            }
        }
        else if (answer.getDir()==Direction.DOWN) {
            for (int i=start.getRow(); i<end.getRow(); i++) {
                Position query= new Position(i,start.getCol());
                finalGuess+=this.board.get(query.getRow()).get(query.getCol());
                
                
            }
        }
        return finalGuess;
    }
    
    /**
     * Updates a player's score (still threadsafe as it is only called within challengeWord)
     * @param playerId player to update score
     * @param incrementScore amount to alter score by
     */
    private void updateScore(String playerId, int incrementScore) {
        int score = playerScores.get(playerId);
        playerScores.put(playerId,  score+incrementScore);
    }
    
    /**
     * Tries to place the word word at place id in the crossword
     * @param id of the word being guessed
     * @param guess the guess
     * @param playerId the id of the player making the request
     * @return information regarding the success or failure of the attempt
     */
    public synchronized String tryWord(int id, String guess, String playerId) {
        String attempt= "failure";
        Word answer= answers.get(id);
        boolean conditionFirst= guess.length()== answer.size();
        
        
        
        boolean conditionSecond=true;
        boolean someoneElse= false;
        if (!(userGuess.get(id)==null)) {
            someoneElse= ! playerId.equals(userGuess.get(id));
        }
        List<Word> conflictList= new ArrayList<Word>();
        if (conditionFirst) {
        conflictList= returnConflict(id, guess);
        for (Word i: conflictList) {
            if (this.confirmedWord.contains(i) || someoneElse) {
                conditionSecond=false;
                break;
            }
        }
        }
        if (conditionFirst && conditionSecond) {
            attempt= "success";
            this.userGuess.put(id, playerId);
            this.updateBlockOwn(answer);

            guessBoard(answer,guess);
            
            
           for (Word word: conflictList) {
               this.removeGuess(word);               
           }
           this.updateBoard();
           
            
            
        }
        String addition="";
        if (this.isDone()) {
            addition="DONE";
        } else {
            addition="INCOMPLETE";
        }
        return addition + attempt;
        
        

    }
    
    /**
     * Tries to challenge the word at place id in the crossword with the word word
     * @param id of the word being challenged
     * @param word the word that is being used for the challenge
     * @param playerId the id of the player making the request
     * @return information regarding the success or failure of the attempt
     */
    public synchronized String challengeWord(int id, String word, String playerId) {
        Word wordAnswer= this.answers.get(id);
        String playerEnter= this.userGuess.get(id);
        String existingWord= this.returnGuess(wordAnswer);
        if ((!playerId.equals(playerEnter)) && (!this.confirmedWord.contains(wordAnswer)) 
                && (! word.equals(existingWord)) && (word.length()==wordAnswer.size())) {
            String rightWord= wordAnswer.getWord();
            if (rightWord.equals(existingWord)) {
                this.confirmedWord.add(wordAnswer);
                this.updateScore(playerId, -1);
                String addition="";
                if (this.isDone()) {
                    addition="DONE";
                } else {
                    addition="INCOMPLETE";
                }
                
                return addition+ "OrigWordCorrect Confirmed -1";
            } else {
                if (word.equals(rightWord)) {
                    this.confirmedWord.add(wordAnswer);
                    this.updateScore(playerId, 2);
                    List<Word> conflictList= returnConflict(id, word);
                    this.userGuess.put(id, playerId);
                    this.updateBlockOwn(wordAnswer);

                    guessBoard(wordAnswer,word);
                    for (Word conflictWord: conflictList) {
                        this.removeGuess(conflictWord);               
                    }
                    this.updateBoard();
                    String addition="";
                    if (this.isDone()) {
                        addition="DONE";
                    } else {
                        addition="INCOMPLETE";
                    }
                    
                    return addition + "YourWordCorrect Confirmed 2";
                } else {
                    String guess="";
                    for (int i=0; i< wordAnswer.size(); i++) {
                        guess+="#";
                    }
                    this.userGuess.remove(id);
                    this.removeGuess(wordAnswer);
                    removeBoardGuess(wordAnswer,guess); //check!! 
                    this.updateBoard();
                    this.updateScore(playerId, -1);
                    return "INCOMPLETE"+ "YourWordIncorrect NotConfirmed -1";

                }
                
            }
            
        }
        return "operation failed";
        
    }
    

    
    /**
     * Returns the name of the puzzle to display to the client.
     * @return the puzzle name
     */
    public synchronized String getName() {
        return this.name;
    }
    
    /**
         * Adds a player ID to the board
         * @param playerID represents the ID of the player
         * @param wait represents boolean, indicating whether or not we should wait to add player
         * 
         */
    public synchronized void addPlayerID(String playerID, boolean wait) {
        if(players.size() == 0) {
            players.add(playerID);
            return;
        } else if(players.size()==1) {
            if(wait) {
                boolean sizeOne = players.size()==1;
                while(sizeOne) {
                    try {
                        wait();
                        sizeOne = players.size()==1;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                players.add(playerID);
                notifyAll();
            }
        }
    }
}

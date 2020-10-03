/* Copyright (c) 2019 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package crossword;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.IllegalArgumentException;

/**
 * AF()= Starts a server that coordinates the crossword game. 
 * The crossword puzzle is a .puzzle file in specified folder
 * 
 * RI()= true;
 * 
 * Safety from Rep Exposure: no reps
 * 
 * Thread Safety: The server class will not be run from multiple threads, that only 
 *                applies to ServerHelper
 * 
 * 
 * 
 */
public class Server {

    
    /**
     * Start a Crossword Extravaganza server.
     * @param args The command line arguments should include only the folder where
     *             the puzzles are located.
     */
    public static void main(String[] args) {
        final Queue<String> arguments = new LinkedList<>(Arrays.asList(args));
        final String folder= arguments.remove();
        File dir = new File(folder);

        File[] fileList= dir.listFiles(new FilenameFilter() { 
                 public boolean accept(File dir, String filename)
                      { return filename.endsWith(".puzzle");}});
        try {
            Set<String> puzzles = Collections.synchronizedSet(new HashSet<String>());
            for(File file:fileList) {
                String puzzleStr = Files.readString(file.toPath());
                //Crossword crossword= CrosswordParser.parse(puzzleStr);
                puzzles.add(puzzleStr);
            }

        new ServerHelper(puzzles).serve();
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid board");
        }

    }
}

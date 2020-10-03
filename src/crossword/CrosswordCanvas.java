/* Copyright (c) 2019 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package crossword;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.swing.JComponent;

/**
 * This component allows you to draw a crossword puzzle. Right now it just has
 * some helper methods to draw cells and add text in them, and some demo code
 * to show you how they are used. You can use this code as a starting point when
 * you develop your own UI.
 * @author asolar
 */
class CrosswordCanvas extends JComponent {
    final String hints;
    final List<String> cells;
    final List<String> numbers;
    final Map<String,String> chars;
    final String tryOrChallenge;
    public CrosswordCanvas(String hints, String numbers, String cells, String chars, String arg) {
        this.hints=hints;
        this.numbers= parseNums(numbers);
        this.cells= parseCells(cells);
        this.chars=parseChars(chars);
        this.tryOrChallenge=arg;
        
    }
    
    private static List<String> parseCells(String cells) {
        List<String> ans = new ArrayList<String>();
        String[] cellList= cells.split("#"); //ignore first element! 
        for (int i=1; i< cellList.length; i++) {
            String[] coor= cellList[i].split(",");
            String xy= coor[0].split("\\(")[1]+ " "+ coor[1].split("\\)")[0];
            ans.add(xy);
        }
        return ans;
    }
    
    private static Map<String,String> parseChars(String chars) {
        Map<String,String> ans = new HashMap<String,String>();
        String[] charList= chars.split("char"); //ignore first element! 
        for (int i=1; i< charList.length; i++) {
            String[] coor= charList[i].split(",");
            String letter=coor[0].split(" ")[0];
            String xy= coor[0].split("\\(")[1]+ " "+ coor[1].split("\\)")[0];
            ans.put(xy, letter);
        }
        return ans;
    }
    
    private static List<String> parseNums(String nums){
        List<String> ans= new ArrayList<String>();
        String [] cellList= nums.split("num");
        for (int i=1; i< cellList.length; i++) {
            String[] numAndRest = cellList[i].split("Dir");
            String[] dirCoor= numAndRest[1].split("Coor");
            String number = numAndRest[0];
            String dir = dirCoor[0];
            //System.out.println(dir);
            String[] coor= dirCoor[1].split(",");
            String numXy=number+ " " + dir + " "+coor[0].split("\\(")[1]+ " "+ coor[1].split("\\)")[0];
            ans.add(numXy);
            
        }
        return ans;
        
        
        
    }
    
    
    private final String boardInfo=""; // create new constructor, set to boardInfo
    /**
     * Horizontal offset from corner for first cell.
     */
    private final int originX = 100;
    /**
     * Vertical offset from corner for first cell.
     */
    private final int originY = 60;
    /**
     * Size of each cell in crossword. Use this to rescale your crossword to have
     * larger or smaller cells.
     */
    private final int delta = 30;

    /**
     * Font for letters in the crossword.
     */
    private final Font mainFont = new Font("Arial", Font.PLAIN, delta * 4 / 5);

    /**
     * Font for small indices used to indicate an ID in the crossword.
     */
    private final Font indexFont = new Font("Arial", Font.PLAIN, delta / 3);

    /**
     * Font for small indices used to indicate an ID in the crossword.
     */
    private final Font textFont = new Font("Arial", Font.PLAIN, 16);

    /**
     * Draw a cell at position (row, col) in a crossword.
     * @param row Row where the cell is to be placed.
     * @param col Column where the cell is to be placed.
     * @param g Graphics environment used to draw the cell.
     */
    private void drawCell(int row, int col, Graphics g) {
        g.drawRect(originX + col * delta,
                   originY + row * delta, delta, delta);
    }

    /**
     * Place a letter inside the cell at position (row, col) in a crossword.
     * @param letter Letter to add to the cell.
     * @param row Row position of the cell.
     * @param col Column position of the cell.
     * @param g Graphics environment to use.
     */
    private void letterInCell(String letter, int row, int col, Graphics g) {
        g.setFont(mainFont);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(letter, originX + col * delta + delta / 6,
                             originY + row * delta + fm.getAscent() + delta / 10);
    }

    /**
     * Add a vertical ID for the cell at position (row, col).
     * @param id ID to add to the position.
     * @param row Row position of the cell.
     * @param col Column position of the cell.
     * @param g Graphics environment to use.
     */
    private void verticalId(String id, int row, int col, Graphics g) {
        g.setFont(indexFont);
        g.drawString(id, originX + col * delta + delta / 8,
                         originY + row * delta - delta / 15);
    }

    /**
     * Add a horizontal ID for the cell at position (row, col).
     * @param id ID to add to the position.
     * @param row Row position of the cell.
     * @param col Column position of the cell.
     * @param g Graphics environment to use.
     */
    private void horizontalId(String id, int row, int col, Graphics g) {
        g.setFont(indexFont);
        FontMetrics fm = g.getFontMetrics();
        int maxwidth = fm.charWidth('0') * id.length();
        g.drawString(id, originX + col * delta - maxwidth - delta / 8,
                         originY + row * delta + fm.getAscent() + delta / 15);
    }

    // The three methods that follow are meant to show you one approach to writing
    // in your canvas. They are meant to give you a good idea of how text output and
    // formatting work, but you are encouraged to develop your own approach to using
    // style and placement to convey information about the state of the game.

    private int line = 0;
    
    // The Graphics interface allows you to place text anywhere in the component,
    // but it is useful to have a line-based abstraction to be able to just print
    // consecutive lines of text.
    // We use a line counter to compute the position where the next line of code is
    // written, but the line needs to be reset every time you paint, otherwise the
    // text will keep moving down.
    private void resetLine() {
        line = 0;
    }

    // This code illustrates how to write a single line of text with a particular
    // color.
    private void println(String s, Graphics g) {
        g.setFont(textFont);
        FontMetrics fm = g.getFontMetrics();
        // Before changing the color it is a good idea to record what the old color
        // was.
        Color oldColor = g.getColor();
        g.setColor(new Color(100, 0, 0));
        g.drawString(s, originX + 500, originY + line * fm.getAscent() * 6 / 5);
        // After writing the text you can return to the previous color.
        g.setColor(oldColor);
        ++line;
    }

    // This code shows one approach for fancier formatting by changing the
    // background color of the line of text.
    private void printlnFancy(String s, Graphics g) {

        g.setFont(textFont);
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getAscent() * 6 / 5;
        int xpos = originX + 500;
        int ypos = originY + line * lineHeight;

        // Before changing the color it is a good idea to record what the old color
        // was.
        Color oldColor = g.getColor();

        g.setColor(new Color(0, 0, 0));
        g.fillRect(xpos, ypos - fm.getAscent(), fm.stringWidth(s), lineHeight);
        g.setColor(new Color(200, 200, 0));
        g.drawString(s, xpos, ypos);
        // After writing the text you can return to the previous color.
        g.setColor(oldColor);
        ++line;
    }

    private int x = 1;
    
    
    

    /**
     * Simple demo code just to illustrate how to paint cells in a crossword puzzle.
     * The paint method is called every time the JComponent is refreshed, or every
     * time the repaint method of this class is called.
     * We added some state just to allow you to see when the class gets repainted,
     * although in general you wouldn't want to be mutating state inside the paint
     * method.
     */
    @Override
    public void paint(Graphics g) {
        for (int i=0; i< this.cells.size(); i++) {
            String ans= this.cells.get(i);
            String[] coor= ans.split(" ");
            Integer row= Integer.parseInt(coor[0]);
            Integer column= Integer.parseInt(coor[1]);
            drawCell(row,column ,g);
            if (chars.containsKey(ans)) {
                letterInCell(chars.get(ans), row, column, g);
            }
        }  
        for (int i=0; i< this.numbers.size(); i++) {
            String ans= this.numbers.get(i);
            String [] info= ans.split(" ");
            if (info[1].equals("ACROSS")) {
                horizontalId(info[0], Integer.parseInt(info[2]), Integer.parseInt(info[3]),g);
            } else {
                verticalId(info[0], Integer.parseInt(info[2]), Integer.parseInt(info[3]),g);
            }
        }
        resetLine();
        String[] ans= hints.split("[0-9]+");
        println(ans[0],g);
        for (int i=1; i< ans.length; i++) {
            println(i+ans[i],g);
            
        }
        if (!tryOrChallenge.equals("")) {
                    String[] boardInfo=this.tryOrChallenge.split("&");
                    String confirmWords= boardInfo[0];
                    String playerPoints=boardInfo[1];
                    String guesses= boardInfo[2];
                    println(confirmWords,g);
                    println(playerPoints,g);
                    println(guesses,g);
                    }


    }
    

}

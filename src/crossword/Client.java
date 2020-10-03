/* Copyright (c) 2019 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package crossword;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
//import java.awt.event*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * AF(Canvas width, Canvas Height)= Represents a client playing a crossword puzzle.Creates a GUI visual of a crossword puzzle that has a width of canvas width
 *                                   and a height of canvas height. Connects to server receive puzzle information
 * RI
 * canvas width > 500 (for visibility to the user)
 * canvas height > 500 (for visibility to the user)
 * 
 * Safety from Rep Exposure:
 * All reps are final and immutable, and are never returned in a function.
 * 
 * Thread Safety:
 * All clients have different processes, so there is no interleaving.
 * 
 *  All updates to the graphics are handled entirely by 1 thread at a time, the main thread creates it all and then another handles all updates
 * based on input from the server.
 * 
 */
public class Client {

    private static final int CANVAS_WIDTH = 1200;
    private static final int CANVAS_HEIGHT = 900;

    private enum ClientStatus {
        START, CHOOSE, WAIT, PLAY, SHOWSCORE, QUIT;
    }

    private static ClientStatus status = ClientStatus.START;
    private static String clientID;

    /**
     * Start a Crossword Extravaganza client.
     * @param args The command line arguments should include only the server address.
     * @throws IOException
     */
    public static void main(String[] args) throws IOException{
        final Queue<String> arguments = new LinkedList<>(List.of(args));
        final String host = arguments.remove();

        Socket socket = new Socket(host,4949); // port defined by spec
        BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
        PrintWriter socketOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8), true);
        BufferedReader readFromUser = new BufferedReader(new InputStreamReader(System.in, UTF_8));

        try {
            System.out.println("You have successfully connected to the server!");
            System.out.println("Please enter a unique ID");
            String ids = socketIn.readLine();
            List<String> idsArr = List.of(ids.substring(1, ids.length()-1).split(", "));
            System.out.println(idsArr);
            //socketOut.println(userInput);
            JFrame window = startGame(socketOut, socketIn, idsArr);
            boolean visible = true;
            while(visible) {
                visible = window.isVisible();
            }
            socketOut.println("socket is about to close");
            socketOut.flush();
        } finally {
            socket.close();
            socketOut.close();
            socketIn.close();
            /*String userInput= readFromUser.readLine();
            socketOut.println(userInput);
            String reply= socketIn.readLine(); 
            //TODO: update board based on reply!

            final String boardInfo= socketIn.readLine();
            String[] sepArray= boardInfo.split("\\|");
            String name= sepArray[0];
            String description=sepArray[1];
            String hints=sepArray[2];
            String numbers= sepArray[3];
            String cells= sepArray[4];
            String chars= sepArray[5];

            //System.out.println("array"+ Arrays.toString(sepArray));
            System.out.println("boardInfo");
            System.out.println("names"+ name);
            System.out.println("description"+ description);
            System.out.println("hints"+ hints);
            System.out.println("numbers"+" "+numbers);
            System.out.println("cells"+" "+cells);
            System.out.println("chars"+ chars);
            launchGameWindow(hints,numbers,cells, chars);
            System.out.println("connection closed");*/
        }


    }



    //not necessary for warmup but implemented


    /**
     * This method starts the GUI and plays the game
     * @param socketOut
     * @param idsArr
     */
    private static JFrame startGame (PrintWriter socketOut, BufferedReader socketIn, List<String> idsArr){
        //JPanel for wait
        JPanel contentPaneWait = new JPanel();
        JLabel waitLabel = new JLabel("Awaiting a second player...");
        ClientStatus drawStatus = ClientStatus.START;

        //JFrame
        JFrame window = new JFrame("START");
        window.setLayout(new BorderLayout());
        window.setSize(CANVAS_WIDTH + 50, CANVAS_HEIGHT + 50);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        /*contentPaneWait.addContainerListener(new ContainerListener() {

            public void componentAdded(ContainerEvent e) {


            }

            public void componentRemoved(ContainerEvent e) {
                // TODO Auto-generated method stub
             // TODO Auto-generated method stub
                Component c = e.getChild();
                System.out.println(c);
                if(c.equals(waitLabel)) {
                    try {
                        System.out.println("listener triggered");
                        String response = processWaitRequest(socketIn, socketOut);
                        System.out.println(response);
                    } catch(IOException i) {
                        i.printStackTrace();
                    }
                }
            }

        });*/


        JTextField textbox = new JTextField(30);
        textbox.setFont(new Font("Arial", Font.BOLD, 20));

        //JPanel for start
        JPanel contentPane = new JPanel();
        JLabel startInstructions = new JLabel("Please enter a unique ID in the textbox. Whitespace characters are not allowed.");
        contentPane.add(startInstructions);
        contentPane.add(textbox);


        /*waitLabel.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
             // Do nothing
            }
            public void componentMoved(ComponentEvent e) {
             // Do nothing
            }
            public void componentShown(ComponentEvent e) {
                try {
                    System.out.println("compshown");
                    String req = processWaitRequest(socketIn, socketOut);
                    System.out.println(req);
                    System.out.println("sent request");
                } catch(IOException ie) {
                    ie.printStackTrace();
                }

            }
            public void componentHidden(ComponentEvent e) {
                // Do nothing

            }
        });*/


        //JPanel for choose
        JLabel puzzlesAvailable = new JLabel("The following puzzles are available: ");
        JLabel matchesAvailable = new JLabel("The following matches are available: ");
        JPanel contentPaneChoose = new JPanel();
        JPanel chooseInstructions = new JPanel();
        JPanel playPane = new JPanel();
        JPanel scorePane = new JPanel();
        chooseInstructions.add(puzzlesAvailable);
        chooseInstructions.add(matchesAvailable);
        chooseInstructions.setLayout(new BoxLayout(chooseInstructions, BoxLayout.PAGE_AXIS));
        JTextField textboxChoose = new JTextField(30);
        textboxChoose.setFont(new Font("Arial", Font.BOLD, 20));
        JButton chooseButton = new JButton("Enter");
        chooseButton.addActionListener((event) -> {
            String request = textboxChoose.getText();
            socketOut.println(request + " " + clientID);
            
        });
        contentPaneChoose.add(chooseInstructions);
        contentPaneChoose.add(textboxChoose);
        contentPaneChoose.add(chooseButton);

        JButton enterButtonPlay = new JButton("Enter");
        
        JTextField textboxPlay = new JTextField(30);
        
        JTextField textboxScore = new JTextField(30);
        JButton buttonScore = new JButton("Enter");
        buttonScore.addActionListener((event)-> {
            String request = textboxScore.getText();
            String[] requestSplit = request.split(" ");
            if(requestSplit[0].equals("EXIT")) {
                socketOut.println("EXIT " + clientID);
            } else if (requestSplit[0].equals("NEW") && requestSplit[1].equals("MATCH")) {
                //socketOut.println("EXIT " + clientID);
                socketOut.println("NEW MATCH " + clientID);
            }
        });
        
        textboxPlay.setFont(new Font("Arial", Font.BOLD, 20));
        playPane.add(textboxPlay);
        playPane.add(enterButtonPlay);
        
        enterButtonPlay.addActionListener((event) -> {
            String request = textboxPlay.getText();
            //request = TRY [word-id] [word-attempt]
            //want to send = TRY [client id] [word id] [word attempt]
            String[] requestSplit = request.split(" ");
            if(requestSplit[0].equals("TRY") || requestSplit[0].equals("CHALLENGE")) {
                socketOut.println(requestSplit[0] + " " + clientID + " " + requestSplit[1] + " " + requestSplit[2]);
            } else if(request.equals("EXIT")) {
                socketOut.println("SHOWSCORE " + clientID);
            }
            //socketOut.println(request);
        });
        /*enterButton.addActionListener((event) -> {
            // This code executes every time the user presses the Enter
            // button. Recall from reading 24 that this code runs on the
            // Event Dispatch Thread, which is different from the main
            // thread.
            canvas.repaint();
        });*/
        enterButtonPlay.setSize(10, 10);


        JButton enterButton = new JButton("Enter");
        enterButton.addActionListener((event) -> {
            // This code executes every time the user presses the Enter
            // button. Recall from reading 24 that this code runs on the
            // Event Dispatch Thread, which is different from the main
            // thread.
            String id = textbox.getText();
            if(id.contains(" ") || id.contains("\t") || id.contains("\r") || id.contains("\n")) {
                startInstructions.setText("Please try again, without using whitespace");
                startInstructions.repaint();
            } else {
                socketOut.println("START " + id);             
            }
        });

        contentPane.add(enterButton);

        window.add(contentPane, BorderLayout.SOUTH);
        window.getContentPane().add(contentPane); 

        new Thread(new Runnable() {
        //START [id] --- assigns the client a unique ID
            //response: START [id] ok
            //          [matches available] 
            //          [puzzles available]
            //response: START [id] bad
        //PLAY [id] [match_id] --- adds the client with unique ID id to the match with match_id
            //response: PLAY [id] [match_id] ok
            //          [puzzle information]
            //response: PLAY [id] [match_id] bad
        //NEW [id] [match_id] [puzzle_id] --- begins a new match hosted by the client with id
            //on the match match_id on puzzle puzzle_id with puzzle_desc as any string
            //response: NEW [id] [match_id] [puzzle_id] ok
            //response: NEW [id] [match_id] [puzzle_id] [puzzle_desc] bad
        //EXIT [id] --- disconnects client with id unique ID
            //disconnect client
        //TRY [id] [word_id] [word_attempt] --- client with id tries a word
            //response: TRY [id] [word_id] [word_attempt] ok
            //response: TRY [id] [word_id] [word_attempt] bad
        //CHALLENGE [id] [word_id][word_attempt] --- client with id challenges a word
            //response: CHALLENGE [id] [word_id][word_attempt] granted
            //response: CHALLENGE [id] [word_id][word_attempt] failed
            public void run() {
                ClientStatus drawingStatus = drawStatus;
                CrosswordCanvas canvas = null;
                while (true) {
                    try {
                        String answer=socketIn.readLine();
                        String[] arrays=answer.split("%");
                        String serverInput=arrays[0];
                        String tryChallengeInput="";
                        if (arrays.length>1) {
                            System.out.println("ENTERED");
                            tryChallengeInput=arrays[1];
                                                }
                        //String serverInput = socketIn.readLine();
                        //System.out.println(serverInput);
                        if(!serverInput.contains("-->")) {
                            serverInput = serverInput + "-->";
                        }
                        String[] inputSplitInitial = serverInput.split("-->");
                        String[] inputSplit = inputSplitInitial[0].split(" ");
                        //String serverInputTwo = "";
                        //String serverInputThree = "";
                        // change status
                        switch (inputSplit[0]) {
                        case "START" : { // implies name is correct
                            if (inputSplit[2].equals("ok") &&( drawingStatus == ClientStatus.START || drawingStatus == ClientStatus.CHOOSE || drawingStatus==ClientStatus.SHOWSCORE)) {
                                if(drawingStatus==ClientStatus.START) {
                                    clientID = inputSplit[1];
                                }
                                if(!(drawingStatus==ClientStatus.SHOWSCORE && !inputSplit[1].equals(clientID))) {
                                    window.getContentPane().remove(scorePane);
                                    drawingStatus = ClientStatus.CHOOSE;
                                }
                                
                                //clientID = inputSplit[1];
                                //serverInputTwo = socketIn.readLine();
                                //serverInputThree = socketIn.readLine();
                            }
                            break;
                        }
                        case "NEW" : { //implies game correctly started
                            if (inputSplit[3].equals(clientID) && inputSplit[4].equals("ok")) {
                                drawingStatus = ClientStatus.WAIT;
                            }
                            break;
                        }
                        case "PLAY" : { //implies connected successfully to game
                            if (inputSplit[3].equals("ok")) {
                                if (drawingStatus == ClientStatus.WAIT || drawingStatus == ClientStatus.CHOOSE) {
                                    drawingStatus = ClientStatus.PLAY;
                                } 
                                //serverInputTwo = socketIn.readLine();
                                //String secondPlayer = socketIn.readLine();
                            }
                            break;
                        }
                        case "TRY" : {
                            if(inputSplit[3].equals("ok")) {
                                if(drawingStatus == ClientStatus.PLAY) {
                                    drawingStatus = ClientStatus.PLAY;
                                
                                }
                            }
                            break;
                        }
                        case "CHALLENGE" : {
                            if(inputSplit[3].equals("granted")) {
                                if(drawingStatus == ClientStatus.PLAY) {
                                    drawingStatus = ClientStatus.PLAY;
                                }
                            }
                            break;
                        }
                        case "SHOWSCORE" : {
                            drawingStatus = ClientStatus.SHOWSCORE;
                            window.getContentPane().remove(playPane);
                            break;
                        }
                        case "QUIT": {
                            if(drawingStatus == ClientStatus.CHOOSE){
                                window.setVisible(false);
                                window.dispose();
                            } else if(drawingStatus == ClientStatus.SHOWSCORE) {
                                window.getContentPane().remove(scorePane);
                                drawingStatus = ClientStatus.START;
                            }
                            break;
                        }
                        default : {
                            //should not get to this point
                            //throw new IllegalArgumentException();
                            break;
                        }
                        //System.out.println(drawingStatus);
                        }
                        // update graphics START, CHOOSE, WAIT, PLAY, SHOWSCORE, QUIT;
                        switch (drawingStatus) {
                        case START : {
                            window.getContentPane().remove(scorePane);
                            //This tells the user to try entering a different ID
                            startInstructions.setText("Sorry, that ID was already taken. Please try again!");
                            startInstructions.repaint();
                            Container currentPane = window.getContentPane();
                            window.getContentPane().remove(currentPane);
                            window.getContentPane().add(contentPane);
                            window.validate();
                            break;
                        }
                        case CHOOSE : {
                            // [START id ok, -->, ->, match1, ->, match2, -->, ->, puzzle1, ->, puzzle2]
                            for(String token:inputSplitInitial) {
                                System.out.println(token);
                            }
                            String[] matchIDs = inputSplitInitial[1].split("->");
                            String matchIDsDisplay = "";
                            if(matchIDs.length>1) {
                                matchIDsDisplay = " ";
                                for(String matchID: matchIDs) {
                                    if(!matchID.trim().equals("")) {
                                        matchIDsDisplay = matchIDsDisplay + matchID + ", ";
                                    } 
                                }
                                matchIDsDisplay = matchIDsDisplay.substring(0, matchIDsDisplay.length()-2);
                            }

                            String[] puzzleIDs = inputSplitInitial[2].split("->");
                            String puzzleIDsDisplay = "";
                            for(String matchID: puzzleIDs) {
                                if(!matchID.trim().equals("")) {
                                    puzzleIDsDisplay = puzzleIDsDisplay + matchID + ", "; 
                                }  
                            }
                            puzzleIDsDisplay = puzzleIDsDisplay.substring(0, puzzleIDsDisplay.length()-2);
                            if(matchIDs.length==1) {
                                matchesAvailable.setText("<html>Currently, no matches are available. <br/>Start a new match by entering \"NEW [valid match ID] [a valid puzzle ID]\" (without the quotes) in the text box below. Valid puzzle IDs are listed above. <br/>You can choose your own match ID, but please do not include whitespace characters.<html>");
                                matchesAvailable.repaint();
                            } else {
                                matchesAvailable.setText("<html>Current matches available:" + matchIDsDisplay + "<br/>To join an existing match enter \"PLAY [valid match ID]\" (without the quotes) in the text box below. <html/>");
                                matchesAvailable.repaint();
                            }
                            System.out.println(matchIDs);
                            if(puzzleIDs.length==1) {
                                puzzlesAvailable.setText("<html> No puzzles were loaded properly. <br> </html>");
                            } else {
                                puzzlesAvailable.setText("<html>The following puzzles are available: " + puzzleIDsDisplay + " <br> </html>");
                            }
                            puzzlesAvailable.repaint();
                            window.getContentPane().remove(contentPane);
                            window.getContentPane().add(contentPaneChoose); 
                            window.validate();
                            break;
                        }
                        case WAIT: {
                            contentPaneWait.add(waitLabel);
                            window.getContentPane().remove(contentPaneChoose);
                            window.getContentPane().add(contentPaneWait); 
                            window.validate();
                            break;
                        }
                        case PLAY: {
                            String info = inputSplitInitial[1];
                            String[] infoSplit = info.split("\\|");
                            
                            if (canvas != null) window.remove(canvas);
                            canvas = new CrosswordCanvas(infoSplit[2], infoSplit[3], infoSplit[4], infoSplit[5],tryChallengeInput);
                            //launchGameWindow(infoSplit[2], infoSplit[3], infoSplit[4], infoSplit[5]);

                            canvas.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
                            //JFrame window = new JFrame("Crossword Client");
                            window.setLayout(new BorderLayout());
                            window.add(canvas, BorderLayout.CENTER);
                            canvas.repaint();
                            canvas.repaint();
                            //contentPane.add(textboxPlay);
                            //contentPane.add(enterButtonPlay);

                            window.add(playPane, BorderLayout.SOUTH);

                            window.setSize(CANVAS_WIDTH + 50, CANVAS_HEIGHT + 50);
                            window.getContentPane().remove(contentPaneWait);
                            window.getContentPane().remove(contentPaneChoose);
                            window.getContentPane().add(playPane);
                            window.validate();
                            break;
                        }
                        case SHOWSCORE: {
                            // remove everything
                            // inputSplit[1:4] playerId score playerId score
                            String playerId1 = inputSplit[1];
                            String playerId2 = inputSplit[3];
                            String player1Score = inputSplit[2];
                            String player2Score = inputSplit[4];
                            //window.getContentPane().remove(playPane);
                            // display scores
                            JLabel scoreText = new JLabel();
                            JLabel nextInstructions = new JLabel();
                            scoreText.setText("<html>Final Scores:<br/>"+playerId1 +" Score: " + player1Score+
                                    "<br/>" +playerId2 + " Score: " + player2Score+" <br/> Enter EXIT to exit or NEW MATCH to play again. <html/>");
                            scorePane.add(scoreText);
                            scorePane.add(textboxScore);
                            scorePane.add(buttonScore);
                            window.getContentPane().remove(playPane);
                            window.getContentPane().add(scorePane);
                            window.validate();
                            break;
                        }
                        default: {
                            break;
                        }
                        }
                        
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    window.validate();
                }
            }
        }).start();

        window.setVisible(true);
        return window;

    }

    private static String processWaitRequest(BufferedReader socketIn, PrintWriter socketOut) throws IOException {
        //String[] inputTokens = choose.split(" ");
        //String matchID = inputTokens[1];
        //String puzzleID = inputTokens[2];
        socketOut.println("WAIT " + clientID);
        socketOut.flush();
        String serverResponse = socketIn.readLine();
        //while(serverResponse==null) {
        //serverResponse = socketIn.readLine();
        //}
        /*for(String input = socketIn.readLine(); input != null; input = socketIn.readLine()) {
            return input;
        }*/
        //return "b";
        return serverResponse;
    }

    private static ClientStatus processStartRequest(String id, BufferedReader socketIn, PrintWriter socketOut) throws IOException {
        socketOut.println("START " + id);
        socketOut.flush();
        status = ClientStatus.CHOOSE;
        String input = socketIn.readLine();
        String[] inputTokens = input.split(" ");
        if(inputTokens[inputTokens.length-1].equals("ok")) {
            clientID = id;
            return status;
        } else {
            return ClientStatus.START;
        }
        /*for(String input = socketIn.readLine(); input != null; input = socketIn.readLine()) {
            System.out.println(input + "from callback");
            //handleRequest(input);
        }*/
    }

    private static ClientStatus processChooseRequest(String choose, BufferedReader socketIn, PrintWriter socketOut) throws IOException {
        String[] inputTokens = choose.split(" ");
        if(inputTokens[0].equals("NEW")) {
            String matchID = inputTokens[1];
            String puzzleID = inputTokens[2];
            socketOut.println("NEW " + matchID + " \"" + puzzleID + "\" " + clientID);
            socketOut.flush();
            String serverResponse = socketIn.readLine();
            System.out.println(serverResponse);
            String[] responseTokens = serverResponse.split(" ");
            if(responseTokens[responseTokens.length-1].equals("ok")) {
                return ClientStatus.WAIT;
            } else {
                return ClientStatus.CHOOSE;
            }
        } else if(inputTokens[0].equals("PLAY")) {
            String matchID = inputTokens[1];
            socketOut.println("PLAY " + matchID + " " + clientID);
            socketOut.flush();
            String serverResponse = socketIn.readLine();
            String[] responseTokens = serverResponse.split(" ");
            if(responseTokens[responseTokens.length-1].equals("ok")) {
                return ClientStatus.PLAY;
            } else {
                return ClientStatus.CHOOSE;
            }
        } else if(choose.equals("QUIT")) {
            //socketOut.println("QUIT");
            //socketOut.flush();
            return ClientStatus.QUIT;
        }
        return ClientStatus.CHOOSE;
    }

    private static String processPlayRequest(String play, BufferedReader socketIn, PrintWriter socketOut) throws IOException{
        String[] inputTokens = play.split(" ");
        if(inputTokens[0].equals("TRY")) {
            String wordID = inputTokens[1];
            String attempt = inputTokens[2];
            socketOut.println("TRY " + clientID + wordID + attempt);
            socketOut.flush();
            String serverResponse = socketIn.readLine();
            return serverResponse;
        } else if(inputTokens[0].equals("CHALLENGE")) {
            String wordID = inputTokens[1];
            String attempt = inputTokens[2];
            socketOut.println("CHALLENGE " + clientID + wordID + attempt);
            socketOut.flush();
            String serverResponse = socketIn.readLine();
            return serverResponse;
        } else if(play.equals("QUIT")) {
            return "quit";
        } else {
            return "bad";
        }
    }

    /**
     * Starter code to display a window with a CrosswordCanvas,
     * a text box to enter commands and an Enter button.
     */
    private static CrosswordCanvas getCrosswordDisplay(String hints, String numbers, String cells, String chars, String input) {

        CrosswordCanvas canvas = new CrosswordCanvas(hints, numbers, cells, chars, "hi");
        
        canvas.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        System.out.println(canvas.toString());
        return canvas;




        /*window.add(contentPane, BorderLayout.SOUTH);

        window.setSize(CANVAS_WIDTH + 50, CANVAS_HEIGHT + 50);

        window.getContentPane().add(contentPane);

        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setVisible(true);*/
    }

    private static void launchGameWindow(String hints, String numbers, String cells, String chars) {

        CrosswordCanvas canvas = new CrosswordCanvas(hints, numbers, cells, chars,"hi");
        canvas.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);

        JButton enterButton = new JButton("Enter");
        /*enterButton.addActionListener((event) -> {
            // This code executes every time the user presses the Enter
            // button. Recall from reading 24 that this code runs on the
            // Event Dispatch Thread, which is different from the main
            // thread.
            canvas.repaint();
        });*/
        enterButton.setSize(10, 10);

        JTextField textbox = new JTextField(30);
        textbox.setFont(new Font("Arial", Font.BOLD, 20));

        JFrame window = new JFrame("Crossword Client");
        window.setLayout(new BorderLayout());
        window.add(canvas, BorderLayout.CENTER);
        canvas.repaint();
        JPanel contentPane = new JPanel();
        contentPane.add(textbox);
        contentPane.add(enterButton);

        window.add(contentPane, BorderLayout.SOUTH);

        window.setSize(CANVAS_WIDTH + 50, CANVAS_HEIGHT + 50);
        //window.getContentPane().remove(contentPanePrev);
        window.getContentPane().add(contentPane);
        //window.validate();

        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

}

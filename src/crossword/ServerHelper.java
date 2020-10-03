package crossword;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.HashMap;

import java.util.Collections;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.mit.eecs.parserlib.UnableToParseException;


public class ServerHelper {
    // AF(ServerSocket, matchID, puzzleID, clientIds) = Runs a server handling crossword matches between pairs of clients.
    //                                       Each match has an ID, and matchID maps an ID to the respective crossword puzzle.
    //                                       Each puzzle also has an ID. puzzleID maps a puzzle to its respective ID. The IDs of 
    //                                       clients currently playing are represented by elements in clientID.
    // RI
    // matchID
    // --No two crossword in matchID.values() refer to the same object in memory.
    // --len(puzzleID) >= len(matchID)
    // puzzleID
    // -- No two strings in puzzleID.values() are equal
    // ServerSocket
    // --port=4949 (as specified by pset)
    // clientIds
    // -- len(clientIds) <= len(puzzleID)*2
    
    // Safety from Rep Exposure: 
    // All fields are private and final, and never returned to the user.
    // Thread Safety:
    // each client is confined to one thread (handleRequest is confined), clientIds is a 
     //synchronizedSet (threadsafe DT). ServerHelper is also confined to single thread (i.e. there should
     //only be one server running and handling the different games).
    // handleRequest only modifies Board, which is a threadsafe type. handleConnection only adds elements
    // to matchID and puzzleID, which are also threadsafe types. No element is ever removed from matchID or puzzleID.

    private final ServerSocket serverSocket;
    private final ConcurrentMap<String,Board> gameIds;
    private final ConcurrentMap<String, List<String>> gameIdsByPlayer;
    /*
     * TODO: it is bad that we are storing a Board for each puzzleID, i think we should construct Boards
     * as needed so we can have multiple Board objects for the same crossword puzzle so different clients can play two
     * games with the same base puzzle concurrently
     */
    private final ConcurrentMap<String, PrintWriter> socketByPlayer;
    private final ConcurrentMap<String, Board> boardByPlayer; 
    private final ConcurrentMap<String, String> puzzleID;
    private final Set<String> clientIds = Collections.synchronizedSet(new HashSet<String>());

    
    
    /**
     * Make a new text game server using board that listens for connections on port.
     * 
     * @param puzzles set of puzzles
     * @throws IOException if an error occurs opening the server socket
     */
    public ServerHelper(Set<String> puzzleStrs)  throws IOException, UnableToParseException {
        this.serverSocket = new ServerSocket(4949);
        this.gameIds= new ConcurrentHashMap<String,Board>();
        this.gameIdsByPlayer = new ConcurrentHashMap<String, List<String>>();
        this.puzzleID=new ConcurrentHashMap<String, String>();
        this.socketByPlayer = new ConcurrentHashMap<String, PrintWriter>();
        this.boardByPlayer=new ConcurrentHashMap<String, Board>();
        for(String puzzleStr: puzzleStrs) {
            try {
                Board crossword= (Board)CrosswordParser.parse(puzzleStr);
                this.puzzleID.put(crossword.getName(), puzzleStr);
            } catch(Exception e) {
                System.out.println(puzzleStr);
            }
        }
    }
    
    
    /**
     * @return the port on which this server is listening for connections
     */
    public int port() {
        return serverSocket.getLocalPort();
    }
    
    /**
     * Run the server, listening for and handling client connections.
     * Never returns normally.
     * 
     * @throws IOException if an error occurs waiting for a connection
     */
    public void serve() throws IOException {
        System.err.println("Server listening on " + serverSocket.getLocalSocketAddress());
        while (true) {
            // block until a client connects
            Socket socket = serverSocket.accept();
            
            new Thread(new Runnable() {
                public void run() {
                    try {

                        handleConnection(socket);

                    } catch (IOException | UnableToParseException ioe) {
                        ioe.printStackTrace(); // but do not stop serving
                    } finally {
                        try {
                        socket.close();
                        } catch (IOException io) {
                            io.printStackTrace();
                        }
                    }
                }
            }).start();            
        }
    }
    
    /**
     * Handle a single client connection.
     * Returns when the client disconnects.
     * 
     * @param socket socket connected to client
     * @throws IOException if the connection encounters an error or closes unexpectedly
     * @throws UnableToParseException 
     */
    private void handleConnection(Socket socket) throws IOException, UnableToParseException { 
        
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
        PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8), true);
        try {
            //out.println(crossword.getClientString());
            out.println(clientIds);
            /*String id = in.readLine();
            System.out.println(id);*/
            System.out.println("connected");
            for(String input = in.readLine(); input != null; input = in.readLine()) {
                System.out.println(input);
                String response = handleRequest(input);
                /*if(response.equals("QUIT")) {
                    out.close();
                    in.close();
                    socket.close();
                }*/
                if(response.startsWith("START")) {
                    String[] responseTokens = response.split(" ");
                    socketByPlayer.put(responseTokens[1], out);
                }
                out.println(response);
            }
        } finally {
            for(String userID: socketByPlayer.keySet()) {
                PrintWriter p = socketByPlayer.get(userID);
                if(p.equals(out)) {
                    socketByPlayer.remove(userID);
                    boardByPlayer.remove(userID);
                }
            }
            out.close();
            in.close();
        }
        
//        try {
//            for (String input = in.readLine(); input != null; input = in.readLine()) {
//                String output = handleRequest(input, player);
//
//                out.println(output);
//            }
//        } finally {
//            out.close();
//            in.close();
//        }

    }
    
    private String matchString() {
        String ids = "";
        for(String matchId: this.gameIds.keySet()) {
            ids = ids + "->" + matchId;
        }
        return ids;
    }
    
    private String puzzlesString() {
        String output = "";
        System.out.println(puzzleID.toString());
        for(String crosswordName: this.puzzleID.keySet()) {
            output = output + "->" + crosswordName;
        }
        return output;
    }
    
    /**
     * Handles client request and returns server response
     * This handles a particular request from the client according to the wire protocol below
     * @param input input from client
     * @return output message to client
     * @throws UnableToParseException 
     */
    private String handleRequest(String input) throws UnableToParseException {
        //Possible requests:
        //START [id] --- assigns the client a unique ID
            //response: START [id] ok
            //          [matches available] 
            //          [puzzles available]
            //response: START [id] bad
        //PLAY [match_id] [id] --- adds the client with unique ID id to the match with match_id
            //response: PLAY [id] [match_id] ok
            //          [puzzle information]
            //response: PLAY [id] [match_id] bad
        //NEW [match_id] [id] [puzzle_id] --- begins a new match hosted by the client with id
            //on the match match_id on puzzle puzzle_id with puzzle_desc as any string
            //response: NEW [id] [match_id] [puzzle_id] ok
            //response: NEW [id] [match_id] [puzzle_id] [puzzle_desc] bad
        //EXIT [id] --- disconnects client with id unique ID
            //disconnect client
        //TRY [match_id] [id] [word_id] [word_attempt] --- client with id tries a word
            //response: TRY [id] [word_id] [word_attempt] ok
            //response: TRY [id] [word_id] [word_attempt] bad
        //CHALLENGE [match_id] [id] [word_id][word_attempt] --- client with id challenges a word
            //response: CHALLENGE [id] [word_id][word_attempt] granted
            //response: CHALLENGE [id] [word_id][word_attempt] failed
        //SHOWSCORE [playerid1] [score1] [playerid2] [score2]-->
        String[] tokens = input.split(" ");
        switch(tokens[0]) {
            case "START": {
                String id = tokens[1];
                if(this.clientIds.contains(id)) {
                    return "START " + id + " bad";
                } else {
                    this.clientIds.add(id);
                    //this.socketByPlayer.put(id,  )
                    return "START " + id + " ok-->" + this.matchString() + "-->" +  this.puzzlesString();
                }
            }
            case "NEW": {
                if(tokens.length==3&&tokens[1].equals("MATCH")) {
                    return "START " + tokens[2] + " ok-->" + this.matchString() + "-->" +  this.puzzlesString();
                }
                String matchID = tokens[1];
                String puzzleID = "\"" + tokens[2] + "\"";
                String clientID = tokens[3];
                if(!this.clientIds.contains(clientID)) {
                    System.out.println("bad client id, can't start new game");
                    return "NEW " + matchID + " " + puzzleID + " " + clientID + " bad";
                } else if(!this.puzzleID.containsKey(puzzleID)) {
                    System.out.println("bad puzzle id, can't start new game");
                    return "NEW " + matchID + " " + puzzleID + " " + clientID + " bad";
                } else if(this.gameIds.containsKey(matchID)) {
                    System.out.println("bad match id, can't start new game");
                    return "NEW " + matchID + " " + puzzleID + " " + clientID + " bad";
                } else {
                    Board puzzle = (Board)CrosswordParser.parse(this.puzzleID.get(puzzleID));
                    System.out.println(puzzle.getName());
                    this.gameIds.put(matchID, puzzle);
                    this.gameIdsByPlayer.put(matchID,  List.of(clientID));
                    this.boardByPlayer.put(clientID,  puzzle);
                    System.out.println(boardByPlayer.get(clientID));
                    puzzle.addPlayer(clientID);
                    for(String somePlayerID: socketByPlayer.keySet()) {
                        if(!somePlayerID.equals(clientID)) {
                            PrintWriter writeToClient = socketByPlayer.get(somePlayerID);
                            writeToClient.println("START " + tokens[2] + " ok-->" + this.matchString() + "-->" +  this.puzzlesString());
                        }
                        //send a request that will send the client to the choose state appropriately
                        
                        //writeToClient.println("NEW " + matchID + " " + puzzleID + " " + clientID + " ok");
                    }
                    return "NEW " + matchID + " " + puzzleID + " " + clientID + " ok";
                }
            }
            case "PLAY": {
                String matchID = tokens[1];
                String clientID = tokens[2];
                if(!this.clientIds.contains(clientID)) {
                    System.out.println("bad client id, can't play game");
                    return "PLAY " + matchID + " " + clientID + " bad";
                } else if(!this.gameIds.containsKey(matchID)) {
                    System.out.println("bad match id, can't start new game");
                    return "PLAY " + matchID + " " + clientID + " bad";
                } else {
                    Board puzzle = gameIds.get(matchID);
                    //synchronized(puzzle) {
                        gameIds.remove(matchID);
                        
                        String otherPlayerID = gameIdsByPlayer.get(matchID).get(0);
                        //gameIdsByPlayer.remove(matchID);
                        gameIdsByPlayer.put(matchID, List.of(otherPlayerID, clientID));
                        boardByPlayer.put(clientID, puzzle);
                        boardByPlayer.put(otherPlayerID, puzzle);
                        puzzle.addPlayer(clientID);
                        PrintWriter playerOneOut = socketByPlayer.get(otherPlayerID);
                        for(String somePlayerID: socketByPlayer.keySet()) {
                            if(!somePlayerID.equals(clientID)&&!somePlayerID.equals(otherPlayerID)) {
                                PrintWriter writeToClient = socketByPlayer.get(somePlayerID);
                                writeToClient.println("START " + tokens[2] + " ok-->" + this.matchString() + "-->" +  this.puzzlesString());
                            }
                            //send a request that will send the client to the choose state appropriately
                            
                            //writeToClient.println("NEW " + matchID + " " + puzzleID + " " + clientID + " ok");
                        }
                        playerOneOut.println("PLAY " + matchID + " " + clientID + " ok-->" + puzzle.getClientString());
                        return "PLAY " + matchID + " " + clientID + " ok-->" + puzzle.getClientString();
                    //}                 
                }
            }
            case "EXIT": {
                return "QUIT";
            }
            case "SHOWSCORE": {
                String clientID = tokens[1];
                System.out.println("showscore req");
                String otherPlayerID = "";
                Board board = boardByPlayer.get(clientID);
                for(String playerID: boardByPlayer.keySet()) {
                    Board gameBoard = boardByPlayer.get(playerID);
                    Set<String> boardPlayers = gameBoard.getPlayerIds();
                    if(boardPlayers.contains(clientID)) {
                        for(String playerInGame:boardPlayers) {
                            if(!playerInGame.equals(clientID)) {
                                otherPlayerID = playerInGame;
                            }
                        }
                    }
                }
                PrintWriter otherOut = socketByPlayer.get(otherPlayerID);
                otherOut.println("SHOWSCORE " + clientID + " " + board.getScore(clientID) + " " + otherPlayerID + " " + board.getScore(otherPlayerID));
                return "SHOWSCORE " + clientID + " " + board.getScore(clientID) + " " + otherPlayerID + " " + board.getScore(otherPlayerID);
            }
            case "TRY": {
                String matchID = "";
                String clientID = tokens[1];
                for(String gameID: gameIdsByPlayer.keySet()) {
                    List<String> players = gameIdsByPlayer.get(gameID);
                    if(players.contains(clientID)) {
                        matchID = gameID;
                    }
                }
                String wordID = tokens[2];
                String attempt = tokens[3];
                Board board = boardByPlayer.get(clientID);
                String otherPlayerID = "";
                for(String playerID: boardByPlayer.keySet()) {
                    Board gameBoard = boardByPlayer.get(playerID);
                    Set<String> boardPlayers = gameBoard.getPlayerIds();
                    if(boardPlayers.contains(clientID)) {
                        for(String playerInGame:boardPlayers) {
                            if(!playerInGame.equals(clientID)) {
                                otherPlayerID = playerInGame;
                            }
                        }
                    }
                }
                PrintWriter playerOneOut = socketByPlayer.get(otherPlayerID);
                synchronized(board) {
                    String result = board.tryWord(Integer.valueOf(wordID),  attempt,  clientID);
                    String returnStr =  "TRY " + clientID +  " " + wordID + attempt;
                    if(result.equals("failure")) {
                        return "TRY " + matchID + " " + clientID + " ok-->" + board.getClientString() + board.tryOrChallengeString();
                        //return returnStr + " bad";
                    } else {
                        String response = "TRY " + matchID + " " + clientID + " ok-->" + board.getClientString() + board.tryOrChallengeString();
                        if(board.isDone()) {
                            response = response + "\nSHOWSCORE " + clientID + " " + board.getScore(clientID) + " " + otherPlayerID + " " + board.getScore(otherPlayerID);
                        }
                        playerOneOut.println(response);
                        return response;
                        //return returnStr + " " + clientID + " ok";
                    }
                }
            }
            case "CHALLENGE": {
                String matchID = "";
                String clientID = tokens[1];
                for(String gameID: gameIdsByPlayer.keySet()) {
                    List<String> players = gameIdsByPlayer.get(gameID);
                    if(players.contains(clientID)) {
                        matchID = gameID;
                    }
                }
                String wordID = tokens[2];
                String attempt = tokens[3];
                Board board = boardByPlayer.get(clientID);
                String otherPlayerID = "";
                for(String playerID: boardByPlayer.keySet()) {
                    Board gameBoard = boardByPlayer.get(playerID);
                    Set<String> boardPlayers = gameBoard.getPlayerIds();
                    if(boardPlayers.contains(clientID)) {
                        for(String playerInGame:boardPlayers) {
                            if(!playerInGame.equals(clientID)) {
                                otherPlayerID = playerInGame;
                            }
                        }
                    }
                }
                PrintWriter playerOneOut = socketByPlayer.get(otherPlayerID);
                synchronized(board) {
                    String result = board.challengeWord(Integer.valueOf(wordID),  attempt,  clientID);
                    String returnStr =  "CHALLENGE " + clientID +  " " + wordID + attempt;
                    if(result.equals("operation failed")) {
                        return "CHALLENGE " + matchID + " " + clientID + " ok-->" + board.getClientString() + board.tryOrChallengeString();
                        //return returnStr + " bad";
                    } else if(result.equals("YourWordCorrect Confirmed 2")){
                        String response = "CHALLENGE " + matchID + " " + clientID + " ok-->" + board.getClientString() + board.tryOrChallengeString();
                        if(board.isDone()) {
                            response = response + "\nSHOWSCORE " + clientID + " " + board.getScore(clientID) + " " + otherPlayerID + " " + board.getScore(otherPlayerID);
                        }
                        playerOneOut.println(response);
                        return response;
                        //return returnStr + " granted";
                    } else {
                        playerOneOut.println("CHALLENGE " + matchID + " " + clientID + " ok-->" + board.getClientString()+ board.tryOrChallengeString());
                        return "CHALLENGE " + matchID + " " + clientID + " ok-->" + board.getClientString() + board.tryOrChallengeString();
                        //return returnStr + " failed";
                    }
                }
            }
            /*case "WAIT": {
                String clientID = tokens[1];
                //String matchID = tokens[1];
                Board puzzle = boardByPlayer.get(clientID);
                System.out.println(boardByPlayer.keySet());
                System.out.println(clientID);
                //System.out.println(matchID);
                System.out.println(boardByPlayer.get(clientID));
                synchronized(puzzle) {
                    boolean unmatched = gameIds.containsKey(matchID);
                    while(unmatched){
                        try {
                            wait();
                            unmatched = gameIds.containsKey(matchID);
                        } catch(InterruptedException e) {
                            System.out.println("error");
                        }
                    }
                    
                }
                //System.out.println(puzzle.getName());
                puzzle.addPlayerID(clientID, true);
                //System.out.println("w");
                return "PLAY " + clientID + " ok \n " + puzzle.getClientString();
            }*/
            //Should never get to this point
            default:
                throw new IllegalArgumentException();
        }
        //return "resp";
    }


    }

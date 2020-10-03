package crossword;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;


public class ServerClientTest {
    //Testing Strategy: (tests at the bottom)
        //input folder-- contains 1 puzzle file, contains 1+ puzzle file
        //The following features were tested manually, through creating a client 
        // and two servers as explained in the pset handout. 
        
        //Start state
        //--text box and instructions for user id
        //Choose state
        //--display instructions for user
        //-- list available matches and puzzles
        //-- refresh page to account for new and removed matches
        //Wait state
        //-- remain in this state until another player joins the match
        //Play state
        //--display the crossword puzzle
        //--which words were entered by which player, 
        //--what the id of each word is, 
        //--how many challenge points each user has, 
        //--which words have been confirmed
        //Show score state
        //--display winner and both individuals' scores
        //--transition to new match or exit
    
    
    
private static final String LOCALHOST = "127.0.0.1";
    
    private static final int MAX_CONNECTION_ATTEMPTS = 6;
    
    private static Socket returnOut(String folder) throws IOException {
        final Thread thread = startServer(folder);
        final Socket socket = connectToServer(thread);
        
        return socket;
    }
    

    /* Start server on its own thread. */
    private static Thread startServer(String ans) {
        Thread thread = new Thread(() ->  {
            try {
                String [] args= {ans};
                Server.main(args);
            } catch (Exception e) {
                throw new RuntimeException("server threw exception");
            }
        });
        thread.start();
        return thread;
    }
    
    /* Connect to server with retries on failure. */
    private static Socket connectToServer(final Thread serverThread) throws IOException {
        final int port = 4949;
        assertTrue(port > 0, "server.port() returned " + port);
        for (int attempt = 0; attempt < MAX_CONNECTION_ATTEMPTS; attempt++) {
            try { Thread.sleep(attempt * 10); } catch (InterruptedException ie) { }
            if ( ! serverThread.isAlive()) {
                throw new IOException("server thread no longer running");
            }
            try {
                final Socket socket = new Socket(LOCALHOST, port);
                socket.setSoTimeout(1000 * 3);
                return socket;
            } catch (ConnectException ce) {
                // may try again
            }
        }
        throw new IOException("unable to connect after " + MAX_CONNECTION_ATTEMPTS + " attempts");
    }
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }

    @Test //1+ puzzle file (contains invalid puzzle)
    public void testConnection() throws IOException, URISyntaxException{
        Socket socket = returnOut("puzzles");
        BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
        String ans= socketIn.readLine();
        assert ans.contains("Hints");
        assert ans.contains("Numbers");
        assert ans.contains("Characters");
        assert ans.contains("Cells");
        socket.close();
        socketIn.close();
        
    }
    
    @Test //1 puzzle file
    public void testConnectionTwo() throws IOException, URISyntaxException{
        Socket socket = returnOut("puzzlesone");
        BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
        String ans= socketIn.readLine();
        assert ans.contains("Hints");
        assert ans.contains("Numbers");
        assert ans.contains("Characters");
        assert ans.contains("Cells");
        socket.close();
        socketIn.close();
        
    }

}

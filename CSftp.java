
import java.lang.System;
import java.io.IOException;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

//
// This is an implementation of a simplified version of a command 
// line ftp client. The program always takes two arguments
//


public class CSftp
{
    static final int MAX_LEN = 255;
    static final int ARG_CNT = 2;

    public static void main(String [] args)
    {
	byte cmdString[] = new byte[MAX_LEN];

	// Get command line arguments and connected to FTP
	// If the arguments are invalid or there aren't enough of them
        // then exit.

	if (args.length != ARG_CNT) {
	    System.out.print("Usage: cmd ServerAddress ServerPort\n");
	    return;
	}

	try {
	    for (int len = 1; len > 0;) {
		System.out.print("csftp> ");
		len = System.in.read(cmdString);
		if (len <= 0) 
		    break;
		// Start processing the command here.

		//user open

		//
		Socket echoSocket = new Socket(args[0], Integer.parseInt(args[1]));
		PrintWriter out =
        new PrintWriter(echoSocket.getOutputStream(), true);
    	BufferedReader in =
        	new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
    	BufferedReader stdIn =
        	new BufferedReader(new InputStreamReader(System.in));

        String userInput;
	while ((userInput = stdIn.readLine()) != null) {
   		 out.println(userInput);
    System.out.println("<-- " + in.readLine()); 
}
		
		String input = new String(cmdString).replaceAll("\\s", "");
		System.out.println("--> " + input);


		// System.out.println("900 Invalid command.");
	    }
	} catch (IOException exception) {
	    System.err.println("998 Input error while reading commands, terminating.");
	}
    }
}

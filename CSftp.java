
import java.lang.System;
import java.io.IOException;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
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

			Socket echoSocket = new Socket(args[0], Integer.parseInt(args[1]));
			PrintWriter out =
			new PrintWriter(echoSocket.getOutputStream(), true);
			BufferedReader in =
			new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
			BufferedReader stdIn =
			new BufferedReader(new InputStreamReader(System.in));

			String connected = in.readLine();
			if(!connected.startsWith("220 ") || connected.startsWith("120 ") || connected.startsWith("421 ")) {
				System.out.println("920 Control connection to " +  args[0] + " on port " + args[1] + " failed to open.");
        		//exit
			}
			System.out.println("<-- " + connected); 
			System.out.print("csftp> ");

			String userInput;
			while ((userInput = stdIn.readLine()) != null) {
	 //    for (int len = 1; len > 0;) {
		// System.out.print("csftp> ");
		// // len = System.in.read(cmdString);
		// len = userInput.length();
		// if (len <= 0) 
		//     break;
		// Start processing the command here.

		//user open

		//



        // while (userInput != null) {
				boolean skipCommand = false;
				if(userInput.startsWith("user ")){
					userInput = "USER "+ userInput.substring(5, userInput.length());
				}

				else if(userInput.startsWith("pw ")){
					userInput = "PASS " + userInput.substring(3, userInput.length());
				}

				else if(userInput.startsWith("quit")){
					userInput = "QUIT";
					out.println(userInput);
					System.out.println("--> " + userInput);
					System.out.println("<-- " + in.readLine());
					System.exit(0);
				}

				else if(userInput.startsWith("cd ")){
					userInput = "CWD " + userInput.substring(3, userInput.length());
				}

				else if(userInput.startsWith("get ")){
					System.out.println("--> PASV");
					out.println("PASV");
					String pasvIP = in.readLine();
					System.out.println("<-- " + pasvIP); 

					if(pasvIP.startsWith("227 ")) {
						pasvIP =   pasvIP.substring(27, pasvIP.length() - 1);
						System.out.println(pasvIP);

						String[] ip = pasvIP.split(",");

						String connectIP = ip[0] + "." + ip[1] + "." + ip[2] +  "." + ip[3];
						int connectPort = Integer.parseInt(ip[4]) * 256 + Integer.parseInt(ip[5]);
					
						Socket pasvSocket = new Socket(connectIP, connectPort);
						PrintWriter pasvOut =
						new PrintWriter(pasvSocket.getOutputStream(), true);
						InputStream inStream = pasvSocket.getInputStream();
						DataInputStream dataReader = new DataInputStream(inStream);
						
						File downloadFile1 = new File("./" + userInput.substring(4, userInput.length()));
						downloadFile1.createNewFile();

						out.println("TYPE I");
						System.out.println("--> " + "TYPE I");
						System.out.println("<-- " + in.readLine());
						out.println("RETR " + userInput.substring(4, userInput.length()));
						System.out.println("--> " + "RETR " + userInput.substring(4, userInput.length()));
						String inReader = in.readLine();
						System.out.println("<-- " + inReader);
						inReader = in.readLine();
						System.out.println("<-- " + inReader);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();

						skipCommand = true;


						byte bytes[] = new byte[1];
						if (inStream != null) {

							FileOutputStream outputStream1 = new FileOutputStream(downloadFile1);
							DataOutputStream bos = new DataOutputStream(outputStream1);
							int bytesRead = inStream.read(bytes, 0, bytes.length);

							do {
								baos.write(bytes);
								bytesRead = inStream.read(bytes);
							} while (bytesRead != -1);

							bos.write(baos.toByteArray());
							bos.flush();
							bos.close();
						}

						pasvOut.println("QUIT");

						// System.out.println("--> " + "QUIT");
						// System.out.println("<-- " + pasvIn.readLine());
						
						
						// System.out.println("<-- " + pasvIn.readLine());

						// inpustStreamReader.close();
						pasvOut.close();
						// pasvIn.close();
						// pasvIn.close();
						// outputStream1.close();
						pasvSocket.close();
					}
				}

				else if(userInput.startsWith("dir")){
					System.out.println("--> PASV");
					out.println("PASV");
					String pasvIP = in.readLine();
					System.out.println("<-- " + pasvIP); 

					if(pasvIP.startsWith("227 ")) {
						pasvIP =   pasvIP.substring(27, pasvIP.length() - 1);
						System.out.println(pasvIP);

						String[] ip = pasvIP.split(",");
						// for (String s: ip) {
						// 	System.out.println(s);
						// }

						String connectIP = ip[0] + "." + ip[1] + "." + ip[2] +  "." + ip[3];
						int connectPort = Integer.parseInt(ip[4]) * 256 + Integer.parseInt(ip[5]);

						Socket pasvSocket = new Socket(connectIP, connectPort);
						PrintWriter pasvOut =
						new PrintWriter(pasvSocket.getOutputStream(), true);
						InputStreamReader inpustStreamReader = new InputStreamReader(pasvSocket.getInputStream());
						BufferedReader pasvIn =
						new BufferedReader(inpustStreamReader);

						out.println("LIST");
						System.out.println("--> " + "LIST");
						String inReader = in.readLine();
						System.out.println("<-- " + inReader);
						inReader = in.readLine();
						System.out.println("<-- " + inReader);

						skipCommand = true;
						String str = pasvIn.readLine();
						while(str!=null && str.length()!=0) {
							System.out.println("<-- " + str); 
							str=pasvIn.readLine();
						}
						
						pasvOut.println("QUIT");
						// System.out.println("--> " + userInput);
						System.out.println("<-- " + pasvIn.readLine());

						inpustStreamReader.close();
						pasvIn.close();
						pasvSocket.close();

					}
					//else errors

				}
				else if (userInput.startsWith("#") || userInput.length() == 0){
					skipCommand = true;
				}

				// else {
				// 	System.out.println("900 Invalid command.");
				// 	skipCommand = true;
				// }
				if (!skipCommand) {
					out.println(userInput);
					System.out.println("--> " + userInput);
					System.out.println("<-- " + in.readLine());
				}
					// String output = "<-- "; 
					// String line;
				// 	while ((line = in.readLine()) != null)
    //     {

    //         System.out.println(line);

    //     }
				// 		output += line + System.getProperty("line.separator");
 			// 	// 	    System.out.println(line);
 			// 	// 	    line = in.readLine();
				// 	// }
				// }
				System.out.print("csftp> ");
		// len = System.in.read(cmdString);
			}

			// String input = new String(cmdString).replaceAll("\\s", "");
			// System.out.println("--> " + input);




		} catch (IOException exception) {
			System.err.println("998 Input error while reading commands, terminating.");
		}
	}
}

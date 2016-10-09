
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

	public static void printInputStream(BufferedReader reader) throws IOException {
		
// String str = reader.readLine();
		// while(str!=null) {
		// 	System.out.println("<-- " + str); 
		// 	str=reader.readLine();
		// }
	for (String line = reader.readLine(); line != null; line = reader.readLine()) {
		System.out.println("<-- " + line);
	}
}

	public static boolean incorrectNumberArguements() {
		System.out.println("901 Incorrect number of arguments.");
		return true;
	}

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
			// InputStream in = echoSocket.getInputStream();
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
				userInput = userInput.trim();
	 
				boolean skipCommand = false;
				if(userInput.startsWith("user ")){
					//assume that usernames can include all characters so don't check for number of arguements
					userInput = "USER "+ userInput.substring(5, userInput.length());
				}

				else if(userInput.startsWith("pw ")){
					userInput = "PASS " + userInput.substring(3, userInput.length());
				}

				else if(userInput.startsWith("quit")){
					if(userInput.length() > 4) {
						skipCommand = CSftp.incorrectNumberArguements();
					} else {
						userInput = "QUIT";
						out.println(userInput);
						System.out.println("--> " + userInput);
						System.out.println("<-- " + in.readLine());
						System.exit(0);
					}
				}

				else if(userInput.startsWith("cd")){
					if( userInput.length() < 4  || userInput.substring(3, userInput.length()).contains(" ")) {
						skipCommand = CSftp.incorrectNumberArguements();
					} else {
						userInput = "CWD " + userInput.substring(3, userInput.length());
					}
				}

				else if(userInput.startsWith("get")){
					if(userInput.length() < 5 ) {
						skipCommand = CSftp.incorrectNumberArguements();
					} else {
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
						// CSftp.printInputStream(in);
						// String str = in.readLine();
						// while(str!=null) {
						// 	System.out.println("<-- " + str); 
						// 	str=in.readLine();
						// }

						out.println("RETR " + userInput.substring(4, userInput.length()));
						System.out.println("--> " + "RETR " + userInput.substring(4, userInput.length()));
						// CSftp.printInputStream(in);
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
				}

				else if(userInput.startsWith("dir")){
					userInput = userInput.trim();
					if(userInput.length() > 3) {
						skipCommand = CSftp.incorrectNumberArguements();
					} else {
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
							// CSftp.printInputStream(in);
							String inReader = in.readLine();
							System.out.println("<-- " + inReader);
							inReader = in.readLine();
							System.out.println("<-- " + inReader);
							// printInputStream(in);

							skipCommand = true;
							CSftp.printInputStream(pasvIn);
							// String str = pasvIn.readLine();
							// while(str!=null && str.length()!=0) {
							// 	System.out.println("<-- " + str); 
							// 	str=pasvIn.readLine();
							// }

							pasvOut.println("QUIT");
						// System.out.println("--> " + userInput);
							System.out.println("<-- " + pasvIn.readLine());

							inpustStreamReader.close();
							pasvIn.close();
							pasvSocket.close();

						}
					}
					//else errors

				}
				else if (userInput.startsWith("#") || userInput.length() == 0){
					skipCommand = true;
				}

				else {
					System.out.println("900 Invalid command.");
					skipCommand = true;
				}

				if (!skipCommand) {
					out.println(userInput);
					System.out.println("--> " + userInput);
					System.out.println("<-- " + in.readLine());
					// CSftp.printInputStream(in);
				}

				System.out.print("csftp> ");

			}





		} catch (IOException exception) {
			System.err.println("998 Input error while reading commands, terminating.");
		}
	}
}

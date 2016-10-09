
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
import java.util.Timer;
import java.net.SocketTimeoutException;
//
// This is an implementation of a simplified version of a command 
// line ftp client. The program always takes two arguments
//


public class CSftp
{
	static final int MAX_LEN = 255;
	static final int ARG_CNT = 2;

	public static Socket getPASVSocket(PrintWriter writer, BufferedReader reader){
		String connectIP = "hello";
			int connectPort = 33;
		Socket pasvSocket = new Socket();
		try {	 
			CSftp.sendCommandControlSocket(writer, "PASV");			
			String pasvIP = CSftp.readInputLine(reader);

			if(pasvIP.startsWith("227 ")) {
				pasvIP =  pasvIP.substring(27, pasvIP.length() - 1);
				String[] ip = pasvIP.split(",");
				connectIP = ip[0] + "." + ip[1] + "." + ip[2] +  "." + ip[3];
				connectPort = Integer.parseInt(ip[4]) * 256 + Integer.parseInt(ip[5]);
				pasvSocket = new Socket(connectIP, connectPort);
			}
		} 
		catch (IOException exception) {
			System.out.println("930 Data transfer connection to " + connectIP + " on port " + connectPort + " failed to open.");
			return null;
		}
		if(pasvSocket != null)
			return pasvSocket;
		else
			return null;
	}

	public static void printPASVInputStream(BufferedReader  pasvReader) throws SocketTimeoutException {
		try {

			String str = pasvReader.readLine();
			while(str!=null && str.length()!=0) {
				System.out.println("<-- " + str); 
				str=pasvReader.readLine();
			}
		} catch (IOException exception) {
			System.out.println("935 Data transfer connection I/O error, closing data connection.");
		}

	}

	public static void sendCommandControlSocket(PrintWriter writer, String command) {
		writer.println(command);
		System.out.println("--> " + command);
	}

	public static void emptyInputStream(BufferedReader reader, Socket socket) throws IOException {
		try {	
			socket.setSoTimeout(500);
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			}
		} catch (SocketTimeoutException exception) {
			return;
		}
	}

	public static String readInputLine (BufferedReader reader) {
		String line = "";
		try {
		 	line = reader.readLine();
		 	return line;
		} catch (IOException exception) {
			System.out.println("925 Control connection I/O error, closing control connection.");
			System.exit(0);
		}
		return line;
	}

	public static void printInputStream(BufferedReader reader, Socket socket) {
		try {	
			socket.setSoTimeout(500);
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				System.out.println("<-- " + line);
			}
		}
		catch (SocketTimeoutException exception) {
			return;
		} catch (IOException exception) {
			System.out.println("925 Control connection I/O error, closing control connection.");
			System.exit(0);
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

			Socket controlSocket = new Socket(args[0], Integer.parseInt(args[1]));
			PrintWriter out =
			new PrintWriter(controlSocket.getOutputStream(), true);
			// InputStream in = echoSocket.getInputStream();
			BufferedReader in =
			new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
			BufferedReader stdIn =
			new BufferedReader(new InputStreamReader(System.in));
			String connected = "";
			try {
				controlSocket.setSoTimeout(30*1000);
				connected = in.readLine();

			}
			catch (SocketTimeoutException exception) {
				System.out.println("920 Control connection to " +  args[0] + " on port " + args[1] + " failed to open.");
				System.exit(0);
			}

			if(!connected.startsWith("220 ") || connected.startsWith("120 ") || connected.startsWith("421 ")) {
				System.out.println("920 Control connection to " +  args[0] + " on port " + args[1] + " failed to open.");
				System.exit(0);
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
						CSftp.sendCommandControlSocket(out, "QUIT");
						CSftp.printInputStream(in, controlSocket);
						controlSocket.close();
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
						// System.out.println("--> PASV");
						// out.println("PASV");
						// String pasvIP = in.readLine();
						// // System.out.println("<-- " + pasvIP); 
						// CSftp.sendCommandControlSocket(out, "PASV");
						// CSftp.printInputStream(in, controlSocket);

						// if(pasvIP.startsWith("227 ")) {
						// 	pasvIP =   pasvIP.substring(27, pasvIP.length() - 1);
						// 	// System.out.println(pasvIP);

						// 	String[] ip = pasvIP.split(",");

						// 	String connectIP = ip[0] + "." + ip[1] + "." + ip[2] +  "." + ip[3];
						// 	int connectPort = Integer.parseInt(ip[4]) * 256 + Integer.parseInt(ip[5]);

							// Socket pasvSocket = new Socket(connectIP, connectPort);

							Socket pasvSocket = CSftp.getPASVSocket(out, in);
							if (pasvSocket != null) {
							PrintWriter pasvOut =
							new PrintWriter(pasvSocket.getOutputStream(), true);
							InputStream inStream = pasvSocket.getInputStream();
							DataInputStream dataReader = new DataInputStream(inStream);

							

							File downloadFile1 = new File("./" + userInput.substring(4, userInput.length()));
							downloadFile1.createNewFile();

							out.println("TYPE I");
							System.out.println("--> " + "TYPE I");
							System.out.println("<-- " + in.readLine());
							String fileName = userInput.substring(4, userInput.length());

							out.println("RETR " + fileName);
							

							System.out.println("--> " + "RETR " + fileName);
							CSftp.printInputStream(in, controlSocket);
							ByteArrayOutputStream baos = new ByteArrayOutputStream();

							skipCommand = true;


							byte bytes[] = new byte[1];
							if (inStream != null) {
								try {
									pasvSocket.setSoTimeout(30*1000);
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
									outputStream1.close();
								}
								catch (SocketTimeoutException exception) {
									CSftp.emptyInputStream(in, controlSocket);
									System.out.println("935 Data transfer connection I/O error, closing data connection.");
								}
								catch (IOException exception) {
									System.out.println("910 Access to local file " + fileName + " denied.");
								}
							}
							pasvOut.println("QUIT");
							pasvOut.close();
							pasvSocket.close();
						}
					}
					}
				

				else if(userInput.startsWith("dir")){
					userInput = userInput.trim();
					if(userInput.length() > 3) {
						skipCommand = CSftp.incorrectNumberArguements();
					} else {
						// System.out.println("--> PASV");
						// out.println("PASV");
						// String pasvIP = in.readLine();
						// System.out.println("<-- " + pasvIP); 

						// if(pasvIP.startsWith("227 ")) {
						// 	pasvIP =   pasvIP.substring(27, pasvIP.length() - 1);
						// 	String[] ip = pasvIP.split(",");
						// 	String connectIP = ip[0] + "." + ip[1] + "." + ip[2] +  "." + ip[3];
						// 	int connectPort = Integer.parseInt(ip[4]) * 256 + Integer.parseInt(ip[5]);

							Socket pasvSocket = CSftp.getPASVSocket(out, in);
							if (pasvSocket != null) {
							PrintWriter pasvOut =
							new PrintWriter(pasvSocket.getOutputStream(), true);
							InputStreamReader inputStreamReader = new InputStreamReader(pasvSocket.getInputStream());
							BufferedReader pasvIn =
							new BufferedReader(inputStreamReader);
							skipCommand = true;
							try {
								pasvSocket.setSoTimeout(30 * 1000);
								CSftp.sendCommandControlSocket(out, "LIST");
								CSftp.printInputStream(in, controlSocket);
								CSftp.printPASVInputStream(pasvIn);
								
							}
							catch (SocketTimeoutException exception) {
								CSftp.emptyInputStream(in, controlSocket);
								System.out.println("935 Data transfer connection I/O error, closing data connection.");
							}
							pasvOut.println("QUIT");
							inputStreamReader.close();
							pasvIn.close();
							pasvSocket.close();

						}
					
					}

				}
				else if (userInput.startsWith("#") || userInput.length() == 0){
					skipCommand = true;
				}

				else {
					System.out.println("900 Invalid command.");
					skipCommand = true;
				}

				if (!skipCommand) {
					CSftp.sendCommandControlSocket(out, userInput);
					CSftp.printInputStream(in, controlSocket);
				}

				System.out.print("csftp> ");

			}

		} catch (IOException exception) {
			// echoSocket.close();
			System.err.println("998 Input error while reading commands, terminating.");
		}
	}
}

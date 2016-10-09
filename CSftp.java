
import java.lang.System;
import java.io.IOException;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.net.SocketTimeoutException;
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
	// Get command line arguments and connected to FTP
	// If the arguments are invalid or there aren't enough of them
        // then exit.

		if (args.length != ARG_CNT) {
			System.out.print("Usage: cmd ServerAddress ServerPort\n");
			return;
		}

		try {
			//initiallize socket, stream reader and print writer
			Socket controlSocket = new Socket(args[0], Integer.parseInt(args[1]));
			PrintWriter out =
			new PrintWriter(controlSocket.getOutputStream(), true);
			BufferedReader in =
			new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
			BufferedReader stdIn =
			new BufferedReader(new InputStreamReader(System.in));
			
			// make sure socket connection is established
			String connected = "";
			try {
				controlSocket.setSoTimeout(30*1000);
				connected = in.readLine();
				System.out.println("<-- " + connected);
			}
			catch (SocketTimeoutException exception) {
				System.out.println("920 Control connection to " +  args[0] + " on port " + args[1] + " failed to open.");
				System.out.println("Socket time out");
				System.exit(0);
			}


			if(!connected.startsWith("220") || connected.startsWith("120 ") || connected.startsWith("421 ")) {
				System.out.println("920 Control connection to " +  args[0] + " on port " + args[1] + " failed to open.");
				System.exit(0);
			}
			
			CSftp.printInputStream(in, controlSocket); 
			System.out.print("csftp> ");

			// start reading user commands
			String userInput;
			while ((userInput = stdIn.readLine()) != null) {
				userInput = userInput.trim();

				boolean skipCommand = false;
				if(userInput.startsWith("user ")){
					if( userInput.length() < 6)  {
					skipCommand = CSftp.incorrectNumberArguements();
					} else {
						userInput = "USER "+ userInput.substring(5, userInput.length());
					}
				}

				else if(userInput.startsWith("pw ")){
					if(userInput.length() < 4) {
						skipCommand = CSftp.incorrectNumberArguements();
					} else {
						userInput = "PASS " + userInput.substring(3, userInput.length());
					}
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
						//create a data transfer connection and initialize print writer and dataInputStream
						Socket pasvSocket = CSftp.getPASVSocket(out, in, controlSocket);
						if (pasvSocket != null) {
							PrintWriter pasvOut =
							new PrintWriter(pasvSocket.getOutputStream(), true);
							InputStream inStream = pasvSocket.getInputStream();
							DataInputStream dataReader = new DataInputStream(inStream);
							
							// create file on local machine
							File downloadFile1 = new File("./" + userInput.substring(4, userInput.length()));
							downloadFile1.createNewFile();

							CSftp.sendCommandControlSocket(out, "TYPE I");
							CSftp.printInputStream(in, controlSocket);
							String fileName = userInput.substring(4, userInput.length());
							String command = "RETR " + fileName;
							CSftp.sendCommandControlSocket(out, command);
							CSftp.printInputStream(in, controlSocket);


							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							//skipCommand is truu because we already executed it so we don't have to do it again
							//at the end of the loop
							skipCommand = true;

							//r
							byte bytes[] = new byte[1];
							if (inStream != null) {
								try {
									//read contents of file on server into byte[]
									pasvSocket.setSoTimeout(30*1000);
									FileOutputStream outputStream1 = new FileOutputStream(downloadFile1);
									DataOutputStream bos = new DataOutputStream(outputStream1);
									int bytesRead = inStream.read(bytes, 0, bytes.length);

									do {
										baos.write(bytes);
										bytesRead = inStream.read(bytes);
									} while (bytesRead != -1);
									// wrte bytes to local file
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
						//create a data transfer connection and initialize print writer and dataInputStream
						Socket pasvSocket = CSftp.getPASVSocket(out, in, controlSocket);
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
					System.out.println("<-- " + "900 Invalid command.");
					skipCommand = true;
				}

				if (!skipCommand) {
					System.out.println("in Skip command");
					CSftp.sendCommandControlSocket(out, userInput);
					CSftp.printInputStream(in, controlSocket);
				}

				System.out.print("csftp> ");

			}
			out.close();
			in.close();
			stdIn.close();
			controlSocket.close();

		} catch (IOException exception) {
			System.err.println("998 Input error while reading commands, terminating.");
		}

		catch (Exception exception) {
			System.err.println("999 Processing error. " + exception.getMessage());
		}
	}

	// create a pasv data transfer connection
	// Parameters are Printwriter, BufferReader and Socket for the control connection
	// Returns Socket
	public static Socket getPASVSocket(PrintWriter writer, BufferedReader reader, Socket controlSocket){
		String connectIP = "";
		int connectPort = 0;
		Socket pasvSocket = new Socket();
		try {	 
			CSftp.sendCommandControlSocket(writer, "PASV");			
			String pasvIP = CSftp.printInputStream(reader, controlSocket);

			if(pasvIP.startsWith("227 ")) {
				pasvIP = pasvIP.substring(pasvIP.indexOf("(") + 1);
				pasvIP = pasvIP.substring(0, pasvIP.indexOf(")"));
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

	// print contents for data transfer connection
	public static void printPASVInputStream(BufferedReader  pasvReader) throws SocketTimeoutException, IOException {
			String str = pasvReader.readLine();
			while(str!=null && str.length()!=0) {
				System.out.println("<-- " + str); 
				str=pasvReader.readLine();
			}
	}

	// send command to ftp server
	public static void sendCommandControlSocket(PrintWriter writer, String command) {
		writer.print(command + "\r\n");
		System.out.println("--> " + command);
		writer.flush();
	}

	
	// empty the control bufferReader
	public static void emptyInputStream(BufferedReader reader, Socket socket) throws IOException {
		try {	
			socket.setSoTimeout(500);
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			}
		} catch (SocketTimeoutException exception) {
			return;
		}
	}

	// Read a single line from the control bufferReader
	public static String readInputLine (BufferedReader reader) {
		String line = "";
		try {
			line = reader.readLine();
			System.out.println("<-- " + line);
			return line;
		} catch (IOException exception) {
			System.out.println("925 Control connection I/O error, closing control connection.");
			System.exit(0);
		}
		return line;
	}
	// Read entire control bufferReader and return the last line
	public static String printInputStream(BufferedReader reader, Socket socket) {
		
		String line = "";
		try {	
			socket.setSoTimeout(1000);
			for (line = reader.readLine(); line != null; line = reader.readLine()) {
				System.out.println("<-- " + line);
			}
		}
		catch (SocketTimeoutException exception) {
			return line;
		} catch (IOException exception) {
			System.out.println("925 Control connection I/O error, closing control connection.");
			System.exit(0);
		}
		return line;
	}
	// print error message for incorrect number of arguements
	public static boolean incorrectNumberArguements() {
		System.out.println("901 Incorrect number of arguments.");
		return true;
	}
}

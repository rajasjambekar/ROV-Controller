import java.io.*;
import java.net.Socket;

/*
 * Sends data over data output stream to client (arduino)
 * Data transfer format is code:value in string format, on each iteration of data transfer
 * 
 */
public class TCPSender {
	private Socket client;
	private OutputStream outToServer;
	private DataOutputStream out;
	private String msg = "";
	
	//get output stream for client and set up a new dataoutputstream
	public TCPSender(Socket client) throws Exception {
		this.client = client;
		outToServer = this.client.getOutputStream();
 	   	out = new DataOutputStream(outToServer);
	}
	
	//pass code and data for arduino
	//convert both to string type for easy transfer
	public void sendData(int code, int value) {
		StringBuilder str = new StringBuilder();
		str.append("s");
		str.append(code);
		str.append(":");
		str.append(value);
		str.append("e");
		msg = str.toString();
		send();
	}
	
	//write chars on dataoutputstream object
	private void send() {
		try {
			out.writeChars(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/*
 * Receives data over TCP from client (arduino)
 * Checks the packet type and sets the data in the container object of that type
 * 
 */
public class TCPReceiver implements Runnable{
	private Socket client;
	private InputStream inFromServer;
	private DataInputStream in;
	private ThreadEnable threadEnable;
	
	public TCPReceiver(Socket client, ThreadEnable threadEnable) throws Exception {
		this.client = client;
		this.threadEnable = threadEnable;
		inFromServer = this.client.getInputStream();
		in = new DataInputStream(inFromServer);
	}

	@Override
	public void run() {
		while(threadEnable.getThreadState() && threadEnable.getTcpState()) {
			String packet = fetchDataPacket();
			if(packet!="") {
				//valid packet received
				//classify data packet
				classifyPacket(packet);
			}
		}
	}
	
	private String fetchDataPacket() {
		StringBuilder received = new StringBuilder();
		char c;
 	    try {
 	    	//check if data available and packet header encountered
 	    	if(in.available()>0 && (c = (char) in.read())=='s') {
 	    		//get data until no data available or packet end encountered
 				while(in.available()>0 && ((c = (char) in.read())!='e')) {
 					received.append(c);
 					if(in.available()==0 && c!='e') {
 						//if no data available and packet end not encountered discard data
 						received = null;
 					}
 				}
 				if(received!=null) {
 					return received.toString();
 				}
 	    	}
		} catch (Exception e) {
			e.printStackTrace();
			threadEnable.setTcpState(false);
		}
 	    return "";
	}
	
	//classify packet based on code and update value in its container object
	private void classifyPacket(String msg) {
		String parts[] = msg.split(":");
		int code = Integer.parseInt(parts[0]);
		switch(code) {
		case 2200:	//Temperature Sensor
			break;
		case 3200:	//Pressure Sensor
			break;
		case 8200:	//Gyro Sensor
			break;
		}
	}

	private void sleep(int time) {
		try {
			TimeUnit.MILLISECONDS.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

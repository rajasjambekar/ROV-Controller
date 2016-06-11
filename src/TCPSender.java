import java.io.*;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

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
	private ThreadEnable threadEnable;
	private int TH_CODE_EN = 1999;
	private int TH_CODE_MIN = 1000;
	private int TH_CODE_MAX = 2000;
	private int TEMP_CODE_MIN = 2000;
	private int TEMP_CODE_MAX = 3000;
	private int PS_CODE_MIN = 3000;
	private int PS_CODE_MAX = 4000;
	private int DCM_CODE_MIN = 4000;
	private int DCM_CODE_MAX = 5000;
	private int LED_CODE_MIN = 5000;
	private int LED_CODE_MAX = 6000;
	private int CAMS_CODE_MIN = 6000;
	private int CAMS_CODE_MAX = 7000;
	
	private int TH_VAL_EN_MIN = 0;
	private int TH_VAL_EN_MAX = 1;
	private int TH_VAL_MIN = 1150;
	private int TH_VAL_MAX = 1850;
	private int TEMP_VAL_MIN = 0;
	private int TEMP_VAL_MAX = 1;
	private int PS_VAL_MIN = 0;
	private int PS_VAL_MAX = 1;
	private int DCM_VAL_MIN = 0;
	private int DCM_VAL_MAX = 255;
	private int LED_VAL_MIN = 0;
	private int LED_VAL_MAX = 1;
	private int CAMS_VAL_MIN = 0;
	private int CAMS_VAL_MAX = 180;
	
	//get output stream for client and set up a new dataoutputstream
	public TCPSender(Socket client, ThreadEnable threadEnable) throws Exception {
		this.client = client;
		this.threadEnable = threadEnable;
		if(client!=null) {
			outToServer = this.client.getOutputStream();
	 	   	out = new DataOutputStream(outToServer);
		}
	}
	
	//pass code and data for arduino
	//convert both to string type for easy transfer
	public void sendData(int code, int value) {
		//check if code and data are valid
		if(checkValues(code, value)) {
			StringBuilder str = new StringBuilder();
			str.append("s");
			str.append(code);
			str.append(":");
			str.append(value);
			str.append("e");
			msg = str.toString();
			System.out.println(msg);
			//check if client is initialized
			if(client!=null)
				send();
		}
	}
	
	//checks the validity of the code and data being sent over tcp
	private boolean checkValues(int code, int value) {
		if(code==TH_CODE_EN) {
			if(value>=TH_VAL_EN_MIN && value<=TH_VAL_EN_MAX)
				return true;
		}
		if(code>=TH_CODE_MIN && code<TH_CODE_MAX) {
			if(value>=TH_VAL_MIN && value<=TH_VAL_MAX)
				return true;
		}
		if(code>=TEMP_CODE_MIN && code<TEMP_CODE_MAX) {
			if(value>=TEMP_VAL_MIN && value<=TEMP_VAL_MAX)
				return true;
		}
		if(code>=PS_CODE_MIN && code<PS_CODE_MAX) {
			if(value>=PS_VAL_MIN && value<=PS_VAL_MAX)
				return true;
		}
		if(code>=DCM_CODE_MIN && code<DCM_CODE_MAX) {
			if(value>=DCM_VAL_MIN && value<=DCM_VAL_MAX)
				return true;
		}
		if(code>=LED_CODE_MIN && code<LED_CODE_MAX) {
			if(value>=LED_VAL_MIN && value<=LED_VAL_MAX)
				return true;
		}
		if(code>=CAMS_CODE_MIN && code<CAMS_CODE_MAX) {
			if(value>=CAMS_VAL_MIN && value<=CAMS_VAL_MAX)
				return true;
		}
		return false;
	}

	//write chars on dataoutputstream object
	private synchronized void send() {
		try {
			out.writeChars(msg);
		} catch (IOException e) {
			e.printStackTrace();
			threadEnable.setTcpState(false);
		}
		//sleep(100);
	}
	
	private void sleep(int i) {
		try {
			TimeUnit.MILLISECONDS.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

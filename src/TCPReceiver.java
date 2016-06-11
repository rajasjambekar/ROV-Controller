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
	DataAccumulator dataStore;
	
	public TCPReceiver(Socket client, ThreadEnable threadEnable, DataAccumulator dataStore) throws Exception {
		this.client = client;
		this.threadEnable = threadEnable;
		this.dataStore = dataStore;
		inFromServer = this.client.getInputStream();
		in = new DataInputStream(inFromServer);
	}

	@Override
	public void run() {
		while(threadEnable.getThreadState() && threadEnable.getTcpState()) {
			String packet = fetchDataPacket();
			if(!packet.equalsIgnoreCase("")) {
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
 	    		sleep(10);
 	    		//get data until no data available or packet end encountered
 				while(in.available()>0 && ((c = (char) in.read())!='e')) {
 					received.append(c);
 					sleep(10);
 				}
				if(in.available()==0 && c!='e') {
					//if no data available and packet end not encountered discard data
					received = null;
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
		double value = Float.parseFloat(parts[1]);
		switch(code) {
		case 2200:	//Temperature Sensor
			temperatureSensor(value);
			break;
		case 3200:	//Pressure Sensor
			pressureSensor(value);
			break;
		case 8001:	//Gyro Sensor
			accelerometer((int)value, 1);
			break;
		case 8002:	//Gyro Sensor
			accelerometer((int)value, 2);
			break;
		case 8003:	//Gyro Sensor
			accelerometer((int)value, 3);
			break;
		case 8011:	//Gyro Sensor
			gyroscope((int)value, 1);
			break;
		case 8012:	//Gyro Sensor
			gyroscope((int)value, 2);
			break;
		case 8013:	//Gyro Sensor
			gyroscope((int)value, 3);
			break;
		case 8021:	//Gyro Sensor
			magnetometer((int)value, 1);
			break;
		case 8022:	//Gyro Sensor
			magnetometer((int)value, 2);
			break;
		case 8023:	//Gyro Sensor
			magnetometer((int)value, 3);
			break;
		case 9999:	//ping
			ping();
		}
	}
	
	//set accelerometer value
	//1 - x, 2- y, 3- z
	private void accelerometer(int value, int i) {
		dataStore.setAccelerometer(value, i);
	}
	
	//set gyroscope value
	//1 - x, 2- y, 3- z
	private void gyroscope(int value, int i) {
		dataStore.setGyroscope(value, i);
	}
	
	//set magnetometer value
	//1 - x, 2- y, 3- z
	private void magnetometer(int value, int i) {
		dataStore.setMagnetometer(value, i);
	}

	//set temperature value
	private void temperatureSensor(double temperature) {
		dataStore.setTemperature((float)temperature);
	}
	
	//set pressure value
	private void pressureSensor(double pressure) {
		dataStore.setPressure((float)pressure);
	}

	//set ping time
	private void ping() {
		System.out.println("ping");
		dataStore.setPing(System.currentTimeMillis());
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

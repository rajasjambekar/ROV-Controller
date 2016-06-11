
/*This class is used as a control over running thread. Each thread is given a reference of this
 *class' object. The thread keeps checking the value of threadOn and stops when the value is false;
 *Another control is the tcpConnected state. If any thread detects tcp connection lost, it will flip
 *the tcpConnected bit to false and cause all threads to stop
 */
public class ThreadEnable {
	private boolean threadOn;
	private boolean tcpConnected;
	
	public ThreadEnable() {
		threadOn = false;
		tcpConnected = false;
	}
	
	public void setThreadState(boolean state) {
		threadOn = state;
	}
	
	public void setTcpState(boolean state) {
		tcpConnected = state;
	}
	
	public boolean getThreadState() {
		return threadOn;
	}
	
	public boolean getTcpState() {
		return tcpConnected;
	}
}

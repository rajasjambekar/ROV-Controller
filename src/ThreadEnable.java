
/*This class is used as a control over running thread. Each thread is given a reference of this
 *class' object. The thread keeps checking the value of threadOn and stops when the value is false;
 *Another control is the tcpConnected state. If any thread detects tcp connection lost, it will flip
 *the tcpConnected bit to false and cause all threads to stop
 */
public class ThreadEnable {
	private boolean threadOn;
	private boolean joystickDisconnected;
	private boolean tcpConnected;
	private boolean thrusterEnable;
	
	public ThreadEnable() {
		threadOn = false;
		tcpConnected = false;
		joystickDisconnected = false;
		thrusterEnable = false;
	}
	
	public void setThreadState(boolean state) {
		threadOn = state;
	}
	
	public void setTcpState(boolean state) {
		tcpConnected = state;
	}
	
	public void setJoystickDisconnectedState(boolean state) {
		joystickDisconnected = state;
	}
	
	public void setThrusterEnableState(boolean state) {
		thrusterEnable = state;
	}
	
	public boolean getThreadState() {
		return threadOn;
	}
	
	public boolean getTcpState() {
		return tcpConnected;
	}
	
	public boolean getJoystickDisconnectedState() {
		return joystickDisconnected;
	}
	
	public boolean getThrusterEnableState() {
		return thrusterEnable;
	}
}

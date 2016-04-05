
/*This class is used as a control over running thread. Each thread is given a reference of this
 *class' object. The thread keeps checking the value of threadOn and stops when the value is false;
 */
public class ThreadEnable {
	private boolean threadOn;
	
	public ThreadEnable() {
		threadOn = false;
	}
	
	public void setThreadState(boolean state) {
		threadOn = state;
	}
	
	public boolean getThreadState() {
		return threadOn;
	}
}

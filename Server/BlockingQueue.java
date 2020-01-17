import java.util.Queue;
import java.util.LinkedList;

public class BlockingQueue <T>
{
	private Queue<T> queue = new LinkedList<>();
 	
 	@SuppressWarnings (value="unchecked")
	
	public synchronized void push(T t) {
		try {
			queue.add((T)(t.getClass().getMethod("clone").invoke(t)));
		} catch (Exception e) {
			throw new RuntimeException("Clone not supported", e);
		}
		
		notifyAll();
	}
	
	public synchronized T pop() {
		while (queue.size() == 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); 
			}
		}
		
		return queue.poll();
	}
}

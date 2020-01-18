import java.util.Queue;
import java.util.LinkedList;

public class BlockingQueue <T>
{
	private Queue<T> queue = new LinkedList<>();
 	
	public synchronized void push(T t) {
		queue.add(t);
		
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


public class Pair <T, E> {
	public T first;
	public E second;
	
	@SuppressWarnings (value="unchecked")
	
	public Pair(T first, E second) {
		try {
			this.first = (T)(first.getClass().getMethod("clone").invoke(first));
			this.second = (E)(second.getClass().getMethod("clone").invoke(second));
		} catch (Exception e) {
			throw new RuntimeException("Clone not supported", e);
		}
	}
}

public class Pair <T, E> implements Cloneable {
	public T first;
	public E second;
	
	@SuppressWarnings (value="unchecked")
	
	public Pair(T first, E second) {
		try {
			if (first != null)
				this.first = (T)(first.getClass().getMethod("clone").invoke(first));
			if (second != null)
				this.second = (E)(second.getClass().getMethod("clone").invoke(second));
		} catch (Exception e) {
			throw new RuntimeException("Clone not supported", e);
		}
	}
	
	public Pair<T, E> clone() {
		try {
			Pair<T, E> pairCopy = (Pair<T, E>)super.clone();
			if (first != null)
				pairCopy.first = (T)(first.getClass().getMethod("clone").invoke(first));
			if (second != null)
				pairCopy.second = (E)(second.getClass().getMethod("clone").invoke(second));
			return pairCopy;
		} catch (Exception e) {
			throw new RuntimeException("Clone not supported", e);
		}
	}
}

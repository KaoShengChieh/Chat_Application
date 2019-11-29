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
/*
	public static long CountDaysBetween(String D1, String D2) {
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		final LocalDate firstDate = LocalDate.parse(D1, formatter);
		final LocalDate secondDate = LocalDate.parse(D2, formatter);
		final long days = ChronoUnit.DAYS.between(firstDate, secondDate);
		// System.out.println("Days between: " + days);
		return days;
	}*/

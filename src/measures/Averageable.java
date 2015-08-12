package measures;

import java.util.List;

public interface Averageable<K> {
	public K average(List<K> set);
}

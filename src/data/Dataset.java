package data;

import java.util.Iterator;
import java.util.List;

public class Dataset<K> implements Iterable<K>{
	List<K> data;
	List<Integer> classIndexes;

	public Dataset(List<K> data, List<Integer> classIndexes) {
		super();
		this.data = data;
		this.classIndexes = classIndexes;
	}

	@Override
	public Iterator<K> iterator() {
		return data.iterator();
	}
	
	public List<K> getData(){
		return data;
	}
	
	public List<Integer> getLabels(){
		return classIndexes;
	}
	
}

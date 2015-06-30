package data;

import java.util.Iterator;
import java.util.List;

public class Dataset implements Iterable<TimeSeries>{
	List<TimeSeries> data;
	List<Integer> classIndexes;

	public Dataset(List<TimeSeries> data, List<Integer> classIndexes) {
		super();
		this.data = data;
		this.classIndexes = classIndexes;
	}

	@Override
	public Iterator<TimeSeries> iterator() {
		return data.iterator();
	}
	
	public List<TimeSeries> getData(){
		return data;
	}
	
	public List<Integer> getLabels(){
		return classIndexes;
	}
	
}

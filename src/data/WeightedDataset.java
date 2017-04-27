package data;

import java.util.List;

public class WeightedDataset<K> extends Dataset<K> {
	List<Double> weights;

	public WeightedDataset(List<K> data, List<Integer> classIndexes,List<Double>weights) {
		super(data,classIndexes);
		this.weights = weights;
	}

	public List<Double>getWeights(){
		return weights;
	}
}

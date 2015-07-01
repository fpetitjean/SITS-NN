package classification;

import data.Dataset;

public interface Classifier<K> {
	public void train(Dataset<K> dataset);
	public int classify(K query);
}

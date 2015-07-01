package classification;

import java.util.List;

import measures.SimilarityMeasure;
import data.Dataset;

public class NearestNeighbor<K> implements Classifier<K> {
	
	protected SimilarityMeasure<K> measure;
	protected Dataset<K> train;

	public NearestNeighbor(SimilarityMeasure<K> measure){
		this.measure = measure;
	}
	
	public void train(Dataset<K> dataset){
		this.train = dataset;
	}
	
	public int classify(K query){
		if(measure.hasLowerBound()){
			double minDist = Double.MAX_VALUE;
			int closestClassIndex = -1;
			
			List<K> dataset = train.getData();
			List<Integer> classLabels = train.getLabels();
			
			for (int i = 0; i < dataset.size(); i++) {
				K s =dataset.get(i);
				double lb = measure.computeLowerBound(query, s);
				if(lb < minDist){
					double d = measure.compute(query, s);
					
					if(d < minDist){
						minDist=d;
						closestClassIndex = classLabels.get(i);
					}
				}
			}
			return closestClassIndex;
		}else{
			double minDist = Double.MAX_VALUE;
			int closestClassIndex = -1;
			
			List<K> dataset = train.getData();
			List<Integer> classLabels = train.getLabels();
			
			for (int i = 0; i < dataset.size(); i++) {
				K s =dataset.get(i);
				double d = measure.compute(query, s);
				
				if(d< minDist){
					minDist=d;
					closestClassIndex = classLabels.get(i);
				}
			}
			return closestClassIndex;
		}
	}
}

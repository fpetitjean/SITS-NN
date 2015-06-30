package classification;

import java.util.List;

import data.Dataset;
import data.TimeSeries;
import measures.SimilarityMeasure;

public class NearestNeighbor {
	
	private SimilarityMeasure measure;
	private Dataset train;

	public NearestNeighbor(SimilarityMeasure measure){
		this.measure = measure;
	}
	
	public void train(Dataset dataset){
		this.train = dataset;
	}
	
	public int classify(TimeSeries query){
		
		double minDist = Double.MAX_VALUE;
		int closestClassIndex = -1;
		
		List<TimeSeries> dataset = train.getData();
		List<Integer> classLabels = train.getLabels();
		
		for (int i = 0; i < dataset.size(); i++) {
			TimeSeries s =dataset.get(i);
			double d = measure.compute(query, s);
			
			if(d< minDist){
				minDist=d;
				closestClassIndex = classLabels.get(i);
			}
		}
		return closestClassIndex;
		
	}
	
	
	
	
}

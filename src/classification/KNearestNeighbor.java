package classification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import measures.SimilarityMeasure;
import data.Dataset;
import data.TimeSeries;

public class KNearestNeighbor implements SequenceClassifier {
	
	protected SimilarityMeasure measure;
	protected Dataset train;
	protected int k;
	protected Neighbor<TimeSeries> []topK;
	protected BitSet allClassesNumbers;
	protected int []classNameToIndex;
	protected int nClasses;
	protected int[]histogram;

	public KNearestNeighbor(int k,SimilarityMeasure measure){
		this.k = k;
		this.measure = measure;
		
	}
	
	public void train(Dataset dataset){
		this.train = dataset;
		this.topK = new Neighbor[k];
		allClassesNumbers = new BitSet();
		for(int label:train.getLabels()){
			allClassesNumbers.set(label);
		}
		nClasses = allClassesNumbers.cardinality();
		classNameToIndex = new int[nClasses];
		int index = 0;
		for (int i = allClassesNumbers.nextSetBit(0); i >= 0; i = allClassesNumbers.nextSetBit(i+1)) {
			classNameToIndex[index]=i;
		     index++;
		}
		histogram = new int[nClasses];
		
	}
	
	public int classify(TimeSeries query){
		Arrays.fill(topK, new Neighbor<TimeSeries>(null, -1, Double.POSITIVE_INFINITY));
		List<TimeSeries> dataset = train.getData();
		List<Integer> classLabels = train.getLabels();
		if(measure.hasLowerBound()){
			for (int i = 0; i < dataset.size(); i++) {
				TimeSeries s =dataset.get(i);
				
				if(measure.hasLowerBound()){
					double lb = measure.computeLowerBound(query, s);
					if(lb >= topK[topK.length-1].distToQuery){
						break;
					}
				}
				
				double d = measure.computeEA(query, s,topK[topK.length-1].distToQuery);
				if(d < topK[topK.length-1].distToQuery){
					int insertionPoint = topK.length-1;
					while(insertionPoint>0 && d <topK[insertionPoint].distToQuery){
						topK[insertionPoint]=topK[insertionPoint-1];
						insertionPoint--;
					}
					Neighbor<TimeSeries> newNeighbor = new Neighbor<TimeSeries>(s, i, d);
					topK[insertionPoint] = newNeighbor;
				}
			}
		}
			
		Arrays.fill(histogram, 0);
		for (int i = 0; i < topK.length; i++) {
			int classForNeighbor = classLabels.get(topK[i].indexInTrain);
			int indexForClass = Arrays.binarySearch(classNameToIndex, classForNeighbor);
			histogram[indexForClass]++;
		}
		
		int bestClassIndex = 0;
		for (int i = 1; i < histogram.length; i++) {
			if(histogram[i]>histogram[bestClassIndex]){
				bestClassIndex = i;
			}
		}
		return classNameToIndex[bestClassIndex];
	}
	
}

class Neighbor<K> implements Comparable<Neighbor<K>>{
	public K neighbor;
	public int indexInTrain;
	public double distToQuery;
	
	public Neighbor(K neighbor,int indexInTrain,double distToQuery){
		this.neighbor = neighbor;
		this.indexInTrain = indexInTrain;
		this.distToQuery = distToQuery;
	}
	
	@Override
	public int compareTo(Neighbor<K> o) {
		return Double.compare(distToQuery, o.distToQuery);
	}
}

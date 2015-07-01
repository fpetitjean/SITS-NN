package classification;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import measures.SimilarityMeasure;
import data.Dataset;

public class KNearestNeighbor<K> implements Classifier<K> {
	
	protected SimilarityMeasure<K> measure;
	protected Dataset<K> train;
	protected int k;
	protected Neighbor<K> []topK;
	protected BitSet allClassesNumbers;
	protected int []classNameToIndex;
	protected int nClasses;
	protected int[]histogram;

	public KNearestNeighbor(int k,SimilarityMeasure<K> measure){
		this.k = k;
		this.measure = measure;
	}
	
	@SuppressWarnings("unchecked")
	public void train(Dataset<K> dataset){
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
	
	public int classify(K query){
		Arrays.fill(topK, new Neighbor<K>(null, -1, Double.POSITIVE_INFINITY));
		List<K> dataset = train.getData();
		List<Integer> classLabels = train.getLabels();
		if(measure.hasLowerBound()){
			for (int i = 0; i < dataset.size(); i++) {
				K s =dataset.get(i);
				
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
					Neighbor<K> newNeighbor = new Neighbor<K>(s, i, d);
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

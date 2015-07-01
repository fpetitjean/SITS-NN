package measures;


public abstract class EarlyAbandonableMeasure<K> extends SimilarityMeasure<K>{
	
	@Override
	public abstract double computeEA(K s1,K s2, double max);
	
	@Override
	public boolean hasEarlyAbandonCapability(){
		return true;
	}
}

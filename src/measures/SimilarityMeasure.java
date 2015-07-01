package measures;

public abstract class SimilarityMeasure<K> {
	protected SimilarityMeasure<K> lb=null;
	public abstract double compute(K s1,K s2);
	public double computeLowerBound(K s1,K s2){
		if(lb!=null){
			return lb.compute(s1, s2);
		}else{
			return Double.POSITIVE_INFINITY;
		}
	}
	public boolean hasLowerBound(){
		return (lb!=null);
	}
	public void setLowerBoundComputer(SimilarityMeasure<K> lb){
		this.lb = lb;
	}
	public boolean hasEarlyAbandonCapability(){
		return false;
	}
	
	/**
	 * Compute measure with early abandon
	 * @param s1
	 * @param s2
	 * @param max
	 * @return the measure if < max or Double.POSITIVE_INFINITY
	 */
	public double computeEA(K s1,K s2, double max){
		double d = compute(s1, s2);
		if(d>max){
			return Double.POSITIVE_INFINITY;
		}else{
			return d;
		}
	}
	
}

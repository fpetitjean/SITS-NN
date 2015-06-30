package measures;

import data.TimeSeries;


public abstract class SimilarityMeasure {
	protected SimilarityMeasure lb=null;
	public abstract double compute(TimeSeries s1,TimeSeries s2);
	public double computeLowerBound(TimeSeries s1,TimeSeries s2){
		if(lb!=null){
			return lb.compute(s1, s2);
		}else{
			return Double.POSITIVE_INFINITY;
		}
	}
	public boolean hasLowerBound(){
		return (lb!=null);
	}
	public void setLowerBoundComputer(SimilarityMeasure lb){
		this.lb = lb;
	}
}

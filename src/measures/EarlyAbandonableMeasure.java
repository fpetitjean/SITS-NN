package measures;

import data.TimeSeries;

public abstract class EarlyAbandonableMeasure extends SimilarityMeasure{
	
	@Override
	public abstract double computeEA(TimeSeries s1,TimeSeries s2, double max);
	
	@Override
	public boolean hasEarlyAbandonCapability(){
		return true;
	}
}

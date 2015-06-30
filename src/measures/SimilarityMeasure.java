package measures;

import data.TimeSeries;


public abstract class SimilarityMeasure {
	public abstract double compute(TimeSeries s1,TimeSeries s2);
}

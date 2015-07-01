package measures;

import data.TimeSeries;

public class Euclidean extends SimilarityMeasure<TimeSeries> {

	public Euclidean() {}
	
	@Override
	public double compute(TimeSeries s1, TimeSeries s2) {
		
		double[] series1 = s1.getSeries();
		double[] series2 = s2.getSeries();
		int minLength = Math.min(series1.length, series2.length);
		
		double distance = 0.0;
		for (int i = 0; i < minLength; i++) {
			distance += Tools.squaredDistance(series1[i], series2[i]);
		}
		return Math.sqrt(distance);
	}

	

}

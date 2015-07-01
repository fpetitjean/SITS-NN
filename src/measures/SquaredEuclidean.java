package measures;

import data.TimeSeries;

public class SquaredEuclidean extends EarlyAbandonableMeasure<TimeSeries>{

	public SquaredEuclidean() {}
	
	@Override
	public double compute(TimeSeries s1, TimeSeries s2) {
		
		double[] series1 = s1.getSeries();
		double[] series2 = s2.getSeries();
		int minLength = Math.min(series1.length, series2.length);
		
		double distance = 0.0;
		for (int i = 0; i < minLength; i++) {
			distance += Tools.squaredDistance(series1[i], series2[i]);
		}
		return distance;
	}

	@Override
	public double computeEA(TimeSeries s1, TimeSeries s2, double max) {
		double[] series1 = s1.getSeries();
		double[] series2 = s2.getSeries();
		int minLength = Math.min(series1.length, series2.length);
		
		double distance = 0.0;
		for (int i = 0; i < minLength; i++) {
			distance += Tools.squaredDistance(series1[i], series2[i]);
			if(distance>=max){
				return Double.MAX_VALUE;
			}
		}
		return distance;
	}

	

}

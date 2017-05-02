package measures;

import java.util.List;

import data.TimeSeries;

public class Euclidean extends SimilarityMeasure<TimeSeries>implements Averageable<TimeSeries>  {

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

	@Override
	public TimeSeries average(List<TimeSeries> set) {
		double[] sample = set.get(0).getSeries();
		
		int length = sample.length;
		double[]mean = new double[length];
		for(TimeSeries ts:set){
			double[]series = ts.getSeries();
			for (int l = 0; l < length; l++) {
				mean[l]+=series[l];
			}
		}
		for (int l = 0; l < length; l++) {
				mean[l]/=set.size();
		}
		return new TimeSeries(mean, -1, set.get(0).getID_polygon());
	}

	

}

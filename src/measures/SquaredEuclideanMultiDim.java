package measures;

import data.TimeSeriesMultiDim;

public class SquaredEuclideanMultiDim extends EarlyAbandonableMeasure<TimeSeriesMultiDim>{

	public SquaredEuclideanMultiDim() {}
	
	@Override
	public double compute(TimeSeriesMultiDim s1, TimeSeriesMultiDim s2) {
		
		double[][] series1 = s1.getSeries();
		double[][] series2 = s2.getSeries();
		int nDims = s1.getNDimS();
		
		int minLength = Math.min(series1.length, series2.length);
		
		double distance = 0.0;
		for (int l = 0; l < minLength; l++) {
			for (int d = 0; d < nDims; d++) {
				distance += Tools.squaredDistance(series1[l][d], series2[l][d]);
			}
		}
		return distance;
	}

	@Override
	public double computeEA(TimeSeriesMultiDim s1, TimeSeriesMultiDim s2, double max) {
		
		double[][] series1 = s1.getSeries();
		double[][] series2 = s2.getSeries();
		int nDims = s1.getNDimS();
		
		int minLength = Math.min(series1.length, series2.length);
		
		double distance = 0.0;
		for (int l = 0; l < minLength; l++) {
			for (int d = 0; d < nDims; d++) {
				distance += Tools.squaredDistance(series1[l][d], series2[l][d]);
			}
			if(distance>=max){
				return Double.MAX_VALUE;
			}
		}
		return distance;
	}

}

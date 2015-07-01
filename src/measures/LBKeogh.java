package measures;

import static java.lang.Math.sqrt;
import data.TimeSeries;

public class LBKeogh extends SimilarityMeasure<TimeSeries> {

	protected double[]L;
	protected double[]U;
	protected int windowSize;
	
	/**
	 * Constructor
	 * @param maxLength maximum length of the series that will be compared with DTW (used to initialize some memory space for the computation)
	 */
	public LBKeogh(int maxLength,int windowSize) {
		this.L = new double[maxLength];
		this.U = new double[maxLength];
		this.windowSize = windowSize;
	}
	
	@Override
	public synchronized double compute(TimeSeries s1, TimeSeries s2) {
		
		double[] series1 = s1.getSeries();
		double[] series2 = s2.getSeries();
		int length1 = series1.length;
		int length2 = series2.length;
		
		final int minLength = Math.min(length1,length2);

		for (int i = 0; i < minLength; i++) {
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			int jStart = (i-windowSize<0)?0:i-windowSize;
			int jStop = (i+windowSize+1>length2)?length2:i+windowSize+1;
			for (int j = jStart; j < jStop; j++) {
				double value = series1[j];
				min = Math.min(min, value);
				max = Math.max(max, value);
			}
			L[i] = min;
			U[i] = max;
		}

		double res = 0;
		for (int i = 0; i < minLength; i++) {
			double c = series2[i];
			if (c < L[i]) {
				res += Tools.squaredDistance(L[i],c);
			} else if (U[i] < c) {
				res += Tools.squaredDistance(U[i],c);
			}
		}
		return sqrt(res);
	}
	
}

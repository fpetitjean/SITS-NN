package measures;

import static java.lang.Math.sqrt;
import data.TimeSeries;

public class DTWWindowed extends DTW{

	protected int windowSize;

	/**
	 * Constructor
	 * @param maxLength maximum length of the series that will be compared with DTW (used to initialize some memory space for the computation)
	 * @param windowSize sets the size of the warping window 
	 */
	public DTWWindowed(int maxLength,int windowSize) {
		super(maxLength);
		this.windowSize = windowSize;
	}
	
	@Override
	public synchronized double compute(TimeSeries s1, TimeSeries s2) {
		
		double[] series1 = s1.getSeries();
		double[] series2 = s2.getSeries();
		int length1 = series1.length;
		int length2 = series2.length;
		
		if(length1>matrix.length || length2>matrix.length){
			throw new RuntimeException("Matrix too small ("+matrix.length+") for sequence of lengths "+length1+" and "+length2);
		}
		
		int i, j;
		matrix[0][0] = squaredDistance(series1[0],series2[0]);
		for (i = 1; i < length1; i++) {
			matrix[i][0] = matrix[i - 1][0]	+ squaredDistance(series1[i],series2[0]);
		}
		for (j = 1; j < Math.min(length2, windowSize); j++) {
			matrix[0][j] = matrix[0][j - 1] + squaredDistance(series1[0],series2[j]);
		}

		for (i = 1; i < length1; i++) {
			for (j = Math.max(1, i-windowSize); j < Math.min(length2, i+windowSize); j++) {
				matrix[i][j] = min(matrix[i - 1][j - 1],matrix[i][j - 1], matrix[i - 1][j])
						+ squaredDistance(series1[i],series2[j]);
			}
		}

		return sqrt(matrix[length1-1][length2-1]);
	}
	

}

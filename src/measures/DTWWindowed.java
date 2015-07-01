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
		matrix[0][0] = Tools.squaredDistance(series1[0],series2[0]);
		for (i = 1; i < Math.min(length1, 1+windowSize); i++) {
			matrix[i][0] = matrix[i - 1][0]	+ Tools.squaredDistance(series1[i],series2[0]);
		}
		
		for (j = 1; j < Math.min(length2, 1+windowSize); j++) {
			matrix[0][j] = matrix[0][j - 1] + Tools.squaredDistance(series1[0],series2[j]);
		}
		if(j<length2)matrix[0][j] = Double.POSITIVE_INFINITY;
		
		for (i = 1; i < length1; i++) {
			int jStart = (i-windowSize<1)?1:i-windowSize;
			int jStop = (i+windowSize+1>length2)?length2:i+windowSize+1;
			
			matrix[i][jStart-1] = Double.POSITIVE_INFINITY;
			for (j = jStart; j < jStop; j++) {
				matrix[i][j] = min(matrix[i - 1][j - 1],matrix[i][j - 1], matrix[i - 1][j])
						+ Tools.squaredDistance(series1[i],series2[j]);
			}
			if(jStop<length2)matrix[i][jStop] = Double.POSITIVE_INFINITY;
		}
		
		return sqrt(matrix[length1-1][length2-1]);
	}
	

}

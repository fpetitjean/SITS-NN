package data;

import java.util.Arrays;

public class TimeSeriesMultiDim implements ZNormalizable{
	
	double[][]series;
	int id_pixel;
	int id_polygon;
	int nDims;

	public TimeSeriesMultiDim(double[][] series, int id_pixel, int id_polygon) {
		super();
		this.series = series;
		this.id_pixel = id_pixel;
		this.id_polygon = id_polygon;
		this.nDims = series[0].length;
		zNormalize();
	}
	
	public double[][]getSeries(){
		return series;
	}

	@Override
	public String toString(){
		return Arrays.toString(series);
	}
	
	public int getID_polygon(){
		return id_polygon;
	}
	public int getID_pixel(){
		return id_pixel;
	}
	public int getNDimS(){
		return this.nDims;
	}

	public void zNormalize() {
		double []means = new double[nDims];
		Arrays.fill(means, 0.0);
		for(double[] el:series){
			for (int i = 0; i < means.length; i++) {
				means[i]+=el[i];
			}
		}
		for (int i = 0; i < means.length; i++) {
			means[i]/=series.length;
		}
		double []stddevs = new double[nDims];
		Arrays.fill(stddevs, 0.0);
		for(double[] el:series){
			for (int i = 0; i < means.length; i++) {
				double diff = (el[i]-means[i]);
				stddevs[i]+=(diff*diff);
			}
		}
		for (int i = 0; i < means.length; i++) {
			stddevs[i] = Math.sqrt(stddevs[i]/series.length);
		}
		
		for (int l = 0; l < series.length; l++) {
			for (int i = 0; i < means.length; i++) {
				series[l][i]=(series[l][i]-means[i])/stddevs[i];
			}
		}
		
	}
}

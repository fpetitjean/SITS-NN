package data;

import java.util.Arrays;

public class TimeSeries implements ZNormalizable{
	
	double[]series;
	int id_pixel;
	int id_polygon;

	public TimeSeries(double[] series, int id_pixel, int id_polygon) {
		super();
		this.series = series;
		this.id_pixel = id_pixel;
		this.id_polygon = id_polygon;
		zNormalize();
	}
	
	public double[]getSeries(){
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

	public void zNormalize() {
		double mean = 0.0;
		for(double v:series){
			mean+=v;
		}
		mean/=series.length;
		double stddev = 0.0;
		for(double v:series){
			double diff = (v-mean);
			stddev+=(diff*diff);
		}
		stddev = Math.sqrt(stddev/series.length);
		
		for (int i = 0; i < series.length; i++) {
			series[i]=(series[i]-mean)/stddev;
		}
		
		
	}
}

package data;

import java.util.Arrays;

public class TimeSeries{
	
	double[]series;
	int id_pixel;
	int id_polygon;

	public TimeSeries(double[] series, int id_pixel, int id_polygon) {
		super();
		this.series = series;
		this.id_pixel = id_pixel;
		this.id_polygon = id_polygon;
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
}

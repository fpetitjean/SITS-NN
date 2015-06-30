package data;

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

}

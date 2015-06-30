package classification;

import data.Dataset;
import data.TimeSeries;

public interface TimeSeriesClassifier {
	public void train(Dataset dataset);
	public int classify(TimeSeries query);
}

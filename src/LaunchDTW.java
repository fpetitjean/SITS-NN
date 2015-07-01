import java.io.File;
import java.io.IOException;
import java.sql.Time;

import measures.DTW;
import measures.SimilarityMeasure;
import classification.NearestNeighbor;
import data.TimeSeries;
import evaluation.NNEvaluationNDVI;


public class LaunchDTW {

	public static void main(String[] args) throws NumberFormatException, IOException {
		File datasetFile = new File(args[0]);
		SimilarityMeasure<TimeSeries> dtw = new DTW(20);
		NearestNeighbor<TimeSeries> classifier = new NearestNeighbor<TimeSeries>(dtw);
		NNEvaluationNDVI eval = new NNEvaluationNDVI(classifier,datasetFile);
		eval.setSamplingRate(1.0);
//		eval.evaluateTrainOnTrain();
		eval.evaluate();
		System.out.println("error rate = "+eval.getErrorRate());

	}

}

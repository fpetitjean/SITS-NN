import java.io.File;
import java.io.IOException;

import measures.SquaredEuclidean;
import classification.KNearestNeighbor;
import data.TimeSeries;
import evaluation.NNEvaluationNDVI;


public class LaunchEUC {

	public static void main(String[] args) throws NumberFormatException, IOException {
		File datasetFile = new File(args[0]);
		SquaredEuclidean euc = new SquaredEuclidean();
//		Euclidean euc = new Euclidean();
		KNearestNeighbor<TimeSeries> classifier = new KNearestNeighbor<TimeSeries>(20,euc);
		NNEvaluationNDVI eval = new NNEvaluationNDVI(classifier,datasetFile);
		eval.setSamplingRate(0.5);
		eval.evaluate();
		System.out.println(eval);
	}

}

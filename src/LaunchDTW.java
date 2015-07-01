import java.io.File;
import java.io.IOException;

import measures.DTW;
import measures.SimilarityMeasure;
import classification.NearestNeighbor;
import evaluation.NNEvaluationNDVI;


public class LaunchDTW {

	public static void main(String[] args) throws NumberFormatException, IOException {
		File datasetFile = new File(args[0]);
		SimilarityMeasure dtw = new DTW(20);
		NearestNeighbor classifier = new NearestNeighbor(dtw);
		NNEvaluationNDVI eval = new NNEvaluationNDVI(classifier,datasetFile);
		eval.setSamplingRate(1.0);
//		eval.evaluateTrainOnTrain();
		eval.evaluate();
		System.out.println("error rate = "+eval.getErrorRate());

	}

}

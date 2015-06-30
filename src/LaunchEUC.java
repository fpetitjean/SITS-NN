import java.io.File;
import java.io.IOException;

import classification.NearestNeighbor;
import measures.DTW;
import measures.Euclidean;
import evaluation.NNEvaluation;


public class LaunchEUC {

	public static void main(String[] args) throws NumberFormatException, IOException {
		File datasetFile = new File(args[0]);
		Euclidean euc = new Euclidean();
		NearestNeighbor classifier = new NearestNeighbor(euc);
		NNEvaluation eval = new NNEvaluation(classifier,datasetFile);
		eval.setSamplingRate(0.1);
		eval.evaluateTrainOnTrain();
		eval.evaluate();
		System.out.println("error rate = "+eval.getErrorRate());

	}

}

import java.io.File;
import java.io.IOException;

import measures.Euclidean;
import classification.NearestNeighbor;
import evaluation.NNEvaluationNDVI;


public class LaunchEUC {

	public static void main(String[] args) throws NumberFormatException, IOException {
		File datasetFile = new File(args[0]);
		Euclidean euc = new Euclidean();
		NearestNeighbor classifier = new NearestNeighbor(euc);
		NNEvaluationNDVI eval = new NNEvaluationNDVI(classifier,datasetFile);
		eval.setSamplingRate(1.0);
		eval.evaluate();
		System.out.println(eval);
	}

}

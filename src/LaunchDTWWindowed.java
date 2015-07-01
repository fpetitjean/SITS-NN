import java.io.File;
import java.io.IOException;

import measures.DTWWindowed;
import measures.SimilarityMeasure;
import classification.NearestNeighbor;
import evaluation.NNEvaluationNDVI;


public class LaunchDTWWindowed {

	public static void main(String[] args) throws NumberFormatException, IOException {
		int maxLength = 20;
		int windowSize = 2;
		
		File datasetFile = new File(args[0]);
		SimilarityMeasure dtw = new DTWWindowed(maxLength,windowSize);
		NearestNeighbor classifier = new NearestNeighbor(dtw);
		NNEvaluationNDVI eval = new NNEvaluationNDVI(classifier,datasetFile);
		eval.setSamplingRate(1.0);
		eval.evaluate();
		System.out.println(eval);
	}

}

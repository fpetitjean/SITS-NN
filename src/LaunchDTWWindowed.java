import java.io.File;
import java.io.IOException;

import measures.DTWWindowed;
import measures.SimilarityMeasure;
import classification.NearestNeighbor;
import evaluation.NNEvaluation;


public class LaunchDTWWindowed {

	public static void main(String[] args) throws NumberFormatException, IOException {
		
		int maxLength = 20;
		int windowSize = 0;
		
		File datasetFile = new File(args[0]);
		SimilarityMeasure dtw = new DTWWindowed(maxLength,windowSize);
		NearestNeighbor classifier = new NearestNeighbor(dtw);
		NNEvaluation eval = new NNEvaluation(classifier,datasetFile);
		eval.setSamplingRate(0.01);
		eval.evaluate();
		System.out.println("error rate = "+eval.getErrorRate());
	}

}

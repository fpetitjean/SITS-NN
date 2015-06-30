import java.io.File;
import java.io.IOException;

import measures.DTWWindowed;
import measures.LBKeogh;
import measures.SimilarityMeasure;
import classification.NearestNeighbor;
import evaluation.NNEvaluation;


public class LaunchDTWLB {

	public static void main(String[] args) throws NumberFormatException, IOException {
		
		int maxLength = 20;
		int windowSize = 2;
		
		File datasetFile = new File(args[0]);
		SimilarityMeasure dtw = new DTWWindowed(maxLength,windowSize);
		SimilarityMeasure lb = new LBKeogh(maxLength,windowSize);
		dtw.setLowerBoundComputer(lb);
		NearestNeighbor classifier = new NearestNeighbor(dtw);
		NNEvaluation eval = new NNEvaluation(classifier,datasetFile);
		eval.setSamplingRate(0.01);
		eval.evaluate();
		System.out.println("error rate = "+eval.getErrorRate());
	}

}

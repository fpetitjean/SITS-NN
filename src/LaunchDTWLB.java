import java.io.File;
import java.io.IOException;

import measures.DTWWindowed;
import measures.LBKeogh;
import measures.SimilarityMeasure;
import classification.NearestNeighbor;
import data.TimeSeries;
import evaluation.NNEvaluationNDVI;


public class LaunchDTWLB {

	public static void main(String[] args) throws NumberFormatException, IOException {
		
		int maxLength = 20;
		int windowSize = 4;
		
		File datasetFile = new File(args[0]);
		SimilarityMeasure<TimeSeries> dtw = new DTWWindowed(maxLength,windowSize);
		SimilarityMeasure<TimeSeries> lb = new LBKeogh(maxLength,windowSize);
		dtw.setLowerBoundComputer(lb);
		NearestNeighbor<TimeSeries> classifier = new NearestNeighbor<TimeSeries>(dtw);
		NNEvaluationNDVI eval = new NNEvaluationNDVI(classifier,datasetFile);
		eval.setSamplingRate(0.4);
		eval.evaluate();
		System.out.println("error rate = "+eval.getErrorRate());
	}

}

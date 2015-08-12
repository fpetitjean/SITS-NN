import java.io.File;
import java.io.IOException;

import measures.SquaredEuclideanMultiDim;
import classification.KNearestNeighbor;
import data.TimeSeriesMultiDim;
import evaluation.NNEvaluationAllDims;


public class LaunchEUCMultiDim {

	public static void main(String[] args) throws NumberFormatException, IOException {
		File datasetFile = new File(args[0]);
		SquaredEuclideanMultiDim euc = new SquaredEuclideanMultiDim();
		KNearestNeighbor<TimeSeriesMultiDim> classifier = new KNearestNeighbor<TimeSeriesMultiDim>(20,euc);
		NNEvaluationAllDims eval = new NNEvaluationAllDims(classifier,datasetFile);
		eval.setSamplingRate(1.0);
		eval.evaluate();
		System.out.println(eval);
	}

}

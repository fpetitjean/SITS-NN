import java.io.File;
import java.io.IOException;

import measures.SquaredEuclidean;
import measures.SquaredEuclideanMultiDim;
import classification.KNearestNeighbor;
import data.TimeSeries;
import data.TimeSeriesMultiDim;
import evaluation.NCPerPolygonEvaluationAllDims;
import evaluation.NNEvaluationNDVI;


public class LaunchNCPerPolygonEUC {

	public static void main(String[] args) throws NumberFormatException, IOException {
		File datasetFile = new File(args[0]);
		SquaredEuclideanMultiDim euc = new SquaredEuclideanMultiDim();
//		Euclidean euc = new Euclidean();
		KNearestNeighbor<TimeSeriesMultiDim> classifier = new KNearestNeighbor<TimeSeriesMultiDim>(3,euc);
		NCPerPolygonEvaluationAllDims eval = new NCPerPolygonEvaluationAllDims(classifier,euc,datasetFile);
		eval.evaluate();
		System.out.println(eval);
	}

}

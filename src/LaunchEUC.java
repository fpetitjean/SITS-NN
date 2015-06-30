import java.io.File;
import java.io.IOException;

import evaluation.NNEvaluation;


public class Launch {

	public static void main(String[] args) throws NumberFormatException, IOException {
		File datasetFile = new File(args[0]);
		NNEvaluation eval = new NNEvaluation(datasetFile);
		eval.setSamplingRate(0.1);
		eval.evaluate();
		System.out.println("error rate = "+eval.getErrorRate());

	}

}

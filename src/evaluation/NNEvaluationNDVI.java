package evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import measures.Euclidean;
import measures.SimilarityMeasure;
import classification.NearestNeighbor;
import classification.TimeSeriesClassifier;
import data.Dataset;
import data.TimeSeries;

public class NNEvaluationNDVI {

	protected File datasetFile;

	/**
	 * Percentage of the dataset to train on
	 */
	protected double percentageForTrain = .5;

	/**
	 * Percentage of pixels to use in each polygon (both train and test)
	 */
	protected double samplingRate = 1.0;

	protected BufferedReader reader;

	protected FileReader fReader;

	protected int[] polygonNumbers;

	protected int[] trainPolygonNumbers;

	protected int[] testPolygonNumbers;
	
	Dataset train,test;

	protected ArrayList<TimeSeries> trainTimeSeries;
	
	protected ArrayList<Integer> trainClassIndexes;

	protected int nErrors;

	protected int nSeriesTested;

	protected SimilarityMeasure measure;

	private TimeSeriesClassifier classifier;

	private long seed;
	
	/**
	 * truth x predicted
	 */
	protected int[][]confusionMatrix;

	private int nClasses;

	private int[] classesNames;
	

	protected static final int N_BYTES = 1024000;
	protected static final int ID_PIXEL_ATTRIBUTE = 0;
	protected static final int ID_POLYGON_ATTRIBUTE = 1;
	protected static final int CLASS_ATTRIBUTE = 4;
	protected static final int INDEX_START_NDVI = 111;
	protected static final int LENGTH_TIME_SERIES = 15;

	public NNEvaluationNDVI(TimeSeriesClassifier classifier, File datasetFile) {
		this.classifier = classifier;
		this.datasetFile = datasetFile;
		this.seed = 3071980L;
	}

	public void evaluate() throws NumberFormatException, IOException {
		createFolds(seed);
		
		Random r = new Random(seed);
		//build train dataset (1pass over the data)
		trainTimeSeries = new ArrayList<TimeSeries>();
		trainClassIndexes = new ArrayList<Integer>();
		fReader = new FileReader(this.datasetFile);
		reader = new BufferedReader(fReader, N_BYTES);
		String line;
		BitSet allClassesNumbers= new BitSet();
		while ((line = reader.readLine()) != null) {
			String[] splitted = line.split(",");
			int polygonID = Integer.valueOf(splitted[ID_POLYGON_ATTRIBUTE]);
			int indexInTrain = Arrays.binarySearch(trainPolygonNumbers, polygonID);
			int pixelID = Integer.valueOf(splitted[ID_PIXEL_ATTRIBUTE]);
			
			if(indexInTrain>=0){
				if(r.nextDouble()<=samplingRate){
					//this is for train
					double[] timeSeries = new double[LENGTH_TIME_SERIES];
					for (int i = 0; i < timeSeries.length; i++) {
						timeSeries[i]=Double.valueOf(splitted[INDEX_START_NDVI+i]);
					}
					TimeSeries ts = new TimeSeries(timeSeries, pixelID, polygonID);
					trainTimeSeries.add(ts);
					
					int classIndex = Integer.valueOf(splitted[CLASS_ATTRIBUTE]);
					trainClassIndexes.add(classIndex);
					allClassesNumbers.set(classIndex);
					
				}
			}
		}
		nClasses = allClassesNumbers.cardinality();
		classesNames = new int[nClasses];
		confusionMatrix = new int[nClasses][nClasses];
		int index = 0;
		for (int i = allClassesNumbers.nextSetBit(0); i >= 0; i = allClassesNumbers.nextSetBit(i+1)) {
		     classesNames[index]=i;
		     index++;
		}
		
		System.out.println(allClassesNumbers.cardinality()+" classes in the dataset");
		
		Dataset trainDataset = new Dataset(trainTimeSeries, trainClassIndexes);
//		System.out.println("train dataset created with "+trainTimeSeries.size()+" time series");
		classifier.train(trainDataset);
		
		//second pass for test
		nSeriesTested = 0;
		nErrors = 0;
		fReader = new FileReader(this.datasetFile);
		reader = new BufferedReader(fReader, N_BYTES);
		while ((line = reader.readLine()) != null) {
			String[] splitted = line.split(",");
			int polygonID = Integer.valueOf(splitted[ID_POLYGON_ATTRIBUTE]);
			int indexInTest = Arrays.binarySearch(testPolygonNumbers, polygonID);
			int pixelID = Integer.valueOf(splitted[ID_PIXEL_ATTRIBUTE]);
			
			if(indexInTest>=0){
				if(r.nextDouble()<samplingRate){
					double[] timeSeries = new double[LENGTH_TIME_SERIES];
					for (int i = 0; i < timeSeries.length; i++) {
						timeSeries[i]=Double.valueOf(splitted[INDEX_START_NDVI+i]);
					}
					TimeSeries ts = new TimeSeries(timeSeries, pixelID, polygonID);
					int predictedClassIndex = classifier.classify(ts);
					
					int classIndex = Integer.valueOf(splitted[CLASS_ATTRIBUTE]);
//					System.out.println("predicted ="+predictedClassIndex+"\tref="+classIndex);
					confusionMatrix[Arrays.binarySearch(classesNames,classIndex)][Arrays.binarySearch(classesNames,predictedClassIndex)]++;
					if(classIndex!=predictedClassIndex){
						nErrors++;
					}
					nSeriesTested++;
				}
			}
		}
		
	}
	
	public void evaluateTrainOnTrain() throws NumberFormatException, IOException {
		createFolds(seed);
		
		Random r = new Random(seed);
		//build train dataset (1pass over the data)
		trainTimeSeries = new ArrayList<TimeSeries>();
		trainClassIndexes = new ArrayList<Integer>();
		fReader = new FileReader(this.datasetFile);
		reader = new BufferedReader(fReader, N_BYTES);
		String line;
		BitSet allClassesNumbers= new BitSet();
		while ((line = reader.readLine()) != null) {
			String[] splitted = line.split(",");
			int polygonID = Integer.valueOf(splitted[ID_POLYGON_ATTRIBUTE]);
			int indexInTrain = Arrays.binarySearch(trainPolygonNumbers, polygonID);
			int pixelID = Integer.valueOf(splitted[ID_PIXEL_ATTRIBUTE]);
			
			if(indexInTrain>=0){
				if(r.nextDouble()<samplingRate){
					//this is for train
					double[] timeSeries = new double[LENGTH_TIME_SERIES];
					for (int i = 0; i < timeSeries.length; i++) {
						timeSeries[i]=Double.valueOf(splitted[INDEX_START_NDVI+i])/1000.0;
					}
					TimeSeries ts = new TimeSeries(timeSeries, pixelID, polygonID);
					trainTimeSeries.add(ts);
					
					int classIndex = Integer.valueOf(splitted[CLASS_ATTRIBUTE]);
					trainClassIndexes.add(classIndex);
					allClassesNumbers.set(classIndex);
					
				}
			}
		}
		System.out.println(allClassesNumbers.cardinality()+" classes in the dataset");
		
		Dataset trainDataset = new Dataset(trainTimeSeries, trainClassIndexes);
//		System.out.println("train dataset created with "+trainTimeSeries.size()+" time series");
		classifier.train(trainDataset);
		
		//second pass for test
		int nSeriesTested = 0;
		int nErrors = 0;
		for (int i = 0; i < trainTimeSeries.size(); i++) {
			TimeSeries ts = trainTimeSeries.get(i);
			int predictedClassIndex = classifier.classify(ts);
			int classIndex = trainClassIndexes.get(i);
//					System.out.println("predicted ="+predictedClassIndex+"\tref="+classIndex);
			if(classIndex!=predictedClassIndex){
				nErrors++;
			}
			nSeriesTested++;
		}
		System.out.println("error rate train on train = "+(1.0*nErrors/nSeriesTested)+" (should be 0)");
	}
	
	public double getErrorRate(){
		return 1.0*nErrors/nSeriesTested;
	}

	private void createFolds(long seed) throws NumberFormatException, IOException {

		// find number of polygons
		BitSet polygonIDs = new BitSet();
		fReader = new FileReader(this.datasetFile);
		reader = new BufferedReader(fReader, N_BYTES);
		String line;
		String[] splitted=null;
		while ((line = reader.readLine()) != null) {
			splitted = line.split(",");
			int polygonID = Integer.valueOf(splitted[ID_POLYGON_ATTRIBUTE]);
			polygonIDs.set(polygonID);
		}

		int nPolygons = polygonIDs.cardinality();
		polygonNumbers = new int[nPolygons];
		int index = 0;
		for (int i = polygonIDs.nextSetBit(0); i >= 0; i = polygonIDs
				.nextSetBit(i + 1)) {
			polygonNumbers[index] = i;
			index++;
		}

		int quotaTrain = (int) Math.ceil(nPolygons * percentageForTrain);
		int quotaTest = nPolygons - quotaTrain;

		trainPolygonNumbers = new int[quotaTrain];
		testPolygonNumbers = new int[quotaTest];
		int indexTrainPolygon = 0;
		int indexTestPolygon = 0;

		Random r = new Random(seed);

		int nPolygonsToAssign = nPolygons;
		for (int polygonID : polygonNumbers) {
			int chosen = r.nextInt(nPolygonsToAssign);
			if (chosen<quotaTrain) {
				// this is a train polygon
				trainPolygonNumbers[indexTrainPolygon] = polygonID;
				indexTrainPolygon++;
				quotaTrain--;
				
			} else {
				// this is a polygon for test
				testPolygonNumbers[indexTestPolygon] = polygonID;
				indexTestPolygon++;
				quotaTest--;
			}
			nPolygonsToAssign--;
		}
		
	}

	public double getPercentageForTrain() {
		return percentageForTrain;
	}

	public void setPercentageForTrain(double percentageForTrain) {
		this.percentageForTrain = percentageForTrain;
	}

	public double getSamplingRate() {
		return samplingRate;
	}

	public void setSamplingRate(double samplingRate) {
		this.samplingRate = samplingRate;
	}
	
	public String toString(){
		String res = "error rate = "+getErrorRate()+"\n";
		res+="confusion matrix (truth x predicted)\n";
		for (int j = 0; j < nClasses; j++) {
			res+="\t"+classesNames[j];
		}
		res+="\n";
		for (int i = 0; i < nClasses; i++) {
			res+=classesNames[i];
			for (int j = 0; j < nClasses; j++) {
				res+="\t"+confusionMatrix[i][j];
			}
			res+="\n";
		}
		return res;
	}

}

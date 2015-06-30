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
import classification.NearestNeighbor;
import data.Dataset;
import data.TimeSeries;

public class NNEvaluation {

	File datasetFile;

	/**
	 * Percentage of the dataset to train on
	 */
	double percentageForTrain = .5;

	/**
	 * Percentage of pixels to use in each polygon (both train and test)
	 */
	double samplingRate = 1.0;

	private BufferedReader reader;

	private FileReader fReader;

	private int[] polygonNumbers;

	private int[] trainPolygonNumbers;

	private int[] testPolygonNumbers;
	
	Dataset train,test;

	private ArrayList<TimeSeries> trainTimeSeries;
	
	private ArrayList<Integer> trainClassIndexes;

	private int nErrors;

	private int nSeriesTested;
	

	private static final int N_BYTES = 1024000;
	private static final int ID_PIXEL_ATTRIBUTE = 0;
	private static final int ID_POLYGON_ATTRIBUTE = 1;
	private static final int CLASS_ATTRIBUTE = 4;
	private static final int INDEX_START_NDVI = 111;
	private static final int LENGTH_TIME_SERIES = 15;

	public NNEvaluation(File datasetFile) {
		this.datasetFile = datasetFile;
	}

	public void evaluate() throws NumberFormatException, IOException {
		createFolds(3071980);
		
		Random r = new Random();
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
		NearestNeighbor nnClassifier = new NearestNeighbor(new Euclidean());
		nnClassifier.train(trainDataset);
		
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
						timeSeries[i]=Double.valueOf(splitted[INDEX_START_NDVI+i])/1000.0;
					}
					TimeSeries ts = new TimeSeries(timeSeries, pixelID, polygonID);
					int predictedClassIndex = nnClassifier.classify(ts);
					
					int classIndex = Integer.valueOf(splitted[CLASS_ATTRIBUTE]);
//					System.out.println("predicted ="+predictedClassIndex+"\tref="+classIndex);
					if(classIndex!=predictedClassIndex){
						nErrors++;
					}
					nSeriesTested++;
				}
			}
		}
		
		
		
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

}

package data.wrangling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * Wrote this file to create a train/test fold for the 2006 SudOuest Formosat-2 data
 * @author Francois Petitjean
 *
 */
public class CreateTrainTestFoldsWithPolygons {

	protected File datasetFile;

	/**
	 * Percentage of the dataset to train on
	 */
	protected double percentageForTrain = .9;

	protected BufferedReader reader;

	protected FileReader fReader;

	protected int[] polygonNumbers;

	protected ArrayList<Integer> trainPolygonNumbers,testPolygonNumbers;

	protected HashMap<Integer, Integer> numberSeriesForParcel;
	
	protected ArrayList<Integer> trainClassIndexes;
	
	protected HashSet<String> classesToSkip;
	
	protected int nTotalSeries;

	private long seed;
	
	protected static final int N_BYTES = 1024000;
	protected static final int ID_POLYGON_ATTRIBUTE = 1;
	protected static final int CLASS_ATTRIBUTE = 0;
	protected static final int INDEX_START_DATA_ATTRIBUTES = 2;
	protected static final int LENGTH_TIME_SERIES = 46;
	protected static final int N_BANDS = 3;
	protected static final int N_DATA_ATTRIBUTES_PER_DATE = N_BANDS;//add number if indices as well
	protected final boolean hasHeader = true;
	
	private Random r ;

	public CreateTrainTestFoldsWithPolygons(File datasetFile) {
		this.datasetFile = datasetFile;
		this.seed = 3071980L;
	}
	
	public void createTrainTestFiles(File trainFile,File testFile) throws NumberFormatException, IOException{
		r = new Random(seed);
		createFolds();
		
		//build train and test datasets (1pass over the data)
		trainClassIndexes = new ArrayList<Integer>();
		fReader = new FileReader(this.datasetFile);
		reader = new BufferedReader(fReader, N_BYTES);
		String line;
		
		BufferedWriter csvTrain = new BufferedWriter(new FileWriter(trainFile), N_BYTES);
		BufferedWriter csvTest = new BufferedWriter(new FileWriter(testFile), N_BYTES);
		
		//skip header
		if(hasHeader)reader.readLine();
		
		Collections.sort(trainPolygonNumbers);
		Collections.sort(testPolygonNumbers);
		
		while ((line = reader.readLine()) != null) {
			String[] splitted = line.split(",");
			String className = splitted[CLASS_ATTRIBUTE]; 
			if(classesToSkip==null || !classesToSkip.contains(className)){
        			int polygonID = Integer.valueOf(splitted[ID_POLYGON_ATTRIBUTE]);
        //			int indexInTrain = Arrays.binarySearch(trainPolygonNumbers, parcelID);
        			int indexInTrain = Collections.binarySearch(trainPolygonNumbers, polygonID);
        			
        			BufferedWriter csv;
        			if(indexInTrain>=0){
        				csv = csvTrain;
        			}else{
        				csv = csvTest;
        			}
        			
        			csv.write(splitted[CLASS_ATTRIBUTE]);
        			
        			for (int i = INDEX_START_DATA_ATTRIBUTES; i < splitted.length; i++) {
        				csv.write(","+splitted[i]);
        			}
        			csv.newLine();
			}
		}
		csvTrain.close();
		csvTest.close();
		
	}

	private void createFolds() throws NumberFormatException, IOException {
		// find number of polygons
		fReader = new FileReader(this.datasetFile);
		reader = new BufferedReader(fReader, N_BYTES);
		String line;
		String[] splitted=null;
		numberSeriesForParcel = new HashMap<Integer, Integer>();
		nTotalSeries = 0;
		
		//skip header
		if(hasHeader)reader.readLine();
		
		while ((line = reader.readLine()) != null) {
			splitted = line.split(",");
			String className = splitted[CLASS_ATTRIBUTE]; 
			if(classesToSkip==null || !classesToSkip.contains(className)){
        			int polygonID = Integer.valueOf(splitted[ID_POLYGON_ATTRIBUTE]);
        			Integer numberSeries = numberSeriesForParcel.get(polygonID);
        			if(numberSeries==null){
        				numberSeriesForParcel.put(polygonID, 1);
        			}else{
        				numberSeriesForParcel.put(polygonID, numberSeries+1);
        			}
        			nTotalSeries++;
			}
		}

		int nPolygons = numberSeriesForParcel.size();
		polygonNumbers = new int[nPolygons];
		int index = 0;
		for (Integer polygonNumber:numberSeriesForParcel.keySet()) {
			polygonNumbers[index] = polygonNumber;
			index++;
		}
		Arrays.sort(polygonNumbers);
//		System.out.println(Arrays.toString(polygonNumbers));

		//~ number of series to try to get in train
		int quotaTrain = (int) Math.ceil(nTotalSeries * percentageForTrain);
		int quotaTest = nTotalSeries - quotaTrain;

		trainPolygonNumbers = new ArrayList<>();
		testPolygonNumbers = new ArrayList<>();
		
		int nRemainingSeries = nTotalSeries;

		
//		int nPolygonsToAssign = nPolygons;
		for (int polygonID : polygonNumbers) {
			
			//find the fold for 
//			int chosen = r.nextInt(nPolygonsToAssign);
			
			int chosen = r.nextInt(nRemainingSeries);
			int nSeriesForPolygon =numberSeriesForParcel.get(polygonID); 
			
			if (chosen<quotaTrain) {
				System.out.println("polygon "+polygonID+" assigned to train");
				// this is a train polygon
				trainPolygonNumbers.add(polygonID);
				quotaTrain-=nSeriesForPolygon;
				
			} else {
				System.out.println("polygon "+polygonID+" assigned to test");
				// this is a polygon for test
				testPolygonNumbers.add(polygonID);
				quotaTest-=nSeriesForPolygon;
			}
//			nPolygonsToAssign--;
			nRemainingSeries-=nSeriesForPolygon;
		}
		
	}

	public double getPercentageForTrain() {
		return percentageForTrain;
	}

	public void setPercentageForTrain(double percentageForTrain) {
		this.percentageForTrain = percentageForTrain;
	}
	
	public void setClassesToSkip(HashSet<String> classes){
		this.classesToSkip = classes;
	}

	
	public static void main(String...args) throws NumberFormatException, IOException{
		File csvWithPolygons = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-NDVI-with-plots-interpolated.csv");
		File train = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-NDVI-with-plots-interpolated-train-90.csv");
		File test = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-NDVI-with-plots-interpolated-test-10.csv");
		double percentageTrainTest = 0.9;
		HashSet<String> classesToSkip = new HashSet<>();
		classesToSkip.add("bati diffus");
		classesToSkip.add("bati dense");
		classesToSkip.add("bati indu");
		classesToSkip.add("surface minerale");
		classesToSkip.add("unclassified");
					
		
		CreateTrainTestFoldsWithPolygons wrangler = new CreateTrainTestFoldsWithPolygons(csvWithPolygons);
		wrangler.setPercentageForTrain(percentageTrainTest);
//		wrangler.setClassesToSkip(classesToSkip);
		
		
		wrangler.createTrainTestFiles(train, test);
	}
	
}

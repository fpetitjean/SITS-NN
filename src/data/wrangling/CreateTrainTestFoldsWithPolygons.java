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
import java.util.Optional;
import java.util.Random;

/**
 * Wrote this file to create a train/test fold for the 2006 SudOuest Formosat-2
 * data
 * 
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

	protected ArrayList<Integer> trainPolygonNumbers, testPolygonNumbers;

	protected HashMap<Integer, Integer> numberSeriesForParcel;

	protected HashMap<String, Integer> classToIndex;

	protected HashSet<String> classesToSkip;

	protected int nTotalSeries;

	protected int nClasses;

	private long seed;

	protected static final int N_BYTES = 1024000;
	protected static final int ID_POLYGON_ATTRIBUTE = 1;
	protected static final int LENGTH_TIME_SERIES = 61*3+2;
	
//	protected static final int CLASS_ATTRIBUTE = 369;
//	protected static final int INDEX_START_DATA_ATTRIBUTES = 3;
//	protected final boolean hasHeader = true;
	
	protected static final int CLASS_ATTRIBUTE = 0;
	protected static final int INDEX_START_DATA_ATTRIBUTES = 2;
	protected final boolean hasHeader = true;
	
	private Random r;

	protected boolean classAsNumber;
	protected boolean outputPolygon;
	protected boolean arff = true; // csv if false

	public CreateTrainTestFoldsWithPolygons(File datasetFile) {
		this.datasetFile = datasetFile;
		this.seed = 3071980L;
		classToIndex = new HashMap<>();
	}

	public void createTrainTestFiles(File trainFile, File testFile) throws NumberFormatException, IOException {
		r = new Random(seed);
		createFolds();

		// build train and test datasets (1pass over the data)
		fReader = new FileReader(this.datasetFile);
		reader = new BufferedReader(fReader, N_BYTES);
		String line;

		BufferedWriter csvTrain = new BufferedWriter(new FileWriter(trainFile), N_BYTES);
		BufferedWriter csvTest = new BufferedWriter(new FileWriter(testFile), N_BYTES);

		if (arff) {// then have to write the header

			// extracting metadata about the data
			if (hasHeader) {
				line = reader.readLine();
			}

			while ((line = reader.readLine()) != null) {
				String[] splitted = line.split(",");
				String className = splitted[CLASS_ATTRIBUTE];

				Integer classIndex = classToIndex.get(className);

				if (classAsNumber && classIndex == null) {
					// find max in keys
					Optional<Integer> mOpt = classToIndex.values().stream().max(Integer::compare);
					classIndex = (mOpt.isPresent()) ? mOpt.get() + 1 : 0;
					classToIndex.put(className, classIndex);
					System.out.println(className + " is mapped to #" + classIndex);
				}
			}
			// reset stream for later reading
			fReader = new FileReader(this.datasetFile);
			reader = new BufferedReader(fReader, N_BYTES);

			nClasses = classToIndex.size();

			csvTrain.write("@relation satellite\n\n");
			csvTest.write("@relation satellite\n\n");
			for (int t = 0; t < LENGTH_TIME_SERIES; t++) {
				csvTrain.write("@attribute t" + (t + 1) + " numeric\n");
				csvTest.write("@attribute t" + (t + 1) + " numeric\n");
			}

			csvTrain.write("@attribute class {c0");
			csvTest.write("@attribute class {c0");

			for (int c = 1; c < nClasses; c++) {
				csvTrain.write(",c" + c);
				csvTest.write(",c" + c);
			}

			csvTrain.write("}\n\n@data\n\n");
			csvTest.write("}\n\n@data\n\n");

		}

		// header
		if (hasHeader) {
			line = reader.readLine();
			if (!arff) {
//				csvTrain.write(line);
//				csvTrain.newLine();
//				csvTest.write(line);
//				csvTest.newLine();
			}
		}

		Collections.sort(trainPolygonNumbers);
		Collections.sort(testPolygonNumbers);

		while ((line = reader.readLine()) != null) {
			line = line.replaceAll("\"", "");
			String[] splitted = line.split(",");
			String className = splitted[CLASS_ATTRIBUTE];

			Integer classIndex = classToIndex.get(className);

			if (classAsNumber && classIndex == null) {
				// find max in keys
				Optional<Integer> mOpt = classToIndex.values().stream().max(Integer::compare);
				classIndex = (mOpt.isPresent()) ? mOpt.get() + 1 : 0;
				classToIndex.put(className, classIndex);
				System.out.println(className + " is mapped to #" + classIndex);
			}

			if (classesToSkip == null || !classesToSkip.contains(className)) {

				int polygonID = Integer.valueOf(splitted[ID_POLYGON_ATTRIBUTE]);
				// int indexInTrain =
				// Arrays.binarySearch(trainPolygonNumbers,
				// parcelID);
				int indexInTrain = Collections.binarySearch(trainPolygonNumbers, polygonID);

				BufferedWriter csv;
				if (indexInTrain >= 0) {
					csv = csvTrain;
				} else {
					csv = csvTest;
				}

				// csv.write(splitted[CLASS_ATTRIBUTE]);
				
				if(arff){//class at the end
        				for (int i = INDEX_START_DATA_ATTRIBUTES; i < (LENGTH_TIME_SERIES+INDEX_START_DATA_ATTRIBUTES); i++) {
        					csv.write(splitted[i]+",");
        				}
        				csv.write("c" + classIndex);
        				csv.newLine();
				}else{
        				if (classAsNumber) {
        					csv.write("" + classIndex);
        				} else {
        					csv.write(splitted[CLASS_ATTRIBUTE]);
        				}
        				if (outputPolygon) {
        					csv.write("," + polygonID);
        				}
        
//        				for (int i = INDEX_START_DATA_ATTRIBUTES; i < (LENGTH_TIME_SERIES+INDEX_START_DATA_ATTRIBUTES); i++) {
        				for (int i = INDEX_START_DATA_ATTRIBUTES; i < splitted.length; i++) {
        					csv.write("," + splitted[i]);
        				}
        				csv.newLine();
				}
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
		String[] splitted = null;
		numberSeriesForParcel = new HashMap<Integer, Integer>();
		nTotalSeries = 0;

		// skip header
		if (hasHeader)
			reader.readLine();

		while ((line = reader.readLine()) != null) {
			line = line.replaceAll("\"", "");
			splitted = line.split(",");
			String className = splitted[CLASS_ATTRIBUTE];
			if (classesToSkip == null || !classesToSkip.contains(className)) {
				int polygonID = Integer.valueOf(splitted[ID_POLYGON_ATTRIBUTE]);
				Integer numberSeries = numberSeriesForParcel.get(polygonID);
				if (numberSeries == null) {
					numberSeriesForParcel.put(polygonID, 1);
				} else {
					numberSeriesForParcel.put(polygonID, numberSeries + 1);
				}
				nTotalSeries++;
			}
		}

		int nPolygons = numberSeriesForParcel.size();
		polygonNumbers = new int[nPolygons];
		int index = 0;
		for (Integer polygonNumber : numberSeriesForParcel.keySet()) {
			polygonNumbers[index] = polygonNumber;
			index++;
		}
		Arrays.sort(polygonNumbers);
		// System.out.println(Arrays.toString(polygonNumbers));

		// ~ number of series to try to get in train
		int quotaTrain = (int) Math.ceil(nTotalSeries * percentageForTrain);

		trainPolygonNumbers = new ArrayList<>();
		testPolygonNumbers = new ArrayList<>();

		int nRemainingSeries = nTotalSeries;

		// int nPolygonsToAssign = nPolygons;
		for (int polygonID : polygonNumbers) {

			// find the fold for
			// int chosen = r.nextInt(nPolygonsToAssign);

			int chosen = r.nextInt(nRemainingSeries);
			int nSeriesForPolygon = numberSeriesForParcel.get(polygonID);

			if (chosen < quotaTrain) {
				System.out.println("polygon " + polygonID + " assigned to train");
				// this is a train polygon
				trainPolygonNumbers.add(polygonID);
				quotaTrain -= nSeriesForPolygon;

			} else {
				System.out.println("polygon " + polygonID + " assigned to test");
				// this is a polygon for test
				testPolygonNumbers.add(polygonID);
			}
			// nPolygonsToAssign--;
			nRemainingSeries -= nSeriesForPolygon;
		}

	}

	public double getPercentageForTrain() {
		return percentageForTrain;
	}

	public void setPercentageForTrain(double percentageForTrain) {
		this.percentageForTrain = percentageForTrain;
	}

	public void setClassesToSkip(HashSet<String> classes) {
		this.classesToSkip = classes;
	}

	public void setClassAsNumber(boolean b) {
		this.classAsNumber = b;

	}

	public void setOutputPolygon(boolean b) {
		this.outputPolygon = b;
	}
	
	public void setArff(boolean b){
		this.arff = b;
	}
	
	public void setSeed(long seed) {
		this.seed = seed;
	}

	public static void main2(String... args) throws NumberFormatException, IOException {
//		File csvWithPolygons = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-NDVI-with-plots-interpolated-shuf.csv");
//		File train = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-NDVI-interpolated-train-90-num.csv");
//		File test = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-NDVI-interpolated-test-10-num.csv");
//		File train = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-NDVI-interpolated-train-90-num.arff");
//		File test = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-NDVI-interpolated-test-10-num.arff");
		
//		File csvWithPolygons = new File("/home/petitjean/Dropbox/Data/vergers/time_series/LinearI_ndvi-shuf.csv");
//		File train = new File("/home/petitjean/Dropbox/Data/vergers/time_series/LinearI_ndvi_train.csv");
//		File test = new File("/home/petitjean/Dropbox/Data/vergers/time_series/LinearI_ndvi_test.csv");
//		
//		File csvWithPolygons = new File("/home/petitjean/Dropbox/Data/vergers/time_series/LinearI_ndvi_train.csv");
//		File train = new File("/home/petitjean/Dropbox/Data/vergers/time_series/LinearI_ndvi_train_train.csv");
//		File test = new File("/home/petitjean/Dropbox/Data/vergers/time_series/LinearI_ndvi_train_val.csv");
		
		File csvWithPolygons = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-RPG-with-plots-interpolated-2-days.csv");
		File train = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-RPG-with-plots-interpolated-2-days-train.csv");
		File test = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-RPG-with-plots-interpolated-2-days-test.csv");
		
		
//		File train = new File("/home/petitjean/Dropbox/share-Germain/TS_Data_Bagnall/SatelliteFull/SITS-2006-NDVI-interpolated-train-90-num.arff");
//		File test = new File("/home/petitjean/Dropbox/share-Germain/TS_Data_Bagnall/SatelliteFull/SITS-2006-NDVI-interpolated-test-10-num.arff");
		
		double percentageTrainTest = 0.9;
//		HashSet<String> classesToSkip = new HashSet<>();
//		classesToSkip.add("bati diffus");
//		classesToSkip.add("bati dense");
//		classesToSkip.add("bati indu");
//		classesToSkip.add("surface minerale");
//		classesToSkip.add("unclassified");

		CreateTrainTestFoldsWithPolygons wrangler = new CreateTrainTestFoldsWithPolygons(csvWithPolygons);
		wrangler.setPercentageForTrain(percentageTrainTest);
		wrangler.setClassAsNumber(true);
		wrangler.setOutputPolygon(true);
		wrangler.setArff(false);
		// wrangler.setClassesToSkip(classesToSkip);

		wrangler.createTrainTestFiles(train, test);
	}
	
	public static void main(String...args) throws NumberFormatException, IOException {
//		String version = "original-sampling";
		String version = "2-days";
//		String version = "5-days";
		File csvWithPolygons = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-RPG-with-plots-interpolated-"+version+".csv");
		double percentageTrainTest = 0.9;
		for(int e=0;e<10;e++) {
			File train = new File("/home/petitjean/Dropbox/share-Charlotte/SITS-2006/RPG-"+version+"-train-fold-"+e+".csv");
			File test = new File("/home/petitjean/Dropbox/share-Charlotte/SITS-2006/RPG-"+version+"-test-fold-"+e+".csv");
			CreateTrainTestFoldsWithPolygons wrangler = new CreateTrainTestFoldsWithPolygons(csvWithPolygons);
			wrangler.setPercentageForTrain(percentageTrainTest);
			wrangler.setClassAsNumber(false);
			wrangler.setOutputPolygon(true);
			wrangler.setArff(false);
			wrangler.setSeed(3071980+e);
			wrangler.createTrainTestFiles(train, test);
		}
	}

}

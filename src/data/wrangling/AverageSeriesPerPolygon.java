package data.wrangling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import data.TimeSeries;
import data.TimeSeriesMultiDim;
import measures.Averageable;
import measures.Euclidean;

/**
 * Wrote this file to create a train/test fold for the 2006 SudOuest Formosat-2
 * data
 * 
 * @author Francois Petitjean
 *
 */
public class AverageSeriesPerPolygon {

	protected File datasetFile;

	protected BufferedReader reader;

	protected FileReader fReader;

	protected HashMap<Integer, Integer> numberSeriesForParcel;

	protected boolean averageSeries = false;

	protected int nTotalSeries;
	
	protected NumberFormat nf;

	protected static final int N_BYTES = 1024000;
	protected static final int ID_POLYGON_ATTRIBUTE = 1;
	protected static final int CLASS_ATTRIBUTE = 0;
	protected static final int INDEX_START_DATA_ATTRIBUTES = 2;
	protected static final int LENGTH_TIME_SERIES = 46;
	protected static final int N_BANDS = 1;
	protected static final int N_DATA_ATTRIBUTES_PER_DATE = N_BANDS+0;
	protected final boolean hasHeader = true;
	
	protected Averageable<TimeSeries> averagingMethod;

	public AverageSeriesPerPolygon(File datasetFile,Averageable<TimeSeries> averagingMethod) {
		this.datasetFile = datasetFile;
		this.averagingMethod = averagingMethod;
		this.nf = NumberFormat.getInstance();
		this.nf.setMaximumFractionDigits(4);
	}

	public void createData(File trainFile) throws NumberFormatException, IOException {
		findNSeriesForPolygons();
		System.out.println(numberSeriesForParcel);

		// build train and test datasets (1pass over the data)
		fReader = new FileReader(this.datasetFile);
		reader = new BufferedReader(fReader, N_BYTES);
		String line;

		BufferedWriter out = new BufferedWriter(new FileWriter(trainFile), N_BYTES);

		// header
		if (hasHeader) {
			line = reader.readLine();
			out.write(line);
			out.newLine();
		}
		HashMap<Integer,ArrayList<TimeSeries>> seriesForParcel = new HashMap<Integer, ArrayList<TimeSeries>>();
		int pixelID = 0;
		while ((line = reader.readLine()) != null) {
			String[] splitted = line.split(",");

			int polygonID = Integer.valueOf(splitted[ID_POLYGON_ATTRIBUTE]);
			
			//this is for train (for averaging purposes, doesn't matter about the band order, just averaging)
			double[] timeSeries = new double[LENGTH_TIME_SERIES*N_DATA_ATTRIBUTES_PER_DATE];
			for (int i = 0; i < timeSeries.length; i++) {
				timeSeries[i]=Double.valueOf(splitted[INDEX_START_DATA_ATTRIBUTES+i]);
			}
			
			TimeSeries ts = new TimeSeries(timeSeries, pixelID, polygonID);
			pixelID++;
			
			ArrayList<TimeSeries> listForParcel = seriesForParcel.get(polygonID);
			if(listForParcel==null){
				listForParcel = new ArrayList<>();
				seriesForParcel.put(polygonID, listForParcel);
			}
			listForParcel.add(ts);

			if(listForParcel.size()==numberSeriesForParcel.get(polygonID)){
				//we have all the series for the parcel
				//so we can create the prototype and delete the series we've been storing
				
				//creating and storing the average
				TimeSeries mean = averagingMethod.average(listForParcel);
				double[] meanTS = mean.getSeries();
//				System.out.println("mean computed:"+mean);
				
				out.write(splitted[CLASS_ATTRIBUTE]+","+polygonID);

				for (int i = 0; i < meanTS.length; i++) {
					out.write("," + nf.format(meanTS[i]));
				}
				out.newLine();
				
				//removing, from the memory, the series from which we created the average
				listForParcel.clear();
				listForParcel = null;
				seriesForParcel.remove(polygonID);
			}
		}
		out.close();
	}

	private void findNSeriesForPolygons() throws NumberFormatException, IOException {
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
			splitted = line.split(",");
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

	public static void main(String... args) throws NumberFormatException, IOException {
		File csvWithPolygons = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-NDVI-with-plots-interpolated-test-10.csv");
		File out = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-NDVI-with-plots-interpolated-test-10-averaged.csv");
		AverageSeriesPerPolygon wrangler = new AverageSeriesPerPolygon(csvWithPolygons,new Euclidean());
		wrangler.createData(out);
	}

}

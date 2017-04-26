package data.wrangling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import javax.management.RuntimeErrorException;

/**
 * Wrote this file to create a train/test fold for the 2006 SudOuest Formosat-2 data
 * @author Francois Petitjean
 *
 */
public class LinearInterpolationSeries {

	protected File datasetFile;
	protected BufferedReader reader;
	protected FileReader fReader;
	NumberFormat nf;
	
	protected static final int N_BYTES = 1024000;
	protected static final int ID_POLYGON_ATTRIBUTE = 1;
	protected static final int CLASS_ATTRIBUTE = 0;
	protected static final int INDEX_START_DATA_ATTRIBUTES = 2;
	protected static final int LENGTH_TIME_SERIES = 46;
	protected static final int N_BANDS = 1;
	protected static final int N_DATA_ATTRIBUTES_PER_DATE = N_BANDS;//add number if indices as well
	protected final boolean hasHeader = true;
	
	
	public LinearInterpolationSeries(File datasetFile) {
		this.datasetFile = datasetFile;
		this.nf = NumberFormat.getInstance();
		
	}
	
	public void createInterpolatedDataset(File interpolatedDataFile,int nDigits) throws NumberFormatException, IOException{
		nf.setMaximumFractionDigits(nDigits);
		fReader = new FileReader(this.datasetFile);
		reader = new BufferedReader(fReader, N_BYTES);
		String line;
		
		BufferedWriter out = new BufferedWriter(new FileWriter(interpolatedDataFile), N_BYTES);
		
		//skip header
		if(hasHeader){
			line = reader.readLine();
			out.write(line);
			out.newLine();
		}
		
		double[][]series=new double[LENGTH_TIME_SERIES][N_BANDS];
		boolean[]missing = new boolean[LENGTH_TIME_SERIES];
		
		
		while ((line = reader.readLine()) != null) {
			String[] splitted = line.split(",");
			
			//getting all values and noting the missing ones
			for (int t = 0; t < LENGTH_TIME_SERIES; t++) {
				
				int indexFirstValInSplitted = INDEX_START_DATA_ATTRIBUTES+t*N_DATA_ATTRIBUTES_PER_DATE;
				if(splitted[indexFirstValInSplitted].equals("?")){
					missing[t]=true;
				}else{
					missing[t]=false;
					for (int a = 0; a < N_DATA_ATTRIBUTES_PER_DATE; a++) {
						series[t][a]=Double.valueOf(splitted[indexFirstValInSplitted+a]);
					}
				}
			}

			//now have all the values, interpolate them
			
			//propagate the first non-missing value back to the start and the last non-missing value to the end
			int index = 0;
			while(index<series.length && missing[index]){
				index++;
			}
			for (int t = 0; t < index; t++) {
				for (int a = 0; a < N_DATA_ATTRIBUTES_PER_DATE; a++) {
					series[t][a]=series[index][a];
				}
				missing[t]=false;
			}
			
			index = series.length-1;
			while(index>=0 && missing[index]){
				index--;
			}
			for (int t = series.length-1; t > index; t--) {
				for (int a = 0; a < N_DATA_ATTRIBUTES_PER_DATE; a++) {
					series[t][a]=series[index][a];
				}
				missing[t]=false;
			}
			
			//now for each missing value, we have to find its previous non-missing and next non-missing
			for (int t = 1; t < series.length; t++) {
				if(!missing[t])continue;
				//this is a missing value, previous one will always exist (doing a propagating forward interpolation)
				double[]previousElement = series[t-1];
				
				//finding next non-missing value
				index = t+1;
				while(missing[index]){
					index++;
				}
				//index now has the first value that is not missing after t
				
				double[]nextElement = series[index];
				
				//now filling the missing values
				for (int t1 = t; t1 < index; t1++) {
					for (int a = 0; a < N_DATA_ATTRIBUTES_PER_DATE; a++) {
						double unitSlope = (nextElement[a]-previousElement[a])/(index-(t-1));
						series[t1][a]=previousElement[a]+unitSlope*(t1-(t-1));
					}
					missing[t1]=false;
				}
			}
			//check no missing remaining
			for (int t = 0; t < series.length; t++) {
				if(missing[t]){
					throw new RuntimeException("Still a missing value here");
				}
			}
			
			out.write(splitted[0]);
			for (int i = 1; i < INDEX_START_DATA_ATTRIBUTES; i++) {
				out.write(","+splitted[i]);
			}
			for (int t = 0; t < series.length; t++) {
				for (int a = 0; a < N_DATA_ATTRIBUTES_PER_DATE; a++) {
					out.write(","+nf.format(series[t][a]));
				}
			}
			out.newLine();
			
			
		}
		out.close();
		
	}

	
	public static void main(String...args) throws NumberFormatException, IOException{
		File csvIn= new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-NDVI-with-plots.csv");
		File out = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-NDVI-with-plots-interpolated.csv");
		
		LinearInterpolationSeries wrangler = new LinearInterpolationSeries(csvIn);
		wrangler.createInterpolatedDataset(out,4);
	}
	
}

package data.wrangling;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Wrote this file to create a train/test fold for the 2006 SudOuest Formosat-2 data
 * 
 * @author Francois Petitjean
 */
public class LinearInterpolationSeriesRegular extends LinearInterpolationSeries {

	protected long samplingFrequency;
	protected int lengthSeriesOutput;

	public LinearInterpolationSeriesRegular(File datasetFile, Date[] dates,int samplingFrequency) {
		super(datasetFile);
		this.setDates(dates);
		this.setSamplingFrequency(samplingFrequency);
	}
	
	@Override
	protected void writeHeader(BufferedWriter out, String line) throws IOException {
		GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
		
		String[] splitted = line.split(",");
		out.write(splitted[0]);
		for (int i = 1; i < INDEX_START_DATA_ATTRIBUTES; i++) {
			out.write(","+splitted[i]);
		}
		for(int t=0;t<lengthSeriesOutput;t++) {
			long timestamp4T = dates[0]+samplingFrequency*t;
			cal.setTimeInMillis(timestamp4T);
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH)+1;
			int day = cal.get(Calendar.DAY_OF_MONTH);
			String dateStr = year+"-";
			dateStr+=(month<10)?"0"+month:month;
			dateStr+="-";
			dateStr+=(day<10)?"0"+day:day;
			dateStr+="-";
			
			for (int a = 0; a < N_DATA_ATTRIBUTES_PER_DATE; a++) {
				out.write(","+dateStr+a);
			}
		}
		out.newLine();
		
	}

	@Override
	protected double[][] getSeriesFromOriginal(double[][] series) {
		double[][] outputSeries = new double[lengthSeriesOutput][N_DATA_ATTRIBUTES_PER_DATE];
		
		// now no missing in original data
		for (int t = 0; t < outputSeries.length; t++) {
			// find previous datapoint and next datapoint in original data
			int indexInOriginalSeries = 0;
			long timestampT = dates[0]+t*samplingFrequency;
			while (indexInOriginalSeries<dates.length && dates[indexInOriginalSeries]<timestampT) {
				indexInOriginalSeries++;
			}
			
			if(timestampT>dates[dates.length-1]) {
				//need a date for after final one
				//TODO no need to do the while loop above
				outputSeries[t] = series[dates.length-1];
			}else if (timestampT == dates[indexInOriginalSeries]) {
				// this means the point is spot on on a day where we had a sensing
				outputSeries[t] = series[indexInOriginalSeries];
			} else {
				//normal case
				double[] previousElement = series[indexInOriginalSeries - 1];
				long datePrevious = dates[indexInOriginalSeries - 1];
				double[] nextElement = series[indexInOriginalSeries];
				long dateNext = dates[indexInOriginalSeries];

				// now interpolating for t
				for (int a = 0; a < N_DATA_ATTRIBUTES_PER_DATE; a++) {
					double unitSlope = (nextElement[a] - previousElement[a])
							/ (dateNext - datePrevious);
					outputSeries[t][a] = previousElement[a] + unitSlope * (timestampT - datePrevious);
					outputSeries[t][a] = Math.round(outputSeries[t][a]*10.0)/10.0;//rounding to 1 decimal place
				}
			}

		}
		return outputSeries;
	}

	/**
	 * Sets the requested sampling frequency.
	 * 
	 * @param nDdaysFrequency
	 *            number of days between samples; eg 7 means weekly sampling
	 */
	public void setSamplingFrequency(int nDdaysFrequency) {
		this.samplingFrequency = nDdaysFrequency * 24 * 3600*1000;//milliseconds
		long firstTimestamp = dates[0];
		long lastTimestamp = dates[dates.length - 1];
		lengthSeriesOutput = (int) ((lastTimestamp - firstTimestamp) / samplingFrequency)+1;
		if ((lastTimestamp - firstTimestamp) % samplingFrequency != 0) {
			lengthSeriesOutput++;
		}
		System.out.println("creating series with "+lengthSeriesOutput+" timestamps");

	}

	public static void main(String... args)
			throws NumberFormatException, IOException, ParseException {
		int samplingFrequency = 5;
		
		String prefix = "/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-RPG-no-label";
		
		File csvIn = new File(prefix+".csv");
		File out = new File(prefix+"-interpolated-"+samplingFrequency+"-days.csv");

		File folderWithImagesForDates = new File(
				"/home/petitjean/Dropbox/Data/SITS/Sudouest/2006-3B");
		File[] files = folderWithImagesForDates.listFiles(f -> f.getName().endsWith("tif"));
		Arrays.sort(files);
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		Date[] dates = new Date[files.length];
		for (int i = 0; i < dates.length; i++) {
			String fileName = files[i].getName();
			fileName = fileName.substring(0, fileName.length() - 4);
			System.out.println(fileName);
			dates[i] = df.parse(fileName);
		}

		LinearInterpolationSeriesRegular wrangler = new LinearInterpolationSeriesRegular(csvIn,
				dates,samplingFrequency);
		wrangler.createInterpolatedDataset(out);
	}

}

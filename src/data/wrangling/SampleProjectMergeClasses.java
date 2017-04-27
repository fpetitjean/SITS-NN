package data.wrangling;

import java.awt.Color;
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
 * Wrote this file to subsample the 2006 SudOuest Formosat-2 data while merging some classes
 * @author Francois Petitjean
 *
 */
public class SampleProjectMergeClasses {

	protected File datasetFile;

	protected double subsample;

	protected BufferedReader reader;

	protected FileReader fReader;

	protected int[] polygonNumbers;

	protected ArrayList<Integer> trainClassIndexes;
	
	protected HashSet<String> classesToSkip;
	
	protected HashMap<String,Integer> classToIndex;
	protected HashMap<String,String> classRenamer;
	
	protected int nTotalSeries;

	private long seed;
	
	protected static final int N_BYTES = 100*1024*1024;
	protected static final int CLASS_ATTRIBUTE = 0;
	protected static final int INDEX_START_DATA_ATTRIBUTES = 2;
	protected final boolean hasHeader = false;
	
	private Random r ;

	public SampleProjectMergeClasses(File datasetFile,double proportion) {
		this.datasetFile = datasetFile;
		this.seed = 3071980L;
		this.classToIndex = new HashMap<>();
		this.subsample = proportion;
	}
	
	public void createSample(File outFile) throws NumberFormatException, IOException{
		r = new Random(seed);
		//build train and test datasets (1pass over the data)
		trainClassIndexes = new ArrayList<Integer>();
		fReader = new FileReader(this.datasetFile);
		reader = new BufferedReader(fReader, N_BYTES);
		String line;
		
		BufferedWriter out = new BufferedWriter(new FileWriter(outFile), N_BYTES);
		
		//header
		if(hasHeader){
			line = reader.readLine();
			out.write(line);
			out.newLine();
		}
		
		while ((line = reader.readLine()) != null) {
			if(r.nextDouble()>subsample){
				continue;
			}
			String[] splitted = line.split(",");
			String className = splitted[CLASS_ATTRIBUTE];
			
			if(classesToSkip!=null && classesToSkip.contains(className)){
				continue;
			}
			
			if(classRenamer!=null && classRenamer.containsKey(className)){
				className = classRenamer.get(className);
			}
			
			Integer classIndex = classToIndex.get(className);
			if(classIndex==null){
				//find max in keys
				Optional<Integer> mOpt = classToIndex.values().stream().max(Integer::compare);
				classIndex = (mOpt.isPresent())?mOpt.get()+1:0;
				classToIndex.put(className, classIndex);
				System.out.println(className+" is mapped to #"+classIndex);
			}
			//write class
        		out.write(""+classIndex);
        			
        		//write data
			for (int i = INDEX_START_DATA_ATTRIBUTES; i < splitted.length; i++) {
				out.write(","+splitted[i]);
			}
       			out.newLine();
		}
		out.close();
		
	}
	
	public void setClassesToSkip(HashSet<String> classes){
		this.classesToSkip = classes;
	}
	
	public void setClassRenamer(HashMap<String, String> renamer){
		this.classRenamer = renamer;
	}

	
	public static void main(String...args) throws NumberFormatException, IOException{
//		File inFile = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-NDVI-with-plots-interpolated-test-10.csv");
//		File outFile = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-NDVI-interpolated-test-10-s0.1.csv");
		
		File inFile = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-NDVI-with-plots-interpolated-train-90.csv");
		File outFile = new File("/home/petitjean/Dropbox/Data/SITS/Sudouest/SITS-2006-NDVI-interpolated-train-90-s0.01.csv");
		
		double sample = 0.01;
		
		
		HashSet<String> classesToSkip = new HashSet<>();
		classesToSkip.add("bati diffus");
		classesToSkip.add("bati dense");
		classesToSkip.add("bati indu");
		classesToSkip.add("surface minerale");
		classesToSkip.add("unclassified");
		classesToSkip.add("eau");
		classesToSkip.add("lac");
		classesToSkip.add("jachere");
		classesToSkip.add("friche");
		classesToSkip.add("prairie temporaire");
		classesToSkip.add("graviere");
		classesToSkip.add("pre");
		classesToSkip.add("pre");
		classesToSkip.add("pre");
		classesToSkip.add("pre");
		
		HashMap<String, String> renamer = new HashMap<>();
		renamer.put("feuillus", "forest");
		renamer.put("resineux", "forest");
		renamer.put("eucalyptus", "forest");
		renamer.put("peupliers", "forest");
		renamer.put("ble", "crop");
		renamer.put("colza", "crop");
		renamer.put("orge", "crop");
		renamer.put("mais", "crop");
		renamer.put("tournesol", "crop");
		renamer.put("sorgho", "crop");
		renamer.put("soja", "crop");
		renamer.put("pois", "crop");
		renamer.put("mais ensillage", "crop");
		renamer.put("sorgho II", "crop");
		renamer.put("mais non irrigue", "crop");
		
		SampleProjectMergeClasses wrangler = new SampleProjectMergeClasses(inFile,sample);
		wrangler.setClassesToSkip(classesToSkip);
		wrangler.setClassRenamer(renamer);
		
		
		wrangler.createSample(outFile);
	}
	
}

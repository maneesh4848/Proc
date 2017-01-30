package project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//import java.util.Map;
//import java.util.zip.GZIPInputStream;

import org.apache.commons.math3.stat.inference.TTest;

//import edu.umass.cs.cs646.utils.SearchResult;

public class SignificanceTest {
	public static void main(String[] args){
		BufferedReader reader = null;

		try {
			HashMap<String, List<Double>> ap = new HashMap<String, List<Double>>();		    
	    HashMap<String, List<Double>> ndcg = new HashMap<String, List<Double>>();
	    HashMap<String, List<Double>> err = new HashMap<String, List<Double>>();
	    
			for(int p=1;p<=9;p++){
				List<Double> apvalues=new ArrayList<Double>();
				List<Double> ndcgvalues=new ArrayList<Double>();
				List<Double> errvalues=new ArrayList<Double>();
				String filename="";
				
				switch(p){
				case 1: 
					filename="/Users/bhuvana/Downloads/result_files/proc1_results_trec123_10terms.txt";
					break;
				case 2:
					filename="/Users/bhuvana/Downloads/result_files/proc1_results_trec123_20terms.txt";
					break;
				case 3:
					filename="/Users/bhuvana/Downloads/result_files/proc1_results_trec123_30terms.txt";
					break;
				case 4:
					filename="/Users/bhuvana/Downloads/result_files/proc2_results_trec123_10terms.txt";
					break;
				case 5:
					filename="/Users/bhuvana/Downloads/result_files/proc2_results_trec123_20terms.txt";
					break;
				case 6:
					filename="/Users/bhuvana/Downloads/result_files/proc2_results_trec123_30terms.txt";
					break;
				case 7: 
					filename="/Users/bhuvana/Downloads/result_files/proc3_results_trec123_10terms.txt";
					break;
				case 8: 
					filename="/Users/bhuvana/Downloads/result_files/proc3_results_trec123_20terms.txt";
					break;
				case 9: 
					filename="/Users/bhuvana/Downloads/result_files/proc3_results_trec123_30terms.txt";
					break;
					}
				FileInputStream fstream = new FileInputStream(filename);
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
				String line;
				
				while ((line = br.readLine()) != null) {
					
					String[] splits = line.split("\\s+");
				apvalues.add(Double.parseDouble(splits[1]));
				ndcgvalues.add(Double.parseDouble(splits[2]));
				errvalues.add(Double.parseDouble(splits[3]));
				
	    }
				
	    ap.put(String.valueOf(p), apvalues);
	    ndcg.put(String.valueOf(p), ndcgvalues);
	    err.put(String.valueOf(p), errvalues);
	    
			}
			List<String> system=new ArrayList<String>();
				int temp=10;
			for(int q=1;q<=3;q++){
				String pathResults="/Users/bhuvana/Desktop";
				switch(q){
				case 1:
					pathResults="/Users/bhuvana/Desktop/Trec123_10feedback_terms";
					break;
				case 2:
					pathResults="/Users/bhuvana/Desktop/Trec123_20feedback_terms";
					break;
				case 3:
					pathResults="/Users/bhuvana/Desktop/Trec123_30feedback_terms";
					break;
				}
	    
	    File folder = new File(pathResults);
			File[] listOfFiles = folder.listFiles(); 
			
			
			
			for (File file: listOfFiles) {
				if (file.isFile() && (!file.getName().equals(".DS_Store"))) {
					String filepath = pathResults + "/" + file.getName();
					system.add(file.getName());
					List<Double> apvalues=new ArrayList<Double>();
					List<Double> ndcgvalues=new ArrayList<Double>();
					List<Double> errvalues=new ArrayList<Double>();
					FileInputStream fstream = new FileInputStream(filepath);
					BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

					String line;
					
					while ((line = br.readLine()) != null) {
						
						String[] splits = line.split("\\s+");
					apvalues.add(Double.parseDouble(splits[1]));
					ndcgvalues.add(Double.parseDouble(splits[2]));
					errvalues.add(Double.parseDouble(splits[3]));
					
		    }
					
		    ap.put(String.valueOf(temp), apvalues);
		    ndcg.put(String.valueOf(temp), ndcgvalues);
		    err.put(String.valueOf(temp), errvalues);
		    temp=temp+1;
		    
			}
			}
			//System.out.println("temp"+temp);
			}
			//System.out.println(ap.size());
			
		    double[] p = { 0.05, 0.01, 0.001 }; //list of p values
				TTest ttest = new TTest();
				

				for (int k = 0; k < 3; k++) {
					
					int apless=0;
					int ndcgless=0;
					int errless=0;
					int temp2=10;
					for(int q=1;q<=9;q++){
						if((q-1)%3==0)
							temp2=10;
						double[] sampleap1 = ap.get(String.valueOf(q)).stream().mapToDouble(r -> r).toArray();
						double[] samplendcg1 = ndcg.get(String.valueOf(q)).stream().mapToDouble(r -> r).toArray();
						double[] sampleerr1 = err.get(String.valueOf(q)).stream().mapToDouble(r -> r).toArray();

						for (int j = 1; j <= 4; j++) {
							//System.out.println(temp2);
							double[] sampleap2 = ap.get(String.valueOf(temp2)).stream().mapToDouble(r -> r).toArray();
							double[] samplendcg2 = ndcg.get(String.valueOf(temp2)).stream().mapToDouble(r -> r).toArray();
							double[] sampleerr2 = err.get(String.valueOf(temp2)).stream().mapToDouble(r -> r).toArray();
							temp2++;
							
							if (ttest.pairedTTest(sampleap2, sampleap1, p[k])) {
								apless++;
							}
							if (ttest.pairedTTest(samplendcg2, samplendcg1, p[k])) {
								ndcgless++;
							}
							if (ttest.pairedTTest(sampleerr2, sampleerr1, p[k])) {
								errless++;
							}
						}
					}
					
					
					System.out.println("#pairs p<" + p[k] + " for AP: " + apless);
					System.out.println("#pairs p<" + p[k] + " for nDCG: " + ndcgless);
					System.out.println("#pairs p<" + p[k] + " for ERR: " + errless);

				}
				

		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

}

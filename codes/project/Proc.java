package project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import utils.DocLengthReader;
import utils.EvalUtils;
import utils.LuceneQLSearcher;
import utils.LuceneUtils;
import utils.SearchResult;
import utils.Utils;

public class Proc {
	public static void main(String[] args)
	{
		try {
			
			String pathIndex = "C:\\Users\\teja\\Documents\\workspace\\IR_Project\\index_robust04"; // change it to your own index path
			Analyzer analyzer = LuceneUtils.getAnalyzer( LuceneUtils.Stemming.Krovetz ); // change the stemming setting accordingly
			
			String pathQueries = "C:\\Users\\teja\\Documents\\workspace\\IR_Project\\queries_robust04"; // change it to your query file path
			String pathQrels = "C:\\Users\\teja\\Documents\\workspace\\IR_Project\\qrels_robust04"; // change it to your qrels file path
			String pathStopwords = "C:\\Users\\teja\\Documents\\workspace\\IR_Project\\stopwords_inquery"; // change to your stop words path
			
			String field_docno = "docno";
			String field_search = "content";
			
			LuceneQLSearcher searcher = new LuceneQLSearcher(pathIndex);
			searcher.setStopwords( pathStopwords );
			
			Map<String, String> queries = EvalUtils.loadQueries( pathQueries );
			Map<String, Map<String,Integer>> qrels = EvalUtils.loadQrels( pathQrels );
			System.out.println("Data Loaded");
			
			/*for(int wsize = 10; wsize <= 100; wsize += 20)
			{
				for(int beta = 1; beta <= 10; beta += 2)
				{
					System.out.println(Double.toString((double)beta/10) + " " + Integer.toString(wsize));
					get_proc1_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)beta/10, wsize, 10);
					get_proc2_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)beta/10, wsize, 10);
					get_proc3_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)beta/10, wsize, 10);
				}
			}*/
			/*double beta = 0.9;
			int wsize = 120;
			System.out.println(Double.toString(beta) + " " + Integer.toString(wsize));
			get_proc1_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)beta, wsize, 10);
			get_proc2_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)beta, wsize, 10);
			get_proc3_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)beta, wsize, 10);
			System.out.println(Double.toString(beta) + " " + Integer.toString(wsize));
			get_proc1_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)beta, wsize, 20);
			get_proc2_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)beta, wsize, 20);
			get_proc3_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)beta, wsize, 20);
			System.out.println(Double.toString(beta) + " " + Integer.toString(wsize));
			get_proc1_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)beta, wsize, 30);
			get_proc2_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)beta, wsize, 30);
			get_proc3_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)beta, wsize, 30);*/
			
			double beta = 0.9;
			int wsize = 80;
			get_proc1_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)beta, wsize, 10);
			get_proc2_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)beta, wsize, 10);
			get_proc3_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)beta, wsize, 10);
			
			//cross_validate_proc1(searcher, analyzer, field_docno, field_search, queries, qrels, 10);
			//cross_validate_proc2(searcher, analyzer, field_docno, field_search, queries, qrels, 10);
			//cross_validate_proc3(searcher, analyzer, field_docno, field_search, queries, qrels, 10);

			System.out.println("COPY THE DAMN AVG STATS INTO AN EXCEL FILE NOW!!!!");
		
		} 
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public static void cross_validate_proc1(LuceneQLSearcher searcher, Analyzer analyzer, String field_docno, String field_search, Map<String, String> queries, Map<String, Map<String,Integer>> qrels, int numfbterms) throws IOException
	{
		double max_ap = Integer.MIN_VALUE;
		double best_beta = 0;
		int best_wsize = 0;
		
		//finding best values
	    for(int beta = 1; beta <= 10; beta += 1)
	    {
	    	for(int wsize = 10; wsize <= 100; wsize += 10)
	    	{
	    		double this_ap = get_proc1_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)beta/10, wsize, 10, 1);
	    		if(this_ap > max_ap)
	    		{
	    			max_ap = this_ap;
	    			best_beta = beta;
	    			best_wsize = wsize;
	    		}
	    	}
	    }
	    get_proc1_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)best_beta/10, best_wsize, 10, 0);
	}
	public static void cross_validate_proc2(LuceneQLSearcher searcher, Analyzer analyzer, String field_docno, String field_search, Map<String, String> queries, Map<String, Map<String,Integer>> qrels, int numfbterms) throws IOException
	{
		double max_ap = Integer.MIN_VALUE;
		double best_beta = 0;
		int best_wsize = 0;
		
		//finding best values
	    for(int beta = 1; beta <= 10; beta += 1)
	    {
	    	for(int wsize = 10; wsize <= 100; wsize += 10)
	    	{
	    		double this_ap = get_proc2_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)beta/10, wsize, 10, 1);
	    		if(this_ap > max_ap)
	    		{
	    			max_ap = this_ap;
	    			best_beta = beta;
	    			best_wsize = wsize;
	    		}
	    	}
	    }
	    get_proc2_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)best_beta/10, best_wsize, 10, 0);
	}
	public static void cross_validate_proc3(LuceneQLSearcher searcher, Analyzer analyzer, String field_docno, String field_search, Map<String, String> queries, Map<String, Map<String,Integer>> qrels, int numfbterms) throws IOException
	{
		double max_ap = Integer.MIN_VALUE;
		double best_beta = 0;
		int best_wsize = 0;
		
		//finding best values
	    for(int beta = 1; beta <= 10; beta += 1)
	    {
	    	for(int wsize = 10; wsize <= 100; wsize += 10)
	    	{
	    		double this_ap = get_proc3_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)beta/10, wsize, 10, 1);
	    		if(this_ap > max_ap)
	    		{
	    			max_ap = this_ap;
	    			best_beta = beta;
	    			best_wsize = wsize;
	    		}
	    	}
	    }
	    get_proc3_results(searcher, analyzer, field_docno, field_search, queries, qrels, (double)best_beta/10, best_wsize, 10, 0);
	}
	//used for cross validation
	public static double get_proc1_results(LuceneQLSearcher searcher, Analyzer analyzer, String field_docno, String field_search, Map<String, String> queries, Map<String, Map<String,Integer>> qrels, double beta, int wsize, int numfbterms, int train_flag) throws IOException
	{
		int numfbdocs = 20;
		int top = 1000;
		int mu = 1000;
		double alpha = 1;
		double[] ap = new double[queries.size()];
		double[] ndcg = new double[queries.size()];
		double[] err = new double[queries.size()];
		
		String proc1_respath = ".\\proc1_results_wt10g_" + Integer.toString(numfbterms) + "terms.txt";
	    PrintWriter writer = new PrintWriter(proc1_respath, "UTF-8");
	    String proc1_avgpath = ".\\proc1_avg_wt10g.txt";
	    PrintWriter writer2 = new PrintWriter(new BufferedWriter(new FileWriter(proc1_avgpath, true)));
	    writer2.append('\n');
		
		int ix = 0;
		int qcount = 0;
		for ( String qid : queries.keySet() ) {
			if(qcount % 2 == train_flag)
			{
				String query = queries.get( qid );
				List<String> terms = LuceneUtils.tokenize( query, analyzer );
				
				//System.out.println("Estimating Proc1");
				Map<String,Double> proc1_vals = Proc.estimateProc1(searcher, analyzer, field_search, terms, mu, numfbdocs, numfbterms, alpha, beta, wsize);
				List<SearchResult> results = searcher.search(field_search, proc1_vals, mu, top);
				SearchResult.dumpDocno( searcher.index, field_docno, results );
				
				ap[ix] = EvalUtils.avgPrec( results, qrels.get( qid ), top );
				ndcg[ix] = EvalUtils.nDCG( results, qrels.get( qid ), 10 );
				err[ix] = EvalUtils.ERR( results, qrels.get( qid ), 10 );
				
				//if(ix % 50 == 0)
				//{
					System.out.printf(
							"%-10s%8.3f%8.3f%8.3f\n",
							qid,
							ap[ix],
							ndcg[ix],
							err[ix]
					);
				//}
				writer.printf("%-10s%8.3f%8.3f%8.3f\n",
						qid,
						ap[ix],
						ndcg[ix],
						err[ix]
				);
				ix++;
				//break;
			}
			qcount++;
		}
		
		System.out.printf(
				"%-10s%10.3f%10.3f%10.3f\n",
				"Proc1",
				StatUtils.mean( ap )*queries.size()/ix,
				StatUtils.mean( ndcg )*queries.size()/ix,
				StatUtils.mean( err )*queries.size()/ix
		);
		writer2.printf("%-10s%-10s%8.3f%8.3f%8.3f\n",
				beta,
				wsize,
				StatUtils.mean( ap )*queries.size()/ix,
				StatUtils.mean( ndcg )*queries.size()/ix,
				StatUtils.mean( err )*queries.size()/ix
		);
		writer.close();
		writer2.close();
		return StatUtils.mean(ap)*queries.size()/ix;
	}
	public static double get_proc2_results(LuceneQLSearcher searcher, Analyzer analyzer, String field_docno, String field_search, Map<String, String> queries, Map<String, Map<String,Integer>> qrels, double beta, int wsize, int numfbterms, int train_flag) throws IOException
	{
		int numfbdocs = 20;
		int top = 1000;
		int mu = 1000;
		double alpha = 1;
		double[] ap = new double[queries.size()];
		double[] ndcg = new double[queries.size()];
		double[] err = new double[queries.size()];
		
		String proc2_respath = ".\\proc2_results_wt10g_" + Integer.toString(numfbterms) + "terms.txt";
	    PrintWriter writer = new PrintWriter(proc2_respath, "UTF-8");
	    String proc2_avgpath = ".\\proc2_avg_wt10g.txt";
	    PrintWriter writer2 = new PrintWriter(new BufferedWriter(new FileWriter(proc2_avgpath, true)));
	    writer2.append('\n');
	    
	    int qcount = 0;
		int ix = 0;
		for ( String qid : queries.keySet() ) {
			if(qcount % 2 == train_flag)
			{
				String query = queries.get( qid );
				List<String> terms = LuceneUtils.tokenize( query, analyzer );
				
				//System.out.println("Estimating Proc2");
				Map<String,Double> proc1_vals = Proc.estimateProc2(searcher, analyzer, field_search, terms, mu, numfbdocs, numfbterms, alpha, beta, wsize);
				List<SearchResult> results = searcher.search(field_search, proc1_vals, mu, top);
				SearchResult.dumpDocno( searcher.index, field_docno, results );
				
				ap[ix] = EvalUtils.avgPrec( results, qrels.get( qid ), top );
				ndcg[ix] = EvalUtils.nDCG( results, qrels.get( qid ), 10 );
				err[ix] = EvalUtils.ERR( results, qrels.get( qid ), 10 );
				
				//if(ix % 50 == 0)
				//{
					System.out.printf(
							"%-10s%8.3f%8.3f%8.3f\n",
							qid,
							ap[ix],
							ndcg[ix],
							err[ix]
					);
				//}
				writer.printf("%-10s%8.3f%8.3f%8.3f\n",
						qid,
						ap[ix],
						ndcg[ix],
						err[ix]
				);
				ix++;
				//break;
			}
			qcount++;
		}
		
		System.out.printf(
				"%-10s%10.3f%10.3f%10.3f\n",
				"Proc2",
				StatUtils.mean( ap )*queries.size()/ix,
				StatUtils.mean( ndcg )*queries.size()/ix,
				StatUtils.mean( err )*queries.size()/ix
		);
		writer2.printf("%-10s%-10s%8.3f%8.3f%8.3f\n",
				beta,
				wsize,
				StatUtils.mean( ap )*queries.size()/ix,
				StatUtils.mean( ndcg )*queries.size()/ix,
				StatUtils.mean( err )*queries.size()/ix
		);
		writer.close();
		writer2.close();
		return StatUtils.mean(ap)*queries.size()/ix;
	}
	public static double get_proc3_results(LuceneQLSearcher searcher, Analyzer analyzer, String field_docno, String field_search, Map<String, String> queries, Map<String, Map<String,Integer>> qrels, double beta, int wsize, int numfbterms, int train_flag) throws IOException
	{
		int numfbdocs = 20;
		int top = 1000;
		int mu = 1000;
		double alpha = 1;
		double[] ap = new double[queries.size()];
		double[] ndcg = new double[queries.size()];
		double[] err = new double[queries.size()];
		
		String proc3_respath = ".\\proc3_results_wt10g_" + Integer.toString(numfbterms) + "terms.txt";
	    PrintWriter writer = new PrintWriter(proc3_respath, "UTF-8");
	    String proc3_avgpath = ".\\proc3_avg_wt10g.txt";
	    PrintWriter writer2 = new PrintWriter(new BufferedWriter(new FileWriter(proc3_avgpath, true)));
	    writer2.append('\n');
	    
	    int qcount = 0;
		int ix = 0;
		for ( String qid : queries.keySet() ) {
			if(qcount % 2 == train_flag)
			{
				String query = queries.get( qid );
				List<String> terms = LuceneUtils.tokenize( query, analyzer );
				
				//System.out.println("Estimating Proc3");
				Map<String,Double> proc1_vals = Proc.estimateProc3(searcher, analyzer, field_search, terms, mu, numfbdocs, numfbterms, alpha, beta, wsize);
				List<SearchResult> results = searcher.search(field_search, proc1_vals, mu, top);
				SearchResult.dumpDocno( searcher.index, field_docno, results );
				
				ap[ix] = EvalUtils.avgPrec( results, qrels.get( qid ), top );
				ndcg[ix] = EvalUtils.nDCG( results, qrels.get( qid ), 10 );
				err[ix] = EvalUtils.ERR( results, qrels.get( qid ), 10 );
				
				//if(ix % 50 == 0)
				//{
					System.out.printf(
							"%-10s%8.3f%8.3f%8.3f\n",
							qid,
							ap[ix],
							ndcg[ix],
							err[ix]
					);
				//}
				writer.printf("%-10s%8.3f%8.3f%8.3f\n",
						qid,
						ap[ix],
						ndcg[ix],
						err[ix]
				);
				ix++;
				//break;
			}
			qcount++;
		}
		
		System.out.printf(
				"%-10s%10.3f%10.3f%10.3f\n",
				"Proc3",
				StatUtils.mean( ap )*queries.size()/ix,
				StatUtils.mean( ndcg )*queries.size()/ix,
				StatUtils.mean( err )*queries.size()/ix
		);
		writer2.printf("%-10s%-10s%8.3f%8.3f%8.3f\n",
				beta,
				wsize,
				StatUtils.mean( ap )*queries.size()/ix,
				StatUtils.mean( ndcg )*queries.size()/ix,
				StatUtils.mean( err )*queries.size()/ix
		);
		writer.close();
		writer2.close();
		return StatUtils.mean(ap)*queries.size()/ix;
	}
	
	//used for running on all queries
	public static double get_proc1_results(LuceneQLSearcher searcher, Analyzer analyzer, String field_docno, String field_search, Map<String, String> queries, Map<String, Map<String,Integer>> qrels, double beta, int wsize, int numfbterms) throws IOException
	{
		int numfbdocs = 20;
		int top = 1000;
		int mu = 1000;
		double alpha = 1;
		double[] ap = new double[queries.size()];
		double[] ndcg = new double[queries.size()];
		double[] err = new double[queries.size()];
		
		String proc1_respath = ".\\proc1_results_robust_" + Integer.toString(numfbterms) + "terms.txt";
	    PrintWriter writer = new PrintWriter(proc1_respath, "UTF-8");
	    String proc1_avgpath = ".\\proc1_avg_robust.txt";
	    PrintWriter writer2 = new PrintWriter(new BufferedWriter(new FileWriter(proc1_avgpath, true)));
	    writer2.append('\n');
		
		int ix = 0;
		for ( String qid : queries.keySet() ) {
			String query = queries.get( qid );
			List<String> terms = LuceneUtils.tokenize( query, analyzer );
			
			//System.out.println("Estimating Proc1");
			Map<String,Double> proc1_vals = Proc.estimateProc1(searcher, analyzer, field_search, terms, mu, numfbdocs, numfbterms, alpha, beta, wsize);
			List<SearchResult> results = searcher.search(field_search, proc1_vals, mu, top);
			SearchResult.dumpDocno( searcher.index, field_docno, results );
			
			ap[ix] = EvalUtils.avgPrec( results, qrels.get( qid ), top );
			ndcg[ix] = EvalUtils.nDCG( results, qrels.get( qid ), 10 );
			err[ix] = EvalUtils.ERR( results, qrels.get( qid ), 10 );
			
			if(ix % 50 == 0)
			{
				System.out.printf(
						"%-10s%8.3f%8.3f%8.3f\n",
						qid,
						ap[ix],
						ndcg[ix],
						err[ix]
				);
			}
			writer.printf("%-10s%8.3f%8.3f%8.3f\n",
					qid,
					ap[ix],
					ndcg[ix],
					err[ix]
			);
			ix++;
			//break;
		}
		
		System.out.printf(
				"%-10s%10.3f%10.3f%10.3f\n",
				"Proc1",
				StatUtils.mean( ap ),
				StatUtils.mean( ndcg ),
				StatUtils.mean( err )
		);
		writer2.printf("%-10s%-10s%8.3f%8.3f%8.3f\n",
				beta,
				wsize,
				StatUtils.mean( ap ),
				StatUtils.mean( ndcg ),
				StatUtils.mean( err )
		);
		writer.close();
		writer2.close();
		return StatUtils.mean(ap);
	}
	public static double get_proc2_results(LuceneQLSearcher searcher, Analyzer analyzer, String field_docno, String field_search, Map<String, String> queries, Map<String, Map<String,Integer>> qrels, double beta, int wsize, int numfbterms) throws IOException
	{
		int numfbdocs = 20;
		int top = 1000;
		int mu = 1000;
		double alpha = 1;
		double[] ap = new double[queries.size()];
		double[] ndcg = new double[queries.size()];
		double[] err = new double[queries.size()];
		
		String proc2_respath = ".\\proc2_results_robust_" + Integer.toString(numfbterms) + "terms.txt";
	    PrintWriter writer = new PrintWriter(proc2_respath, "UTF-8");
	    String proc2_avgpath = ".\\proc2_avg_robust.txt";
	    PrintWriter writer2 = new PrintWriter(new BufferedWriter(new FileWriter(proc2_avgpath, true)));
	    writer2.append('\n');
	    
		int ix = 0;
		for ( String qid : queries.keySet() ) {
			String query = queries.get( qid );
			List<String> terms = LuceneUtils.tokenize( query, analyzer );
			
			//System.out.println("Estimating Proc2");
			Map<String,Double> proc1_vals = Proc.estimateProc2(searcher, analyzer, field_search, terms, mu, numfbdocs, numfbterms, alpha, beta, wsize);
			List<SearchResult> results = searcher.search(field_search, proc1_vals, mu, top);
			SearchResult.dumpDocno( searcher.index, field_docno, results );
			
			ap[ix] = EvalUtils.avgPrec( results, qrels.get( qid ), top );
			ndcg[ix] = EvalUtils.nDCG( results, qrels.get( qid ), 10 );
			err[ix] = EvalUtils.ERR( results, qrels.get( qid ), 10 );
			
			if(ix % 50 == 0)
			{
				System.out.printf(
						"%-10s%8.3f%8.3f%8.3f\n",
						qid,
						ap[ix],
						ndcg[ix],
						err[ix]
				);
			}
			writer.printf("%-10s%8.3f%8.3f%8.3f\n",
					qid,
					ap[ix],
					ndcg[ix],
					err[ix]
			);
			ix++;
			//break;
		}
		
		System.out.printf(
				"%-10s%10.3f%10.3f%10.3f\n",
				"Proc2",
				StatUtils.mean( ap ),
				StatUtils.mean( ndcg ),
				StatUtils.mean( err )
		);
		writer2.printf("%-10s%-10s%8.3f%8.3f%8.3f\n",
				beta,
				wsize,
				StatUtils.mean( ap ),
				StatUtils.mean( ndcg ),
				StatUtils.mean( err )
		);
		writer.close();
		writer2.close();
		return StatUtils.mean(ap);
	}
	public static double get_proc3_results(LuceneQLSearcher searcher, Analyzer analyzer, String field_docno, String field_search, Map<String, String> queries, Map<String, Map<String,Integer>> qrels, double beta, int wsize, int numfbterms) throws IOException
	{
		int numfbdocs = 20;
		int top = 1000;
		int mu = 1000;
		double alpha = 1;
		double[] ap = new double[queries.size()];
		double[] ndcg = new double[queries.size()];
		double[] err = new double[queries.size()];
		
		String proc3_respath = ".\\proc3_results_robust_" + Integer.toString(numfbterms) + "terms.txt";
	    PrintWriter writer = new PrintWriter(proc3_respath, "UTF-8");
	    String proc3_avgpath = ".\\proc3_avg_robust.txt";
	    PrintWriter writer2 = new PrintWriter(new BufferedWriter(new FileWriter(proc3_avgpath, true)));
	    writer2.append('\n');
	    
		int ix = 0;
		for ( String qid : queries.keySet() ) {
			String query = queries.get( qid );
			List<String> terms = LuceneUtils.tokenize( query, analyzer );
			
			//System.out.println("Estimating Proc3");
			Map<String,Double> proc1_vals = Proc.estimateProc3(searcher, analyzer, field_search, terms, mu, numfbdocs, numfbterms, alpha, beta, wsize);
			List<SearchResult> results = searcher.search(field_search, proc1_vals, mu, top);
			SearchResult.dumpDocno( searcher.index, field_docno, results );
			
			ap[ix] = EvalUtils.avgPrec( results, qrels.get( qid ), top );
			ndcg[ix] = EvalUtils.nDCG( results, qrels.get( qid ), 10 );
			err[ix] = EvalUtils.ERR( results, qrels.get( qid ), 10 );
			
			if(ix % 50 == 0)
			{
				System.out.printf(
						"%-10s%8.3f%8.3f%8.3f\n",
						qid,
						ap[ix],
						ndcg[ix],
						err[ix]
				);
			}
			writer.printf("%-10s%8.3f%8.3f%8.3f\n",
					qid,
					ap[ix],
					ndcg[ix],
					err[ix]
			);
			ix++;
			//break;
		}
		
		System.out.printf(
				"%-10s%10.3f%10.3f%10.3f\n",
				"Proc3",
				StatUtils.mean( ap ),
				StatUtils.mean( ndcg ),
				StatUtils.mean( err )
		);
		writer2.printf("%-10s%-10s%8.3f%8.3f%8.3f\n",
				beta,
				wsize,
				StatUtils.mean( ap ),
				StatUtils.mean( ndcg ),
				StatUtils.mean( err )
		);
		writer.close();
		writer2.close();
		return StatUtils.mean(ap);
	}

	protected File dirBase;
	protected Directory dirLucene;
	protected static IndexReader index;
	protected Map<String, DocLengthReader> doclens;
	
	protected HashSet<String> stopwords;
	
	public Proc( String dirPath ) throws IOException {
		this( new File( dirPath ) );
	}
	
	public Proc( File dirBase ) throws IOException {
		this.dirBase = dirBase;
		this.dirLucene = FSDirectory.open( this.dirBase.toPath() );
		//this.index = DirectoryReader.open( dirLucene );
		this.doclens = new HashMap<>();
		this.stopwords = new HashSet<>();
	}
	
	public void setStopwords( Collection<String> stopwords ) {
		this.stopwords.addAll( stopwords );
	}
	
	public void setStopwords( String stopwordsPath ) throws IOException {
		setStopwords( new File( stopwordsPath ) );
	}
	
	public void setStopwords( File stopwordsFile ) throws IOException {
		BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( stopwordsFile ), "UTF-8" ) );
		String line;
		while ( ( line = reader.readLine() ) != null ) {
			line = line.trim();
			if ( line.length() > 0 ) {
				this.stopwords.add( line );
			}
		}
		reader.close();
	}
	
	public static Map<String, Double> estimateProc1(LuceneQLSearcher searcher, Analyzer analyzer, String field, List<String> terms, double mu, int numfbdocs, int numfbterms, double alpha, double beta, int wsize) throws IOException 
	{
		// Getting results
		Map<String, Double> collector = new HashMap<>();
		List<SearchResult> results = searcher.search( field, terms, mu, numfbdocs );
		Map<String,Double> idf = new HashMap<>();
		long corpusLength = searcher.index.getSumTotalTermFreq( field );
		//System.out.println("Results Loaded");
		
		// Computing IDF(qi)
		for(String term: terms)
		{
			int N = searcher.index.numDocs();
			int n = searcher.index.docFreq( new Term( field, term ) );
			double idf_val = Math.log( ( N - n + 0.5 ) / ( n + 0.5 ) );
			if(idf_val < 0)
				idf_val = 0;
			idf.put(term, idf_val);
		}
		/*for(Map.Entry<String, Double> entry: idf.entrySet())
		{
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
		System.out.println();*/
		
		// Computing ptf1
		for(SearchResult result: results)
		{
			Map<String, Double> mini_collector = new TreeMap<>();
			Document doc = searcher.index.document(result.getDocid());
			String text = doc.get(field).toString();
			List<String> docterms = LuceneUtils.tokenize( text, analyzer );
			long doclen = docterms.size();
			for(int i = 0; i < doclen; i++)
			{
				if(terms.contains(docterms.get(i)))
				{
					double idf_val = idf.get(docterms.get(i));
					for(int j = 1; j < wsize; j++)
					{
						if(i + j < doclen && !searcher.isStopwords(docterms.get(i + j)))
							mini_collector.put(docterms.get(i + j), mini_collector.getOrDefault(docterms.get(i + j), 0.0) + idf_val);
						if(i - j >= 0 && !searcher.isStopwords(docterms.get(i - j)))
							mini_collector.put(docterms.get(i - j), mini_collector.getOrDefault(docterms.get(i - j), 0.0) + idf_val);
					}
				}
			}
			
			// updating score(w)
			for(Map.Entry<String, Double> entry: mini_collector.entrySet())
			{
				if(entry.getValue() > 0.0)
				{
					long corpusTF = searcher.index.totalTermFreq( new Term( field, entry.getKey() ) ); 
					double pwc = 1.0 * corpusTF / corpusLength;
					collector.put(entry.getKey(), collector.getOrDefault(entry.getKey(), 0.0) + entry.getValue()*Math.log(entry.getValue()/pwc));
				}
			}
		}
		collector = sortByValue(collector);
		//collector.remove("termvector");
		/*for(Map.Entry<String, Double> entry: collector.entrySet())
		{
			System.out.println(entry.getKey() + " " + entry.getValue());
		}*/
		
		// adding feedback to query	
		for(Map.Entry<String, Double> entry: collector.entrySet())
		{
			collector.put(entry.getKey(), (double)(beta*entry.getValue())/numfbdocs);
		}
		Map<String, Double> tfreqs = new HashMap<>();
		for ( String term : terms ) 
		{
			tfreqs.put( term, tfreqs.getOrDefault( term, 0.0 ) + 1.0 );
		}
		
		
		for(String term: terms)
		{
			collector.put(term, collector.getOrDefault(term, 0.0) + alpha*tfreqs.get(term));
		}
		collector = sortByValue(Utils.norm(collector));
		/*for(Map.Entry<String, Double> entry: collector.entrySet())
		{
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
		System.out.println();*/
		return Utils.getTop(collector, numfbterms);
	}
	
	public static Map<String, Double> estimateProc2(LuceneQLSearcher searcher, Analyzer analyzer, String field, List<String> terms, double mu, int numfbdocs, int numfbterms, double alpha, double beta, int wsize) throws IOException 
	{
		// Getting results
		Map<String, Double> collector = new HashMap<>();
		List<SearchResult> results = searcher.search( field, terms, mu, numfbdocs );
		Map<String,Double> idf = new HashMap<>();
		long corpusLength = searcher.index.getSumTotalTermFreq( field );
		//System.out.println("Results Loaded");
		
		// Computing IDF(qi)
		for(String term: terms)
		{
			int N = searcher.index.numDocs();
			int n = searcher.index.docFreq( new Term( field, term ) );
			double idf_val = Math.log( ( N - n + 0.5 ) / ( n + 0.5 ) );
			if(idf_val < 0)
				idf_val = 0;
			idf.put(term, idf_val);
		}
		/*for(Map.Entry<String, Double> entry: idf.entrySet())
		{
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
		System.out.println();*/
		
		// Computing ptf2
		for(SearchResult result: results)
		{
			Map<String, Double> mini_collector = new TreeMap<>();
			Document doc = searcher.index.document(result.getDocid());
			String text = doc.get(field).toString();
			List<String> docterms = LuceneUtils.tokenize( text, analyzer );
			long doclen = docterms.size();
			for(int i = 0; i < doclen; i++)
			{
				if(terms.contains(docterms.get(i)))
				{
					double idf_val = idf.get(docterms.get(i));
					for(int j = 1; j < wsize; j++)
					{
						double weight = Math.exp(-(i-j)*(i-j)/(2*wsize*wsize));
						if(i + j < doclen && !searcher.isStopwords(docterms.get(i + j)))
							mini_collector.put(docterms.get(i + j), mini_collector.getOrDefault(docterms.get(i + j), 0.0) + weight*idf_val);
						if(i - j >= 0 && !searcher.isStopwords(docterms.get(i - j)))
							mini_collector.put(docterms.get(i - j), mini_collector.getOrDefault(docterms.get(i - j), 0.0) + weight*idf_val);
					}
				}
			}
			
			// updating score(w)
			for(Map.Entry<String, Double> entry: mini_collector.entrySet())
			{
				if (entry.getKey() == "termvector")
				{
					System.out.println(entry.getValue());
				}
				if(entry.getValue() > 0.0)
				{
					long corpusTF = searcher.index.totalTermFreq( new Term( field, entry.getKey() ) ); 
					double pwc = 1.0 * corpusTF / corpusLength;
					collector.put(entry.getKey(), collector.getOrDefault(entry.getKey(), 0.0) + entry.getValue()*Math.log(entry.getValue()/pwc));
				}
			}
		}
		collector = sortByValue(collector);
		collector.remove("termvector");
		/*for(Map.Entry<String, Double> entry: collector.entrySet())
		{
			System.out.println(entry.getKey() + " " + entry.getValue());
		}*/
		
		// adding feedback to query	
		for(Map.Entry<String, Double> entry: collector.entrySet())
		{
			collector.put(entry.getKey(), (double)(beta*entry.getValue())/numfbdocs);
		}
		Map<String, Double> tfreqs = new HashMap<>();
		for ( String term : terms ) 
		{
			tfreqs.put( term, tfreqs.getOrDefault( term, 0.0 ) + 1.0 );
		}
		
		
		for(String term: terms)
		{
			collector.put(term, collector.getOrDefault(term, 0.0) + alpha*tfreqs.get(term));
		}
		collector = sortByValue(Utils.norm(collector));
		/*for(Map.Entry<String, Double> entry: collector.entrySet())
		{
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
		System.out.println();*/
		return Utils.getTop(collector, numfbterms);
	}
	
	public static Map<String, Double> estimateProc3(LuceneQLSearcher searcher, Analyzer analyzer, String field, List<String> terms, double mu, int numfbdocs, int numfbterms, double alpha, double beta, int wsize) throws IOException 
	{
		// Getting results
		Map<String, Double> collector = new HashMap<>();
		List<SearchResult> results = searcher.search( field, terms, mu, numfbdocs );
		Map<String,Double> idf = new HashMap<>();
		long corpusLength = searcher.index.getSumTotalTermFreq( field );
		//System.out.println("Results Loaded");
		
		// Computing IDF(qi)
		for(String term: terms)
		{
			int N = searcher.index.numDocs();
			int n = searcher.index.docFreq( new Term( field, term ) );
			double idf_val = Math.log( ( N - n + 0.5 ) / ( n + 0.5 ) );
			if(idf_val < 0)
				idf_val = 0;
			idf.put(term, idf_val);
		}
		/*for(Map.Entry<String, Double> entry: idf.entrySet())
		{
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
		System.out.println();*/
		
		// Computing ptf3
		for(SearchResult result: results)
		{
			Map<String, Double> mini_collector = new TreeMap<>();
			Document doc = searcher.index.document(result.getDocid());
			String text = doc.get(field).toString();
			List<String> docterms = LuceneUtils.tokenize( text, analyzer );
			long doclen = docterms.size();
			for(int i = 0; i < doclen; i++)
			{
				if(terms.contains(docterms.get(i)))
				{
					double idf_val = idf.get(docterms.get(i));
					for(int j = 1; j < wsize; j++)
					{
						double weight = wsize - j + 1;
						if(i + j < doclen && !searcher.isStopwords(docterms.get(i + j)))
							mini_collector.put(docterms.get(i + j), mini_collector.getOrDefault(docterms.get(i + j), 0.0) + weight*idf_val);
						if(i - j >= 0 && !searcher.isStopwords(docterms.get(i - j)))
							mini_collector.put(docterms.get(i - j), mini_collector.getOrDefault(docterms.get(i - j), 0.0) + weight*idf_val);
					}
				}
			}
			
			// updating score(w)
			for(Map.Entry<String, Double> entry: mini_collector.entrySet())
			{
				if (entry.getKey() == "termvector")
				{
					System.out.println(entry.getValue());
				}
				if(entry.getValue() > 0.0)
				{
					long corpusTF = searcher.index.totalTermFreq( new Term( field, entry.getKey() ) ); 
					double pwc = 1.0 * corpusTF / corpusLength;
					collector.put(entry.getKey(), collector.getOrDefault(entry.getKey(), 0.0) + entry.getValue()*Math.log(entry.getValue()/pwc));
				}
			}
		}
		collector = sortByValue(collector);
		collector.remove("termvector");
		/*for(Map.Entry<String, Double> entry: collector.entrySet())
		{
			System.out.println(entry.getKey() + " " + entry.getValue());
		}*/
		
		// adding feedback to query	
		for(Map.Entry<String, Double> entry: collector.entrySet())
		{
			collector.put(entry.getKey(), (double)(beta*entry.getValue())/numfbdocs);
		}
		Map<String, Double> tfreqs = new HashMap<>();
		for ( String term : terms ) 
		{
			tfreqs.put( term, tfreqs.getOrDefault( term, 0.0 ) + 1.0 );
		}
		
		
		for(String term: terms)
		{
			collector.put(term, collector.getOrDefault(term, 0.0) + alpha*tfreqs.get(term));
		}
		collector = sortByValue(Utils.norm(collector));
		/*for(Map.Entry<String, Double> entry: collector.entrySet())
		{
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
		System.out.println();*/
		return Utils.getTop(collector, numfbterms);
	}
	
	/*public static Map<String, Double> estimateProc2(LuceneQLSearcher searcher, Analyzer analyzer, String field, List<String> terms, double mu, int numfbdocs, int numfbterms, double alpha, double beta, int wsize, double sigma) throws IOException 
	{
		// Getting results
		Map<String, Double> collector = new HashMap<>();
		List<SearchResult> results = searcher.search( field, terms, mu, numfbdocs );
		System.out.println("Results Loaded");
		
		for(SearchResult result: results)
		{
			Document doc = searcher.index.document(result.getDocid());
			String text = doc.getField(field).toString();
			List<String> docterms = LuceneUtils.tokenize( text, analyzer );
			long doclen = docterms.size();
			for(int i = 0; i < doclen; i++)
			{
				if(terms.contains(docterms.get(i)))
				{
					int N = searcher.index.numDocs();
					int n = searcher.index.docFreq( new Term( field, docterms.get(i) ) );
					double idf = Math.log( ( N + 1 ) / ( n + 1 ) );
					for(int j = 0; j < wsize; j++)
					{
						double weight = Math.exp(-(i-j)*(i-j)/2*sigma*sigma);
						try
						{
							if(!searcher.isStopwords(docterms.get(i + j)))
								collector.put(docterms.get(i + j), collector.getOrDefault(docterms.get(i + j), 0.0) + weight*idf);
							if(!searcher.isStopwords(docterms.get(i - j)))
								collector.put(docterms.get(i - j), collector.getOrDefault(docterms.get(i - j), 0.0) + weight*idf);
						}
						catch(Exception IndexOutOfBoundsException)
						{
							//do nothing
						}
					}
				}
			}
		}
		for(Map.Entry<String, Double> entry: collector.entrySet())
		{
			collector.put(entry.getKey(), (double)(beta*entry.getValue())/numfbdocs);
		}
		Map<String, Double> tfreqs = new HashMap<>();
		for ( String term : terms ) 
		{
			tfreqs.put( term, tfreqs.getOrDefault( term, 0.0 ) + 1.0 );
		}
		collector = Utils.getTop( Utils.norm( collector ), numfbterms );
		for(String term: terms)
		{
			collector.put(term, alpha*tfreqs.get(term));
		}
		return collector;
	}

	public static Map<String, Double> estimateProc3(LuceneQLSearcher searcher, Analyzer analyzer, String field, List<String> terms, double mu, int numfbdocs, int numfbterms, double alpha, double beta, int wsize) throws IOException 
	{
		// Getting results and vocab
		Map<String, Double> collector = new HashMap<>();
		List<SearchResult> results = searcher.search( field, terms, mu, numfbdocs );
		Set<String> voc = new HashSet<>();
		for ( SearchResult result : results ) {
			TermsEnum iterator = searcher.index.getTermVector( result.getDocid(), field ).iterator();
			BytesRef br;
			while ( ( br = iterator.next() ) != null ) {
				if ( !searcher.isStopwords( br.utf8ToString() ) ) {
					voc.add( br.utf8ToString() );
				}
			}
		}
		System.out.println("Vocab Loaded");
		
		//BufferedWriter bw = new BufferedWriter(new FileWriter("./test_file.txt"));
		// Getting positions of query terms in documents
		Map<String,Map<Integer,List<Integer>>> termpos = new TreeMap<>();
		for( String term: terms)
		{
			System.out.print(term + " ");
			termpos.put(term, new TreeMap<>());
	        bw.write(term + " ");
			PostingsEnum posting = MultiFields.getTermDocsEnum( searcher.index, field, new BytesRef( term ), PostingsEnum.POSITIONS );
			if ( posting != null ) 
			{
			    int docid;
			    while ( ( docid = posting.nextDoc() ) != PostingsEnum.NO_MORE_DOCS ) 
			    {
			    	termpos.get(term).put(docid, new ArrayList<Integer>());
			        int freq = posting.freq();
			        List<Integer> ref = termpos.get(term).get(docid);
			        if(docid == 670673)
			        {
			        	bw.write(Integer.toString(docid) + " " + Integer.toString(freq) + " ");
			        	//System.out.printf( "%-10d%-10d", docid, freq );
			        }
			        for ( int i = 0; i < freq; i++ ) 
			        {
			            //System.out.print( ( i > 0 ? "," : "" ) + posting.nextPosition() );
			        	int pos = posting.nextPosition();
			        	ref.add(pos);
			        	if(docid == 670673)
			        		bw.write(Integer.toString(pos) + " ");
			        }
			        //System.out.println();
		        	if(docid == 670673)
		        		bw.newLine();
			    }
			}
			//bw.newLine();
		}
		System.out.println();
		System.out.println("Query Term Positions Loaded");
		
		// Computing proc3
		for ( SearchResult result : results ) 
		{
			Document doc = searcher.index.document(result.getDocid());
			doc.
			String text = doc.getField(field).toString();
			List<String> docterms = LuceneUtils.tokenize( text, analyzer );
			bw.write(Integer.toString(result.getDocid()));
			bw.newLine();
			bw.write(text);
			bw.newLine();
			//System.out.println(text);
			for(Map.Entry<String, Map<Integer,List<Integer>>> qterm: termpos.entrySet())
			{
				String queryterm = qterm.getKey();
				Map<Integer,List<Integer>> posmap = qterm.getValue();
				if(posmap.containsKey(result.getDocid()))
				{
					for(Integer position: posmap.get(result.getDocid()))
					{
						//System.out.println(queryterm + " " + docterms.get(position));
					}
				}
			}
		}
		bw.close();
		for(SearchResult result: results)
		{
			Document doc = searcher.index.document(result.getDocid());
			String text = doc.getField(field).toString();
			List<String> docterms = LuceneUtils.tokenize( text, analyzer );
			long doclen = docterms.size();
			for(int i = 0; i < doclen; i++)
			{
				if(terms.contains(docterms.get(i)))
				{
					int N = searcher.index.numDocs();
					int n = searcher.index.docFreq( new Term( field, docterms.get(i) ) );
					double idf = Math.log( ( N + 1 ) / ( n + 1 ) );
					for(int j = 0; j < wsize; j++)
					{
						double weight = wsize - j + 1;
						try
						{
							if(!searcher.isStopwords(docterms.get(i + j)))
								collector.put(docterms.get(i + j), collector.getOrDefault(docterms.get(i + j), 0.0) + weight*idf);
							if(!searcher.isStopwords(docterms.get(i - j)))
								collector.put(docterms.get(i - j), collector.getOrDefault(docterms.get(i - j), 0.0) + weight*idf);
						}
						catch(Exception IndexOutOfBoundsException)
						{
							//do nothing
						}
					}
				}
			}
		}
		for(Map.Entry<String, Double> entry: collector.entrySet())
		{
			collector.put(entry.getKey(), (double)(beta*entry.getValue())/numfbdocs);
			long corpusTF = searcher.index.totalTermFreq( new Term( field, entry.getKey() ) );
			long corpusLength = searcher.index.getSumTotalTermFreq( field );
			double pwc = 1.0 * corpusTF / corpusLength;
			if(entry.getValue() != 0)
				collector.put(entry.getKey(), (double)(entry.getValue()*Math.log(entry.getValue()/pwc)));
		}
		
		for(Map.Entry<String, Double> entry: collector.entrySet())
		{
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
		Map<String, Double> tfreqs = new HashMap<>();
		for ( String term : terms ) 
		{
			tfreqs.put( term, tfreqs.getOrDefault( term, 0.0 ) + 1.0 );
		}
		collector = Utils.getTop(collector, numfbterms );
		for(String term: terms)
		{
			collector.put(term, alpha*tfreqs.get(term));
		}
		return collector;
	}*/
	private static Map<String, Double> sortByValue(Map<String, Double> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<String, Double>> list =
                new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}


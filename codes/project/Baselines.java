package project;

import utils.*;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.*;
import java.util.*;

public class Baselines {

	public static void main( String[] args ) 
	{			
		try
		{
			String pathIndex = "C:\\Users\\teja\\Documents\\workspace\\IR_Project\\index_trec123"; // change it to your own index path
			Analyzer analyzer = LuceneUtils.getAnalyzer( LuceneUtils.Stemming.Krovetz ); // change the stemming setting accordingly
			
			String pathQueries = "C:\\Users\\teja\\Documents\\workspace\\IR_Project\\queries_trec1-3"; // change it to your query file path
			String pathQrels = "C:\\Users\\teja\\Documents\\workspace\\IR_Project\\qrels_trec1-3"; // change it to your qrels file path
			String pathStopwords = "C:\\Users\\teja\\Documents\\workspace\\IR_Project\\stopwords_inquery"; // change to your stop words path
			
			String field_docno = "docno";
			String field_search = "content";
			
			LuceneQLSearcher searcher = new LuceneQLSearcher(pathIndex);
			searcher.setStopwords( pathStopwords );
			
			Map<String, String> queries = EvalUtils.loadQueries( pathQueries );
			Map<String, Map<String,Integer>> qrels = EvalUtils.loadQrels( pathQrels );
			
			get_RM4_results(searcher, analyzer, field_docno, field_search, queries, qrels, 10);
			get_DMM_results(searcher, analyzer, field_docno, field_search, queries, qrels, 10);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void get_RM3_results(LuceneQLSearcher searcher, Analyzer analyzer, String field_docno, String field_search, Map<String, String> queries, Map<String, Map<String,Integer>> qrels, int numfbterms) throws IOException
	{
		int top = 1000;
		int numfbdocs = 20;
		int mu1 = 1000;
		int mu2 = 0;
		double weight_org = 0.2;
		double[] ap = new double[queries.size()];
		double[] ndcg = new double[queries.size()];
		double[] err = new double[queries.size()];
		
		String rm3_respath = ".\\rm3_results_trec123_" + Integer.toString(numfbterms) + "terms.txt";
	    PrintWriter writer = new PrintWriter(rm3_respath, "UTF-8");
		int ix = 0;
		for ( String qid : queries.keySet() ) {
			
			String query = queries.get( qid );
			List<String> terms = LuceneUtils.tokenize( query, analyzer );
			
			List<SearchResult> results = searcher.search(field_search, searcher.estimateQueryModelRM3(terms, searcher.estimateQueryModelRM1(field_search, terms, mu1, mu2, numfbdocs, numfbterms), weight_org), mu1, top);
			SearchResult.dumpDocno( searcher.index, field_docno, results );
			
			ap[ix] = EvalUtils.avgPrec( results, qrels.get( qid ), top );
			ndcg[ix] = EvalUtils.nDCG( results, qrels.get( qid ), 10 );
			err[ix] = EvalUtils.ERR( results, qrels.get( qid ), 10 );
			
			System.out.printf(
					"%-10s%8.3f%8.3f%8.3f\n",
					qid,
					ap[ix],
					ndcg[ix],
					err[ix]
			);
			ix++;
			writer.printf("%-10s%8.3f%8.3f%8.3f\n",
					qid,
					ap[ix],
					ndcg[ix],
					err[ix]
			);
			//break;
		}
		
		System.out.printf(
				"%-10s%10.3f%10.3f%10.3f\n",
				"Proc1",
				StatUtils.mean( ap ),
				StatUtils.mean( ndcg ),
				StatUtils.mean( err )
		);
		writer.close();
	}
	public static void get_RM4_results(LuceneQLSearcher searcher, Analyzer analyzer, String field_docno, String field_search, Map<String, String> queries, Map<String, Map<String,Integer>> qrels, int numfbterms) throws IOException
	{
		int top = 1000;
		int numfbdocs = 20;
		int mu1 = 1000;
		int mu2 = 0;
		double weight_org = 0.2;
		double[] ap = new double[queries.size()];
		double[] ndcg = new double[queries.size()];
		double[] err = new double[queries.size()];
		
		String rm4_respath = ".\\rm4_results_trec123.txt";
	    PrintWriter writer = new PrintWriter(rm4_respath, "UTF-8");
	    
		int ix = 0;
		for ( String qid : queries.keySet() ) {
			
			String query = queries.get( qid );
			List<String> terms = LuceneUtils.tokenize( query, analyzer );
			
			// Create method estimateQueryModelRM2 in LuceneQLSearcher
			List<SearchResult> results = searcher.search(field_search, searcher.estimateQueryModelRM3(terms, searcher.estimateQueryModelRM2(field_search, terms, mu1, mu2, numfbdocs, numfbterms), weight_org), mu1, top);
			SearchResult.dumpDocno( searcher.index, field_docno, results );
			
			ap[ix] = EvalUtils.avgPrec( results, qrels.get( qid ), top );
			ndcg[ix] = EvalUtils.nDCG( results, qrels.get( qid ), 10 );
			err[ix] = EvalUtils.ERR( results, qrels.get( qid ), 10 );
			
			System.out.printf(
					"%-10s%8.3f%8.3f%8.3f\n",
					qid,
					ap[ix],
					ndcg[ix],
					err[ix]
			);
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
				"RM4",
				StatUtils.mean( ap ),
				StatUtils.mean( ndcg ),
				StatUtils.mean( err )
		);
		writer.close();
	}
	public static void get_DMM_results(LuceneQLSearcher searcher, Analyzer analyzer, String field_docno, String field_search, Map<String, String> queries, Map<String, Map<String,Integer>> qrels, int numfbterms) throws IOException
	{
		int top = 1000;
		int numfbdocs = 20;
		int mu1 = 1000;
		int mu2 = 0;
		double lambda = 0.2;
		double weight_org = 0.2;
		double[] ap = new double[queries.size()];
		double[] ndcg = new double[queries.size()];
		double[] err = new double[queries.size()];
		
		String dmm_respath = ".\\dmm_results_trec123_" + Integer.toString(numfbterms) + "terms.txt";
	    PrintWriter writer = new PrintWriter(dmm_respath, "UTF-8");
	    
		int ix = 0;
		for ( String qid : queries.keySet() ) {
			
			String query = queries.get( qid );
			List<String> terms = LuceneUtils.tokenize( query, analyzer );
			
			List<SearchResult> results = searcher.search(field_search, searcher.estimateQueryModelDMM(field_search, terms, mu1, lambda, numfbdocs, numfbterms, weight_org), mu1, top);
			SearchResult.dumpDocno( searcher.index, field_docno, results );
			
			ap[ix] = EvalUtils.avgPrec( results, qrels.get( qid ), top );
			ndcg[ix] = EvalUtils.nDCG( results, qrels.get( qid ), 10 );
			err[ix] = EvalUtils.ERR( results, qrels.get( qid ), 10 );
			
			System.out.printf(
					"%-10s%8.3f%8.3f%8.3f\n",
					qid,
					ap[ix],
					ndcg[ix],
					err[ix]
			);
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
		writer.close();
	}
	public static void get_SMM_results(LuceneQLSearcher searcher, Analyzer analyzer, String field_docno, String field_search, Map<String, String> queries, Map<String, Map<String,Integer>> qrels, int numfbterms) throws IOException
	{
		int top = 1000;
		int numfbdocs = 20;
		int mu1 = 1000;
		int mu2 = 0;
		double weight_org = 0.2;
		double lambda = 0.2;
		double[] ap = new double[queries.size()];
		double[] ndcg = new double[queries.size()];
		double[] err = new double[queries.size()];
		
		String smm_respath = ".\\smm_results_trec123.txt";
	    PrintWriter writer = new PrintWriter(smm_respath, "UTF-8");
	    
		int ix = 0;
		for ( String qid : queries.keySet() ) {
			
			String query = queries.get( qid );
			List<String> terms = LuceneUtils.tokenize( query, analyzer );
			
			// Create method estimateQueryModelSMM in LuceneQLSearcher
			List<SearchResult> results = searcher.search(field_search, searcher.estimateQueryModelSMM(field_search, terms, mu1, lambda, numfbdocs, numfbterms, weight_org), mu1, top);
			SearchResult.dumpDocno( searcher.index, field_docno, results );
			
			ap[ix] = EvalUtils.avgPrec( results, qrels.get( qid ), top );
			ndcg[ix] = EvalUtils.nDCG( results, qrels.get( qid ), 10 );
			err[ix] = EvalUtils.ERR( results, qrels.get( qid ), 10 );
			
			System.out.printf(
					"%-10s%8.3f%8.3f%8.3f\n",
					qid,
					ap[ix],
					ndcg[ix],
					err[ix]
			);
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
				"SMM",
				StatUtils.mean( ap ),
				StatUtils.mean( ndcg ),
				StatUtils.mean( err )
		);
		writer.close();
	}
	
	protected File dirBase;
	protected Directory dirLucene;
	protected IndexReader index;
	protected Map<String, DocLengthReader> doclens;
	
	protected HashSet<String> stopwords;
	
	public Baselines( String dirPath ) throws IOException {
		this( new File( dirPath ) );
	}
	
	public Baselines( File dirBase ) throws IOException {
		this.dirBase = dirBase;
		this.dirLucene = FSDirectory.open( this.dirBase.toPath() );
		this.index = DirectoryReader.open( dirLucene );
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
	
	public DocLengthReader getDocLengthReader( String field ) throws IOException {
		DocLengthReader doclen = doclens.get( field );
		if ( doclen == null ) {
			doclen = new FileDocLengthReader( this.dirBase, field );
			doclens.put( field, doclen );
		}
		return doclen;
	}
	
	public void close() throws IOException {
		index.close();
		dirLucene.close();
		for ( DocLengthReader doclen : doclens.values() ) {
			doclen.close();
		}
	}
	
	public double logpdc(String field, String docno) throws IOException
	{
		String field_docno = "docno";
		
		int docid = LuceneUtils.findByDocno( index, field_docno, docno );
		Terms vector = index.getTermVector( docid, field );
		TermsEnum terms = vector.iterator();
		BytesRef term;
		double total = 0;
		while ( ( term = terms.next() ) != null ) {

		    long freq = terms.totalTermFreq(); 
		    long corpusTF = index.totalTermFreq( new Term( field, term ) ); 
			long corpusLength = index.getSumTotalTermFreq( field );
			double pwc = 1.0 * corpusTF / corpusLength;

		    total += freq*Math.log(pwc);
		}
		return total;
	}
	
	public interface ScoringFunction {
		
		/**
		 * @param weights Weight of the query terms, e.g., P(t|q) or c(t,q).
		 * @param tfs     The frequencies of the query terms in documents.
		 * @param tfcs    The frequencies of the query terms in the corpus.
		 * @param dl      The length of the document.
		 * @param cl      The length of the whole corpus.
		 * @return
		 */
		double score( List<Double> weights, List<Double> tfs, List<Double> tfcs, double dl, double cl );
	}
	
	public static class QLJMSmoothing implements ScoringFunction {
		
		protected double lambda;
		
		public QLJMSmoothing( double lambda ) {
			this.lambda = lambda;
		}
		
		public double score( List<Double> weights, List<Double> tfs, List<Double> tfcs, double dl, double cl ) {
			double total = 0;
			for(int i = 0; i < weights.size(); i++)
			{
				total += weights.get(i)*Math.log(((1-lambda)*(tfs.get(i)/dl) + lambda*(tfcs.get(i)/cl)));
			}
			return total;
		}
	}
	
	public static class QLDirichletSmoothing implements ScoringFunction {
		
		protected double mu;
		
		public QLDirichletSmoothing( double mu ) {
			this.mu = mu;
		}
		
		public double score( List<Double> weights, List<Double> tfs, List<Double> tfcs, double dl, double cl ) {
			double total = 0;
			for(int i = 0; i < weights.size(); i++)
			{
				total += weights.get(i)*Math.log((tfs.get(i) + (mu*tfcs.get(i)/cl))/(dl + mu));
			}
			return total;
		}
	}
	
}

package utils;

import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LuceneQLSearcher extends AbstractQLSearcher {
	
	protected File dirBase;
	protected Directory dirLucene;
	public IndexReader index;
	protected Map<String, DocLengthReader> doclens;
	
	public LuceneQLSearcher( String dirPath ) throws IOException {
		this( new File( dirPath ) );
	}
	
	public LuceneQLSearcher( File dirBase ) throws IOException {
		this.dirBase = dirBase;
		this.dirLucene = FSDirectory.open( this.dirBase.toPath() );
		this.index = DirectoryReader.open( dirLucene );
		this.doclens = new HashMap<>();
	}
	
	public IndexReader getIndex() {
		return this.index;
	}
	
	public PostingList getPosting( String field, String term ) throws IOException {
		return new LuceneTermPostingList( index, field, term );
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
	
	public Map<String, Double> estimateQueryModelRM1( String field, List<String> terms, double mu, double mufb, int numfbdocs, int numfbterms ) throws IOException {
		
		List<SearchResult> results = search( field, terms, mu, numfbdocs );
		Set<String> voc = new HashSet<>();
		for ( SearchResult result : results ) {
			TermsEnum iterator = index.getTermVector( result.getDocid(), field ).iterator();
			BytesRef br;
			while ( ( br = iterator.next() ) != null ) {
				if ( !isStopwords( br.utf8ToString() ) ) {
					voc.add( br.utf8ToString() );
				}
			}
		}
		
		Map<String, Double> collector = new HashMap<>();
		for ( SearchResult result : results ) {
			double ql = result.getScore();
			double dw = Math.exp( ql );
			TermsEnum iterator = index.getTermVector( result.getDocid(), field ).iterator();
			Map<String, Integer> tfs = new HashMap<>();
			int len = 0;
			BytesRef br;
			while ( ( br = iterator.next() ) != null ) {
				tfs.put( br.utf8ToString(), (int) iterator.totalTermFreq() );
				len += iterator.totalTermFreq();
			}
			for ( String w : voc ) {
				int tf = tfs.getOrDefault( w, 0 );
				double pw = ( tf + mufb * index.totalTermFreq( new Term( field, w ) ) / index.getSumTotalTermFreq( field ) ) / ( len + mufb );
				collector.put( w, collector.getOrDefault( w, 0.0 ) + pw * dw );
			}
		}
		return Utils.getTop( Utils.norm( collector ), numfbterms );
	}
	
	public Map<String, Double> estimateQueryModelDMM( String field, List<String> terms, double mu1, double lambda, int numfbdocs, int numfbterms, double weight_org ) throws IOException
	{
		List<SearchResult> results = search(field, terms, mu1, numfbdocs);
		DocLengthReader doclen = getDocLengthReader( field );
		Map<String, Map<Integer, Integer>> vocab = new TreeMap<>();
		Map<String, Double> dmm_scores = new TreeMap<>();
		
		//Getting vocab and corpus length
		double cl = index.getSumTotalTermFreq( field );
		for(int i = 0; i < results.size(); i++)
		{
			Terms vector = index.getTermVector( results.get(i).docid, field );

			TermsEnum temp_terms = vector.iterator();
			BytesRef term;
			while ( ( term = temp_terms.next() ) != null ) 
			{
			    String termstr = term.utf8ToString();
			    if (!stopwords.contains(termstr))
			    {
			    	if(vocab.containsKey(termstr))
			    	{
			    		vocab.get(termstr).put(results.get(i).docid, (int)temp_terms.totalTermFreq());
			    	}
			    	else
			    	{
			    		vocab.put(termstr, new TreeMap<>());
			    		vocab.get(termstr).put(results.get(i).docid, (int) temp_terms.totalTermFreq());
			    	}
			    }
			}
		}
		
		//Getting dmm_scores
		for(Map.Entry<String, Map<Integer, Integer>> entry: vocab.entrySet())
		{
			String term = entry.getKey();
	    	double term_freq_corpus = Math.log(index.totalTermFreq(new Term( field, term ))/cl);
	    	
	    	double score = 0;
			for(int i = 0; i < results.size(); i++)
			{
				double term_count = (double) entry.getValue().getOrDefault(results.get(i).docid, 0);
				if (term_count > 0.0)
				{
					double doclent = doclen.getLength(results.get(i).docid);
					term_count = Math.log(term_count/doclent);
				}
				double fbscore = term_count/numfbdocs;
				score += fbscore;
			}
			//score  -= lambda*term_freq_corpus;
			//dmm_scores.put(term, Math.exp(score/(1-lambda)));
			dmm_scores.put(term, score/(1-lambda));
		}
		
		//Taking first n feedback terms and normalizing them
		dmm_scores = Utils.getTop(Utils.norm(dmm_scores), numfbterms);
		/*for(String term: terms)
		{
			System.out.println(term);
		}
		for(Map.Entry<String, Double> entry: dmm_scores.entrySet())
		{
			System.out.println(entry.getKey() + " " + entry.getValue());
		}*/
		return estimateQueryModelRM3(terms, dmm_scores, weight_org);
	}
	
	public Map<String, Double> estimateQueryModelRM3( List<String> terms, Map<String, Double> rm1, double weight_org ) throws IOException {
		
		Map<String, Double> mle = new HashMap<>();
		for ( String term : terms ) {
			mle.put( term, mle.getOrDefault( term, 0.0 ) + 1.0 );
		}
		for ( String w : mle.keySet() ) {
			mle.put( w, mle.get( w ) / terms.size() );
		}
		
		Set<String> v = new TreeSet<>();
		v.addAll( terms );
		v.addAll( rm1.keySet() );
		
		Map<String, Double> rm3 = new HashMap<>();
		for ( String w : v ) {
			rm3.put( w, weight_org * mle.getOrDefault( w, 0.0 ) + ( 1 - weight_org ) * rm1.getOrDefault( w, 0.0 ) );
		}
		
		return rm3;
	}
	public Map<String, Double> estimateQueryModelSMM( String field, List<String> terms, double mu1, double lambda, int numfbdocs, int numfbterms, double weight_org ) throws IOException
	{
		
		List<SearchResult> results = search(field, terms, mu1, numfbdocs);
		DocLengthReader doclen = getDocLengthReader( field );
		Map<String, Map<Integer, Integer>> vocab = new TreeMap<>();
		Map<String, Double> smm_scores = new TreeMap<>();
		
		//Getting vocab and corpus length
		double cl = index.getSumTotalTermFreq( field );
		for(int i = 0; i < results.size(); i++)
		{
			Terms vector = index.getTermVector( results.get(i).docid, field );

			TermsEnum temp_terms = vector.iterator();
			BytesRef term;
			while ( ( term = temp_terms.next() ) != null ) 
			{
			    String termstr = term.utf8ToString();
			    if (!stopwords.contains(termstr))
			    {
			    	if(vocab.containsKey(termstr))
			    	{
			    		vocab.get(termstr).put(results.get(i).docid, (int)temp_terms.totalTermFreq());
			    	}
			    	else
			    	{
			    		vocab.put(termstr, new TreeMap<>());
			    		vocab.get(termstr).put(results.get(i).docid, (int) temp_terms.totalTermFreq());
			    	}
			    }
			}
		}
		
		//Getting smm_scores
		for(Map.Entry<String, Map<Integer, Integer>> entry: vocab.entrySet())
		{
			String term = entry.getKey();
	    	double pwc = (index.totalTermFreq(new Term( field, term ))/cl);
	    	
	    	double score = 0;
	    	double term_count=0;
	    	double length_fbdoc=0;
			for(int i = 0; i < results.size(); i++)
			{
				term_count = term_count+(double) entry.getValue().getOrDefault(results.get(i).docid, 0); //count of term in the set of feedback documents
				length_fbdoc=length_fbdoc+doclen.getLength(results.get(i).docid);// length of all feedback documents
			}
			double pwf;
			if(term_count>0){
				pwf=term_count/length_fbdoc;
			}
			else
				pwf=0;
			score=term_count*(Math.log(((1-lambda)*pwf)+(lambda*pwc)));
			smm_scores.put(term, score);
		}
		
		//Taking first n feedback terms and normalizing them
		smm_scores = Utils.getTop(Utils.norm(smm_scores), numfbterms);
		return estimateQueryModelRM3(terms, smm_scores, weight_org);
	}
	public Map<String, Double> estimateQueryModelRM2( String field, List<String> terms, double mu, double mufb, int numfbdocs, int numfbterms ) throws IOException {
		
		// for a given query, retrieve the top documents 
		List<SearchResult> results = search( field, terms, mu, numfbdocs );
		Set<String> voc = new HashSet<>();

		// get all the vocabulary for top results(excluding stop words)
		for ( SearchResult result : results ) {
			TermsEnum iterator = index.getTermVector( result.getDocid(), field ).iterator();
			BytesRef br;
			while ( ( br = iterator.next() ) != null ) {
				if ( !isStopwords( br.utf8ToString() ) ) {
					voc.add( br.utf8ToString() );
				}
			}
		}
		
		
		Map<String, Double> collector = new HashMap<>();
		double ans=0, sum=0, y=0;
		for ( String w : voc ){
			ans=1;
			
			for(String t: terms){
				
				y=0;
				sum=0;
				for ( SearchResult result : results ) {
					// for each term in the document get its tfs
					TermsEnum iterator = index.getTermVector( result.getDocid(), field ).iterator();
					Map<String, Integer> tfs = new HashMap<>();
					int len = 0;
					BytesRef br;
					while ( ( br = iterator.next() ) != null ) {
						tfs.put( br.utf8ToString(), (int) iterator.totalTermFreq() );
						len += iterator.totalTermFreq();// length of the document
					}
				
					int count=tfs.getOrDefault(t,0);
					double pqd = ( count + mu * index.totalTermFreq( new Term( field, t ) ) / index.getSumTotalTermFreq( field ) ) / ( len + mu );
					int tf = tfs.getOrDefault( w, 0 );
					double pw = ( tf + mufb * index.totalTermFreq( new Term( field, w ) ) / index.getSumTotalTermFreq( field ) ) / ( len + mufb );
					y=y+pw;
					double x=pqd*pw;
					sum=sum+x;
				}
				sum=sum/Math.pow(y,terms.size());
				
				ans=ans*sum;
			}
			ans=ans*y;
			
			collector.put(w, ans);
		}

		return Utils.getTop( Utils.norm( collector ), numfbterms );
	}
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

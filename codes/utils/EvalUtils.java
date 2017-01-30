package utils;

import java.io.*;
import java.util.*;

public class EvalUtils {
	
	/**
	 * Evaluate the precision of the ranked list until some cutoff rank n.
	 *
	 * @param results   A list of search results.
	 * @param relDocnos The set of judged relevant documents.
	 * @param n         The cutoff rank n (results at lower ranks will not be evaluated).
	 * @return The precision of the ranked list until some cutoff rank n (P@n).
	 */
	public static double precision( Collection<SearchResult> results, Set<String> relDocnos, int n ) {
		double numrel = 0;
		int count = 0;
		for ( SearchResult result : results ) {
			if ( relDocnos.contains( result.getDocno() ) ) {
				numrel++;
			}
			count++;
			if ( count >= n ) {
				break;
			}
		}
		return numrel / n;
	}
	
	/**
	 * Evaluate the recall of the ranked list until some cutoff rank n.
	 *
	 * @param results   A list of search results.
	 * @param relDocnos The set of judged relevant documents.
	 * @param n         The cutoff rank n (results at lower ranks will not be evaluated).
	 * @return The recall of the ranked list until some cutoff rank n.
	 */
	public static double recall( Collection<SearchResult> results, Set<String> relDocnos, int n ) {
		double numrel = 0;
		int count = 0;
		for ( SearchResult result : results ) {
			if ( relDocnos.contains( result.getDocno() ) ) {
				numrel++;
			}
			count++;
			if ( count >= n ) {
				break;
			}
		}
		return numrel / relDocnos.size();
	}
	
	/**
	 * Evaluate the average precision of the ranked list until some cutoff rank n.
	 *
	 * @param results   A list of search results.
	 * @param relDocnos The set of judged relevant documents.
	 * @param n         The cutoff rank n (results at lower ranks will not be evaluated).
	 * @return The average precision (AP) of the ranked list until some cutoff rank n.
	 */
//	public static double avgPrec( Collection<SearchResult> results, Set<String> relDocnos, int n ) {
//		double numrel = 0;
//		double sumprec = 0;
//		int count = 0;
//		for ( SearchResult result : results ) {
//			if ( relDocnos.contains( result.getDocno() ) ) {
//				numrel++;
//				sumprec += ( numrel / ( count + 1 ) );
//			}
//			count++;
//			if ( count >= n ) {
//				break;
//			}
//		}
//		return sumprec / relDocnos.size();
//	}
	public static double avgPrec( Collection<SearchResult> results, Map<String,Integer> relDocnos, int n) {
		double numrel = 0;
		double sumprec = 0;
		int count = 0;
		for ( SearchResult result : results ) {
			if ( relDocnos.getOrDefault(result.getDocno(), 0) != 0 ) {
				numrel++;
				sumprec += ( numrel / ( count + 1 ) );
			}
			count++;
			if ( count >= n ) {
				break;
			}
		}
		int total_rel = 0;
		for(Map.Entry<String, Integer> entry: relDocnos.entrySet())
		{
			if (entry.getValue() != 0)
				total_rel++;
		}
		return sumprec / total_rel;
	}
	
	public static double DCG(Collection<SearchResult> results, Map<String,Integer> qrels, int n)
	{
		double toreturn = 0;
		int count = 0;
		Collections.sort( (List<SearchResult>) results, ( o1, o2 ) -> o2.getScore().compareTo( o1.getScore() ) );
		for(SearchResult result: results)
		{
			if(qrels.getOrDefault(result.getDocno(), 0) != 0)
			{
				double denom = Math.log(count+2)/Math.log(2);
				toreturn += (Math.pow(2, qrels.get(result.getDocno()))-1)/denom;
			}
			count++;
			if ( count >= n ) {
				break;
			}
		}
		return toreturn;
	}
	
	public static double nDCG(Collection<SearchResult> results, Map<String,Integer> qrels, int n)
	{
		double toreturn = 0;
		double dcg_score = DCG(results, qrels, n);
		int count = 0;
		qrels = sortByValue(qrels);
		for(Map.Entry<String, Integer> entry: qrels.entrySet())
		{
			double denom = Math.log(count+2)/Math.log(2);
			toreturn += (Math.pow(2, entry.getValue()) - 1)/denom;
			count++;
			if ( count >= n ) {
				break;
			}
		}
		return dcg_score/toreturn;
	}
	
	public static double ERR(Collection<SearchResult> results, Map<String,Integer> qrels, int n)
	{
		double toreturn = 0;
		int count = 0;
		double prev = 1;
		for(SearchResult result: results)
		{
			if(qrels.getOrDefault(result.getDocno(), 0) != 0)
			{
				double rval = r_function(qrels.get(result.getDocno()), 2);
				toreturn += rval/(count+1)*prev;
				prev *= (1 - rval);
			}
			count++;
			if ( count >= n ) {
				break;
			}
		}
		return toreturn;
	}
	
	private static double r_function(int g, int gmax)
	{
		return (Math.pow(2, g) - 1)/Math.pow(2, gmax);
	}
	
	/**
	 * Load a TREC-format relevance judgment (qrels) file (such as "qrels_robust04" in HW2).
	 *
	 * @param f A qrels file.
	 * @return A map storing the set of relevant documents for each qid.
	 * @throws IOException
	 */
	/*public static Map<String, Set<String>> loadQrels( String f ) throws IOException {
		return loadQrels( new File( f ) );
	}*/
	public static Map<String, Map<String,Integer>> loadQrels( String f ) throws IOException {
		return loadQrels( new File( f ) );
	}
	
	/**
	 * Load a TREC-format relevance judgment (qrels) file (such as "qrels_robust04" in HW2).
	 *
	 * @param f A qrels file.
	 * @return A map storing the set of relevant documents for each qid.
	 * @throws IOException
	 */
	/*public static Map<String, Set<String>> loadQrels( File f ) throws IOException {
		Map<String, Set<String>> qrels = new TreeMap<>();
		BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( f ), "UTF-8" ) );
		String line;
		while ( ( line = reader.readLine() ) != null ) {
			String[] splits = line.split( "\\s+" );
			String qid = splits[0];
			String docno = splits[2];
			qrels.putIfAbsent( qid, new TreeSet<>() );
			if ( Integer.parseInt( splits[3] ) > 0 ) {
				qrels.get( qid ).add( docno );
			}
		}
		reader.close();
		return qrels;
	}*/
	public static Map<String, Map<String,Integer>> loadQrels( File f ) throws IOException {
		Map<String, Map<String,Integer>> qrels = new TreeMap<>();
		BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( f ), "UTF-8" ) );
		String line;
		while ( ( line = reader.readLine() ) != null ) {
			String[] splits = line.split( "\\s+" );
			String qid = splits[0];
			String docno = splits[2];
			if(!qrels.containsKey(qid))
				qrels.put(qid, new TreeMap<>());
			qrels.get(qid).put(docno, Integer.parseInt(splits[3]));
		}
		reader.close();
		return qrels;
	}
	
	/**
	 * Load a query file (such as "queries_robust04" in HW2).
	 *
	 * @param f A query file.
	 * @return A map storing the text query for each qid.
	 * @throws IOException
	 */
	public static Map<String, String> loadQueries( String f ) throws IOException {
		return loadQueries( new File( f ) );
	}
	
	/**
	 * Load a query file (such as "queries_robust04" in HW2).
	 *
	 * @param f A query file.
	 * @return A map storing the text query for each qid.
	 * @throws IOException
	 */
	public static Map<String, String> loadQueries( File f ) throws IOException {
		Map<String, String> queries = new TreeMap<>();
		BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( f ), "UTF-8" ) );
		String line;
		while ( ( line = reader.readLine() ) != null ) {
			String[] splits = line.split( "\t" );
			String qid = splits[0];
			String query = splits[1];
			queries.put( qid, query );
		}
		reader.close();
		return queries;
	}
	
	private static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
	
}

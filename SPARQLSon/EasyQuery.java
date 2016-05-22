package SPARQLSon;

public class EasyQuery {

	public static void main(String[] args) {

		String TDBdirectory = "/Users/adriansotosuarez/Desktop/minidistinct";
		String queryString = "SELECT * WHERE{?x ?y ?z . ?z <http://example.org/label/> ?n}";
		
		String select_everything = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/> " +
								   "PREFIX  ns:  <http://example.org/ns#> " +
								   "SELECT  ?x ?price " +
								   "{  ?x ns:price ?p . " +
								   "   ?x ns:discount ?discount " +
								   "   BIND (?p/(1-?discount) AS ?price) " +
								   "   FILTER (bound(?price) && ?p > 1)" +
								   "}";
		
		String[] variables = {"x", "y", "z", "n"};
		
		long start = System.nanoTime();
		
		DatabaseWrapper dbw = new DatabaseWrapper(TDBdirectory);
		dbw.execQuery(queryString, variables);
		
		long elapsedTime = System.nanoTime() - start;
		
		System.out.println(elapsedTime / 1000000000.0);
		
	}

}

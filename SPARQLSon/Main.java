package SPARQLSon;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class Main {

	public static void main(String[] args) throws Exception {

		// Put the TDB Directory where database is stored.
		String TDBdirectory = "/Path/to/database";
		DatabaseWrapper dbw = new DatabaseWrapper(TDBdirectory);

		// Query for best burguers in Chile.
		String query_yelp = "SELECT ?x ?n ?b ?r WHERE {?x <http://yago-knowledge.org/resource/isLocatedIn> <http://yago-knowledge.org/resource/Chile> .  ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://yago-knowledge.org/resource/wikicat_Communes_of_Chile> . ?x <http://www.w3.org/2000/01/rdf-schema#label> ?n  " +
				 "BIND_API <https://api.yelp.com/v2/search?term=Burguers&location={n}&sort=2>([\"businesses\"][0][\"name\"], [\"businesses\"][0][\"rating\"]) AS (?b, ?r) " +
				 "}";

		// Borsuk–Ulam query. Replace KEY-OPEN-WEATHER-API by the key.
		String query_borsuk_ulam = "SELECT ?x ?y ?t ?t2 WHERE{?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/City> . " +
					   "?y <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/City> . " +
					   "?x <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?lo . " +
					   "?y <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?lo2 . " +
					   "?x <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?la . " +
					   "?y <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?la2 . " +
					   "?x <http://www.w3.org/2000/01/rdf-schema#label> ?x2 . " +
					   "?y <http://www.w3.org/2000/01/rdf-schema#label> ?y2 " +
					   "FILTER((?la <= -?la2 + 0.25) && (?la >= -?la2 - 0.25) && " +
					   "(?lo <= -((?lo2/abs(?lo2))*(180 - abs(?lo2))) + 0.25) && (?lo >= -((?lo2/abs(?lo2))*(180 - abs(?lo2))) - 0.25)) " +
					   "BIND_API <http://api.openweathermap.org/data/2.5/weather?q={x2}&appid=KEY-OPEN-WEATHER-API>([\"main\"][\"temp\"]) AS (?t) " +
					   "BIND_API <http://api.openweathermap.org/data/2.5/weather?q={y2}&appid=KEY-OPEN-WEATHER-API>([\"main\"][\"temp\"]) AS (?t2) " +
					   "}";


		// Museum query.
		String query_museum = "SELECT ?x ?l ?h ?t WHERE {?x ?y <http://dbpedia.org/ontology/Museum> . ?x <http://dbpedia.org/ontology/location> <http://dbpedia.org/resource/London> . ?x <http://www.w3.org/2000/01/rdf-schema#label> ?l " +
				              "BIND_API <https://api.yelp.com/v2/search?term={l}&location=London&radius_filter=40000>([\"businesses\"][0][\"is_closed\"]) AS (?h) " +
										  "BIND_API <https://api.twitter.com/1.1/search/tweets.json?q={l}&result_type=recent>([\"statuses\"][0][\"text\"]) AS (?t) " +
							        "FILTER(bound(?h) && bound(?t)) }";

		// Sky in Japan. Replace KEY-OPEN-WEATHER-API by the key.
		String query_ski = "SELECT ?x ?y ?n ?t WHERE {?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/class/yago/SkiAreasAndResortsInJapan> . ?x <http://dbpedia.org/ontology/location> ?y . ?y <http://www.w3.org/2000/01/rdf-schema#label> ?n  " +
						   "BIND_API <http://api.openweathermap.org/data/2.5/weather?q={n},Japan&appid=KEY-OPEN-WEATHER-API>([\"weather\"][0][\"description\"]) AS (?t) " +
						   " }";

		// Sky in Hokkaido. Replace KEY-OPEN-WEATHER-API by the key.
		String query_ski2 = "SELECT ?x ?n ?t WHERE {?x <http://yago-knowledge.org/resource/isLocatedIn> <http://yago-knowledge.org/resource/Hokkaido> . ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://yago-knowledge.org/resource/wikicat_Ski_areas_and_resorts_in_Japan> . ?x <http://www.w3.org/2000/01/rdf-schema#label> ?n  " +
				   "BIND_API <http://api.openweathermap.org/data/2.5/weather?q={n},Japan&appid=KEY-OPEN-WEATHER-API>([\"weather\"][0][\"description\"]) AS (?t) " +
				   " }";

    // Custom API query.
		String query_distinct = "SELECT ?n ?t WHERE {?x ?y ?z . ?z <http://example.org/label/> ?n " +
								"BIND_API <http://localhost:3000/{n}/5>([\"time\"]) AS (?t) }";


		/*
		First, we create the parameters hashmaps. One for each API call in the query.
		Then we create strategy objects. One for each API call.
		Actually it's possible to choice between OAuth and a Basic Strategy,
		where the key is in the URI.
		Then we put all of these strategies and parameters in their respective ArrayList objects.
		*/

		// Uncomment the following for YELP Query.

		/*
		HashMap<String, String> params1 = new HashMap<String, String>();

		// Put your OAuth keys for Yelp
		params1.put("consumerKey", "CONSUMER-KEY-YELP");
		params1.put("consumerSecret", "CONSUMER-SECRET-YELP");
		params1.put("token", "TOKEN-YELP");
		params1.put("tokenSecret", "TOKEN-SECRET-YELP");
		params1.put("replace_string", "_");


		GetJSONStrategy strategy_oauth = new OAuthStrategy();

		ArrayList<GetJSONStrategy> strategy = new ArrayList<>();
		ArrayList<HashMap<String, String>> params = new ArrayList<HashMap<String,String>>();

		strategy.add(strategy_basic);
		params.add(params1);
		*/

		// Uncomment the following for Borsuk - Ulam Query.

		/*
		HashMap<String, String> params1 = new HashMap<String, String>();
		HashMap<String, String> params2 = new HashMap<String, String>();

		params1.put("replace_string", "_");
		params2.put("replace_string", "_");

		GetJSONStrategy strategy_basic = new BasicStrategy();

		ArrayList<GetJSONStrategy> strategy = new ArrayList<>();
		ArrayList<HashMap<String, String>> params = new ArrayList<HashMap<String,String>>();

		strategy.add(strategy_basic);
		strategy.add(strategy_basic);
		params.add(params1);
		params.add(params2);
		*/

		// Uncomment the following for Borsuk - Ulam Query.

		/*
		HashMap<String, String> params1 = new HashMap<String, String>();
		HashMap<String, String> params2 = new HashMap<String, String>();

		params1.put("replace_string", "_");
		params2.put("replace_string", "_");

		GetJSONStrategy strategy_basic = new BasicStrategy();

		ArrayList<GetJSONStrategy> strategy = new ArrayList<>();
		ArrayList<HashMap<String, String>> params = new ArrayList<HashMap<String,String>>();

		strategy.add(strategy_basic);
		strategy.add(strategy_basic);
		params.add(params1);
		params.add(params2);
		*/

		// Uncomment the following for Museum Query.

		/*
		HashMap<String, String> params1 = new HashMap<String, String>();
		HashMap<String, String> params2 = new HashMap<String, String>();

		params1.put("consumerKey", "CONSUMER-KEY-YELP");
		params1.put("consumerSecret", "CONSUMER-SECRET-YELP");
		params1.put("token", "TOKEN-YELP");
		params1.put("tokenSecret", "TOKEN-SECRET-YELP");
		params1.put("replace_string", "_");
		params2.put("consumerKey", "CONSUMER-KEY-TWITTER");
		params2.put("consumerSecret", "CONSUMER-SECRET-TWITTER");
		params2.put("token", "TOKEN-TWITTER");
		params2.put("tokenSecret", "TOKEN-SECRET-TWITTER");
		params2.put("replace_string", "_");

		GetJSONStrategy strategy_oauth = new OAuthStrategy();

		ArrayList<GetJSONStrategy> strategy = new ArrayList<>();
		ArrayList<HashMap<String, String>> params = new ArrayList<HashMap<String,String>>();

		strategy.add(strategy_oauth);
		strategy.add(strategy_oauth);
		params.add(params1);
		params.add(params2);
		*/

		// Uncomment the following for Ski Queries.

		/*
		HashMap<String, String> params1 = new HashMap<String, String>();

		params1.put("replace_string", "_");

		GetJSONStrategy strategy_basic = new BasicStrategy();

		ArrayList<GetJSONStrategy> strategy = new ArrayList<>();
		ArrayList<HashMap<String, String>> params = new ArrayList<HashMap<String,String>>();

		strategy.add(strategy_basic);
		params.add(params1);
		*/

		// Uncomment the following for the distinct query.

		/*
		HashMap<String, String> params1 = new HashMap<String, String>();

		params1.put("replace_string", "_");
		params1.put("distinct", "true");
		params1.put("distinct_var", "?z ?n");

		GetJSONStrategy strategy_basic = new BasicStrategy();

		ArrayList<GetJSONStrategy> strategy = new ArrayList<>();
		ArrayList<HashMap<String, String>> params = new ArrayList<HashMap<String,String>>();

		strategy.add(strategy_basic);
		params.add(params1);
		*/


		long start = System.nanoTime();

		// Put here the right query. Now the "distinct query" is being evaluated.
		dbw.evaluateSPARQLSon(query_distinct, strategy, params);

		long elapsedTime = System.nanoTime() - start;

		System.out.println("Total: " + elapsedTime / 1000000000.0);
		System.out.println("API: " + dbw.timeApi / 1000000000.0);
		elapsedTime = elapsedTime - dbw.timeApi;

		System.out.println("DB: " + elapsedTime / 1000000000.0);


	}

}

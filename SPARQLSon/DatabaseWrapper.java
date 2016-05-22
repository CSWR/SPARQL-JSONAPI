package SPARQLSon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.QueryStageException;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;



public class DatabaseWrapper {

	String TDBdirectory;
	long timeApi;
	ArrayList<String> cacheKeys;
	HashMap<String, JSONObject> cache;
	static final int CACHE_SIZE = 400;
	static final boolean LOG = false;
	
	static final Class[] no_quote_types = {Boolean.class, Integer.class, Double.class, Float.class};

	public DatabaseWrapper(String _directory) {
		this.TDBdirectory = _directory;
		this.cacheKeys = new ArrayList<String>();
		this.cache = new HashMap<>();
		this.timeApi = 0;
	}

	public void createDataset(String source, String format) {
		Dataset dataset = TDBFactory.createDataset(this.TDBdirectory);
		Model tdb = dataset.getDefaultModel();
		FileManager.get().readModel(tdb, source, format);
		dataset.close();
	}

	public void execQuery(String queryString, String[] variables) {
		Query query = QueryFactory.create(queryString);
		Dataset dataset = TDBFactory.createDataset(this.TDBdirectory);
		QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
		try {
			// Assumption: it's a SELECT query.
			ResultSet rs = qexec.execSelect();
			

			// The order of results is undefined. 
			int mapping_count = 1;
			for ( ; rs.hasNext() ; ) {

				QuerySolution rb = rs.nextSolution() ;

				System.out.println("### Mapping no. " + mapping_count + " ###");

				// Get title - variable names do not include the '?' (or '$')
				for (String v: variables) {	
					System.out.println("Variable: " + v + ", Value: " + rb.get(v));

				}

				System.out.println("######");
				mapping_count++;

			}
		}
		finally
		{
			// QueryExecution objects should be closed to free any system resources 
			qexec.close() ;
			dataset.close();
		}
	}

	public MappingSet execQueryGenURL(String queryString, String apiUrlRequest, String[] jpath, 
			String[] bindName, GetJSONStrategy strategy, 
			HashMap<String, String> params) throws JSONException, Exception {
		Query query = QueryFactory.create(queryString);
		Dataset dataset = TDBFactory.createDataset(this.TDBdirectory);
		QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
		MappingSet ms = new MappingSet();
		ArrayList<String> ms_varnames = new ArrayList<String>();
		try {
			// Assumption: it's a SELECT query.
			ResultSet rs = qexec.execSelect() ;
			List<String> vars_name =  rs.getResultVars();
			for (String vn: vars_name) {
				ms_varnames.add(vn);
			}
			for (String bn: bindName) {
				ms_varnames.add(bn);
			}
			ms.set_var_names(ms_varnames);
			// The order of results is undefined. 

			for ( ; rs.hasNext() ; ) {

				QuerySolution rb = rs.nextSolution() ;

				HashMap<String, String> mapping = new HashMap<String, String>();
				for(String var: vars_name) {
					if (rb.contains(var))
					{
						if (rb.get(var).isLiteral())
						{
							if(rb.get(var).asLiteral().getDatatypeURI() != null) {
								mapping.put(var, rb.get(var).asLiteral().getString());
							}
							else {
								if (!rb.get(var).asLiteral().getLanguage().equals("")) {
									mapping.put(var, "\"" + rb.get(var).asLiteral().getValue().toString() + "\"@" + rb.get(var).asLiteral().getLanguage());
								}
								else {
									mapping.put(var, "\"" + rb.get(var).asLiteral().getValue().toString() + "\"");
								}
							}
						}
						else 
						{
							mapping.put(var, "<" + rb.get(var).toString() + ">");
						}
					}
					else {
						mapping.put(var, "UNDEF");
					}
				}


				String url_req = ApiWrapper.insertValuesURL(apiUrlRequest, rb, params.get("replace_string"));
				JSONObject json = null;

				try {
					long start = System.nanoTime();
					json = retrieve_json(url_req, params, strategy);
					long stop = System.nanoTime();
					this.timeApi += (stop - start);
				}
				catch (Exception name) {
					System.out.println("ERROR: " + name);
					for (int i = 0; i < bindName.length; i++) {
						mapping.put(bindName[i], "UNDEF");
					}
				}


				// TODO: resolve 404 and 429 errors.
				if (json != null) {
					for (int i = 0; i < jpath.length; i++) {
						try {
							ArrayList<String> keys = ApiWrapper.getKeys(jpath[i]);
							Object value = ApiWrapper.getValueJson(json, keys);
							mapping.put(bindName[i], serializeValue(value));

						}
						catch (Exception name) {
							System.out.println("ERROR: " + name);
							mapping.put(bindName[i], "UNDEF");

						}
					}
				}
				ms.addMapping(mapping);

			}
		}
		finally
		{
			// QueryExecution objects should be closed to free any system resources 
			qexec.close() ;
			dataset.close();
		}
		return ms;
	}
	
	public MappingSet execQueryDistinct(String queryString, HashMap<String, String> params) throws JSONException, Exception {
		Query query = QueryFactory.create(queryString);
		Dataset dataset = TDBFactory.createDataset(this.TDBdirectory);
		QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
		MappingSet ms = new MappingSet();
		ArrayList<String> ms_varnames = new ArrayList<String>();
		try {
			// Assumption: it's a SELECT query.
			ResultSet rs = qexec.execSelect() ;
			List<String> vars_name =  rs.getResultVars();
			for (String vn: vars_name) {
				ms_varnames.add(vn);
			}
			ms.set_var_names(ms_varnames);
			// The order of results is undefined. 

			for ( ; rs.hasNext() ; ) {

				QuerySolution rb = rs.nextSolution() ;

				HashMap<String, String> mapping = new HashMap<String, String>();
				for(String var: vars_name) {
					if (rb.contains(var))
					{
						if (rb.get(var).isLiteral())
						{
							if(rb.get(var).asLiteral().getDatatypeURI() != null) {
								mapping.put(var, rb.get(var).asLiteral().getString());
							}
							else {
								if (!rb.get(var).asLiteral().getLanguage().equals(""))
								{
									mapping.put(var, "\"" + rb.get(var).asLiteral().getValue().toString() + "\"@" + rb.get(var).asLiteral().getLanguage());
								}
								else {
									mapping.put(var, "\"" + rb.get(var).asLiteral().getValue().toString() + "\"");
								}
							}
						}
						else 
						{
							mapping.put(var, "<" + rb.get(var).toString() + ">");
						}
					}
					else {
						mapping.put(var, "UNDEF");
					}
				}
				
				ms.addMapping(mapping);

			}
		}
		finally
		{
			// QueryExecution objects should be closed to free any system resources 
			qexec.close() ;
			dataset.close();
		}
		return ms;
	}

	public static String serializeValue(Object value) {
		for (Class c: no_quote_types) {
			if (value.getClass().equals(c)) {
				return value.toString();
			}
		}
		return "\"" + value.toString().replace('\n', ' ').replace("\"", "\\\"") + "\"";

	}

	public ResultSet execPostBindQuery(String selectSection, String bodySection, MappingSet ms) {
		ResultSet rs = null;
		String queryString = selectSection + ms.serializeAsValues() + bodySection;
		// System.out.println(queryString);
		Dataset dataset = TDBFactory.createDataset(this.TDBdirectory);
		QueryExecution qexec = QueryExecutionFactory.create(queryString, dataset);
		try {
			rs = qexec.execSelect() ;
			if (LOG) {
				printResultSet(rs);
			}
		}
		finally
		{
			// QueryExecution objects should be closed to free any system resources 
			// TODO: Resolve what to do with ResultSet rs, actually is useless
			qexec.close() ;
			dataset.close();
		}
		return rs;
	}

	public static void printResultSet(ResultSet rs) {
		int mapping_count = 0;
		for ( ; rs.hasNext() ; ) {

			QuerySolution rb = rs.nextSolution() ;

			System.out.println("### Mapping no. " + mapping_count + " ###");

			// Get title - variable names do not include the '?' (or '$')
			Iterator<String> names = rb.varNames();
			while (names.hasNext()) {	
				String v = names.next();
				System.out.println("Variable: " + v + ", Value: " + rb.get(v));
			}

			System.out.println("######");
			mapping_count++;

		}
	}

	public void evaluateSPARQLSon(String queryString, ArrayList<GetJSONStrategy> strategy, ArrayList<HashMap<String, String>> params, boolean replace) throws JSONException, Exception {
		strategy.get(0).set_params(params.get(0));
		HashMap<String, Object> parsedQuery = SPARQLSonParser.parseSPARQLSonQuery(queryString, replace);
		String firstQuery = apply_params_to_first_query(params, parsedQuery);
		MappingSet ms = execQueryGenURL(firstQuery, 
				(String) parsedQuery.get("URL"), 
				(String[]) parsedQuery.get("PATH"), 
				(String[]) parsedQuery.get("ALIAS"),
				strategy.get(0), params.get(0));
		if (params.get(0).containsKey("distinct") && params.get(0).get("distinct").equals("true")) {
			String new_query = "SELECT * WHERE{ " +  ms.serializeAsValues() + " " + (String) parsedQuery.get("FIRST") + " } ";
			ms = execQueryDistinct(new_query, params.get(0));
		}
		String bind_url_string = " BIND_API <([\\w\\-\\%\\?\\&\\=\\.\\{\\}\\:\\/\\,]+)>(?=((\\\\[\\\\\"]|[^\\\\\"])*\"(\\\\[\\\\\"]|[^\\\\\"])*\")*(\\\\[\\\\\"]|[^\\\\\"])*$)";
		Pattern pattern_variables = Pattern.compile(bind_url_string);
		Matcher m = pattern_variables.matcher(" " + (String) parsedQuery.get("LAST"));
		if (m.find()) {
			String recursive_query_string = ((String) parsedQuery.get("SELECT")) + ms.serializeAsValues() + (String) parsedQuery.get("LAST");
			evaluateSPARQLSon(recursive_query_string, new ArrayList<GetJSONStrategy>(strategy.subList(1, strategy.size())), new ArrayList<HashMap<String,String>>(params.subList(1, params.size())), false);
		}
		else {
			execPostBindQuery((String) parsedQuery.get("SELECT"), (String) parsedQuery.get("LAST"), ms);
		}

	}

	public void evaluateSPARQLSon(String queryString, ArrayList<GetJSONStrategy> strategy, ArrayList<HashMap<String, String>> params) throws JSONException, Exception {
		evaluateSPARQLSon(queryString, strategy, params, true);

	}

	public String apply_params_to_first_query(ArrayList<HashMap<String, String>> params, HashMap<String, Object> parsedQuery) throws JSONException, Exception {
		String new_query = "";
		if (params.get(0).containsKey("distinct") && params.get(0).get("distinct").equals("true")) {
			new_query = "SELECT DISTINCT " + params.get(0).get("distinct_var") + " WHERE { " + (String) parsedQuery.get("FIRST") + " } ";
		}
		else {
			new_query = "SELECT * WHERE { " + (String) parsedQuery.get("FIRST") + " } ";
		}
		if (params.get(0).containsKey("limit")) {
			new_query += "LIMIT " + params.get(0).get("limit") + " ";
		}
		return new_query;
		
	}
	
	public JSONObject retrieve_json(String url_req, HashMap<String,String> params, GetJSONStrategy strategy) throws JSONException, Exception {
		if (params.containsKey("cache") && params.get("cache").equals("true")) {
			if (cache.containsKey(url_req)) {
				return cache.get(url_req);
			}
			else {
				JSONObject json =  ApiWrapper.getJSON(url_req, params, strategy);
				if (cacheKeys.size() < CACHE_SIZE) {
					cacheKeys.add(url_req);
					cache.put(url_req, json);
				}
				else {
					String removed_key = cacheKeys.remove(0);
					cache.remove(removed_key);
					cache.put(url_req, json);
				}
				return json;
			}
		}
		else {
			return ApiWrapper.getJSON(url_req, params, strategy);
		}
	}
	

}

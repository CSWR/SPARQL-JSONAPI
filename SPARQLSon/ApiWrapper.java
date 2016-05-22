package SPARQLSon;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import com.hp.hpl.jena.query.QuerySolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.*;


public class ApiWrapper {
	
	
	public static JSONObject getJSON(String urlString, HashMap<String,String> params, GetJSONStrategy strategy) throws JSONException, Exception {
		JSONObject json = new JSONObject(strategy.readURL(urlString));
		return json;
	}
	
	public static String insertValuesURL(String apiUrlRequest, QuerySolution rb, String replace_string) {
		String url = apiUrlRequest;
		Pattern pattern_variables = Pattern.compile("\\{(\\w*?)\\}");
		Matcher m = pattern_variables.matcher(url);
		while (m.find()) {
		    String s = m.group(1);
		    // System.out.println(rb.get(s).asLiteral().getValue().toString());
		    String value = rb.get(s).asLiteral().getValue().toString().replaceAll("[\\ ]+", replace_string);
		    url = m.replaceFirst(value);
		    m = pattern_variables.matcher(url);
		}
		// System.out.println(url);
		return url;
	}
	
	public static ArrayList<String> getKeys(String serializedKeys) {
		ArrayList<String> keys = new ArrayList<String>();
		Pattern pattern_variables = Pattern.compile("\\[([\"]{0,1}\\w*[\"]{0,1})\\]");
		Matcher m = pattern_variables.matcher(serializedKeys);
		while (m.find()) {
		    String s = m.group(1);
		    keys.add(s);
		}
		return keys;
	}
	
	public static Object getValueJson(Object json, ArrayList<String> keys) throws JSONException {
		if (json.getClass().equals(JSONObject.class)) {
			String key = keys.get(0).substring(1, keys.get(0).length() - 1);
			if (keys.size() == 1) {
				return ((JSONObject)json).get(key);
			}
			else {
				if (keys.get(1).charAt(0) == '"') {
					ArrayList<String> new_keys = new ArrayList<String>(keys.subList(1, keys.size()));
					return getValueJson(((JSONObject)json).get(key), new_keys);
				}
				else {
					ArrayList<String> new_keys = new ArrayList<String>(keys.subList(1, keys.size()));
					return getValueJson(((JSONObject)json).getJSONArray(key), new_keys);
				}
			}
		}
		else if(json.getClass().equals(JSONArray.class)) {
			if (keys.size() == 1) {
				String key = keys.get(0).substring(0, keys.get(0).length());
				return ((JSONArray)json).get(Integer.parseInt(key));
			}
			else {
				if (keys.get(1).charAt(0) == '"') {
					ArrayList<String> new_keys = new ArrayList<String>(keys.subList(1, keys.size()));
					return getValueJson(((JSONArray)json).get(Integer.parseInt(keys.get(0))), new_keys);
				}
				else {
					ArrayList<String> new_keys = new ArrayList<String>(keys.subList(1, keys.size()));
					return getValueJson(((JSONArray)json).get(Integer.parseInt(keys.get(0))), new_keys);
				}
			}
		}
		else {
			return null;
		}

	}
}


package edu.columbia.ldpd.dvhs.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.columbia.ldpd.dvhs.DurstRecord;
import edu.columbia.ldpd.dvhs.DurstVoyagerHyacinthSync;
import edu.columbia.ldpd.dvhs.exceptions.MultipleRecordsException;

public class HyacinthUtils {
	
	public static HttpPost getNewAuthenticatedHttpPostObject(String url) {
		HttpPost post = new HttpPost(url);
	 
		String authDigest = new String(Base64.encodeBase64((DurstVoyagerHyacinthSync.hyacinthUserEmail + ":" + DurstVoyagerHyacinthSync.hyacinthUserPassword).getBytes()));
		post.setHeader("Authorization", "Basic " + authDigest);
		
		return post;
	}
	
	public static String getPidForClioIdentifier(String clioIdentifier) throws MultipleRecordsException, ClientProtocolException, IOException, JSONException {
		if(clioIdentifier == null) { return null; }
		
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = getNewAuthenticatedHttpPostObject(DurstVoyagerHyacinthSync.hyacinthAppUrl + "/digital_objects/search.json");
		
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("_method", "POST"));
		urlParameters.add(new BasicNameValuePair("search[fq][project_string_key_sim][0][equals]", "durst")); // Durst project
		urlParameters.add(new BasicNameValuePair("search[fq][df_clio_identifier_value_sim][0][equals]", clioIdentifier)); // Specific CLIO identifier search
		urlParameters.add(new BasicNameValuePair("facet", "false")); //No need to facet. Better performance.

		//Make sure to use UTF-8
		post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
		HttpResponse response = client.execute(post);
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuffer resultSb = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			resultSb.append(line);
		}
		JSONObject jsonResponse = new JSONObject(resultSb.toString());
		JSONArray results = jsonResponse.getJSONArray("results");
		
		switch(results.length()) {
			case 0:
				// Found zero results. No record in Hyacinth with this CLIO identifier.
				return null;
			case 1:
				// Found one result
				return results.getJSONObject(0).getString("pid");
			default:
				// Found multiple Hyacinth records with this CLIO identifier, indicating that something went wrong. Inform the user.
				ArrayList<String> pids = new ArrayList<String>();
				for(int i = 0; i < results.length(); i++) {
					pids.add(results.getJSONObject(i).getString("pid"));
				}
				throw new MultipleRecordsException("Found multiple Hyacinth records with CLIO identifier " + clioIdentifier + ": " + StringUtils.join(pids, ","));
		}
	}
	
	/**
	 * Check to see if a Hyacinth record exists with the given pid AND with the given marc005Value
	 * @param pidForRecord
	 * @param marc005Value
	 * @return true if Hyacinth record exists with the given pid AND with the given marc005Value. Otherwise returns false.
	 * @throws JSONException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static boolean recordHasSpecificMarc005Value(String pidForRecord, String marc005Value) throws JSONException, ClientProtocolException, IOException {
		
		// If this is a new record without a pid, then there is no existing value in Hyacinth.  Just return false.
		if(pidForRecord == null) { return false; }
		
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = getNewAuthenticatedHttpPostObject(DurstVoyagerHyacinthSync.hyacinthAppUrl + "/digital_objects/search.json");
	 
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("_method", "POST"));
		urlParameters.add(new BasicNameValuePair("search[fq][project_string_key_sim][0][equals]", "durst")); // To be safe, limit scope to Durst project
		urlParameters.add(new BasicNameValuePair("search[fq][pid][0][equals]", pidForRecord)); // Search by pid
		urlParameters.add(new BasicNameValuePair("search[fq][df_marc_005_last_modified_value_sim][0][equals]", marc005Value)); // Attempt to find match for existing 005 update time
		urlParameters.add(new BasicNameValuePair("facet", "false")); //No need to facet. Better performance.
	 
		post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
		HttpResponse response = client.execute(post);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuffer resultSb = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			resultSb.append(line);
		}
		JSONObject jsonResponse = new JSONObject(resultSb.toString());
		JSONArray results = jsonResponse.getJSONArray("results");
		
		if(results.length() == 1) {
			// Found record with pid that matches marc 005 value.
			return true;
		} else {
			// Either:
			// 1) Didn't find record with given pid
			// or
			// 2) Found record with given pid, but marc 005 value did not match the given parameter value
			return false;
		}
		
	}
	
	public static void sendDurstRecordToHyacinth(DurstRecord record, boolean publish, boolean includeTestParam) throws IOException, JSONException {
		String pid = record.getPid();
		JSONObject digitalObjectData = record.getDigitalObjectData();
		
		// Inject additional digital object data params that apply to all objects
		digitalObjectData.put("digital_object_type", new JSONObject().put("string_key", "item"));
		digitalObjectData.put("publish", publish);
		digitalObjectData.put("project", new JSONObject().put("string_key", "durst"));
		digitalObjectData.put("publish_targets", new JSONArray().put(new JSONObject().put("string_key", "durst")));
		
		String digitalObjectDataString = digitalObjectData.toString();
		
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post;
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		if(pid == null) {
			// This is a NEW record without a pid
			post = getNewAuthenticatedHttpPostObject(DurstVoyagerHyacinthSync.hyacinthAppUrl + "/digital_objects.json");
			urlParameters.add(new BasicNameValuePair("_method", "POST"));
		} else {
			// This is an update to an existing record that has a pid 
			post = getNewAuthenticatedHttpPostObject(DurstVoyagerHyacinthSync.hyacinthAppUrl + "/digital_objects/" + pid + ".json");
			urlParameters.add(new BasicNameValuePair("_method", "PUT"));
		}
		urlParameters.add(new BasicNameValuePair("publish", "true")); // Re-publish record
		urlParameters.add(new BasicNameValuePair("digital_object_data_json", digitalObjectDataString));
		if(includeTestParam) { urlParameters.add(new BasicNameValuePair("test", "true")); }
		
		post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
		HttpResponse response = client.execute(post);
		 
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuffer resultSb = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			resultSb.append(line);
		}
		
		// Handle response
		
		JSONObject jsonResponse;
		try {
			jsonResponse = new JSONObject(resultSb.toString());
			if(jsonResponse.getBoolean("success") == true) {
				// Success!
			} else {
				DurstVoyagerHyacinthSync.logger.error("Error encountered during Hyacinth record save (for pid "
					+ pid + ", with CLIO bib(s): " + StringUtils.join(record.getClioIdentifiers(), ",") + "\n" +
					"Error " + jsonResponse.toString()
				);
			}
		} catch (JSONException e) {
			DurstVoyagerHyacinthSync.logger.error("Invalid JSON encountered during Hyacinth record save (for pid "
				+ pid + ", with CLIO bib(s): " + StringUtils.join(record.getClioIdentifiers(), ",") + "\n" +
				"Server response:\n" + resultSb.toString()
			);
			throw e;
		}
		
	}
}

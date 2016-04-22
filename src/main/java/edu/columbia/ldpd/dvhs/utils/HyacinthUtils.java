package edu.columbia.ldpd.dvhs.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
		String result = resultSb.toString();
		JSONObject jsonResponse = new JSONObject(result);
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
				String erorrMessage = "Found multiple Hyacinth records with CLIO identifier " + clioIdentifier + ": ";
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
	 * @return
	 * @throws JSONException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static boolean recordHasSpecificMarc005Value(String pidForRecord, String marc005Value) throws JSONException, ClientProtocolException, IOException {
		
		//If this is a new record without a pid, then there is no existing value in Hyacinth.  Just return true.
		if(pidForRecord == null) { return true; }
		
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
		String result = resultSb.toString();
		JSONObject jsonResponse = new JSONObject(result);
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
	
	public static void sendDurstRecordToHyacinth(DurstRecord record) {
		
		String result = "";
		
		try {
			
			String recordDynamicFieldDataJson = this.getDynamicFieldDataJSON();
			
			if(! actuallyDoSend) {
				//System.out.println("Not actually sending record to Hyacinth");
				return;
			}
			
		
			//Post JSON to Hyacinth
			
			String url;
			if(this.pid == null) {
				url = DDBSync.hyacinthAppUrl + "/digital_objects.json";
			} else {
				System.out.println("Performing UPDATE because existing PID was found: " + this.pid);
				url = DDBSync.hyacinthAppUrl + "/digital_objects/" + this.pid + ".json";
			}
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(url);
			String authDigest = new String(Base64.encodeBase64((DDBSync.hyacinthAppLoginEmail + ":" + DDBSync.hyacinthAppLoginPassword).getBytes()));
			post.setHeader("Authorization", "Basic " + authDigest);
			
			//post.setHeader("Content-Type", "text/plain;charset=UTF-8");
			//application/x-www-form-urlencoded; charset=utf-8
		 
			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		
		
			if(this.pid == null) {
				//This is a NEW record without a pid
				urlParameters.add(new BasicNameValuePair("_method", "POST"));
				urlParameters.add(new BasicNameValuePair("digital_object[digital_object_type_string_key]", "item"));
				urlParameters.add(new BasicNameValuePair("digital_object[project_string_key]", "durst"));
				urlParameters.add(new BasicNameValuePair("digital_object[publish_targets][]", "cul:sqv9s4mwg3")); //Durst publish target
			} else {
				//This is an update to an existing record with pid: lastSeenPid
				urlParameters.add(new BasicNameValuePair("_method", "PUT"));
			}
			
			//urlParameters.add(new BasicNameValuePair("merge", "false")); //This will replace ALL existing data.  Un-set this when you want to do incremental updates again.
			
			urlParameters.add(new BasicNameValuePair("publish", "true")); //Attempt to re-publish record (if record has publish targets)
			urlParameters.add(new BasicNameValuePair("digital_object[dynamic_field_data_json]", recordDynamicFieldDataJson));
			
			if(includeTestParam) {
				urlParameters.add(new BasicNameValuePair("test", "true"));
			}
			
		
			//post.setEntity(new UrlEncodedFormEntity(urlParameters));
			post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
			HttpResponse response = client.execute(post);
			
			//System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		 
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		 
			StringBuffer resultSb = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				resultSb.append(line);
			}
			
			result = resultSb.toString();
			
			//Handle response
			JSONObject jsonResponse = new JSONObject(result);
			System.out.println(jsonResponse.toString());
			if(jsonResponse.getBoolean("success") == true) {
				// Success!
			} else {
				DDBSync.writeToLog("Error encountered during Hyacinth record save (for pid " + this.pid + ", with CLIO bib(s): " + StringUtils.join(this.clio_identifier) + " or cul-assigned postcard id: " + StringUtils.join(this.cul_assigned_postcard_identifier) + "). Error " + jsonResponse.toString(), true, DDBSync.LOG_TYPE_ERROR);
			}
			
		} catch (JSONException e) {
			DDBSync.writeToLog("Error encountered during Hyacinth record save: " + e.getMessage(), true, DDBSync.LOG_TYPE_ERROR);
			DDBSync.writeToLog("Error encountered during Hyacinth record save (for pid " + this.pid + ", with CLIO bib(s): " + StringUtils.join(this.clio_identifier) + " or cul-assigned postcard id: " + StringUtils.join(this.cul_assigned_postcard_identifier) + "). Error: " + e.getMessage(), true, DDBSync.LOG_TYPE_ERROR);
			DDBSync.writeToLog("Post Response: " + result, true, DDBSync.LOG_TYPE_ERROR);
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

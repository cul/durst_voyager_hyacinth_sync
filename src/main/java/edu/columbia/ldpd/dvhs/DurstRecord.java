package edu.columbia.ldpd.dvhs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

import com.opencsv.CSVReader;

import edu.columbia.ldpd.dvhs.BetterMarcRecord;
import edu.columbia.ldpd.dvhs.exceptions.MultipleRecordsException;
import edu.columbia.ldpd.dvhs.utils.HyacinthUtils;

public class DurstRecord {
	
	private static final Pattern VALID_035_AND_776_FIELD_PATTERN = Pattern.compile("\\(OCoLC\\)(oc(m|n))*(0*)(\\d+)"); //including (0*) to remove leading zeros
	
	private JSONObject digitalObjectData;
	private JSONObject dynamicFieldData;
	private ArrayList<String> ocolc035FieldValues;
	private ArrayList<String> ocolc776FieldValues;
	private boolean isElectronicRecord = false; //true when 965 $a field == "eDurst"
	private String pid = null;
	
	public DurstRecord(File marcXmlFile, File[] holdingsFiles, String[] barcodes) {
		
		this.digitalObjectData = new JSONObject();
		this.dynamicFieldData = new JSONObject();
		try {
			this.digitalObjectData.put("dynamic_field_data", this.dynamicFieldData);
		} catch (JSONException e1) { e1.printStackTrace(); }
		
		// Get MARC record bibliographic data into a BetterMarcRecord
		FileInputStream marcXmlFileInputStream = null;
		try {
			marcXmlFileInputStream = new FileInputStream(marcXmlFile);
			MarcXmlReader reader = new MarcXmlReader(marcXmlFileInputStream);
			if (reader.hasNext()) {
				Record bibRecord = reader.next();
				BetterMarcRecord bibMarcRecord = new BetterMarcRecord(bibRecord);

				// If one of the 965 $a fields equals "965edurst" (with case insensitive check),
				// this is an electronic record. Otherwise we expect to see "965durst" (with case
				// insensitive check), indicating that this is a print record.
				String value965Durst = get965DurstMarkerFromMarcRecord(bibMarcRecord);
				
				if(value965Durst.equals("965edurst")) {
					// This is an electronic record
					this.isElectronicRecord = true;
					ocolc776FieldValues = getOcolc776ValuesFromMarcRecord(bibMarcRecord);
					
				} else {
					// This is a print record
					this.isElectronicRecord = false;
					ocolc035FieldValues = getOcolc035ValuesFromMarcRecord(bibMarcRecord);
				}
				
				addHardCodedFieldValues();
				extractMarcDataFromBibRecord(bibMarcRecord);
				extractDataFromRawMarcHoldingsRecords(holdingsFiles);
			}
			
			// Close marc file input stream
			try {
				marcXmlFileInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException | JSONException e) {
			e.printStackTrace();
		}
		
		// Determine whether this is an electronic record or print record
		
		// If this is an electronic record, get 776 ocolc values
		// If this is a print record, get 035 ocolc values
	}
	
	private void addHardCodedFieldValues() throws JSONException {
		// collection-1:collection_term.*
		// Always the same value: "Seymour B. Durst Old York Library Collection"
		// Always the same uri: "http://id.library.columbia.edu/term/79de0ce6-55d5-4665-925a-11adfa425bf7"
		dynamicFieldData.put("collection", new JSONArray().put(new JSONObject()
				.put("collection_term", new JSONObject()
					.put("value", "Seymour B. Durst Old York Library Collection")
					.put("uri", "http://id.library.columbia.edu/term/79de0ce6-55d5-4665-925a-11adfa425bf7")
				)
			)
		);
	}

	private void extractMarcDataFromBibRecord(BetterMarcRecord betterMarcRecord) throws JSONException {
		ArrayList<DataField> fields; // This variable is reused many times throughout this method
		
		// clio_identifier-1:clio_identifier_value
		// MARC 001
		dynamicFieldData.put("clio_identifier", new JSONArray().put(new JSONObject().put("clio_identifier_value", betterMarcRecord.getControlField("001").getData().trim())));
		
		// marc_005_last_modified-1:marc_005_last_modified_value
		// MARC 005
		dynamicFieldData.put("marc_005_last_modified", new JSONArray().put(new JSONObject().put("marc_005_last_modified_value", betterMarcRecord.getControlField("005").getData().trim())));
		
		// title-1:title_non_sort_portion
		// title-1:title_sort_portion
		// MARC 245
		// title: ----- 245 $a,$b,$n,$p -- indicator 2 for num nonsort chars ----- 
		fields = betterMarcRecord.getDataFields("245");
		for(DataField field : fields) {
			int numNonSortCharacters = Integer.parseInt(""+field.getIndicator2());
			String result = BetterMarcRecord.removeCommonTrailingCharacters(
				StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'a'), ", ").trim() + " " +
				StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'b'), ", ").trim() + " " +
				StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'n'), ", ").trim() + " " +
				StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'p'), ", ").trim()
			).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
			if( ! result.isEmpty() ) {
				dynamicFieldData.put("title", new JSONArray().put(new JSONObject()
						.put("title_non_sort_portion", result.substring(0, numNonSortCharacters).trim())
						.put("title_sort_portion", result.substring(numNonSortCharacters).trim())
					)
				);
			}
		}
		
		// alternative_title-1:alternative_title_value
		// MARC 246 $a (or 740 $a in older records)
		JSONArray alternativeTitleJSONArray = new JSONArray();
		dynamicFieldData.put("alternative_title", alternativeTitleJSONArray);
		fields = betterMarcRecord.getDataFields("246");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					alternativeTitleJSONArray.put(new JSONObject()
						.put("alternative_title_value", result)
					);
				}
			}
		}
		fields = betterMarcRecord.getDataFields("740");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					alternativeTitleJSONArray.put(new JSONObject()
						.put("alternative_title_value", result)
					);
				}
			}
		}
		
		// abstract-1:abstract_value
		// MARC 520 $a
		JSONArray abstractJSONArray = new JSONArray();
		dynamicFieldData.put("abstract", abstractJSONArray);
		fields = betterMarcRecord.getDataFields("520");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					abstractJSONArray.put(new JSONObject()
						.put("abstract_value", result)
					);
				}
			}
		}
		
		// name-1:name_term.* 
		//name ----- 	First entry in 100 $a,$b,$c,$d (personal) or 110 $a,$b,$c,$d (corporate) or 111 $a,$b,$c,$d (conference),
		//				additional entries in 700 $a,$b,$c,$d (personal) or 710 $a,$b,$c,$d (corporate) or 711 $a,$b,$c,$d (conference)
		JSONArray nameJSONArray = new JSONArray();
		dynamicFieldData.put("name", nameJSONArray);
		String[] nameFields = {"100", "110", "111", "700", "710", "711"};
		for(String marcFieldNumberTag : nameFields) {
			fields = betterMarcRecord.getDataFields(marcFieldNumberTag);
			for(DataField field : fields) {
				//Special rule: If the marcFieldNumberTag is "700" and $e value is "former owner" and $a value starts with "Durst, Seymour", skip this name
				if(marcFieldNumberTag.equals("700")) {
					boolean formerOwnerValueFound = false;
					boolean skipThisName = false;
					
					for(String aSubfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'e')) {
						if (aSubfieldValue.startsWith("former owner")) {
							formerOwnerValueFound = true;					
						}
					}
					if(formerOwnerValueFound) {
						for(String aSubfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
							if (aSubfieldValue.startsWith("Durst, Seymour")) {
								skipThisName = true;					
							}
						}
					}
					if(skipThisName) {
						continue;
					}
				}
				
				String result = BetterMarcRecord.removeCommonTrailingCharacters(
						StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'a'), ", ").trim() + " " +
						StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'b'), ", ").trim() + " " +
						StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'c'), ", ").trim() + " " +
						StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'd'), ", ").trim()
				).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
				if( ! result.isEmpty() ) {
					
					String nameType = "";
					boolean primaryName = false;
					if(marcFieldNumberTag.equals("100")) {
						nameType = "personal";
						primaryName = true;
					} else if(marcFieldNumberTag.equals("110")) {
						nameType = "corporate";
						primaryName = true;
					} else if(marcFieldNumberTag.equals("111")) {
						nameType = "conference";
						primaryName = true;
					} else if(marcFieldNumberTag.equals("700")) {
						nameType = "personal";
						primaryName = false;
					} else if(marcFieldNumberTag.equals("710")) {
						nameType = "corporate";
						primaryName = false;
					} else if(marcFieldNumberTag.equals("711")) {
						nameType = "conference";
						primaryName = false;
					}
					
					nameJSONArray.put(new JSONObject()
						.put("name_term", new JSONObject()
							.put("value", result)
							.put("name_type", nameType)							
						).put("name_usage_primary", primaryName)
					);
				}
			}
		}
		
		// publisher-1:publisher_value
		// MARC 260 $b or 264_1 $b
		JSONArray publisherJSONArray = new JSONArray();
		dynamicFieldData.put("publisher", publisherJSONArray);
		fields = betterMarcRecord.getDataFields("260");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'b')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					publisherJSONArray.put(new JSONObject()
						.put("publisher_value", result)
					);
				}
			}
		}
		fields = betterMarcRecord.getDataFields("264");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, '1', 'b')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					publisherJSONArray.put(new JSONObject()
						.put("publisher_value", result)
					);
				}
			}
		}
		
		// place_of_origin-1:place_of_origin_value
		// MARC 260 $a or 264_1 $a
		JSONArray placeOfOriginJSONArray = new JSONArray();
		dynamicFieldData.put("place_of_origin", placeOfOriginJSONArray);
		fields = betterMarcRecord.getDataFields("260");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					placeOfOriginJSONArray.put(new JSONObject()
						.put("place_of_origin_value", result)
					);
				}
			}
		}
		fields = betterMarcRecord.getDataFields("264");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, '1', 'a')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					placeOfOriginJSONArray.put(new JSONObject()
						.put("place_of_origin_value", result)
					);
				}
			}
		}
		
		// date_other_textual-1:date_other_textual_value
		// MRC 260 $c or 264_1 $c
		JSONArray dateOtherTextualJSONArray = new JSONArray();
		dynamicFieldData.put("date_other_textual", dateOtherTextualJSONArray);
		fields = betterMarcRecord.getDataFields("260");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'c')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					dateOtherTextualJSONArray.put(new JSONObject()
						.put("date_other_textual_value", result)
					);
				}
			}
		}
		fields = betterMarcRecord.getDataFields("264");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, '1', 'c')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					dateOtherTextualJSONArray.put(new JSONObject()
						.put("date_other_textual_value", result)
					);
				}
			}
		}
		
		//date_*-1:date_*_start_value
		//date_*-1:date_*_end_value
		//date_*-1:date_*type
		//date_*-1:date_*_key_date
		// MARC start: 008, Byte 07-10 -- end: 008, Byte 11-14, type: Byte 06
		JSONArray dateCreatedJSONArray = new JSONArray();
		dynamicFieldData.put("date_created", dateCreatedJSONArray);
		JSONArray dateIssuedJSONArray = new JSONArray();
		dynamicFieldData.put("date_issued", dateIssuedJSONArray);
		JSONArray dateOtherJSONArray = new JSONArray();
		dynamicFieldData.put("date_other", dateOtherJSONArray);
		HashMap<String, String> start_and_end_and_type_and_keydate = new HashMap<String, String>();
		String typeOfDate = betterMarcRecord.getControlField("008").getData().substring(6, 7).trim();
		String date1 = betterMarcRecord.getControlField("008").getData().substring(7, 11).trim();
		String date2 = betterMarcRecord.getControlField("008").getData().substring(11, 15).trim();
		//Agreed upon handling for only these specific date types for now:
		if( typeOfDate.equals("c") || typeOfDate.equals("d") || typeOfDate.equals("i") || typeOfDate.equals("k") || typeOfDate.equals("m") ) {
			dateIssuedJSONArray.put(new JSONObject()
				.put("date_issued_start_value", date1)
				.put("date_issued_end_value", date2)
				.put("date_issued_key_date", true)
			);
		} else if(typeOfDate.equals("e")) {
			dateIssuedJSONArray.put(new JSONObject()
				.put("date_issued_start_value", date1)
				.put("date_issued_key_date", true)
			);
		} else if(typeOfDate.equals("n")) {
			dateIssuedJSONArray.put(new JSONObject()
				.put("date_issued_start_value", date1) //"n" means unknown date: we expect date1 to equal "uuuu" (via catalog value)
				.put("date_issued_end_value", date2) //"n" means unknown date: we expect date2 to equal "uuuu" (via catalog value)
				.put("date_issued_key_date", true)
			);
		} else if(typeOfDate.equals("p")) {
			//date2 is always a single dateCreated date
			dateCreatedJSONArray.put(new JSONObject()
				.put("date_created_start_value", date2)
				.put("date_created_key_date", true)
			);
			if( ! date1.equals(date2) ) {
				//If these dates are equal, then date1 is a single dateIssued date,
				//but should not be marked as a key date
				dateIssuedJSONArray.put(new JSONObject()
					.put("date_issued_start_value", date1)
				);
			}
		} else if(typeOfDate.equals("q")) {
			dateIssuedJSONArray.put(new JSONObject()
				.put("date_issued_start_value", date1)
				.put("date_issued_end_value", date2)
				.put("date_issued_type", "questionable")
				.put("date_issued_key_date", true)
			);
		} else if(typeOfDate.equals("r")) {
			dateCreatedJSONArray.put(new JSONObject()
				.put("date_created_start_value", date2) //Note: date2 is indeed the start/single date that we want here
				.put("date_created_key_date", true)
			);
		} else if(typeOfDate.equals("s") || typeOfDate.equals("t")) {
			dateIssuedJSONArray.put(new JSONObject()
				.put("date_issued_start_value", date1)
				.put("date_issued_key_date", true)
			);
		} else if(typeOfDate.equals("u")) {
			dateIssuedJSONArray.put(new JSONObject()
				.put("date_issued_start_value", date1) //"u" means continuing resource with unknown date: we expect date1 to equal something like "19uu" (via catalog value)
				.put("date_issued_end_value", date2) //"u" means continuing resource with unknown date: we expect date2 to equal "uuuu" (via catalog value)
				.put("date_issued_key_date", true)
			);
		}
		
		// edition-1:edition_value
		// MARC 250 $a
		JSONArray editionJSONArray = new JSONArray();
		dynamicFieldData.put("edition", editionJSONArray);
		fields = betterMarcRecord.getDataFields("250");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					editionJSONArray.put(new JSONObject()
						.put("edition_value", result)
					);
				}
			}
		}
		
		// table_of_contents-1:table_of_contents_value
		// MARC 505 $a
		JSONArray tableOfContentsJSONArray = new JSONArray();
		dynamicFieldData.put("table_of_contents", tableOfContentsJSONArray);
		fields = betterMarcRecord.getDataFields("505");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
				String result = subfieldValue.trim();
				if( ! result.isEmpty() ) {
					tableOfContentsJSONArray.put(new JSONObject()
						.put("table_of_contents_value", result)
					);
				}
			}
		}
		
		// subject_topic-1:subject_topic_term.* 
		//MARC 650_0, all alphabetical subfields in the order that they appear
		JSONArray subjectTopicJSONArray = new JSONArray();
		dynamicFieldData.put("subject_topic", subjectTopicJSONArray);
		fields = betterMarcRecord.getDataFields("650");
		for(DataField field : fields) {
			String result = BetterMarcRecord.getDataFieldValueForAllSubfieldsInOrder(field, null, '0', "[a-z]", "--");
			if( result != null ) {
				subjectTopicJSONArray.put(new JSONObject()
					.put("subject_topic_term", new JSONObject()
						.put("value", result)							
					)
				);
			}
		}
		
		// subject_name-1:subject_name_term.* 
		//600 10, all alphabetical subfields in the order that they appear
		//610 10, all alphabetical subfields in the order that they appear
		//610 20, all alphabetical subfields in the order that they appear
		//611 20, all alphabetical subfields in the order that they appear
		JSONArray subjectNameJSONArray = new JSONArray();
		dynamicFieldData.put("subject_name", subjectNameJSONArray);
		fields = betterMarcRecord.getDataFields("600");
		for(DataField field : fields) {
			String result = BetterMarcRecord.getDataFieldValueForAllSubfieldsInOrder(field, '1', '0', "[a-z]", " ");
			if( result != null ) {
				subjectNameJSONArray.put(new JSONObject()
					.put("subject_name_term", new JSONObject()
						.put("value", result)
						.put("name_type", "personal")
					)
				);
			}
		}
		fields = betterMarcRecord.getDataFields("611");
		for(DataField field : fields) {
			String result = BetterMarcRecord.getDataFieldValueForAllSubfieldsInOrder(field, '2', '0', "[a-z]", " ");
			if( result != null ) {
				subjectNameJSONArray.put(new JSONObject()
					.put("subject_name_term", new JSONObject()
						.put("value", result)
						.put("name_type", "conference")
					)
				);
			}
		}
		fields = betterMarcRecord.getDataFields("610");
		for(DataField field : fields) {
			String result = BetterMarcRecord.getDataFieldValueForAllSubfieldsInOrder(field, '1', '0', "[a-z]", " ");
			if( result != null ) {
				subjectNameJSONArray.put(new JSONObject()
					.put("subject_name_term", new JSONObject()
						.put("value", result)
						.put("name_type", "corporate")
					)
				);
			}
		}
		fields = betterMarcRecord.getDataFields("610");
		for(DataField field : fields) {
			String result = BetterMarcRecord.getDataFieldValueForAllSubfieldsInOrder(field, '2', '0', "[a-z]", " ");
			if( result != null ) {
				subjectNameJSONArray.put(new JSONObject()
					.put("subject_name_term", new JSONObject()
						.put("value", result)
						.put("name_type", "corporate")
					)
				);
			}
		}
		
		// subject_title-1:subject_title_term.*
		// MARC 630 00 $a
		JSONArray subjectTitleJSONArray = new JSONArray();
		dynamicFieldData.put("subject_title", subjectTitleJSONArray);
		fields = betterMarcRecord.getDataFields("630");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, '0', '0', 'a')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					subjectTitleJSONArray.put(new JSONObject()
						.put("subject_title_term", new JSONObject()
							.put("value", result)
						)
					);
				}
			}
		}
		
		// subject_geographic-1:subject_geographic_term.*
		// MARC 651_0, all alphabetical subfields in the order that they appear
		JSONArray subjectGeographicJSONArray = new JSONArray();
		dynamicFieldData.put("subject_geographic", subjectGeographicJSONArray);
		fields = betterMarcRecord.getDataFields("651");
		for(DataField field : fields) {
			String result = BetterMarcRecord.getDataFieldValueForAllSubfieldsInOrder(field, null, '0', "[a-z]", "--");
			if( result != null ) {
				subjectGeographicJSONArray.put(new JSONObject()
					.put("subject_geographic_term", new JSONObject()
						.put("value", result)
					)
				);
			}
		}
		
		// coordinates-1:coordinates_value
		//MARC 034 $d,$e,$f,$g
		//MARC  255 $c
		//Note: 034 value is in the authority record of the place recorded in the 651.
		JSONArray coordinatesJSONArray = new JSONArray();
		dynamicFieldData.put("coordinates", coordinatesJSONArray);
		fields = betterMarcRecord.getDataFields("034");
		for(DataField field : fields) {
			String result = (
				StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'd'), ", ").trim() + "--" +
				StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'e'), ", ").trim() + "/" +
				StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'f'), ", ").trim() + "--" +
				StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'g'), ", ").trim()
			).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
			if( ! result.equals("--/--") ) {
				coordinatesJSONArray.put(new JSONObject()
					.put("coordinates_value", normalizeCoordinatesToDecimal(result))
				);
			}
		}
		fields = betterMarcRecord.getDataFields("255");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'c')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					coordinatesJSONArray.put(new JSONObject()
						.put("coordinates_value", normalizeCoordinatesToDecimal(result))
					);
				}
			}
		}
		
		// scale-1:scale_value
		// MARC 255 $a
		// MARC 034 $b,$c
		JSONArray scaleJSONArray = new JSONArray();
		dynamicFieldData.put("scale", scaleJSONArray);
		fields = betterMarcRecord.getDataFields("255");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					scaleJSONArray.put(new JSONObject()
						.put("scale_value", result)
					);
				}
			}
		}
		fields = betterMarcRecord.getDataFields("034");
		for(DataField field : fields) {
			String result = BetterMarcRecord.removeCommonTrailingCharacters(
					StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'b'), ", ").trim() + " " +
					StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'c'), ", ").trim()
				).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
			if( ! result.isEmpty() ) {
				scaleJSONArray.put(new JSONObject()
					.put("scale_value", result)
				);;
			}
		}
		
		//genre-1:genre_term.*
		//655, all alphabetical subfields in the order that they appear
		JSONArray genreJSONArray = new JSONArray();
		dynamicFieldData.put("genre", genreJSONArray);
		fields = betterMarcRecord.getDataFields("655");
		for(DataField field : fields) {
			String result = BetterMarcRecord.getDataFieldValueForAllSubfieldsInOrder(field, null, null, "[a-z]", "--");
			if( result != null ) {
				genreJSONArray.put(new JSONObject()
					.put("genre_term", new JSONObject()
						.put("value", result)
					)
				);
			}
		}
		
		//note-1:note_value
		//MARC 500 $a
		JSONArray noteJSONArray = new JSONArray();
		dynamicFieldData.put("note", noteJSONArray);
		fields = betterMarcRecord.getDataFields("500");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					noteJSONArray.put(new JSONObject()
						.put("note_value", result)
					);
				}
			}
		}
		
		//form-1:form_term.*
		// MARC leader, bytes 6 and 7
		JSONArray formJSONArray = new JSONArray();
		dynamicFieldData.put("form", formJSONArray);
		char byte6 = betterMarcRecord.getLeader().getTypeOfRecord();
		char[] bytes7and8 = betterMarcRecord.getLeader().getImplDefined1();
		char byte7 = bytes7and8[0];
		HashMap<Character, String> typeOfRecordCharactersToStrings = new HashMap<Character, String>();
		typeOfRecordCharactersToStrings.put('a', "Language material");
		typeOfRecordCharactersToStrings.put('c', "Notated music");
		typeOfRecordCharactersToStrings.put('d', "Manuscript notated music");
		typeOfRecordCharactersToStrings.put('e', "Cartographic material");
		typeOfRecordCharactersToStrings.put('f', "Manuscript cartographic material");
		typeOfRecordCharactersToStrings.put('g', "Projected medium");
		typeOfRecordCharactersToStrings.put('i', "Nonmusical sound recording");
		typeOfRecordCharactersToStrings.put('j', "Musical sound recording");
		typeOfRecordCharactersToStrings.put('k', "Two-dimensional nonprojectable graphic");
		typeOfRecordCharactersToStrings.put('m', "Computer file");
		typeOfRecordCharactersToStrings.put('o', "Kit");
		typeOfRecordCharactersToStrings.put('p', "Mixed materials");
		typeOfRecordCharactersToStrings.put('r', "Three-dimensional artifact or naturally occurring object");
		typeOfRecordCharactersToStrings.put('t', "Manuscript language material");
		HashMap<Character, String> bibliographicLevelCharactersToStrings = new HashMap<Character, String>();
		bibliographicLevelCharactersToStrings.put('a', "Monographic component part");
		bibliographicLevelCharactersToStrings.put('b', "Serial component part");
		bibliographicLevelCharactersToStrings.put('c', "Collection");
		bibliographicLevelCharactersToStrings.put('d', "Subunit");
		bibliographicLevelCharactersToStrings.put('i', "Integrating resource");
		bibliographicLevelCharactersToStrings.put('m', "Monograph/Item");
		bibliographicLevelCharactersToStrings.put('s', "Serial");
		String stringRepresentation = typeOfRecordCharactersToStrings.get(byte6) + ", " + bibliographicLevelCharactersToStrings.get(byte7);
		
		String formValue = "other";
		
		//Below we have a set of manually-selected mappings (agreed upon by Durst group)
		if(stringRepresentation.equals("Language material, Monograph/Item")) {
			formValue = "books";
		} else if(stringRepresentation.equals("Language material, Serial")) {
			formValue = "periodicals";
		} else if(stringRepresentation.equals("Cartographic material, Monograph/Item")) {
			formValue = "maps";
		} else if(stringRepresentation.equals("Manuscript language material, Monograph/Item")) {
			formValue = "books";
		} else if(stringRepresentation.equals("Two-dimensional nonprojectable graphic, Monograph/Item")) {
			formValue = "ephemera";
		} else if(stringRepresentation.equals("Language material, Collection")) {
			formValue = "ephemera";
		} else if(stringRepresentation.equals("Three-dimensional artifact or naturally occurring object, Monograph/Item")) {
			formValue = "objects";
		} else if(stringRepresentation.equals("Manuscript language material, Collection")) {
			formValue = "manuscripts";
		} else if(stringRepresentation.equals("Language material, Monographic component part")) {
			formValue = "books";
		} else if(stringRepresentation.equals("Notated music, Monograph/Item")) {
			formValue = "music";
		} else if(stringRepresentation.equals("Manuscript cartographic material, Monograph/Item")) {
			formValue = "maps";
		} else if(stringRepresentation.equals("Mixed materials, Collection")) {
			formValue = "ephemera"; 
		} else if(stringRepresentation.equals("Two-dimensional nonprojectable graphic, Collection")) {
			formValue = "ephemera"; 
		} else if(stringRepresentation.equals("Cartographic material, Monographic component part")) {
			formValue = "maps";
		} else if(stringRepresentation.equals("Language material, Serial component part")) {
			formValue = "periodicals";
		} else if(stringRepresentation.equals("Musical sound recording, Monograph/Item")) {
			formValue = "music";
			formValue = "sound recording";
		} else if(stringRepresentation.equals("Manuscript cartographic material, Monographic component part")) {
			formValue = "maps";
		}
		
	
		if(formValue.equals("albums")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm000229")
				)
			);
		} else if(formValue.equals("architectural drawings")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "gmgpc")
				.put("uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm000455")
				)
			);
		} else if(formValue.equals("books")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm001221")
				)
			);
		} else if(formValue.equals("caricatures and cartoons")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "lcsh")
				.put("uri", "http://id.loc.gov/authorities/subjects/sh99001244.html")
				)
			);
		} else if(formValue.equals("clippings")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "gmgpc")
				.put("uri", "http://www.loc.gov/pictures/collection/tgm/item/tgm002169/")
				)
			);
		} else if(formValue.equals("corporation reports")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://id.loc.gov/authorities/subjects/sh85032921.html")
				)
			);
		} else if(formValue.equals("correspondence")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://vocab.getty.edu/aat/300026877")
				)
			);
		} else if(formValue.equals("drawings")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "gmgpc")
				.put("uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm003279")
				)
			);
		} else if(formValue.equals("ephemera")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://vocab.getty.edu/aat/300028881")
				)
			);
		} else if(formValue.equals("filmstrips")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://vocab.getty.edu/aat/300028048")
				)
			);
		} else if(formValue.equals("illustrations")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "gmgpc")
				.put("uri", "http://www.loc.gov/pictures/collection/tgm/item/tgm005314/")
				)
			);
		} else if(formValue.equals("lantern slides")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://vocab.getty.edu/aat/300134977")
				)
			);
		} else if(formValue.equals("manuscripts")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "lcsh")
				.put("uri", "http://id.loc.gov/authorities/subjects/sh85080672.html")
				)
			);
		} else if(formValue.equals("maps")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm006261")
				)
			);
		} else if(formValue.equals("minutes (administrative records)")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://vocab.getty.edu/aat/300027440")
				)
			);
		} else if(formValue.equals("mixed materials")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://vocab.getty.edu/aat/300027440")
				)
			);
		} else if(formValue.equals("moving images")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://vocab.getty.edu/aat/300263857")
				)
			);
		} else if(formValue.equals("music")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "lcgf")
				.put("uri", "	http://id.loc.gov/authorities/genreForms/gf2014026952")
				)
			);
		} else if(formValue.equals("negatives")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "gmgpc")
				.put("uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm007029")
				)
			);
		} else if(formValue.equals("objects")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm007159")
				)
			);
		} else if(formValue.equals("oral histories")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "local")
				.put("uri", "http://id.loc.gov/authorities/subjects/sh2008025718.html")
				)
			);
		} else if(formValue.equals("other")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "local")
				.put("uri", "http://id.library.columbia.edu/term/64d4b967-f7fc-4229-9dbb-a72b6d68e83f")
				)
			);
		} else if(formValue.equals("paintings")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "gmgpc")
				.put("uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm007393")
				)
			);
		} else if(formValue.equals("pamphlets")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm007415")
				)
			);
		} else if(formValue.equals("papyri")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://vocab.getty.edu/resource/aat/300055047")
				)
			);
		} else if(formValue.equals("periodicals")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm007641")
				)
			);
		} else if(formValue.equals("photographs")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm007721")
				)
			);
		} else if(formValue.equals("playing cards")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm007907")
				)
			);
		} else if(formValue.equals("postcards")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm008103")
				)
			);
		} else if(formValue.equals("posters")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm008104")
				)
			);
		} else if(formValue.equals("printed ephemera")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://vocab.getty.edu/aat/300264821")
				)
			);
		} else if(formValue.equals("prints")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "gmgpc")
				.put("uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm008237")
				)
			);
		} else if(formValue.equals("record covers")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://vocab.getty.edu/aat/300247936")
				)
			);
		} else if(formValue.equals("scrapbooks")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm009266")
				)
			);
		} else if(formValue.equals("slides (photographs)")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://vocab.getty.edu/resource/aat/300128371")
				)
			);
		} else if(formValue.equals("sound recordings")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm009874")
				)
			);
		} else if(formValue.equals("video recordings")) {
			formJSONArray.put(new JSONObject()
				.put("form_term", new JSONObject()
				.put("value", formValue)
				.put("authority", "aat")
				.put("uri", "http://vocab.getty.edu/aat/300028682")
				)
			);
		}
		
		// extent-1:extent_value
		// MARC 300 $a, $b, $c
		JSONArray extentJSONArray = new JSONArray();
		dynamicFieldData.put("extent", noteJSONArray);
		fields = betterMarcRecord.getDataFields("300");
		for(DataField field : fields) {
			String result = (
					StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'a'), ", ").trim() + " " +
					StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'b'), ", ").trim() + " " +
					StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'c'), ", ").trim()
				).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
			if( ! result.isEmpty() ) {
				extentJSONArray.put(new JSONObject()
					.put("extent_value", result)
				);
			}
		}
		
		// series-1:series_value
		// MARC 490 0 $a
		// MARC 830 _0 $a
		JSONArray seriesJSONArray = new JSONArray();
		dynamicFieldData.put("series", noteJSONArray);
		fields = betterMarcRecord.getDataFields("490");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, '0', null, 'a')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					seriesJSONArray.put(new JSONObject()
						.put("series_title", result)
					);
				}
			}
		}
		fields = betterMarcRecord.getDataFields("830");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, '0', 'a')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					seriesJSONArray.put(new JSONObject()
						.put("series_title", result)
					);
				}
			}
		}
		
		//isbn-1:isbn_value
		// MARC 020 $a
		JSONArray isbnJSONArray = new JSONArray();
		dynamicFieldData.put("isbn", isbnJSONArray);
		fields = betterMarcRecord.getDataFields("020");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					isbnJSONArray.put(new JSONObject()
						.put("isbn_value", result)
					);
				}
			}
		}
		
		//issn-1:issn_value
		//MARC 022 $a
		JSONArray issnJSONArray = new JSONArray();
		dynamicFieldData.put("issn", issnJSONArray);
		fields = betterMarcRecord.getDataFields("022");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					issnJSONArray.put(new JSONObject()
						.put("issn_value", result)
					);
				}
			}
		}
		
		// url-1:url_value
		// url-1:url_display_label
		// MARC 856 $u
		JSONArray urlJSONArray = new JSONArray();
		dynamicFieldData.put("url", urlJSONArray);
		fields = betterMarcRecord.getDataFields("856");
		HashMap<String, String> url_value;
		for(DataField field : fields) {
			
			//For a given field, subfield $3 is the label for any URL in subfield $u.
			//Even though $u is technically repeatable, it makes sense to use the $3 value for all instances
			//because there would be no other way to match up multiple $3 values and $u values in a MARC
			//record if someone chose to put multiple values in for each.  So it someone put a $3 in, we
			//can only assume that it applies to all $u values in the same 856 field.
			ArrayList<String> subfield3 = BetterMarcRecord.getDataFieldValue(field, '4', '0', '3');
			String displayLabelResult = subfield3.size() == 0 ? null : subfield3.get(0);
			
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, '4', '0', 'u')) {
				url_value = new HashMap<String, String>();
				String urlValueResult = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! urlValueResult.isEmpty() ) {
					urlJSONArray.put(new JSONObject()
						.put("url_value", urlValueResult)
						.put("url_display_label", (displayLabelResult != null ? displayLabelResult : ""))
					);
				}
			}
		}

		// type_of_resource-1:type_of_resource_value
		// type_of_resource-1:type_of_resource_is_collection
		// MARC Leader byte 6
		JSONArray typeOfResourceJSONArray = new JSONArray();
		dynamicFieldData.put("type_of_resource", typeOfResourceJSONArray);
		JSONObject typeOfResourceJSONObject = new JSONObject();
		if(byte6 == 'a' || byte6 == 't') {
			typeOfResourceJSONObject.put("type_of_resource_value", "text");
		} else if(byte6 == 'e' || byte6 == 'f') {
			typeOfResourceJSONObject.put("type_of_resource_value", "cartographic");
		} else if(byte6 == 'c' || byte6 == 'd') {
			typeOfResourceJSONObject.put("type_of_resource_value", "notated music");
		} else if(byte6 == 'i') {
			typeOfResourceJSONObject.put("type_of_resource_value", "sound recording - nonmusical");
		} else if(byte6 == 'j') {
			typeOfResourceJSONObject.put("type_of_resource_value", "sound recording - musical");
		} else if(byte6 == 'k') {
			typeOfResourceJSONObject.put("type_of_resource_value", "still image");
		} else if(byte6 == 'g') {
			typeOfResourceJSONObject.put("type_of_resource_value", "moving image");
		} else if(byte6 == 'o') {
			typeOfResourceJSONObject.put("type_of_resource_value", "kit");
		} else if(byte6 == 'r') {
			typeOfResourceJSONObject.put("type_of_resource_value", "three dimensional object");
		} else if(byte6 == 'm') {
			typeOfResourceJSONObject.put("type_of_resource_value", "software, multimedia");
		} else if(byte6 == 'p') {
			typeOfResourceJSONObject.put("type_of_resource_value", "mixed material");
		}
		
		if(byte7 == 'c') {
			typeOfResourceJSONObject.put("type_of_resource_is_collection", "yes");
		}
		typeOfResourceJSONArray.put(typeOfResourceJSONObject);
		
		//System.out.println(dynamicFieldData.toString(2));
		
	}
	
	public void extractDataFromRawMarcHoldingsRecords(File[] rawMarcHoldingsRecords) throws JSONException {
		if(rawMarcHoldingsRecords.length == 0) {
			return;
		}
		
		for(File marcHoldingsFile : rawMarcHoldingsRecords) {
			try {
				FileInputStream fis = new FileInputStream(marcHoldingsFile);
				MarcReader reader = new MarcStreamReader(fis);
				while (reader.hasNext()) {
		            Record record = reader.next();
		            BetterMarcRecord holdingsRecord = new BetterMarcRecord(record);
		            
		            ArrayList<DataField> fields;
		            
		            //dims_identifier
		            //MARC Holdings 950 $a
		            JSONArray dimsIdentifierJSONArray = new JSONArray();
		    		dynamicFieldData.put("dims_identifier", dimsIdentifierJSONArray);
		            fields = holdingsRecord.getDataFields("950");
					for(DataField field : fields) {
						for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
							String result = BetterMarcRecord.removeCommonTrailingCharacters((
								subfieldValue
							));
							if( ! result.isEmpty() ) {
								dimsIdentifierJSONArray.put(new JSONObject()
									.put("dims_identifier_value", result)
								);
							}
						}
					}
					
					//location-1:location_enumeration_and_chronology (add to existing location-1, if present)
					//Holdings 866 41 $a
					JSONObject locationJSONObject = null;
					if(dynamicFieldData.has("location")) {
						locationJSONObject = dynamicFieldData.getJSONArray("location").getJSONObject(0);
					} else {
						locationJSONObject = new JSONObject();
						dynamicFieldData.put("location", new JSONArray().put(locationJSONObject));
					}
		            fields = holdingsRecord.getDataFields("866");
		            boolean foundValue = false;
					for(DataField field : fields) {
						if(foundValue) { break; }
						for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, '4', '1', 'a')) {
							if(foundValue) { break; }
							String result = BetterMarcRecord.removeCommonTrailingCharacters((
								subfieldValue
							));
							if( ! result.isEmpty() ) {
								locationJSONObject.put("location_enumeration_and_chronology", result);
								foundValue = true;
								//And foundValue to true so that we break out of this double loop. Enumeration and chronology is not a repeatable field in Hyacinth.
							}
						}
					}
					
		        }
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}								
		}
	}

	public JSONObject getDigitalObjectData() {
		return this.digitalObjectData;
	}
	
	private String get965DurstMarkerFromMarcRecord(BetterMarcRecord betterMarcRecord) {
		String normalized965MarkerValue = "";
		ArrayList<DataField> fields = betterMarcRecord.getDataFields("965");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
				String value = subfieldValue.toLowerCase().trim();
				if(value.endsWith("durst")) {
					normalized965MarkerValue = value;
				}
			}
		}
		return normalized965MarkerValue;
	}
	
	private ArrayList<String> getOcolc035ValuesFromMarcRecord(BetterMarcRecord betterMarcRecord) {
		ArrayList<String> values = new ArrayList<String>();
		//marc_identifier_035_ocolc_for_physical_records: ----- 035 $a, but only $a that contains "(OCoLC)" -----
		ArrayList<DataField> fields = betterMarcRecord.getDataFields("035");
		for(DataField field : fields) {
			List<Subfield> subfields = field.getSubfields('a');
			for(Subfield subfield : subfields) {
				Matcher ocolcValue = VALID_035_AND_776_FIELD_PATTERN.matcher(subfield.getData());
				if (ocolcValue.matches()) {
					String newVal = "(OCoLC)" + ocolcValue.group(4);
					if( ! values.contains(newVal) ) {
						values.add("(OCoLC)" + ocolcValue.group(4));
					}
				}
			}
		}
		return values;
	}
	
	private ArrayList<String> getOcolc776ValuesFromMarcRecord(BetterMarcRecord betterMarcRecord) {
		ArrayList<String> values = new ArrayList<String>();
		//marc_identifier_776_ocolc_linker_field_for_electronic_records: ----- 776 $w, but only $w that contains "(OCoLC)" -----
		ArrayList<DataField> fields = betterMarcRecord.getDataFields("776");
		for(DataField field : fields) {
			List<Subfield> subfields = field.getSubfields('w');
			for(Subfield subfield : subfields) {
				Matcher ocolcValue = VALID_035_AND_776_FIELD_PATTERN.matcher(subfield.getData());
				if (ocolcValue.matches()) {
					String newVal = "(OCoLC)" + ocolcValue.group(4);
					if( ! values.contains(newVal) ) {
						values.add(newVal);
					}
				}
			}
		}
		return values;
	}

	public boolean isElectronicRecord() {
		return this.isElectronicRecord;
	}
	
	public ArrayList<String> getOcolc035FieldValues() {
		return this.ocolc035FieldValues;
	}
	
	public ArrayList<String> getOcolc776FieldValues() {
		return this.ocolc776FieldValues;
	}
	
	public void setPid(String pid) {
		this.pid = pid;
	}
	
	public String getPid() {
		return this.pid;
	}

	/**
	 * Returns an error message if there's a merge problem, or null if no problems are encountered.
	 * @param printRecord
	 * @param electronicRecord
	 */
	public static String mergeElectronicRecordDataIntoPrintRecord(DurstRecord printRecord, ArrayList<DurstRecord> electronicRecordsToMergeIn) {
		// Safety check: Make sure that we're only merging electronic records into print records  
		if( printRecord.isElectronicRecord() ) { return "Supplied printRecord was actually an electronic record."; }
		for(DurstRecord electronicRecord : electronicRecordsToMergeIn) {
			if( !electronicRecord.isElectronicRecord() ) { return "One of supplied electronicRecordsToMergeIn was actually a print record."; }
		}
		
		// The records that we're merging should all have the same pid value, OR a
		// null pid value (for new records). If we find two different pids among
		// the set, something is wrong. This indicates that a prior merge
		// happened improperly, or that the current intended merge is incorrect.
		HashSet<String> pids = new HashSet<String>();
		if(printRecord.pid != null) {
			pids.add(printRecord.pid);
		}
		for(DurstRecord electronicRecord : electronicRecordsToMergeIn) {
			if(electronicRecord.pid != null) {
				pids.add(printRecord.pid);
			}
		}
		if(pids.size() > 1) {
			return "Tried to merge multiple records that already exist in Hyacinth. Pids: " + StringUtils.join(pids, ",") + ". You need to look at these records and possibly delete one of them (or do a manual merge and then delete). This was probably caused by the recent addition of a new record in Voyager (and even more likely, the creation of an electronic record before a print record).";
		}
		
		return null;
	}

	public String getMarc005Value() {
		try {
			if(this.dynamicFieldData.has("marc_005_last_modified") && this.dynamicFieldData.getJSONArray("marc_005_last_modified").length() > 0) {
				return this.dynamicFieldData.getJSONArray("marc_005_last_modified").getJSONObject(0).getString("marc_005_last_modified_value");
			}
		} catch (JSONException e) {
			DurstVoyagerHyacinthSync.logger.error(e.getClass().getName() + ": " + e.getMessage());
		}
		return "-1";
	}

	public ArrayList<String> getClioIdentifiers() {
		ArrayList<String> clioIdentifiers = new ArrayList<String>();
		
		try {
			if(this.dynamicFieldData.has("clio_identifier") && this.dynamicFieldData.getJSONArray("clio_identifier").length() > 0) {
				JSONArray jsonArr = this.dynamicFieldData.getJSONArray("clio_identifier");
				for(int i = 0; i < jsonArr.length(); i++) {
					clioIdentifiers.add(jsonArr.getJSONObject(0).getString("clio_identifier_value"));
				}
			}
		} catch (JSONException e) {
			DurstVoyagerHyacinthSync.logger.error(e.getClass().getName() + ": " + e.getMessage());
		}
				
		return clioIdentifiers;
	}
	
//	private static Pattern valid035And776FieldPattern = Pattern.compile("\\(OCoLC\\)(oc(m|n))*(0*)(\\d+)"); //including (0*) to remove leading zeros
//	
//	// Fields Related to Sync/Update/Storage
//	
//	private boolean isEDurstRecord = false; // true if 965 $a field == "eDurst"
//	private String pid = null;
//	
//	//Dynamic fields to store
//	private JSONObject dynamicFieldData;
//	
//	public ArrayList<String> marc_identifier_035_ocolc_for_physical_records = new ArrayList<String>(); //035 $a, but only $a that contains "(OCoLC)"
//	public ArrayList<String> marc_identifier_776_ocolc_linker_field_for_electronic_records = new ArrayList<String>(); //776 $w, but only $w that contains "(OCoLC)"
//	
//	public ArrayList<String> marc_005_last_modified = new ArrayList<String>(); //005 (no subfield)
//	public ArrayList<HashMap<String, String>> title = new ArrayList<HashMap<String, String>>(); //245 $a, $b, $n and $p.  Num nonsort chars are supplied by the second indicator. 
//	public ArrayList<String> alternative_title = new ArrayList<String>(); //246 $a (or 740 $a in older records)
//	public ArrayList<String> mods_abstract = new ArrayList<String>(); //520 $a
//	public ArrayList<HashMap<String, String>> name = new ArrayList<HashMap<String, String>>();	//First entry in 100 $a (personal) or 110 $a (corporate) or 111 $a (conference),
//																								//additional entries in 700 (personal) or 710 (corporate) or 711 (conference)
//	//public ArrayList<String> name_role = new ArrayList<String>(); // n/a
//	public ArrayList<String> publisher = new ArrayList<String>(); //260 $b or [264,second indicator = "1",$b]
//	public ArrayList<String> place_of_origin = new ArrayList<String>();  //260 $a or [264,second indicator = "1",$a]
//	
//	
//	//Date rules are complex.  All rules are in parsing section and based on the MARC header, field 008, bytes 6-14
//	//008 -- date1: Bytes 07-10 -- date2: Bytes 11-14 -- date type: Byte 06
//	public ArrayList<HashMap<String, String>> date_created_start_and_end_and_type_and_key_date = new ArrayList<HashMap<String, String>>();
//	public ArrayList<String> date_created_textual = new ArrayList<String>();
//	public ArrayList<HashMap<String, String>> date_other_start_and_end_and_type_and_key_date = new ArrayList<HashMap<String, String>>();
//	public ArrayList<String> date_other_textual = new ArrayList<String>();
//	public ArrayList<HashMap<String, String>> date_issued_start_and_end_and_type_and_key_date = new ArrayList<HashMap<String, String>>();
//	public ArrayList<String> date_issued_textual = new ArrayList<String>(); //260 $c or [264,second indicator = "1",$c]
//	
//	public ArrayList<String> edition = new ArrayList<String>(); //250 $a
//	public ArrayList<String> table_of_contents = new ArrayList<String>(); //505 $a
//	public ArrayList<String> subject_topic = new ArrayList<String>(); //650_0, all alphabetical subfields in the order that they appear
//	public boolean			 hasLctgmTermInteriors = false; //postcard spreadsheet
//	public ArrayList<HashMap<String,String>> subject_durst = new ArrayList<HashMap<String,String>>(); //From DIMS
//	//public ArrayList<String> subject_temporal = new ArrayList<String>(); //650_0 $y or 651_0 $y //UPDATE: NO LONGER USED
//	public ArrayList<HashMap<String, String>> subject_name = new ArrayList<HashMap<String, String>>(); //600 10, 611 20, 600 30, 610 10, 610 20, $a & $b
//	public ArrayList<HashMap<String, String>> spreadsheet_additional_subject_name = new ArrayList<HashMap<String, String>>(); //Subject Names from Geo Note spreadsheet data
//	public ArrayList<String> subject_title = new ArrayList<String>(); //630 00
//	public ArrayList<String> subject_geographic = new ArrayList<String>(); //651_0, all alphabetical subfields in the order that they appear
//	public ArrayList<String> spreadsheet_additional_subject_geographic = new ArrayList<String>(); //Geographic subjects from Geo Note spreadsheet data
//	
//	public ArrayList<HashMap<String,String>> subject_hierarchical_geographic = new ArrayList<HashMap<String, String>>(); // Spreadsheet, includes sub-fields like country, province, region, etc.
//	
//	public ArrayList<String> coordinates = new ArrayList<String>(); // 034 $d,$e,$f,$g or 255 $c.  Note: 034 value is in the authority record of the place recorded in the 651.
//	public ArrayList<String> spreadsheet_additional_coordinates = new ArrayList<String>(); //Coordinates from Geo Note spreadsheet data
//	public ArrayList<String> scale = new ArrayList<String>(); // 255 $a or 034 $b,$c
//	public ArrayList<String> genre = new ArrayList<String>(); //655
//	public ArrayList<String> view_direction = new ArrayList<String>(); // n/a
//	public ArrayList<String> note = new ArrayList<String>(); //500 $a
//	public ArrayList<HashMap<String, String>> form = new ArrayList<HashMap<String, String>>(); //form -- leader, bytes 6 and 7, converted to Hyacinth form values using manual ruleset
//	public ArrayList<String> extent = new ArrayList<String>(); //300 $a, $b, $c
//	public ArrayList<String> orientation = new ArrayList<String>(); // n/a
//	public ArrayList<String> series = new ArrayList<String>(); //490 0 or 830 _0
//	public ArrayList<String> box_title = new ArrayList<String>(); // n/a
//	public ArrayList<String> group_title = new ArrayList<String>(); // n/a
//	//public ArrayList<String> collection = new ArrayList<String>(); // n/a -- Hard-coded
//	public ArrayList<HashMap<String,String>> clio_identifier = new ArrayList<HashMap<String,String>>(); //001
//	public ArrayList<String> dims_identifier = new ArrayList<String>(); //Holdings 950
//	public ArrayList<String> durst_postcard_identifier = new ArrayList<String>(); // n/a
//	public ArrayList<String> cul_assigned_postcard_identifier = new ArrayList<String>(); // n/a
//	public ArrayList<String> filename_front_identifier = new ArrayList<String>(); // n/a
//	public ArrayList<String> filename_back_identifier = new ArrayList<String>(); // n/a
//	public ArrayList<String> isbn = new ArrayList<String>(); //020 $a
//	public ArrayList<String> issn = new ArrayList<String>(); //022 $a
//	public ArrayList<String> box_number = new ArrayList<String>(); // n/a
//	public ArrayList<String> item_number = new ArrayList<String>(); // n/a
//	public ArrayList<String> enumeration_and_chronology = new ArrayList<String>(); //Holdings 866 41 $a
//	public ArrayList<HashMap<String,String>> physical_location = new ArrayList<HashMap<String, String>>(); // n/a
//	public ArrayList<String> sublocation = new ArrayList<String>(); // n/a
//	public ArrayList<HashMap<String,String>> the_urls = new ArrayList<HashMap<String,String>>(); //856 $u, along with 856 $3 for display label
//	
//	public boolean durst_favorite = false; // From Durst favorite spreadsheet
//	public ArrayList<String> type_of_resource = new ArrayList<String>();
//	public ArrayList<String> type_of_resource_is_collection = new ArrayList<String>();
//	//public ArrayList<String> record_content_source = new ArrayList<String>(); //This is a hard-coded value in JSON export 
//	public ArrayList<String> record_origin = new ArrayList<String>();
//	public ArrayList<String> language_of_cataloging = new ArrayList<String>();
//	//public ArrayList<String> cul_scan_note = new ArrayList<String>(); // n/a
//	
//	
//	public DurstRecord() {
//		//Creates an empty record
//		dynamicFieldData = new JSONObject();
//	}
//	
//	public DurstRecord(File marcXmlBibRecord, String marcHoldingsDownloadDirectory, boolean setPidIfBibKeyExistsForSingleItemInHyacinth) {
//		String bibKey = FilenameUtils.removeExtension(marcXmlBibRecord.getName());
//		
//		try {
//			File possibleDirectoryForHoldings = new File(marcHoldingsDownloadDirectory + "/" + bibKey);
//			if(possibleDirectoryForHoldings.exists()) {
//				File[] marcHoldingsFiles = possibleDirectoryForHoldings.listFiles(new FilenameFilter() {
//				    public boolean accept(File dir, String name) {
//				        return name.toLowerCase().endsWith(".mrc");
//				    }
//				});
//				extractDataFromRawMarcHoldingsRecords(marcHoldingsFiles);
//			}
//			extractDataFromMarcXmlBibRecord(marcXmlBibRecord);
//		}
//		catch (JSONException e) {
//			e.printStackTrace();
//		}
//		
//		mergeInRelevantSpreadsheetData();
//		
//		if(setPidIfBibKeyExistsForSingleItemInHyacinth) {
//			setPidIfBibKeyExistsForSingleItemInHyacinth();
//		}
//	}
//	
//	
//	
//	
//	
//	private static void processDurstFavoriteSpreadsheet() {
//		InputStreamReader isr = new InputStreamReader(DurstRecord.class.getClassLoader().getResourceAsStream("durst_favorites_2013_10_25.csv"), Charsets.UTF_8);
//		CSVReader csvReader = new CSVReader(isr);
//		try {
//			List<String[]> csvLines = csvReader.readAll();
//			
//			//Skip first several lines, which have csv column labels and spacing
//			for(int i = 4; i < csvLines.size(); i++) {
//				
//				String[] singleLineOfData = csvLines.get(i);
//				//[0] == Possible DIMS id (I say "possible" because the first several lines need to be skipped before we actually get to the DIMS id part)
//				if( ! singleLineOfData[0].contains("DIM0") ) { continue; }
//				
//				if( ! singleLineOfData[0].trim().isEmpty() ) {
//					String dimsId = singleLineOfData[0].trim();
//					durstFavorites.add(dimsId);
//				}
//			}
//		} catch (IOException e) {
//			DDBSync.writeToLog("Error: IOException occurred during read of Durst csv file.", true, DDBSync.LOG_TYPE_ERROR);
//			e.printStackTrace();
//			System.exit(DDBSync.EXIT_CODE_ERROR);
//		}
//		try {
//			isr.close();
//			csvReader.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	private static void processDurstSubjectMappingMetadataSpreadsheet() {
//		InputStreamReader isr = new InputStreamReader(DurstRecord.class.getClassLoader().getResourceAsStream("durst_motm_mapping_metadata_2015_04_16.tsv"), Charsets.UTF_16);
//		CSVReader csvReader = new CSVReader(isr, '\t');
//		try {
//			List<String[]> csvLines = csvReader.readAll();
//			
//			//Skip first line, which has csv column labels
//			for(int i = 1; i < csvLines.size(); i++) {
//				
//				String[] singleLineOfData = csvLines.get(i);
//				
//				if( ! singleLineOfData[0].trim().isEmpty() ) {
//					rawDurstSubjectCodesToNormalizedDurstSubjectCodes.put(singleLineOfData[0].trim(), singleLineOfData[1].trim());
//					normalizedDurstSubjectCodesToDurstSubjectTerms.put(singleLineOfData[1].trim(), singleLineOfData[3].trim());
//				}
//			}
//		} catch (IOException e) {
//			DDBSync.writeToLog("Error: IOException occurred during read of Durst csv file.", true, DDBSync.LOG_TYPE_ERROR);
//			e.printStackTrace();
//			System.exit(DDBSync.EXIT_CODE_ERROR);
//		}
//		try {
//			isr.close();
//			csvReader.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	private static void processDurstLocationTermSupplementalDataSpreadsheet() {
//		InputStreamReader isr = new InputStreamReader(DurstRecord.class.getClassLoader().getResourceAsStream("durst_geo_note_conversion_and_location_data_2015_05_12_final.tsv"), Charsets.UTF_16);
//		CSVReader csvReader = new CSVReader(isr, '\t');
//		try {
//			List<String[]> csvLines = csvReader.readAll();
//			
//			//Skip first line, which has csv column labels
//			for(int i = 1; i < csvLines.size(); i++) {
//				
//				String[] singleLineOfData = csvLines.get(i);
//				
//				if( ! singleLineOfData[0].trim().isEmpty() ) {
//
//					HashMap<String, String> locationData = new HashMap<String, String>();
//					
//					String typeOfField = singleLineOfData[3];
//					//610 = Subject Name
//					//F610 = Fake Subject Name
//					//651 = Subject Geographic
//					//F651 = Fake Subject Geographic
//
//					
//					if(typeOfField.equals("610") || typeOfField.equals("F610")) {
//						//Subject Name
//						locationData.put("spreadsheet_additional_subject_name", singleLineOfData[4].trim());
//					} else if(typeOfField.equals("651") || typeOfField.equals("F651")) {
//						//Subject Geographic
//						locationData.put("spreadsheet_additional_subject_geographic", singleLineOfData[4].trim());
//					}
//					
//					locationData.put("street", singleLineOfData[5].trim());
//					locationData.put("city", singleLineOfData[6].trim());
//					locationData.put("state", singleLineOfData[7].trim());
//					locationData.put("zip_code", singleLineOfData[8].trim());
//					locationData.put("borough", singleLineOfData[9].trim());
//					locationData.put("neighborhood", singleLineOfData[10].trim());
//					locationData.put("latitude", singleLineOfData[12].trim());
//					locationData.put("longitude", singleLineOfData[13].trim());
//					
//					//Add raw geo note key
//					rawAndNormalizedGeoNotesToAssociatedLocationData.put(singleLineOfData[0].trim(), locationData);
//					//Also add normalized value key
//					rawAndNormalizedGeoNotesToAssociatedLocationData.put(singleLineOfData[4].trim(), locationData);
//				}
//			}
//		} catch (IOException e) {
//			DDBSync.writeToLog("Error: IOException occurred during read of Durst csv file.", true, DDBSync.LOG_TYPE_ERROR);
//			e.printStackTrace();
//			System.exit(DDBSync.EXIT_CODE_ERROR);
//		}
//		try {
//			isr.close();
//			csvReader.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	private static void processDimsDataSpreadsheet() {
//		InputStreamReader isr = new InputStreamReader(DurstRecord.class.getClassLoader().getResourceAsStream("dims_data_to_import_2015_01_28.tsv"), Charsets.UTF_16);
//		CSVReader csvReader = new CSVReader(isr, '\t');
//		try {
//			List<String[]> csvLines = csvReader.readAll();
//			
//			//Skip first line, which has csv column labels
//			for(int i = 1; i < csvLines.size(); i++) {
//				
//				String[] singleLineOfData = csvLines.get(i);
//				
//				if( ! singleLineOfData[3].trim().isEmpty() ) {
//					
//					String dimsId = singleLineOfData[3].trim();
//					
//					ArrayList<String> rawGeoNotes = new ArrayList<String>(Arrays.asList(singleLineOfData[8].trim().split("; *")));
//					rawGeoNotes.removeAll(Arrays.asList("")); //Remove blank values
//					
//					ArrayList<String> durstSubjectCodes = new ArrayList<String>();
//					durstSubjectCodes.add(singleLineOfData[47]); //mind_of_the_man_1
//					durstSubjectCodes.add(singleLineOfData[60]); //mind_of_the_man_2
//					durstSubjectCodes.add(singleLineOfData[61]); //mind_of_the_man_3
//					durstSubjectCodes.add(singleLineOfData[62]); //mind_of_the_man_4
//					durstSubjectCodes.add(singleLineOfData[63]); //mind_of_the_man_5
//					durstSubjectCodes.add(singleLineOfData[64]); //mind_of_the_man_6
//					durstSubjectCodes.add(singleLineOfData[65]); //mind_of_the_man_7
//					durstSubjectCodes.add(singleLineOfData[66]); //mind_of_the_man_8
//					durstSubjectCodes.removeAll(Arrays.asList("")); //Remove blank values
//					
//					dimsIDsToRawGeoNotes.put(dimsId, rawGeoNotes);
//					
//					ArrayList<String> normalizedSubjectCodes = new ArrayList<String>();
//					for(String singleDurstSubjectCode : durstSubjectCodes) {
//						normalizedSubjectCodes.add(rawDurstSubjectCodesToNormalizedDurstSubjectCodes.containsKey(singleDurstSubjectCode) ? rawDurstSubjectCodesToNormalizedDurstSubjectCodes.get(singleDurstSubjectCode) : singleDurstSubjectCode);
//					}
//					dimsIDsToNormalizedDurstSubjectCodes.put(dimsId, normalizedSubjectCodes);
//					
//				}
//			}
//		} catch (IOException e) {
//			DDBSync.writeToLog("Error: IOException occurred during read of Durst favorites csv file.", true, DDBSync.LOG_TYPE_ERROR);
//			e.printStackTrace();
//			System.exit(DDBSync.EXIT_CODE_ERROR);
//		}
//		try {
//			isr.close();
//			csvReader.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//	}
//	
//	private static void processNonDimsGeoDataSpreadsheet() {
//		InputStreamReader isr = new InputStreamReader(DurstRecord.class.getClassLoader().getResourceAsStream("non_dims_geo_data_to_merge_in_2014_12_10.tsv"), Charsets.UTF_16);
//		CSVReader csvReader = new CSVReader(isr, '\t');
//		try {
//			List<String[]> csvLines = csvReader.readAll();
//			
//			//Skip first line, which has csv column labels
//			for(int i = 1; i < csvLines.size(); i++) {
//				
//				String[] singleLineOfData = csvLines.get(i);
//				
//				if( ! singleLineOfData[0].trim().isEmpty() ) {
//					
//					String clioId = singleLineOfData[0].trim();
//					
//					ArrayList<String> rawGeoNotes = new ArrayList<String>(Arrays.asList(singleLineOfData[1].trim().split("; *")));
//					rawGeoNotes.removeAll(Arrays.asList("")); //Remove blank values
//					clioIDsToRawGeoNotes.put(clioId, rawGeoNotes);
//				}
//			}
//		} catch (IOException e) {
//			DDBSync.writeToLog("Error: IOException occurred during read of Durst favorites csv file.", true, DDBSync.LOG_TYPE_ERROR);
//			e.printStackTrace();
//			System.exit(DDBSync.EXIT_CODE_ERROR);
//		}
//		try {
//			isr.close();
//			csvReader.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	/**
//	 * If this record's bib key is found in Hyacinth (and that bib key is associated with only one record),
//	 * set the pid on the local record.
//	 * If more than one Hyacinth record is found for this bib key, that means that this record has been merged
//	 * into multiple other records, so we'll leave the pid blank in preparation for the subsequent merge.
//	 */
//	public void setPidIfBibKeyExistsForSingleItemInHyacinth() {
//		
//		if(this.clio_identifier.size() == 0) {
//			return;
//		}
//		
//		String bibKey = this.clio_identifier.get(0).get("value");
//		
//		String url = DDBSync.hyacinthAppUrl + "/digital_objects/search.json";
//		
//		try {
//			HttpClient client = HttpClientBuilder.create().build();
//			HttpPost post = new HttpPost(url);
//		 
//			String authDigest = new String(Base64.encodeBase64((DDBSync.hyacinthAppLoginEmail + ":" + DDBSync.hyacinthAppLoginPassword).getBytes()));
//			post.setHeader("Authorization", "Basic " + authDigest);
//		 
//			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
//			urlParameters.add(new BasicNameValuePair("_method", "POST"));
//			urlParameters.add(new BasicNameValuePair("search[search_field]", "df_clio_identifier_value_sim"));
//			urlParameters.add(new BasicNameValuePair("search[q]", bibKey));
//			urlParameters.add(new BasicNameValuePair("search[f][project_string_key_sim][]", "durst")); //Limit scope to Durst project
//			urlParameters.add(new BasicNameValuePair("facet", "false")); //No need to facet. Better performance.
//		 
//			post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
//			HttpResponse response = client.execute(post);
//			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//			StringBuffer resultSb = new StringBuffer();
//			String line = "";
//			while ((line = rd.readLine()) != null) {
//				resultSb.append(line);
//			}
//			String result = resultSb.toString();
//			
//			JSONObject jsonResponse = new JSONObject(result);
//			JSONArray results = jsonResponse.getJSONArray("results");
//			int numResults = results.length();
//			
//			if(numResults == 0) {
//				return; // No Hyacinth records found.  No pid to set for this record.
//			} else if(numResults == 1) {
//				this.pid = results.getJSONObject(0).getString("pid"); //Found a Hyacinth pid!  We'll set it for this record.
//				return;
//			} else {
//				return; // Found multiple Hyacinth records with this pid, indicating that it was merged into other records.  Don't set it for this record.
//			}
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClientProtocolException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		DDBSync.writeToLog("Error encountered while trying to get PID for bib key: " + bibKey, true, DDBSync.LOG_TYPE_ERROR);
//		System.exit(DDBSync.EXIT_CODE_ERROR);
//	}
//	
//	/**
//	 * If this record's cul_assigned_postcard_identifier_value key is found in Hyacinth (and that value is associated with only one record),
//	 * set the pid on the local record.
//	 * If more than one Hyacinth record is found for this cul_assigned_postcard_identifier_value, that means that this record has been merged
//	 * into multiple other records, so we'll leave the pid blank in preparation for the subsequent merge.
//	 */
//	public void setPidIfCulPostcardIdentifierExistsForSingleItemInHyacinth() {
//		
//		if(this.cul_assigned_postcard_identifier.size() == 0) {
//			return;
//		}
//		
//		String culPostcardIdentifier = this.cul_assigned_postcard_identifier.get(0);
//		
//		String url = DDBSync.hyacinthAppUrl + "/digital_objects/search.json";
//		
//		try {
//			HttpClient client = HttpClientBuilder.create().build();
//			HttpPost post = new HttpPost(url);
//		 
//			String authDigest = new String(Base64.encodeBase64((DDBSync.hyacinthAppLoginEmail + ":" + DDBSync.hyacinthAppLoginPassword).getBytes()));
//			post.setHeader("Authorization", "Basic " + authDigest);
//		 
//			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
//			urlParameters.add(new BasicNameValuePair("_method", "POST"));
//			urlParameters.add(new BasicNameValuePair("search[fq][df_cul_assigned_postcard_identifier_value_sim][0][equals]", culPostcardIdentifier));
//			urlParameters.add(new BasicNameValuePair("search[f][project_string_key_sim][]", "durst")); //Limit scope to Durst project
//			urlParameters.add(new BasicNameValuePair("facet", "false")); //No need to facet. Better performance.
//		 
//			post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
//			HttpResponse response = client.execute(post);
//			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//			StringBuffer resultSb = new StringBuffer();
//			String line = "";
//			while ((line = rd.readLine()) != null) {
//				resultSb.append(line);
//			}
//			String result = resultSb.toString();
//			
//			JSONObject jsonResponse = new JSONObject(result);
//			JSONArray results = jsonResponse.getJSONArray("results");
//			int numResults = results.length();
//			
//			if(numResults == 0) {
//				return; // No Hyacinth records found.  No pid to set for this record.
//			} else if(numResults == 1) {
//				this.pid = results.getJSONObject(0).getString("pid"); //Found a Hyacinth pid!  We'll set it for this record.
//				return;
//			} else {
//				//This should never happen.  If it does, something is terribly wrong, and we definitely want to throw an error.
//				DDBSync.writeToLog("Did not expect to get more than one search result for CUL Postcard Identifier lookup: " + culPostcardIdentifier + ".  Got " + numResults + ".", true, DDBSync.LOG_TYPE_ERROR);
//				System.exit(DDBSync.EXIT_CODE_ERROR);
//			}
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClientProtocolException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		DDBSync.writeToLog("Error encountered while trying to get PID for CUL Postcard Identifier: " + culPostcardIdentifier, true, DDBSync.LOG_TYPE_ERROR);
//		System.exit(DDBSync.EXIT_CODE_ERROR);
//	}
//	
//	public boolean recordMarc005ValueIsDifferentThanHyacinthValue() {
//		
//		if(this.pid == null) {
//			//If this is a new record without a pid, then there is no existing value in Hyacinth.  Just return true.
//			return true;
//		}
//		
//		String url = DDBSync.hyacinthAppUrl + "/digital_objects/search.json";
//		
//		try {
//			HttpClient client = HttpClientBuilder.create().build();
//			HttpPost post = new HttpPost(url);
//		 
//			String authDigest = new String(Base64.encodeBase64((DDBSync.hyacinthAppLoginEmail + ":" + DDBSync.hyacinthAppLoginPassword).getBytes()));
//			post.setHeader("Authorization", "Basic " + authDigest);
//		 
//			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
//			urlParameters.add(new BasicNameValuePair("_method", "POST"));
//			urlParameters.add(new BasicNameValuePair("search[search_field]", "pid"));
//			urlParameters.add(new BasicNameValuePair("search[q]", this.pid));
//			urlParameters.add(new BasicNameValuePair("search[f][project_string_key_sim][]", "durst")); //Limit scope to Durst project
//			urlParameters.add(new BasicNameValuePair("facet", "false")); //No need to facet. Better performance.
//			urlParameters.add(new BasicNameValuePair("search[fq][df_marc_005_last_modified_value_sim][0][equals]", this.marc_005_last_modified.get(0))); //Attempt to find match for existing 005 update time
//		 
//			post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
//			HttpResponse response = client.execute(post);
//			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//			StringBuffer resultSb = new StringBuffer();
//			String line = "";
//			while ((line = rd.readLine()) != null) {
//				resultSb.append(line);
//			}
//			String result = resultSb.toString();
//			
//			JSONObject jsonResponse = new JSONObject(result);
//			JSONArray results = jsonResponse.getJSONArray("results");
//			int numResults = results.length();
//			if(numResults == 0) {
//				return true; //Did not find result with same 005 modified time.  This means that there IS a difference. 
//			} else if(numResults == 1) {
//				return false; //Found result with same 005 modified time.  This means no difference. 
//			} else {
//				//This should never happen.  If it does, something is terribly wrong, and we definitely want to throw an error.
//				DDBSync.writeToLog("Did not expect to get more than one search result for PID lookup: " + this.pid + ".  Got " + numResults + ".", true, DDBSync.LOG_TYPE_ERROR);
//				System.exit(DDBSync.EXIT_CODE_ERROR);				
//			}
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClientProtocolException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		DDBSync.writeToLog("Error encountered while trying to get Marc 005 last modified value for PID: " + this.pid, true, DDBSync.LOG_TYPE_ERROR);
//		System.exit(DDBSync.EXIT_CODE_ERROR);
//		
//		return true;
//	}
//	
//	public void mergeInRelevantSpreadsheetData() {
//		
//		if(this.dims_identifier.size() == 0 && this.clio_identifier.size() == 0) {
//			//Merges are based on DIMS ID or CLIO ID.  If neither is available, there's nothing to do.
//			return;
//		}
//		
//		// Things to merge in:
//		// - Durst favorite indicator
//		// - Durst subjects (codes and full terms)
//		// - Geo Notes (that go into subject_geographic)
//		
//		String dimsId = null;
//		if(this.dims_identifier.size() > 0) {
//			dimsId = this.dims_identifier.get(0);
//		}
//		
//		String clioId = null;
//		if(this.clio_identifier.size() > 0) {
//			clioId = this.clio_identifier.get(0).get("value");
//		}
//		
//		//Favorite
//		if(dimsId != null && durstFavorites.contains(dimsId)) {
//			this.durst_favorite = true;
//		}
//		
//		//Durst subjects
//		if(dimsId != null && dimsIDsToNormalizedDurstSubjectCodes.containsKey(dimsId)) {
//			for(String normalizedSubjectCode : dimsIDsToNormalizedDurstSubjectCodes.get(dimsId)) {
//				HashMap<String,String> valueAndCodePair = new HashMap<String,String>();
//				valueAndCodePair.put("value", normalizedDurstSubjectCodesToDurstSubjectTerms.containsKey(normalizedSubjectCode) ? normalizedDurstSubjectCodesToDurstSubjectTerms.get(normalizedSubjectCode) : "");
//				valueAndCodePair.put("code", normalizedSubjectCode);
//				subject_durst.add(valueAndCodePair);							
//			}
//		}
//		
//		//Durst geo notes (to be merged into subject_geographic, subject_topic and other geo-related fields)
//		ArrayList<String> allRelevantRawGeoNotes = new ArrayList<String>();
//		
//		if(dimsId != null && dimsIDsToRawGeoNotes.containsKey(dimsId)) {
//			allRelevantRawGeoNotes.addAll(dimsIDsToRawGeoNotes.get(dimsId));
//		}
//		if(clioId != null && clioIDsToRawGeoNotes.containsKey(clioId)) {
//			allRelevantRawGeoNotes.addAll(clioIDsToRawGeoNotes.get(clioId));
//		}
//		
//		//And because we also want to match CLIO subjects to geo note date, we'll be checking for that too
//		//Verify that this record has a CLIO ID
//		if(clioId != null) {
//			for(String subjectGeographic : this.subject_geographic) {
//				for(String subjectGeographicSubstring : subjectGeographic.split("--")) {
//					//Subjects may be separated by "--", so we need to split on that and check against each
//					if(rawAndNormalizedGeoNotesToAssociatedLocationData.containsKey(subjectGeographicSubstring)) {
//						allRelevantRawGeoNotes.add(subjectGeographicSubstring);
//					}
//				}
//			}
//			for(HashMap<String, String> subjectName : this.subject_name) {
//				for(String subjectNameSubstring : subjectName.get("value").split("--")) {
//					//Subjects may be separated by "--", so we need to split on that and check against each
//					if(rawAndNormalizedGeoNotesToAssociatedLocationData.containsKey(subjectNameSubstring)) {
//						allRelevantRawGeoNotes.add(subjectNameSubstring);
//					}
//				}
//			}
//		}
//		
//		
//		for(String rawGeoNote : allRelevantRawGeoNotes) {
//			
//			if(rawAndNormalizedGeoNotesToAssociatedLocationData.containsKey(rawGeoNote)) {
//				HashMap<String,String> associatedLocationData = rawAndNormalizedGeoNotesToAssociatedLocationData.get(rawGeoNote);
//				
//				if(associatedLocationData.containsKey("spreadsheet_additional_subject_geographic")){
//					this.spreadsheet_additional_subject_geographic.add(associatedLocationData.get("spreadsheet_additional_subject_geographic"));
//				}
//				if(associatedLocationData.containsKey("spreadsheet_additional_subject_name")){
//					HashMap<String, String> newSubjectName = new HashMap<String, String>();
//					newSubjectName.put("value", associatedLocationData.get("spreadsheet_additional_subject_name"));
//					newSubjectName.put("type", "corporate"); //The names from the spreadsheet are corporate names because they have associated coordinates
//					this.spreadsheet_additional_subject_name.add(newSubjectName);
//				}
//				
//				HashMap<String,String> newHierarchicalGeographic = new HashMap<String,String>();
//				newHierarchicalGeographic.put("street", associatedLocationData.get("street"));
//				newHierarchicalGeographic.put("city", associatedLocationData.get("city"));
//				newHierarchicalGeographic.put("state", associatedLocationData.get("state"));
//				newHierarchicalGeographic.put("zip_code", associatedLocationData.get("zip_code"));
//				newHierarchicalGeographic.put("borough", associatedLocationData.get("borough"));
//				newHierarchicalGeographic.put("neighborhood", associatedLocationData.get("neighborhood"));
//				if( ! hierarchicalGeographicListAlreadyContainsThisValue(newHierarchicalGeographic) ) {
//					this.subject_hierarchical_geographic.add(newHierarchicalGeographic);
//				}
//				
//				this.spreadsheet_additional_coordinates.add(associatedLocationData.get("latitude") + ", " + associatedLocationData.get("longitude"));
//			}
//		}
//	}
//	
//	public boolean hierarchicalGeographicListAlreadyContainsThisValue(HashMap<String,String> newHierarchicalGeographic) {
//		boolean foundMatchingValue = false;
//		
//		for(HashMap<String,String> existingHierarchicalGeographic : this.subject_hierarchical_geographic) {
//			boolean foundNonMatch = false;
//			for(Entry<String,String> entry: existingHierarchicalGeographic.entrySet()) {
//				if( ! newHierarchicalGeographic.get(entry.getKey()).equals(entry.getValue())) {
//					foundNonMatch = true;
//					break;
//				}
//			}
//			if( ! foundNonMatch ) {
//				foundMatchingValue = true;
//				break;
//			}
//		}
//		
//		return foundMatchingValue;
//	}
//	
//	/**
//	 * Returns an error message if there's a merge problem, or null if no problems are encountered.
//	 * @param physicalRecord
//	 * @param electronicRecordsToMergeIntoPhysical
//	 * @return
//	 */
//	public static String mergeElectronicRecordIntoPhysical(DurstRecord physicalRecord, ArrayList<DurstRecord> electronicRecordsToMergeIntoPhysical) {
//		
//		//Use ALL information from the physical record as a base.
//		//Only merging in the some fields from the electronic record.
//		
//		String lastSeenPid = physicalRecord.pid; 
//		
//		for(DurstRecord eDurstRecord : electronicRecordsToMergeIntoPhysical) {
//			
//			// The records that we're merging should have
//			// the same pid value, OR a null pid value for new records.
//			// If we find two different pids among the set, something is wrong.
//			// This indicates that a prior merge happened improperly, or that
//			// the current intended merge is incorrect.
//			if(lastSeenPid == null) {
//				lastSeenPid = eDurstRecord.pid;
//			} else {
//				if( eDurstRecord.pid != null && ! lastSeenPid.equals(eDurstRecord.pid) ) {
//					return "Error: Tried to merge two different records that already exist in Hyacinth. Pid " + lastSeenPid + " and " + eDurstRecord.pid + ".  You need to check these records out, and possibly delete one of them (or do a manual merge and then delete).  This was probably caused by the recent addition of a new record in Voyager.";
//				}
//			}
//
//			
//			// Merge marc_005_last_modified, using the latest (alphabetically sorted) value.  Note: Last modified string format: "19940223151047.0" (i.e. February 23, 1994, 3:10:47 P.M. (15:10:47)
//			if(eDurstRecord.marc_005_last_modified.get(0).compareTo(physicalRecord.marc_005_last_modified.get(0)) > 0) {
//				physicalRecord.marc_005_last_modified = eDurstRecord.marc_005_last_modified;
//			}
//			
//			//Mark as favorite if any of the electronic records are favorites
//			if( ! physicalRecord.durst_favorite && eDurstRecord.durst_favorite) {
//				physicalRecord.durst_favorite = true;
//			}
//			
//			
//			physicalRecord.marc_identifier_776_ocolc_linker_field_for_electronic_records.addAll(eDurstRecord.marc_identifier_776_ocolc_linker_field_for_electronic_records);
//			//Make unique (other fields will be made unique during json export, but we may use the 776 before then)
//			physicalRecord.marc_identifier_776_ocolc_linker_field_for_electronic_records = new ArrayList<String>(new HashSet<String>(physicalRecord.marc_identifier_776_ocolc_linker_field_for_electronic_records));  
//			
//			//Easy fields to merge
//			physicalRecord.clio_identifier.addAll(eDurstRecord.clio_identifier);
//			physicalRecord.the_urls.addAll(eDurstRecord.the_urls);
//			physicalRecord.dims_identifier.addAll(eDurstRecord.dims_identifier);
//			physicalRecord.subject_durst.addAll(eDurstRecord.subject_durst);
//			
//			//Need to merge geo–note-related fields because records may get this data from external spreadsheets
//			physicalRecord.spreadsheet_additional_subject_geographic.addAll(eDurstRecord.spreadsheet_additional_subject_geographic);
//			physicalRecord.spreadsheet_additional_subject_name.addAll(eDurstRecord.spreadsheet_additional_subject_name);
//			physicalRecord.spreadsheet_additional_coordinates.addAll(eDurstRecord.spreadsheet_additional_coordinates);
//			physicalRecord.subject_hierarchical_geographic.addAll(eDurstRecord.subject_hierarchical_geographic);
//		}
//		
//		//Doing this below so that if an existing electronic record is merged with a new physical record, the merged record re-uses the existing pid rather than creating a new pid when it's uploaded.
//		physicalRecord.pid = lastSeenPid;
//		
//		//Return null if there were no problems.
//		return null;
//	}
//	
//	public String getDynamicFieldDataJSON() throws JSONException {
//		
//		JSONObject jsonObject = new JSONObject();
//		JSONArray newArr;
//		
//		
//		newArr = new JSONArray();
//		jsonObject.put("marc_005_last_modified", newArr);
//		for(String val : marc_005_last_modified) {
//			newArr.put(
//				new JSONObject().put(
//					"marc_005_last_modified_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("title", newArr);
//		for(HashMap<String, String> val : title) {
//			newArr.put(
//			new JSONObject().put(
//					"title_non_sort_portion", val.get("non_sort")
//				).put(
//					"title_sort_portion", val.get("sort")
//				)
//			);
//		}
//	
//		newArr = new JSONArray();
//		jsonObject.put("alternative_title", newArr);
//		for(String val : alternative_title) {
//			newArr.put(
//				new JSONObject().put(
//					"alternative_title_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("mods_abstract", newArr);
//		for(String val : mods_abstract) {
//			newArr.put(
//				new JSONObject().put(
//					"mods_abstract_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("name", newArr);
//		//Do main entry set first
//		for(HashMap<String, String> val : name) {
//			//
//			if(val.containsKey("marc_field") && (val.get("marc_field").equals("100") || val.get("marc_field").equals("110") || val.get("marc_field").equals("111")))
//			newArr.put(
//				new JSONObject().put(
//					"name_value", val.get("value")
//				).put(
//					"name_usage_primary", true //A name from a 1XX field is a primary (i.e. "main entry") field
//				).put(
//					"name_type", (val.get("marc_field").equals("100") ? "personal" : (val.get("marc_field").equals("110") ? "corporate" : "conference"))
//				)
//			);
//		}
//		//Do other names second
//		for(HashMap<String, String> val : name) {
//			if(val.containsKey("marc_field") && (val.get("marc_field").equals("700") || val.get("marc_field").equals("710") || val.get("marc_field").equals("711")))
//			newArr.put(
//				new JSONObject().put(
//					"name_value", val.get("value")
//				).put(
//					"name_type", (val.get("marc_field").equals("700") ? "personal" : (val.get("marc_field").equals("710") ? "corporate" : "conference"))
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("publisher", newArr);
//		for(String val : new LinkedHashSet<String>(publisher)) {
//			newArr.put(
//				new JSONObject().put(
//					"publisher_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("place_of_origin", newArr);
//		for(String val : new LinkedHashSet<String>(place_of_origin)) {
//			newArr.put(
//				new JSONObject().put(
//					"place_of_origin_value", val
//				)
//			);
//		}
//		
//		//date_created_textual
//		newArr = new JSONArray();
//		jsonObject.put("date_created_textual", newArr);
//		for(String val : date_created_textual) {
//			newArr.put(
//				new JSONObject().put(
//					"date_created_textual_value", val
//				)
//			);
//		}
//		
//		//date_other_textual
//		newArr = new JSONArray();
//		jsonObject.put("date_other_textual", newArr);
//		for(String val : date_other_textual) {
//			newArr.put(
//				new JSONObject().put(
//					"date_other_textual_value", val
//				)
//			);
//		}
//		
//		//date_issued_textual
//		newArr = new JSONArray();
//		jsonObject.put("date_issued_textual", newArr);
//		for(String val : date_issued_textual) {
//			newArr.put(
//				new JSONObject().put(
//					"date_issued_textual_value", val
//				)
//			);
//		}
//		
//		//date_created_start_and_end_and_type
//		newArr = new JSONArray();
//		jsonObject.put("date_created", newArr);
//		for(HashMap<String, String> val : date_created_start_and_end_and_type_and_key_date) {
//			JSONObject dateDataObject = new JSONObject(); 
//			newArr.put(dateDataObject);
//			
//			if(val.containsKey("start")) {
//				dateDataObject.put(
//					"date_created_start_value", val.get("start")
//				);
//			}
//			if(val.containsKey("end")) {
//				dateDataObject.put(
//					"date_created_end_value", val.get("end")
//				);
//			}
//			if(val.containsKey("type")) {
//				dateDataObject.put(
//					"date_created_type", val.get("type")
//				);
//			}
//			if(val.containsKey("key_date")) {
//				dateDataObject.put(
//					"date_created_key_date", Boolean.parseBoolean(val.get("key_date"))
//				);
//			}
//		}
//		
//		//date_other_start_and_end_and_type
//		newArr = new JSONArray();
//		jsonObject.put("date_other", newArr);
//		for(HashMap<String, String> val : date_other_start_and_end_and_type_and_key_date) {
//			JSONObject dateDataObject = new JSONObject(); 
//			newArr.put(dateDataObject);
//			
//			if(val.containsKey("start")) {
//				dateDataObject.put(
//					"date_other_start_value", val.get("start")
//				);
//			}
//			if(val.containsKey("end")) {
//				dateDataObject.put(
//					"date_other_end_value", val.get("end")
//				);
//			}
//			if(val.containsKey("type")) {
//				dateDataObject.put(
//					"date_other_type", val.get("type")
//				);
//			}
//			if(val.containsKey("key_date")) {
//				dateDataObject.put(
//					"date_other_key_date", Boolean.parseBoolean(val.get("key_date"))
//				);
//			}
//		}
//		
//		//date_issued_start_and_end_and_type
//		newArr = new JSONArray();
//		jsonObject.put("date_issued", newArr);
//		for(HashMap<String, String> val : date_issued_start_and_end_and_type_and_key_date) {
//			JSONObject dateDataObject = new JSONObject(); 
//			newArr.put(dateDataObject);
//			
//			if(val.containsKey("start")) {
//				dateDataObject.put(
//					"date_issued_start_value", val.get("start")
//				);
//			}
//			if(val.containsKey("end")) {
//				dateDataObject.put(
//					"date_issued_end_value", val.get("end")
//				);
//			}
//			if(val.containsKey("type")) {
//				dateDataObject.put(
//					"date_issued_type", val.get("type")
//				);
//			}
//			if(val.containsKey("key_date")) {
//				dateDataObject.put(
//					"date_issued_key_date", Boolean.parseBoolean(val.get("key_date"))
//				);
//			}
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("edition", newArr);
//		for(String val : new LinkedHashSet<String>(edition)) {
//			newArr.put(
//				new JSONObject().put(
//					"edition_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("table_of_contents", newArr);
//		for(String val : table_of_contents) {
//			newArr.put(
//				new JSONObject().put(
//					"table_of_contents_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("subject_topic", newArr);
//		for(String val : new LinkedHashSet<String>(subject_topic)) {
//			newArr.put(
//				new JSONObject().put(
//					"subject_topic_value", val
//				)
//			);
//		}
//		
//		//Comes from postcards
//		if(hasLctgmTermInteriors) {
//			newArr = new JSONArray();
//			jsonObject.put("subject_topic", newArr);
//			
//			newArr.put(
//				new JSONObject().put(
//					"subject_topic_value", "Interiors"
//				).put(
//					"subject_topic_authority", "lctgm"
//				)
//			);
//			
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("subject_durst", newArr);
//		for(HashMap<String, String> val : subject_durst) {
//			newArr.put(
//				new JSONObject().put(
//					"subject_durst_value", val.get("value")
//				).put(
//					"subject_durst_code", val.get("code")
//				)
//			);
//		}
//		
//		//No longer using subject temporal
////		newArr = new JSONArray();
////		jsonObject.put("subject_temporal", newArr);
////		for(String val : new LinkedHashSet<String>(subject_temporal)) {
////			newArr.put(
////				new JSONObject().put(
////					"subject_temporal_value", val
////				)
////			);
////		}
//		
//		ArrayList<String> subjectNameValuesSeenSoFar = new ArrayList<String>();
//		newArr = new JSONArray();
//		jsonObject.put("subject_name", newArr);
//		for(HashMap<String, String> val : subject_name) {
//			
//			newArr.put(
//				new JSONObject().put(
//					"subject_name_value", val.get("value")
//				).put(
//					"subject_name_type", val.get("type")
//				)
//			);
//			
//			subjectNameValuesSeenSoFar.add(val.get("value"));
//		}
//		for(HashMap<String, String> val : spreadsheet_additional_subject_name) {
//			
//			//Avoid duplicate values in additional subject name field
//			if(subjectNameValuesSeenSoFar.contains(val.get("value"))) {
//				continue;
//			}
//			newArr.put(
//				new JSONObject().put(
//					"subject_name_value", val.get("value")
//				).put(
//					"subject_name_type", val.get("type")
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("subject_title", newArr);
//		for(String val : new LinkedHashSet<String>(subject_title)) {
//			newArr.put(
//				new JSONObject().put(
//					"subject_title_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("subject_geographic", newArr);
//		for(String val : new LinkedHashSet<String>(subject_geographic)) {
//			newArr.put(
//				new JSONObject().put(
//					"subject_geographic_value", val
//				).put(
//					"subject_geographic_source", "clio"
//				)
//			);
//			
//			//If the value also exists in spreadsheet_additional_subject_geographic, then remove it from spreadsheet_additional_subject_geographic
//			if(spreadsheet_additional_subject_geographic.contains(val)) {
//				spreadsheet_additional_subject_geographic.remove(val);
//			}
//		}
//		for(String val : new LinkedHashSet<String>(spreadsheet_additional_subject_geographic)) {
//			newArr.put(
//				new JSONObject().put(
//					"subject_geographic_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("subject_hierarchical_geographic", newArr);
//		for(HashMap<String,String> val : subject_hierarchical_geographic) {
//
//			JSONObject hierarchicalGeographicObject = new JSONObject(); 
//			newArr.put(hierarchicalGeographicObject);
//			
//			if(val.containsKey("country")) {
//				JSONObject hierarchicalGeographicCountry = new JSONObject();
//				hierarchicalGeographicObject.put("subject_hierarchical_geographic_country", new JSONArray().put(hierarchicalGeographicCountry));
//				hierarchicalGeographicCountry.put("subject_hierarchical_geographic_country_value", val.get("country"));
//			}
//			if(val.containsKey("province")) {
//				JSONObject hierarchicalGeographicProvince = new JSONObject();
//				hierarchicalGeographicObject.put("subject_hierarchical_geographic_province", new JSONArray().put(hierarchicalGeographicProvince));
//				hierarchicalGeographicProvince.put("subject_hierarchical_geographic_province_value", val.get("province"));
//			}
//			if(val.containsKey("region")) {
//				JSONObject hierarchicalGeographicRegion = new JSONObject();
//				hierarchicalGeographicObject.put("subject_hierarchical_geographic_region", new JSONArray().put(hierarchicalGeographicRegion));
//				hierarchicalGeographicRegion.put("subject_hierarchical_geographic_region_value", val.get("region"));
//			}
//			if(val.containsKey("state")) {
//				JSONObject hierarchicalGeographicState = new JSONObject();
//				hierarchicalGeographicObject.put("subject_hierarchical_geographic_state", new JSONArray().put(hierarchicalGeographicState));
//				hierarchicalGeographicState.put("subject_hierarchical_geographic_state_value", val.get("state"));
//			}
//			if(val.containsKey("county")) {
//				JSONObject hierarchicalGeographicCounty = new JSONObject();
//				hierarchicalGeographicObject.put("subject_hierarchical_geographic_county", new JSONArray().put(hierarchicalGeographicCounty));
//				hierarchicalGeographicCounty.put("subject_hierarchical_geographic_county_value", val.get("county"));
//			}
//			if(val.containsKey("borough")) {
//				JSONObject hierarchicalGeographicBorough = new JSONObject();
//				hierarchicalGeographicObject.put("subject_hierarchical_geographic_borough", new JSONArray().put(hierarchicalGeographicBorough));
//				hierarchicalGeographicBorough.put("subject_hierarchical_geographic_borough_value", val.get("borough"));
//			}
//			if(val.containsKey("city")) {
//				JSONObject hierarchicalGeographicCity = new JSONObject();
//				hierarchicalGeographicObject.put("subject_hierarchical_geographic_city", new JSONArray().put(hierarchicalGeographicCity));
//				hierarchicalGeographicCity.put("subject_hierarchical_geographic_city_value", val.get("city"));
//			}
//			if(val.containsKey("neighborhood")) {
//				JSONObject hierarchicalGeographicNeighborhood = new JSONObject();
//				hierarchicalGeographicObject.put("subject_hierarchical_geographic_neighborhood", new JSONArray().put(hierarchicalGeographicNeighborhood));
//				hierarchicalGeographicNeighborhood.put("subject_hierarchical_geographic_neighborhood_value", val.get("neighborhood"));
//			}
//			if(val.containsKey("zip_code")) {
//				JSONObject hierarchicalGeographicZipCode = new JSONObject();
//				hierarchicalGeographicObject.put("subject_hierarchical_geographic_zip_code", new JSONArray().put(hierarchicalGeographicZipCode));
//				hierarchicalGeographicZipCode.put("subject_hierarchical_geographic_zip_code_value", val.get("zip_code"));
//			}
//			if(val.containsKey("street")) {
//				JSONObject hierarchicalGeographicStreet = new JSONObject();
//				hierarchicalGeographicObject.put("subject_hierarchical_geographic_street", new JSONArray().put(hierarchicalGeographicStreet));
//				hierarchicalGeographicStreet.put("subject_hierarchical_geographic_street_value", val.get("street"));
//			}
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("coordinates", newArr);
//		for(String val : coordinates) {
//			newArr.put(
//				new JSONObject().put(
//					"coordinates_value", val
//				).put(
//					"coordinates_source", "clio"
//				)
//			);
//			//If the value also exists in spreadsheet_additional_coordinates, then remove it from spreadsheet_additional_coordinates
//			if(spreadsheet_additional_coordinates.contains(val)) {
//				spreadsheet_additional_coordinates.remove(val);
//			}
//		}
//		for(String val : new LinkedHashSet<String>(spreadsheet_additional_coordinates)) {
//			newArr.put(
//				new JSONObject().put(
//					"coordinates_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("scale", newArr);
//		for(String val : new LinkedHashSet<String>(scale)) {
//			newArr.put(
//				new JSONObject().put(
//					"scale_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("genre", newArr);
//		for(String val : new LinkedHashSet<String>(genre)) {
//			newArr.put(
//				new JSONObject().put(
//					"genre_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("note", newArr);
//		for(String val : note) {
//			newArr.put(
//				new JSONObject().put(
//					"note_value", val
//				)
//			);
//		}
//		//View direction also goes into the note field with type="view direction"
//		for(String val : new LinkedHashSet<String>(view_direction)) {
//			newArr.put(
//				new JSONObject().put(
//					"note_value", val
//				).put(
//					"note_type", "view direction"
//				)
//			);
//		}
//		
//		
//		//form
//		newArr = new JSONArray();
//		jsonObject.put("form", newArr);
//		for(HashMap<String, String> val : form) {
//			JSONObject newForm = new JSONObject();
//			newArr.put(newForm);
//			if(val.containsKey("value")) { newForm.put("form_value", val.get("value")); }
//			if(val.containsKey("uri")) { newForm.put("form_value_uri", val.get("uri")); }
//			if(val.containsKey("authority")) { newForm.put("form_authority", val.get("authority")); }
//			if(val.containsKey("authority_uri")) { newForm.put("form_authority_uri", val.get("authority_uri")); }
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("extent", newArr);
//		for(String val : new LinkedHashSet<String>(extent)) {
//			newArr.put(
//				new JSONObject().put(
//					"extent_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("orientation", newArr);
//		for(String val : new LinkedHashSet<String>(orientation)) {
//			newArr.put(
//				new JSONObject().put(
//					"orientation_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("series", newArr);
//		for(String val : new LinkedHashSet<String>(series)) {
//			newArr.put(
//				new JSONObject().put(
//					"series_title", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("box_title", newArr);
//		for(String val : new LinkedHashSet<String>(box_title)) {
//			newArr.put(
//				new JSONObject().put(
//					"box_title_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("group_title", newArr);
//		for(String val : new LinkedHashSet<String>(group_title)) {
//			newArr.put(
//				new JSONObject().put(
//					"group_title_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("collection", newArr);
//		newArr.put(
//			new JSONObject().put(
//				"collection_value", "Seymour B. Durst Old York Library Collection"
//			)
//		);
//		
//		newArr = new JSONArray();
//		jsonObject.put("clio_identifier", newArr);
//		for(HashMap<String,String> val : clio_identifier) {
//			newArr.put(
//				new JSONObject().put(
//					"clio_identifier_value", val.get("value")
//				).put(
//					"clio_identifier_type", val.get("type")
//				).put(
//					"clio_identifier_omit_from_mods", val.get("type").equals("electronic")
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("dims_identifier", newArr);
//		for(String val : dims_identifier) {
//			newArr.put(
//				new JSONObject().put(
//					"dims_identifier_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("durst_postcard_identifier", newArr);
//		for(String val : durst_postcard_identifier) {
//			newArr.put(
//				new JSONObject().put(
//					"durst_postcard_identifier_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("cul_assigned_postcard_identifier", newArr);
//		for(String val : cul_assigned_postcard_identifier) {
//			newArr.put(
//				new JSONObject().put(
//					"cul_assigned_postcard_identifier_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("filename_front_identifier", newArr);
//		for(String val : filename_front_identifier) {
//			newArr.put(
//				new JSONObject().put(
//					"filename_front_identifier_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("filename_back_identifier", newArr);
//		for(String val : filename_back_identifier) {
//			newArr.put(
//				new JSONObject().put(
//					"filename_back_identifier_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("isbn", newArr);
//		for(String val : isbn) {
//			newArr.put(
//				new JSONObject().put(
//					"isbn_value", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("issn", newArr);
//		for(String val : issn) {
//			newArr.put(
//				new JSONObject().put(
//					"issn_value", val
//				)
//			);
//		}
//		
//		///// START - Location Data /////
//		
//		JSONArray locationArr = new JSONArray();
//		jsonObject.put("location", locationArr);
//		JSONObject location = new JSONObject();
//		locationArr.put(location);
//		
//			JSONArray physicalLocationArr = new JSONArray();
//			location.put("location_physical_location", physicalLocationArr);
//			for(HashMap<String, String> val : physical_location) {
//				JSONObject newPhysicalLocation = new JSONObject();
//				physicalLocationArr.put(newPhysicalLocation);
//				if(val.containsKey("value")) { newPhysicalLocation.put("location_physical_location_value", val.get("value")); }
//				if(val.containsKey("code")) { newPhysicalLocation.put("location_physical_location_code", val.get("code")); }
//				if(val.containsKey("uri")) { newPhysicalLocation.put("location_physical_location_uri", val.get("uri")); }
//				if(val.containsKey("authority")) { newPhysicalLocation.put("location_physical_location_authority", val.get("authority")); }
//				if(val.containsKey("authority_uri")) { newPhysicalLocation.put("location_physical_location_authority_uri", val.get("authority_uri")); }
//			}
//			
//			JSONArray urlArr = new JSONArray();
//			location.put("location_url", urlArr);
//			for(HashMap<String,String> val : the_urls) {
//				JSONObject urlVal = new JSONObject();
//				urlVal.put("location_url_value", val.get("value"));
//				if(val.containsKey("display_label")) {
//					urlVal.put("location_url_display_label", val.get("display_label"));
//				}
//				urlArr.put(urlVal);
//			}
//		
//			///// START - Holding Data /////
//			
//			JSONArray holdingArr = new JSONArray();
//			location.put("location_holding", holdingArr);
//			if(enumeration_and_chronology.size() > 0) {
//				//This is a Voyager record, since we're only getting Enumeration and Chronology from Voyager
//				//Create a holding record for each enumeration_and_chronology value
//				
//				for(String val : new LinkedHashSet<String>(enumeration_and_chronology)) {
//					JSONObject newHolding = new JSONObject();
//					holdingArr.put(newHolding);
//					JSONArray enumAndChronArr = new JSONArray();
//					newHolding.put("location_holding_enumeration_and_chronology", enumAndChronArr);
//					enumAndChronArr.put(
//						new JSONObject().put(
//							"location_holding_enumeration_and_chronology_value", val
//						)
//					);
//				}
//				
//			} else if(sublocation.size() > 0 || box_number.size() > 0 || item_number.size() > 0) {
//				//This is a Postcard spreadsheet record, since we're only getting sublocation/box_number/item_number from the Postcard spreadsheet
//				//Create one combined holding record for both the sublocation and shelf location.  We expect only one value for each, but we're collecting all just in case.
//				
//				JSONObject newHolding = new JSONObject();
//				holdingArr.put(newHolding);
//				JSONArray sublocationArr = new JSONArray();
//				JSONArray shelfLocationArr = new JSONArray();
//				newHolding.put("location_holding_sublocation", sublocationArr);
//				newHolding.put("location_holding_shelf_location", shelfLocationArr);
//				
//				sublocationArr.put(
//					new JSONObject().put(
//						"location_holding_sublocation_value", sublocation.get(0)
//					)
//				);
//				
//				shelfLocationArr.put(
//					new JSONObject().put(
//						"location_holding_shelf_location_box_number", this.box_number.get(0)
//					).put(
//						"location_holding_shelf_location_item_number", this.item_number.get(0)
//					)
//				);
//				
//			} else {
//				//Nothing to do because there's no holding-related data.
//			}
//			
//			///// END - Holding Data /////
//		///// END - Location Data /////	
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		newArr = new JSONArray();
//		jsonObject.put("durst_favorite", newArr);
//		newArr.put(
//			new JSONObject().put(
//				"durst_favorite_value", durst_favorite
//			)
//		);
//		
//		
//		newArr = new JSONArray();
//		jsonObject.put("type_of_resource", newArr);
//		for(String val : type_of_resource) {
//			newArr.put(
//				new JSONObject().put(
//					"type_of_resource", val
//				).put(
//					"type_of_resource_is_collection", (type_of_resource_is_collection.size() > 0 && type_of_resource_is_collection.get(0).equals("yes"))
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("record_content_source", newArr);
//		//Hard-coded value
//		newArr.put(
//			new JSONObject().put(
//				"record_content_source_value", "NNC"
//			)
//		);
//		
//		newArr = new JSONArray();
//		jsonObject.put("record_origin", newArr);
//		for(String val : record_origin) {
//			newArr.put(
//				new JSONObject().put(
//					"record_origin", val
//				)
//			);
//		}
//		
//		newArr = new JSONArray();
//		jsonObject.put("language_of_cataloging", newArr);
//		for(String val : language_of_cataloging) {
//			newArr.put(
//				new JSONObject().put(
//					"language_of_cataloging_value", "eng"
//				)
//			);
//		}
//		
//		return jsonObject.toString();
//	}
//	
//	public void extractDataFromMarcXmlBibRecord(File marcXmlBibRecordFile) throws JSONException {
//		
//		ArrayList<DataField> fields = null;
//		
//		try {
//			//Get MARC record bibliographic data
//			FileInputStream fis = new FileInputStream(marcXmlBibRecordFile);
//			MarcXmlReader reader = new MarcXmlReader(fis);
//			if (reader.hasNext()) {
//				Record bibRecord = reader.next();
//				BetterMarcRecord betterMarcRecord = new BetterMarcRecord(bibRecord);
//				
//				// is_edurst_record ----- one of the 965 $a fields == "965edurst" (with case insensitive check)
//				// otherwise the field will include "965durst" (with case insensitive check)
//				String normalized965MarkerValue = "";
//				fields = betterMarcRecord.getDataFields("965");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
//						String value = subfieldValue.toLowerCase().trim();
//						if(value.endsWith("durst")) {
//							normalized965MarkerValue = value;
//						}
//					}
//				}
//				
//				//marc_identifier_776_ocolc_linker_field_for_electronic_records and marc_identifier_035_ocolc_for_physical_records
//				if(normalized965MarkerValue.equals("965edurst")) {
//					this.isEDurstRecord = true;
//					
//					//marc_identifier_776_ocolc_linker_field_for_electronic_records: ----- 776 $w, but only $w that contains "(OCoLC)" -----
//					fields = betterMarcRecord.getDataFields("776");
//					for(DataField field : fields) {
//						List<Subfield> subfields = field.getSubfields('w');
//						for(Subfield subfield : subfields) {
//							Matcher ocolcValue = valid035And776FieldPattern.matcher(subfield.getData());
//							if (ocolcValue.matches()) {
//								String newVal = "(OCoLC)" + ocolcValue.group(4);
//								if( ! marc_identifier_776_ocolc_linker_field_for_electronic_records.contains(newVal) ) {
//									marc_identifier_776_ocolc_linker_field_for_electronic_records.add(newVal);
//								}
//							}
//						}
//					}
//					
//				} else {
//					//marc_identifier_035_ocolc_for_physical_records: ----- 035 $a, but only $a that contains "(OCoLC)" -----
//					fields = betterMarcRecord.getDataFields("035");
//					for(DataField field : fields) {
//						List<Subfield> subfields = field.getSubfields('a');
//						for(Subfield subfield : subfields) {
//							Matcher ocolcValue = valid035And776FieldPattern.matcher(subfield.getData());
//							if (ocolcValue.matches()) {
//								String newVal = "(OCoLC)" + ocolcValue.group(4);
//								if( ! marc_identifier_035_ocolc_for_physical_records.contains(newVal) ) {
//									marc_identifier_035_ocolc_for_physical_records.add("(OCoLC)" + ocolcValue.group(4));
//								}
//							}
//						}
//					}
//				}
//				
//				//clio_identifier = new ArrayList<String>(); //001
//				HashMap <String,String> clioIdData = new HashMap<String,String>();
//				clioIdData.put("value", betterMarcRecord.getControlField("001").getData().trim());
//				clioIdData.put("type", this.isEDurstRecord ? "electronic" : "print");
//				this.clio_identifier.add(clioIdData);
//				
//				//marc_005_last_modified -- 005
//				marc_005_last_modified.add(betterMarcRecord.getControlField("005").getData().trim());
//				
//				// title: ----- 245 $a,$b,$n,$p -- indicator 2 for num nonsort chars ----- 
//				fields = betterMarcRecord.getDataFields("245");
//				for(DataField field : fields) {
//					int numNonSortCharacters = Integer.parseInt(""+field.getIndicator2());
//					String result = BetterMarcRecord.removeCommonTrailingCharacters(
//						StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'a'), ", ").trim() + " " +
//						StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'b'), ", ").trim() + " " +
//						StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'n'), ", ").trim() + " " +
//						StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'p'), ", ").trim()
//					).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
//					if( ! result.isEmpty() ) {
//						HashMap<String, String> title_value = new HashMap<String, String>();
//						title_value.put("non_sort", result.substring(0, numNonSortCharacters).trim());
//						title_value.put("sort", result.substring(numNonSortCharacters).trim());
//						this.title.add(title_value);
//					}
//				}
//				
//				// alternative title: ----- 246 $a (or 740 $a in older records)
//				// Since mods_title_info_type_alternative is normally 246, but 740 in older records, we'll check 740 if we don't already have a value for mods_title_info_type_alternative
//				fields = betterMarcRecord.getDataFields("246");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
//						String result = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! result.isEmpty() ) {
//							this.alternative_title.add(result);
//						}
//					}
//				}
//				fields = betterMarcRecord.getDataFields("740");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
//						String result = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! result.isEmpty() ) {
//							this.alternative_title.add(result);
//						}
//					}
//				}
//				
//				//mods_abstract ----- //520 $a
//				fields = betterMarcRecord.getDataFields("520");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
//						String result = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! result.isEmpty() ) {
//							this.mods_abstract.add(result);
//						}
//					}
//				}
//				
//				//name ----- 	First entry in 100 $a,$b,$c,$d (personal) or 110 $a,$b,$c,$d (corporate) or 111 $a,$b,$c,$d (conference),
//				//				additional entries in 700 $a,$b,$c,$d (personal) or 710 $a,$b,$c,$d (corporate) or 711 $a,$b,$c,$d (conference)
//				HashMap<String, String> name_value;
//				String[] nameFields = {"100", "110", "111", "700", "710", "711"};
//				for(String marcFieldNumberTag : nameFields) {
//					fields = betterMarcRecord.getDataFields(marcFieldNumberTag);
//					for(DataField field : fields) {
//						
////						if(this.clio_identifier.get(0).get("value").equals("10922021")) {
////							System.out.println("$a: " + StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'a')));
////							System.out.println("$e: " + StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'e')));
////							System.exit(1);
////						}
//						
//						//Special rule: If the marcFieldNumberTag is "700" and $e value is "former owner" and $a value starts with "Durst, Seymour", skip this name
//						if(marcFieldNumberTag.equals("700")) {
//							boolean formerOwnerValueFound = false;
//							boolean skipThisName = false;
//							
//							for(String aSubfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'e')) {
//								if (aSubfieldValue.startsWith("former owner")) {
//									formerOwnerValueFound = true;					
//								}
//							}
//							if(formerOwnerValueFound) {
//								for(String aSubfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
//									if (aSubfieldValue.startsWith("Durst, Seymour")) {
//										skipThisName = true;					
//									}
//								}
//							}
//							if(skipThisName) {
//								continue;
//							}
//						}
//						
//						name_value = new HashMap<String, String>();
//						String result = BetterMarcRecord.removeCommonTrailingCharacters(
//								StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'a'), ", ").trim() + " " +
//								StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'b'), ", ").trim() + " " +
//								StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'c'), ", ").trim() + " " +
//								StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'd'), ", ").trim()
//						).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
//						if( ! result.isEmpty() ) {
//							name_value.put("value", result);
//							name_value.put("marc_field", marcFieldNumberTag);
//							this.name.add(name_value);
//						}
//					}					
//				}
//
//				//publisher ----- 260 $b or 264_1 $b
//				fields = betterMarcRecord.getDataFields("260");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'b')) {
//						String result = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! result.isEmpty() ) {
//							this.publisher.add(result);
//						}
//					}
//				}
//				fields = betterMarcRecord.getDataFields("264");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, '1', 'b')) {
//						String result = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! result.isEmpty() ) {
//							this.publisher.add(result);
//						}
//					}
//				}
//				
//				// place_of_origin ----- 260 $a or 264_1 $a
//				fields = betterMarcRecord.getDataFields("260");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
//						String result = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! result.isEmpty() ) {
//							this.place_of_origin.add(result);
//						}
//					}
//				}
//				fields = betterMarcRecord.getDataFields("264");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, '1', 'a')) {
//						String result = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! result.isEmpty() ) {
//							this.place_of_origin.add(result);
//						}
//					}
//				}
//				
//				// date_other_textual ----- 260 $c or 264_1 $c
//				fields = betterMarcRecord.getDataFields("260");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'c')) {
//						String result = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! result.isEmpty() ) {
//							this.date_other_textual.add(result);
//						}
//					}
//				}
//				fields = betterMarcRecord.getDataFields("264");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, '1', 'c')) {
//						String result = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! result.isEmpty() ) {
//							this.date_other_textual.add(result);
//						}
//					}
//				}
//				
//				//date_other_start_and_end_and_type ----- start: 008, Byte 07-10 -- end: 008, Byte 11-14, type: Byte 06
//				HashMap<String, String> start_and_end_and_type_and_keydate = new HashMap<String, String>();
//				String typeOfDate = betterMarcRecord.getControlField("008").getData().substring(6, 7).trim();
//				String date1 = betterMarcRecord.getControlField("008").getData().substring(7, 11).trim();
//				String date2 = betterMarcRecord.getControlField("008").getData().substring(11, 15).trim();
//				
//				//Agreed upon handling for only these specific date types for now:
//				
//				if( typeOfDate.equals("c") || typeOfDate.equals("d") || typeOfDate.equals("i") || typeOfDate.equals("k") || typeOfDate.equals("m") ) {
//					start_and_end_and_type_and_keydate.put("start", date1);
//					start_and_end_and_type_and_keydate.put("end", date2);
//					start_and_end_and_type_and_keydate.put("key_date", "true");
//					date_issued_start_and_end_and_type_and_key_date.add(start_and_end_and_type_and_keydate);
//				} else if(typeOfDate.equals("e")) {
//					start_and_end_and_type_and_keydate.put("start", date1);
//					start_and_end_and_type_and_keydate.put("key_date", "true");
//					date_issued_start_and_end_and_type_and_key_date.add(start_and_end_and_type_and_keydate);
//				} else if(typeOfDate.equals("n")) {
//					start_and_end_and_type_and_keydate.put("start", date1); //"n" means unknown date: we expect date1 to equal "uuuu" (via catalog value)
//					start_and_end_and_type_and_keydate.put("end", date2); //"n" means unknown date: we expect date2 to equal "uuuu" (via catalog value)
//					start_and_end_and_type_and_keydate.put("key_date", "true");
//					date_issued_start_and_end_and_type_and_key_date.add(start_and_end_and_type_and_keydate);
//				} else if(typeOfDate.equals("p")) {
//					//date2 is always a single dateCreated date
//					start_and_end_and_type_and_keydate.put("start", date2);
//					start_and_end_and_type_and_keydate.put("key_date", "true");
//					date_created_start_and_end_and_type_and_key_date.add(start_and_end_and_type_and_keydate);
//					
//					if( ! date1.equals(date2) ) {
//						//If these dates are equal, then date1 is a single dateIssued date
//						start_and_end_and_type_and_keydate = new HashMap<String,String>();
//						start_and_end_and_type_and_keydate.put("start", date1);
//						date_issued_start_and_end_and_type_and_key_date.add(start_and_end_and_type_and_keydate);
//					}
//				} else if(typeOfDate.equals("q")) {
//					start_and_end_and_type_and_keydate.put("start", date1);
//					start_and_end_and_type_and_keydate.put("end", date2);
//					start_and_end_and_type_and_keydate.put("type", "questionable");
//					start_and_end_and_type_and_keydate.put("key_date", "true");
//					date_issued_start_and_end_and_type_and_key_date.add(start_and_end_and_type_and_keydate);
//				} else if(typeOfDate.equals("r")) {
//					start_and_end_and_type_and_keydate.put("start", date2); //Note: date2 is indeed the start date that we want here
//					start_and_end_and_type_and_keydate.put("key_date", "true");
//					date_created_start_and_end_and_type_and_key_date.add(start_and_end_and_type_and_keydate);
//				} else if(typeOfDate.equals("s")) {
//					start_and_end_and_type_and_keydate.put("start", date1);
//					start_and_end_and_type_and_keydate.put("key_date", "true");
//					date_issued_start_and_end_and_type_and_key_date.add(start_and_end_and_type_and_keydate);
//				} else if(typeOfDate.equals("t")) {
//					start_and_end_and_type_and_keydate.put("start", date1);
//					start_and_end_and_type_and_keydate.put("key_date", "true");
//					date_issued_start_and_end_and_type_and_key_date.add(start_and_end_and_type_and_keydate);
//				} else if(typeOfDate.equals("u")) {
//					start_and_end_and_type_and_keydate.put("start", date1); //"u" means continuing resource with unknown date: we expect date1 to equal something like "19uu" (via catalog value)
//					start_and_end_and_type_and_keydate.put("end", date2); //"u" means continuing resource with unknown date: we expect date2 to equal "uuuu" (via catalog value)
//					start_and_end_and_type_and_keydate.put("key_date", "true");
//					date_issued_start_and_end_and_type_and_key_date.add(start_and_end_and_type_and_keydate);
//				}
//				
//				// edition ----- 250 $a
//				fields = betterMarcRecord.getDataFields("250");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
//						String result = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! result.isEmpty() ) {
//							this.edition.add(result);
//						}
//					}
//				}
//				
//				// table_of_contents ----- 505 $a
//				fields = betterMarcRecord.getDataFields("505");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
//						String result = subfieldValue.trim();
//						if( ! result.isEmpty() ) {
//							this.table_of_contents.add(result);
//						}
//					}
//				}
//				
//				// subject_topic ----- 
//				//650_0, all alphabetical subfields in the order that they appear
//				fields = betterMarcRecord.getDataFields("650");
//				for(DataField field : fields) {
//					String result = BetterMarcRecord.getDataFieldValueForAllSubfieldsInOrder(field, null, '0', "[a-z]", "--");
//					if( result != null ) {
//						this.subject_topic.add(result);
//					}
//				}
//
//				//No longer using subject temporal
////				// subject_temporal ----- 
////				//650_0 $y
////				//651_0 $y
////				fields = betterMarcRecord.getDataFields("650");
////				for(DataField field : fields) {
////					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, '0', 'y')) {
////						String result = BetterMarcRecord.removeCommonTrailingCharacters((
////							subfieldValue
////						));
////						if( ! result.isEmpty() ) {
////							this.subject_temporal.add(result);
////						}
////					}
////				}
////				fields = betterMarcRecord.getDataFields("651");
////				for(DataField field : fields) {
////					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, '0', 'y')) {
////						String result = BetterMarcRecord.removeCommonTrailingCharacters((
////							subfieldValue
////						));
////						if( ! result.isEmpty() ) {
////							this.subject_temporal.add(result);
////						}
////					}
////				}
//				
//				// subject_name ----- 
//				//600 10, all alphabetical subfields in the order that they appear
//				//610 10, all alphabetical subfields in the order that they appear
//				//610 20, all alphabetical subfields in the order that they appear
//				//611 20, all alphabetical subfields in the order that they appear
//				fields = betterMarcRecord.getDataFields("600");
//				for(DataField field : fields) {
//					String result = BetterMarcRecord.getDataFieldValueForAllSubfieldsInOrder(field, '1', '0', "[a-z]", " ");
//					if( result != null ) {
//						HashMap<String, String> newSubjectName = new HashMap<String, String>();
//						newSubjectName.put("value", result);
//						newSubjectName.put("type", "personal");
//						this.subject_name.add(newSubjectName);
//					}
//				}
//				fields = betterMarcRecord.getDataFields("611");
//				for(DataField field : fields) {
//					String result = BetterMarcRecord.getDataFieldValueForAllSubfieldsInOrder(field, '2', '0', "[a-z]", " ");
//					if( result != null ) {
//						HashMap<String, String> newSubjectName = new HashMap<String, String>();
//						newSubjectName.put("value", result);
//						newSubjectName.put("type", "conference");
//						this.subject_name.add(newSubjectName);
//					}
//				}
//				fields = betterMarcRecord.getDataFields("610");
//				for(DataField field : fields) {
//					String result = BetterMarcRecord.getDataFieldValueForAllSubfieldsInOrder(field, '1', '0', "[a-z]", " ");
//					if( result != null ) {
//						HashMap<String, String> newSubjectName = new HashMap<String, String>();
//						newSubjectName.put("value", result);
//						newSubjectName.put("type", "corporate");
//						this.subject_name.add(newSubjectName);
//					}
//				}
//				fields = betterMarcRecord.getDataFields("610");
//				for(DataField field : fields) {
//					String result = BetterMarcRecord.getDataFieldValueForAllSubfieldsInOrder(field, '2', '0', "[a-z]", " ");
//					if( result != null ) {
//						HashMap<String, String> newSubjectName = new HashMap<String, String>();
//						newSubjectName.put("value", result);
//						newSubjectName.put("type", "corporate");
//						this.subject_name.add(newSubjectName);
//					}
//				}
//				
//				// subject_title ----- 630 00 $a
//				fields = betterMarcRecord.getDataFields("630");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, '0', '0', 'a')) {
//						String result = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! result.isEmpty() ) {
//							this.subject_title.add(result);
//						}
//					}
//				}
//				
//				// subject_geographic ----- 651_0, all alphabetical subfields in the order that they appear
//				fields = betterMarcRecord.getDataFields("651");
//				for(DataField field : fields) {
//					String result = BetterMarcRecord.getDataFieldValueForAllSubfieldsInOrder(field, null, '0', "[a-z]", "--");
//					if( result != null ) {
//						this.subject_geographic.add(result);
//					}
//				}
//				
//				// coordinates -----
//				//034 $d,$e,$f,$g
//				//255 $c
//				//Note: 034 value is in the authority record of the place recorded in the 651.
//				fields = betterMarcRecord.getDataFields("034");
//				for(DataField field : fields) {
//					String result = (
//						StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'd'), ", ").trim() + "--" +
//						StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'e'), ", ").trim() + "/" +
//						StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'f'), ", ").trim() + "--" +
//						StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'g'), ", ").trim()
//					).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
//					if( ! result.equals("--/--") ) {
//						//DDBSync.writeToLog("bib: " + StringUtils.join(this.clio_identifier), true, DDBSync.LOG_TYPE_NOTICE);
//						this.coordinates.add(normalizeCoordinatesToDecimal(result));
//					}
//				}
//				fields = betterMarcRecord.getDataFields("255");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'c')) {
//						String result = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! result.isEmpty() ) {
//							//DDBSync.writeToLog("bib: " + StringUtils.join(this.clio_identifier), true, DDBSync.LOG_TYPE_NOTICE);
//							this.coordinates.add(normalizeCoordinatesToDecimal(result));
//						}
//					}
//				}
//				
//				// scale ----
//				// 255 $a
//				// 034 $b,$c
//				fields = betterMarcRecord.getDataFields("255");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
//						String result = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! result.isEmpty() ) {
//							this.scale.add(result);
//						}
//					}
//				}
//				fields = betterMarcRecord.getDataFields("034");
//				for(DataField field : fields) {
//					String result = BetterMarcRecord.removeCommonTrailingCharacters(
//							StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'b'), ", ").trim() + " " +
//							StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'c'), ", ").trim()
//						).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
//					if( ! result.isEmpty() ) {
//						this.scale.add(result);
//					}
//				}
//				
//				//genre -----
//				//655, all alphabetical subfields in the order that they appear
//				fields = betterMarcRecord.getDataFields("655");
//				for(DataField field : fields) {
//					String result = BetterMarcRecord.getDataFieldValueForAllSubfieldsInOrder(field, null, null, "[a-z]", "--");
//					if( result != null ) {
//						this.genre.add(result);
//					}
//				}
//				
//				//note ----- 500 $a
//				fields = betterMarcRecord.getDataFields("500");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
//						String result = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! result.isEmpty() ) {
//							this.note.add(result);
//						}
//					}
//				}
//				
//				//form -- leader, bytes 6 and 7
//				char byte6 = betterMarcRecord.getLeader().getTypeOfRecord();
//				char[] bytes7and8 = betterMarcRecord.getLeader().getImplDefined1();
//				char byte7 = bytes7and8[0];
//				HashMap<Character, String> typeOfRecordCharactersToStrings = new HashMap<Character, String>();
//				typeOfRecordCharactersToStrings.put('a', "Language material");
//				typeOfRecordCharactersToStrings.put('c', "Notated music");
//				typeOfRecordCharactersToStrings.put('d', "Manuscript notated music");
//				typeOfRecordCharactersToStrings.put('e', "Cartographic material");
//				typeOfRecordCharactersToStrings.put('f', "Manuscript cartographic material");
//				typeOfRecordCharactersToStrings.put('g', "Projected medium");
//				typeOfRecordCharactersToStrings.put('i', "Nonmusical sound recording");
//				typeOfRecordCharactersToStrings.put('j', "Musical sound recording");
//				typeOfRecordCharactersToStrings.put('k', "Two-dimensional nonprojectable graphic");
//				typeOfRecordCharactersToStrings.put('m', "Computer file");
//				typeOfRecordCharactersToStrings.put('o', "Kit");
//				typeOfRecordCharactersToStrings.put('p', "Mixed materials");
//				typeOfRecordCharactersToStrings.put('r', "Three-dimensional artifact or naturally occurring object");
//				typeOfRecordCharactersToStrings.put('t', "Manuscript language material");
//				HashMap<Character, String> bibliographicLevelCharactersToStrings = new HashMap<Character, String>();
//				bibliographicLevelCharactersToStrings.put('a', "Monographic component part");
//				bibliographicLevelCharactersToStrings.put('b', "Serial component part");
//				bibliographicLevelCharactersToStrings.put('c', "Collection");
//				bibliographicLevelCharactersToStrings.put('d', "Subunit");
//				bibliographicLevelCharactersToStrings.put('i', "Integrating resource");
//				bibliographicLevelCharactersToStrings.put('m', "Monograph/Item");
//				bibliographicLevelCharactersToStrings.put('s', "Serial");
//				String stringRepresentation = typeOfRecordCharactersToStrings.get(byte6) + ", " + bibliographicLevelCharactersToStrings.get(byte7);
//				
//				ArrayList<String> formValues = new ArrayList<String>();
//				
//				//Below we have a set of manually-selected mappings (agreed upon by Durst group)
//				if(stringRepresentation.equals("Language material, Monograph/Item")) {
//					formValues.add("books");
//				} else if(stringRepresentation.equals("Language material, Serial")) {
//					formValues.add("periodicals");
//				} else if(stringRepresentation.equals("Cartographic material, Monograph/Item")) {
//					formValues.add("maps");
//				} else if(stringRepresentation.equals("Manuscript language material, Monograph/Item")) {
//					formValues.add("books");
//				} else if(stringRepresentation.equals("Two-dimensional nonprojectable graphic, Monograph/Item")) {
//					formValues.add("ephemera");
//				} else if(stringRepresentation.equals("Language material, Collection")) {
//					formValues.add("ephemera");
//				} else if(stringRepresentation.equals("Three-dimensional artifact or naturally occurring object, Monograph/Item")) {
//					formValues.add("objects");
//				} else if(stringRepresentation.equals("Manuscript language material, Collection")) {
//					formValues.add("manuscripts");
//				} else if(stringRepresentation.equals("Language material, Monographic component part")) {
//					formValues.add("books");
//				} else if(stringRepresentation.equals("Notated music, Monograph/Item")) {
//					formValues.add("music");
//				} else if(stringRepresentation.equals("Manuscript cartographic material, Monograph/Item")) {
//					formValues.add("maps");
//				} else if(stringRepresentation.equals("Mixed materials, Collection")) {
//					formValues.add("ephemera"); 
//				} else if(stringRepresentation.equals("Two-dimensional nonprojectable graphic, Collection")) {
//					formValues.add("ephemera"); 
//				} else if(stringRepresentation.equals("Cartographic material, Monographic component part")) {
//					formValues.add("maps");
//				} else if(stringRepresentation.equals("Language material, Serial component part")) {
//					formValues.add("periodicals");
//				} else if(stringRepresentation.equals("Musical sound recording, Monograph/Item")) {
//					formValues.add("music");
//					formValues.add("sound recording");
//				} else if(stringRepresentation.equals("Manuscript cartographic material, Monographic component part")) {
//					formValues.add("maps");
//				} else {
//					formValues.add("other");
//				}
//				
//				for(String formValue : formValues) {
//					HashMap<String, String> fullFormValue = new HashMap<String, String>();
//					fullFormValue.put("value", formValue);
//					
//					//TODO: Get the values below from Hyacinth via the API.  That will also include the latest URIs for locally controlled terms.
//					
//					if(formValue.equals("albums")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm000229");
//					} else if(formValue.equals("architectural drawings")) {
//						fullFormValue.put("authority", "gmgpc");
//						fullFormValue.put("value_uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm000455");
//					} else if(formValue.equals("books")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm001221");
//					} else if(formValue.equals("caricatures and cartoons")) {
//						fullFormValue.put("authority", "lcsh");
//						fullFormValue.put("value_uri", "http://id.loc.gov/authorities/subjects/sh99001244.html");
//					} else if(formValue.equals("clippings")) {
//						fullFormValue.put("authority", "gmgpc");
//						fullFormValue.put("value_uri", "http://www.loc.gov/pictures/collection/tgm/item/tgm002169/");
//					} else if(formValue.equals("corporation reports")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://id.loc.gov/authorities/subjects/sh85032921.html");
//					} else if(formValue.equals("correspondence")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://vocab.getty.edu/aat/300026877");
//					} else if(formValue.equals("drawings")) {
//						fullFormValue.put("authority", "gmgpc");
//						fullFormValue.put("value_uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm003279");
//					} else if(formValue.equals("ephemera")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://vocab.getty.edu/aat/300028881");
//					} else if(formValue.equals("filmstrips")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://vocab.getty.edu/aat/300028048");
//					} else if(formValue.equals("illustrations")) {
//						fullFormValue.put("authority", "gmgpc");
//						fullFormValue.put("value_uri", "http://www.loc.gov/pictures/collection/tgm/item/tgm005314/");
//					} else if(formValue.equals("lantern slides")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://vocab.getty.edu/aat/300134977");
//					} else if(formValue.equals("manuscripts")) {
//						fullFormValue.put("authority", "lcsh");
//						fullFormValue.put("value_uri", "http://id.loc.gov/authorities/subjects/sh85080672.html");
//					} else if(formValue.equals("maps")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm006261");
//					} else if(formValue.equals("minutes (administrative records)")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://vocab.getty.edu/aat/300027440");
//					} else if(formValue.equals("mixed materials")) {
//						fullFormValue.put("authority", "local");
//					} else if(formValue.equals("moving images")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://vocab.getty.edu/aat/300263857");
//					} else if(formValue.equals("music")) {
//						fullFormValue.put("authority", "local");
//					} else if(formValue.equals("negatives")) {
//						fullFormValue.put("authority", "gmgpc");
//						fullFormValue.put("value_uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm007029");
//					} else if(formValue.equals("objects")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm007159");
//					} else if(formValue.equals("oral histories")) {
//						fullFormValue.put("authority", "local");
//						fullFormValue.put("value_uri", "http://id.loc.gov/authorities/subjects/sh2008025718.html");
//					} else if(formValue.equals("other")) {
//						fullFormValue.put("authority", "local");
//					} else if(formValue.equals("paintings")) {
//						fullFormValue.put("authority", "gmgpc");
//						fullFormValue.put("value_uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm007393");
//					} else if(formValue.equals("pamphlets")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm007415");
//					} else if(formValue.equals("papyri")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://vocab.getty.edu/resource/aat/300055047");
//					} else if(formValue.equals("periodicals")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm007641");
//					} else if(formValue.equals("photographs")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm007721");
//					} else if(formValue.equals("playing cards")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm007907");
//					} else if(formValue.equals("postcards")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm008103");
//					} else if(formValue.equals("posters")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm008104");
//					} else if(formValue.equals("printed ephemera")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://vocab.getty.edu/aat/300264821");
//					} else if(formValue.equals("prints")) {
//						fullFormValue.put("authority", "gmgpc");
//						fullFormValue.put("value_uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm008237");
//					} else if(formValue.equals("record covers")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://vocab.getty.edu/aat/300247936");
//					} else if(formValue.equals("scrapbooks")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm009266");
//					} else if(formValue.equals("slides (photographs)")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://vocab.getty.edu/resource/aat/300128371");
//					} else if(formValue.equals("sound recordings")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://id.loc.gov/vocabulary/graphicMaterials/tgm009874");
//					} else if(formValue.equals("video recordings")) {
//						fullFormValue.put("authority", "aat");
//						fullFormValue.put("value_uri", "http://vocab.getty.edu/aat/300028682");
//					}
//					
//					this.form.add(fullFormValue);
//				}
//				
//				// extent ----- 300 $a, $b, $c
//				fields = betterMarcRecord.getDataFields("300");
//				for(DataField field : fields) {
//					String result = (
//							StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'a'), ", ").trim() + " " +
//							StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'b'), ", ").trim() + " " +
//							StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'c'), ", ").trim()
//						).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
//					if( ! result.isEmpty() ) {
//						this.extent.add(result);
//					}
//				}
//				
//				//series -----
//				//490 0 $a
//				//830 _0 $a
//				fields = betterMarcRecord.getDataFields("490");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, '0', null, 'a')) {
//						String result = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! result.isEmpty() ) {
//							this.series.add(result);
//						}
//					}
//				}
//				fields = betterMarcRecord.getDataFields("830");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, '0', 'a')) {
//						String result = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! result.isEmpty() ) {
//							this.series.add(result);
//						}
//					}
//				}
//				
//				//isbn ----- 020 $a
//				fields = betterMarcRecord.getDataFields("020");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
//						String result = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! result.isEmpty() ) {
//							this.isbn.add(result);
//						}
//					}
//				}
//				
//				//issn ----- 022 $a
//				fields = betterMarcRecord.getDataFields("022");
//				for(DataField field : fields) {
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
//						String result = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! result.isEmpty() ) {
//							this.issn.add(result);
//						}
//					}
//				}
//				
//				//url ----- 856 $u
//				fields = betterMarcRecord.getDataFields("856");
//				HashMap<String, String> url_value;
//				for(DataField field : fields) {
//					
//					//For a given field, subfield $3 is the label for any URL in subfield $u.
//					//Even though $u is technically repeatable, it makes sense to use the $3 value for all instances
//					//because there would be no other way to match up multiple $3 values and $u values in a MARC
//					//record if someone chose to put multiple values in for each.  So it someone put a $3 in, we
//					//can only assume that it applies to all $u values in the same 856 field.
//					ArrayList<String> subfield3 = BetterMarcRecord.getDataFieldValue(field, '4', '0', '3');
//					String displayLabelResult = subfield3.size() == 0 ? null : subfield3.get(0);
//					
//					for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, '4', '0', 'u')) {
//						url_value = new HashMap<String, String>();
//						String urlValueResult = BetterMarcRecord.removeCommonTrailingCharacters((
//							subfieldValue
//						));
//						if( ! urlValueResult.isEmpty() ) {
//							url_value.put("value", urlValueResult);
//							if(displayLabelResult != null) {
//								url_value.put("display_label", displayLabelResult);
//							}
//							this.the_urls.add(url_value);
//						}
//					}
//				}
//				
//				//type_of_resource ----- Leader byte 6
//				
//				if(byte6 == 'a' || byte6 == 't') {
//					this.type_of_resource.add("text");
//				} else if(byte6 == 'e' || byte6 == 'f') {
//					this.type_of_resource.add("cartographic");
//				} else if(byte6 == 'c' || byte6 == 'd') {
//					this.type_of_resource.add("notated music");
//				} else if(byte6 == 'i') {
//					this.type_of_resource.add("sound recording - nonmusical");
//				} else if(byte6 == 'j') {
//					this.type_of_resource.add("sound recording - musical");
//				} else if(byte6 == 'k') {
//					this.type_of_resource.add("still image");
//				} else if(byte6 == 'g') {
//					this.type_of_resource.add("moving image");
//				} else if(byte6 == 'o') {
//					this.type_of_resource.add("kit");
//				} else if(byte6 == 'r') {
//					this.type_of_resource.add("three dimensional object");
//				} else if(byte6 == 'm') {
//					this.type_of_resource.add("software, multimedia");
//				} else if(byte6 == 'p') {
//					this.type_of_resource.add("mixed material");
//				}
//				
//				if(byte7 == 'c') {
//					this.type_of_resource_is_collection.add("yes");
//				}
//				
//				
//				
//			} else {
//				DDBSync.writeToLog("Error: Expected to find marc record in file, but none was found.", true, DDBSync.LOG_TYPE_ERROR);
//			}
//			fis.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
//	
//	public void extractDataFromRawMarcHoldingsRecords(File[] rawMarcHoldingsRecords) {
//		
//		if(rawMarcHoldingsRecords.length > 0) {
//			for(File marcHoldingsFile : rawMarcHoldingsRecords) {
//				try {
//					FileInputStream fis = new FileInputStream(marcHoldingsFile);
//					MarcReader reader = new MarcStreamReader(fis);
//					while (reader.hasNext()) {
//			            Record record = reader.next();
//			            BetterMarcRecord betterMarcRecord = new BetterMarcRecord(record);
//			            
//			            ArrayList<DataField> fields;
//			            
//			            //dims_identifier ----- Holdings 950 $a
//			            fields = betterMarcRecord.getDataFields("950");
//						for(DataField field : fields) {
//							for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
//								String result = BetterMarcRecord.removeCommonTrailingCharacters((
//									subfieldValue
//								));
//								if( ! result.isEmpty() ) {
//									this.dims_identifier.add(result);
//								}
//							}
//						}
//						
//						//enumeration_and_chronology ----- Holdings 866 41 $a
//			            fields = betterMarcRecord.getDataFields("866");
//						for(DataField field : fields) {
//							for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, '4', '1', 'a')) {
//								String result = BetterMarcRecord.removeCommonTrailingCharacters((
//									subfieldValue
//								));
//								if( ! result.isEmpty() ) {
//									this.enumeration_and_chronology.add(result);
//								}
//							}
//						}
//						
//			        }
//					fis.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}								
//			}
//		}
//	}
//	
//	/**
//	 * @param actuallyDoSend - Set this to false when testing to avoid actually sending the record 
//	 * @param includeTestParam- Includes test=true in the params so that Hyacinth does a test ingest and only validates rather than saving
//	 */
//	public void sendRecordToHyacinth(boolean actuallyDoSend, boolean includeTestParam) {
//		
//		String result = "";
//		
//		try {
//			
//			String recordDynamicFieldDataJson = this.getDynamicFieldDataJSON();
//			
//			if(! actuallyDoSend) {
//				//System.out.println("Not actually sending record to Hyacinth");
//				return;
//			}
//			
//		
//			//Post JSON to Hyacinth
//			
//			String url;
//			if(this.pid == null) {
//				url = DDBSync.hyacinthAppUrl + "/digital_objects.json";
//			} else {
//				System.out.println("Performing UPDATE because existing PID was found: " + this.pid);
//				url = DDBSync.hyacinthAppUrl + "/digital_objects/" + this.pid + ".json";
//			}
//			HttpClient client = HttpClientBuilder.create().build();
//			HttpPost post = new HttpPost(url);
//			String authDigest = new String(Base64.encodeBase64((DDBSync.hyacinthAppLoginEmail + ":" + DDBSync.hyacinthAppLoginPassword).getBytes()));
//			post.setHeader("Authorization", "Basic " + authDigest);
//			
//			//post.setHeader("Content-Type", "text/plain;charset=UTF-8");
//			//application/x-www-form-urlencoded; charset=utf-8
//		 
//			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
//		
//		
//			if(this.pid == null) {
//				//This is a NEW record without a pid
//				urlParameters.add(new BasicNameValuePair("_method", "POST"));
//				urlParameters.add(new BasicNameValuePair("digital_object[digital_object_type_string_key]", "item"));
//				urlParameters.add(new BasicNameValuePair("digital_object[project_string_key]", "durst"));
//				urlParameters.add(new BasicNameValuePair("digital_object[publish_targets][]", "cul:sqv9s4mwg3")); //Durst publish target
//			} else {
//				//This is an update to an existing record with pid: lastSeenPid
//				urlParameters.add(new BasicNameValuePair("_method", "PUT"));
//			}
//			
//			//urlParameters.add(new BasicNameValuePair("merge", "false")); //This will replace ALL existing data.  Un-set this when you want to do incremental updates again.
//			
//			urlParameters.add(new BasicNameValuePair("publish", "true")); //Attempt to re-publish record (if record has publish targets)
//			urlParameters.add(new BasicNameValuePair("digital_object[dynamic_field_data_json]", recordDynamicFieldDataJson));
//			
//			if(includeTestParam) {
//				urlParameters.add(new BasicNameValuePair("test", "true"));
//			}
//			
//		
//			//post.setEntity(new UrlEncodedFormEntity(urlParameters));
//			post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
//			HttpResponse response = client.execute(post);
//			
//			//System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
//		 
//			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//		 
//			StringBuffer resultSb = new StringBuffer();
//			String line = "";
//			while ((line = rd.readLine()) != null) {
//				resultSb.append(line);
//			}
//			
//			result = resultSb.toString();
//			
//			//Handle response
//			JSONObject jsonResponse = new JSONObject(result);
//			System.out.println(jsonResponse.toString());
//			if(jsonResponse.getBoolean("success") == true) {
//				// Success!
//			} else {
//				DDBSync.writeToLog("Error encountered during Hyacinth record save (for pid " + this.pid + ", with CLIO bib(s): " + StringUtils.join(this.clio_identifier) + " or cul-assigned postcard id: " + StringUtils.join(this.cul_assigned_postcard_identifier) + "). Error " + jsonResponse.toString(), true, DDBSync.LOG_TYPE_ERROR);
//			}
//			
//		} catch (JSONException e) {
//			DDBSync.writeToLog("Error encountered during Hyacinth record save: " + e.getMessage(), true, DDBSync.LOG_TYPE_ERROR);
//			DDBSync.writeToLog("Error encountered during Hyacinth record save (for pid " + this.pid + ", with CLIO bib(s): " + StringUtils.join(this.clio_identifier) + " or cul-assigned postcard id: " + StringUtils.join(this.cul_assigned_postcard_identifier) + "). Error: " + e.getMessage(), true, DDBSync.LOG_TYPE_ERROR);
//			DDBSync.writeToLog("Post Response: " + result, true, DDBSync.LOG_TYPE_ERROR);
//			e.printStackTrace();
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalStateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
	public String normalizeCoordinatesToDecimal(String someCoordinateVal) {
		
		//TODO: Normalize all non-decimal values into decimal format (i.e. "12.345,56.7890")
		
//		Pattern p = Pattern.compile("[\\d\\.]+,[\\d\\.]+");
//		Matcher m = p.matcher(someCoordinateVal);
//		
//		if( ! m.matches() ) {
//			System.out.println("No match for " + someCoordinateVal);
//			System.exit(DDBSync.EXIT_CODE_ERROR);
//		}
		
		return someCoordinateVal;
		
	}
	
}

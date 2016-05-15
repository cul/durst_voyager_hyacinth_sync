package edu.columbia.ldpd.dvhs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

import edu.columbia.ldpd.dvhs.exceptions.UnhandledCoordinateFormatException;

public class DurstRecord {
	
	private static final Pattern VALID_035_AND_776_FIELD_PATTERN = Pattern.compile("\\(OCoLC\\)(oc(m|n))*(0*)(\\d+)"); //including (0*) to remove leading zeros
	private static final Pattern FULL_LAT_LONG_COORDINATE_DECIMAL_FORMAT = Pattern.compile("^-*\\d{0,2}(\\.\\d+)*,-*\\d{0,3}(\\.\\d+)*$"); //-12.345,56.7890
	private static final Pattern DECIMAL_COORDINATE_FORMAT = Pattern.compile("[-\\d\\.]+"); //-12.345
	
	private static final Pattern LAT_LONG_SIX_DIGIT_NUMBER_FORMAT = Pattern.compile("(-*\\d{2})(\\d{2})(\\d{2})"); //-738070
	private static final Pattern LAT_LONG_SEVEN_DIGIT_NUMBER_FORMAT = Pattern.compile("(-*\\d{3})(\\d{2})(\\d{2})"); //-0738070
	private static final Pattern LAT_LONG_DEGREES_MINUTES_SECONDS_FORMAT = Pattern.compile("([-\\d])+°([\\d]+)*ʹ*([\\d]+)*ʺ*"); //40° or 40°42ʹ or 40°42ʹ28ʺ
	
	private JSONObject digitalObjectData;
	private JSONObject dynamicFieldData;
	private ArrayList<String> ocolc035FieldValues = new ArrayList<String>();
	private HashSet<String> ocolc776FieldValues = new HashSet<String>();
	private boolean isElectronicRecord = false; //true when 965 $a field == "eDurst"
	private String pid = null;
	
	public DurstRecord(File marcXmlFile, File[] holdingsFiles, String[] barcodes) {
		
		//Note: bacodes are not currently used
		
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
				
				if(holdingsFiles != null) {
					extractDataFromRawMarcHoldingsRecords(holdingsFiles);
				}
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
		
		// record_content_source-1:record_content_source_value
		// Value is always NNC
		dynamicFieldData.put("record_content_source", new JSONArray().put(new JSONObject()
				.put("record_content_source_value", "NNC")
			)
		);
	}

	private void extractMarcDataFromBibRecord(BetterMarcRecord betterMarcRecord) throws JSONException {
		ArrayList<DataField> fields; // This variable is reused many times throughout this method
		
		// clio_identifier-1:clio_identifier_value
		// MARC 001
		dynamicFieldData.put("clio_identifier", new JSONArray().put(
			new JSONObject()
				.put("clio_identifier_value", betterMarcRecord.getControlField("001").getData().trim())
				.put("clio_identifier_type", this.isElectronicRecord ? "electronic" : "print")
				.put("clio_identifier_omit_from_mods", !this.isElectronicRecord) // omit print records from MODS
		));
		
		
		// marc_005_last_modified-1:marc_005_last_modified_value
		// MARC 005
		setMarc005Value(betterMarcRecord.getControlField("005").getData().trim());
		
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
				try {
					coordinatesJSONArray.put(new JSONObject()
						.put("coordinates_value", normalizeCoordinatesToDecimal(result))
					);
				} catch (UnhandledCoordinateFormatException e) {
					DurstVoyagerHyacinthSync.logger.error("Problem found in record with CLIO ID " + this.getClioIdentifiers().get(0) + ", MARC field 034: " + e.getMessage());
				}
			}
		}
		fields = betterMarcRecord.getDataFields("255");
		for(DataField field : fields) {
			for(String subfieldValue : BetterMarcRecord.getDataFieldValue(field, null, null, 'c')) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters((
					subfieldValue
				));
				if( ! result.isEmpty() ) {
					try {
						coordinatesJSONArray.put(new JSONObject()
							.put("coordinates_value", normalizeCoordinatesToDecimal(result))
						);
					} catch (UnhandledCoordinateFormatException e) {
						DurstVoyagerHyacinthSync.logger.error("Problem found in record with CLIO ID " + this.getClioIdentifiers().get(0) + ", MARC field 255: " + e.getMessage());
					}
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
		dynamicFieldData.put("extent", extentJSONArray);
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
		dynamicFieldData.put("series", seriesJSONArray);
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
	
	private HashSet<String> getOcolc776ValuesFromMarcRecord(BetterMarcRecord betterMarcRecord) {
		HashSet<String> values = new HashSet<String>();
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
	
	public HashSet<String> getOcolc776FieldValues() {
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
	 * @throws JSONException 
	 */
	public static String mergeElectronicRecordDataIntoPrintRecord(DurstRecord printRecord, ArrayList<DurstRecord> electronicRecordsToMergeIn) throws JSONException {
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
		
		String printRecordMarc005Value = printRecord.getMarc005Value();
		for(DurstRecord electronicRecord : electronicRecordsToMergeIn) {
			
			// Merge latest (alphabetically sorted) MARC 005 value.
			// Note: Last modified string format is "19940223151047.0" (i.e. February 23, 1994, 3:10:47 P.M. (15:10:47)
			String electronicRecordMarc005Value = electronicRecord.getMarc005Value();
			if(electronicRecordMarc005Value.compareTo(printRecordMarc005Value) > 1) {
				printRecord.setMarc005Value(electronicRecord.getMarc005Value());
			}
			
			// Merge in 776 values from electronic records and make the list unique
			// Note: We can just get the value from the first electronic records
			printRecord
				.ocolc776FieldValues
					.addAll(
						electronicRecord
							.getOcolc776FieldValues()
					);
			
			// Add electronic record CLIO identifiers
			if(! printRecord.dynamicFieldData.has("clio_identifier")) {
				printRecord.dynamicFieldData.put("clio_identifier", new JSONArray());
			}
			if( electronicRecord.dynamicFieldData.has("clio_identifier")) {
				JSONArray clioIdentifiers = electronicRecord.dynamicFieldData.getJSONArray("clio_identifier");
				for(int i = 0; i < clioIdentifiers.length(); i++) {
					// Copy the element values without maintaining a reference to the original electronic record
					printRecord.dynamicFieldData.getJSONArray("clio_identifier").put(
						new JSONObject()
						.put("clio_identifier_value", clioIdentifiers.getJSONObject(i).getString("clio_identifier_value"))
						.put("clio_identifier_type", clioIdentifiers.getJSONObject(i).getString("clio_identifier_type"))
						.put("clio_identifier_omit_from_mods", clioIdentifiers.getJSONObject(i).getBoolean("clio_identifier_omit_from_mods"))
					);
				}
			}
			
			// Add electronic record URL values
			if(! printRecord.dynamicFieldData.has("url")) {
				printRecord.dynamicFieldData.put("url", new JSONArray());
			}
			if( electronicRecord.dynamicFieldData.has("url")) {
				JSONArray urls = electronicRecord.dynamicFieldData.getJSONArray("url");
				for(int i = 0; i < urls.length(); i++) {
					// Copy the element values without maintaining a reference to the original electronic record
					printRecord.dynamicFieldData.getJSONArray("url").put(
						new JSONObject()
							.put("url_value", urls.getJSONObject(i).getString("url_value"))
							.put("url_display_label", urls.getJSONObject(i).getString("url_display_label"))
					);
				}
			}
			
			// Add dims identifier values
			if(! printRecord.dynamicFieldData.has("dims_identifier")) {
				printRecord.dynamicFieldData.put("dims_identifier", new JSONArray());
			}
			if( electronicRecord.dynamicFieldData.has("dims_identifier")) {
				JSONArray dimsIdentifiers = electronicRecord.dynamicFieldData.getJSONArray("dims_identifier");
				for(int i = 0; i < dimsIdentifiers.length(); i++) {
					// Copy the element values without maintaining a reference to the original electronic record
					printRecord.dynamicFieldData.getJSONArray("url").put(
						new JSONObject()
							.put("dims_identifier_value", dimsIdentifiers.getJSONObject(i).getString("dims_identifier_value"))
					);
				}
			}
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
	
	public void setMarc005Value(String value) {
		try {
			if(this.dynamicFieldData.has("marc_005_last_modified") && this.dynamicFieldData.getJSONArray("marc_005_last_modified").length() > 0) {
				this.dynamicFieldData.getJSONArray("marc_005_last_modified").getJSONObject(0).put("marc_005_last_modified_value", value);
			} else {
				this.dynamicFieldData.put("marc_005_last_modified", new JSONArray().put(new JSONObject().put("marc_005_last_modified_value", value)));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
	
	public static String normalizeCoordinatesToDecimal(String coordinateVal) throws UnhandledCoordinateFormatException {
		
		Matcher m = FULL_LAT_LONG_COORDINATE_DECIMAL_FORMAT.matcher(coordinateVal);
		if( m.matches() ) {
			return coordinateVal; // Coordinate is already in the correct format. Return as is.
		}
		
		// Normalize all non-decimal values into decimal format (i.e. "12.345,56.7890")
		
		//Normalize degree characters
		String normalizedCoordinateVal = coordinateVal.replace("⁰", "°");
		
		//Remove leading and trailing parentheses (which should be the only parentheses in the entire string)
		normalizedCoordinateVal = normalizedCoordinateVal.replace("(", "").replace(")", "");
		
		//If this string has only a "--" and no "/", swap the "--" with a "/"
		if(normalizedCoordinateVal.contains("--") && ! normalizedCoordinateVal.contains("/")) {
			normalizedCoordinateVal = normalizedCoordinateVal.replace("--", "/");
		}
		
		//If this string doesn't contain any "/" at this point, and only contains one space (" "), replace the space with a "/"
		if( !normalizedCoordinateVal.contains("/") && normalizedCoordinateVal.indexOf(" ") == normalizedCoordinateVal.lastIndexOf(" ")) {
			normalizedCoordinateVal = normalizedCoordinateVal.replace(" ", "/");
		}
		
		// W/E is -/+ longitude
		// N/S is +/- latitude
		
		//Identify latitude and longitude sections
		if( ! normalizedCoordinateVal.contains("/") ) {
			throw new UnhandledCoordinateFormatException("Unhandled coordinate format (expected \"/\"): " + coordinateVal);
		}
		
		//If there's whitespace around the "/", remove that whitespace
		normalizedCoordinateVal = normalizedCoordinateVal.replaceAll("\\s*\\/\\s*", "/");
		
		String[] parts = normalizedCoordinateVal.split("/");
		String longitude = null, latitude = null;
		if(parts[0].contains("W") || parts[0].contains("E")) {
			// If a W or E are present in the first part, that means we're dealing with longitude first 
			longitude = parts[0];
			latitude = parts[1];
		} else {
			//Otherwise assume that the first part is latitude
			latitude = parts[0];
			longitude = parts[1];
		}
		
		//Convert all "W"s to "-" and "E"s to ""
		//Convert all "N"s to "" and "S"s to "-"
		latitude = latitude.replaceAll("N *", "").replaceAll("S *", "-");
		longitude = longitude.replaceAll("W *", "-").replaceAll("E *", "");
		
		//If this is a range, split it up into latStart/latEnd and longStart/longEnd
		//If it's not a range, redundantly put the single latitude and longitude values in latStart/latEnd and longStart/longEnd
		String latStart = null, latEnd = null, longStart = null, longEnd = null;
		if(latitude.contains("--")) {
			String[] latParts = latitude.split("--");
			latStart = latParts[0];
			latEnd = latParts[1];
		} else {
			latStart = latitude;
			latEnd = latitude;
		}
		if(longitude.contains("--")) {
			String[] longParts = longitude.split("--");
			longStart = longParts[0];
			longEnd = longParts[1];
		} else {
			longStart = longitude;
			longEnd = longitude;
		}
		
		//If any of the start/end coordinates end in a minus sign, move that minus sign to the beginning
		if(latStart.endsWith("-")) {
			latStart = "-" + latStart.substring(0, latStart.length()-1);
		}
		if(latEnd.endsWith("-")) {
			latEnd = "-" + latEnd.substring(0, latEnd.length()-1);
		}
		if(longStart.endsWith("-")) {
			longStart = "-" + longStart.substring(0, longStart.length()-1);
		}
		if(longEnd.endsWith("-")) {
			longEnd = "-" + longEnd.substring(0, longEnd.length()-1);
		}
		
		//Convert seven-digit number values to decimal numbers, if applicable
		m = LAT_LONG_SEVEN_DIGIT_NUMBER_FORMAT.matcher(latStart);
		if( m.matches() ) {
			//Group 1 = degrees, 2 = minutes, 3 = seconds
			latStart = degreesMinutesSecondsToDecomalDegrees(m.group(1), m.group(2), m.group(3));
		}
		m = LAT_LONG_SEVEN_DIGIT_NUMBER_FORMAT.matcher(latEnd);
		if( m.matches() ) {
			//Group 1 = degrees, 2 = minutes, 3 = seconds
			latEnd = degreesMinutesSecondsToDecomalDegrees(m.group(1), m.group(2), m.group(3));
		}
		m = LAT_LONG_SEVEN_DIGIT_NUMBER_FORMAT.matcher(longStart);
		if( m.matches() ) {
			longStart = degreesMinutesSecondsToDecomalDegrees(m.group(1), m.group(2), m.group(3));
		}
		m = LAT_LONG_SEVEN_DIGIT_NUMBER_FORMAT.matcher(longEnd);
		if( m.matches() ) {
			longEnd = degreesMinutesSecondsToDecomalDegrees(m.group(1), m.group(2), m.group(3));
		}
		
		//Convert six-digit number values to decimal numbers, if applicable
		m = LAT_LONG_SIX_DIGIT_NUMBER_FORMAT.matcher(latStart);
		if( m.matches() ) {
			//Group 1 = degrees, 2 = minutes, 3 = seconds
			latStart = degreesMinutesSecondsToDecomalDegrees(m.group(1), m.group(2), m.group(3));
		}
		m = LAT_LONG_SIX_DIGIT_NUMBER_FORMAT.matcher(latEnd);
		if( m.matches() ) {
			//Group 1 = degrees, 2 = minutes, 3 = seconds
			latEnd = degreesMinutesSecondsToDecomalDegrees(m.group(1), m.group(2), m.group(3));
		}
		m = LAT_LONG_SIX_DIGIT_NUMBER_FORMAT.matcher(longStart);
		if( m.matches() ) {
			longStart = degreesMinutesSecondsToDecomalDegrees(m.group(1), m.group(2), m.group(3));
		}
		m = LAT_LONG_SIX_DIGIT_NUMBER_FORMAT.matcher(longEnd);
		if( m.matches() ) {
			longEnd = degreesMinutesSecondsToDecomalDegrees(m.group(1), m.group(2), m.group(3));
		}
		
		//Convert degrees minutes seconds, if applicable
		m = LAT_LONG_DEGREES_MINUTES_SECONDS_FORMAT.matcher(latStart);
		if( m.matches() ) {
			//Group 1 = degrees, 2 = minutes, 3 = seconds
			latStart = degreesMinutesSecondsToDecomalDegrees(m.group(1), m.group(2), m.group(3));
		}
		m = LAT_LONG_DEGREES_MINUTES_SECONDS_FORMAT.matcher(latEnd);
		if( m.matches() ) {
			//Group 1 = degrees, 2 = minutes, 3 = seconds
			latEnd = degreesMinutesSecondsToDecomalDegrees(m.group(1), m.group(2), m.group(3));
		}
		m = LAT_LONG_DEGREES_MINUTES_SECONDS_FORMAT.matcher(longStart);
		if( m.matches() ) {
			longStart = degreesMinutesSecondsToDecomalDegrees(m.group(1), m.group(2), m.group(3));
		}
		m = LAT_LONG_DEGREES_MINUTES_SECONDS_FORMAT.matcher(longEnd);
		if( m.matches() ) {
			longEnd = degreesMinutesSecondsToDecomalDegrees(m.group(1), m.group(2), m.group(3));
		}
		
		// If the lat/long start/end values have a degree symbol in them, that's
		// most likely a sign that we were given a degree range. We need to
		// remove the degree symbol.
		latStart = latStart.replace("°", "");
		latEnd = latEnd.replace("°", "");
		longStart = longStart.replace("°", "");
		longEnd = longEnd.replace("°", "");
		
		//At this point, latStart/latEnd/longStart/longEnd should all be in the decimal format. If not, we're dealing with an unexpected format
		if( ! DECIMAL_COORDINATE_FORMAT.matcher(latStart).matches() ||
			! DECIMAL_COORDINATE_FORMAT.matcher(latEnd).matches() ||
			! DECIMAL_COORDINATE_FORMAT.matcher(longStart).matches() ||
			! DECIMAL_COORDINATE_FORMAT.matcher(longEnd).matches()
		)
		{
			throw new UnhandledCoordinateFormatException("Unhandled coordinate format: " + coordinateVal);
		}
		
		//If we got here, then we have four decimal coordiantes.  We'll average them into two coordinates.
		double averagedLatitude = (Double.parseDouble(latStart) + Double.parseDouble(latEnd))/2.0;
		double averagedLongitude = (Double.parseDouble(longStart) + Double.parseDouble(longEnd))/2.0;
		
		//Truncate to four decimal places max
		averagedLatitude = new BigDecimal(averagedLatitude).setScale(4, RoundingMode.HALF_EVEN).doubleValue();
		averagedLongitude = new BigDecimal(averagedLongitude).setScale(4, RoundingMode.HALF_EVEN).doubleValue();
		
		String finalConvertedCoordinates = averagedLatitude + "," + averagedLongitude;
		
		m = FULL_LAT_LONG_COORDINATE_DECIMAL_FORMAT.matcher(finalConvertedCoordinates);
		if( m.matches() ) {
			return finalConvertedCoordinates; // Coordinate is already in the correct format. Return as is.
		} else {
			throw new UnhandledCoordinateFormatException("Unhandled coordinate format: " + coordinateVal);
		}
	}

	private static String degreesMinutesSecondsToDecomalDegrees(String degrees, String minutes, String seconds) {
		
		double value = Double.parseDouble(degrees);
		if(minutes != null) {
			value += Double.parseDouble(minutes)/60.0;
		}
		if(seconds != null) {
			value += Double.parseDouble(seconds)/3600.0;
		}
		
		return "" + value;
	}
	
}

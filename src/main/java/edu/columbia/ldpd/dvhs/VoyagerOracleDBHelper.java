package edu.columbia.ldpd.dvhs;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

public class VoyagerOracleDBHelper {
	
	private static Connection staticVoyagerConnection;
	//private static Pattern dimsIdPattern = Pattern.compile(".*(DIM\\d{6}).*");
	//private static Pattern enumerationAndChronologyPattern = Pattern.compile(".*$81$a(.+)$*.*");
	
	public static void testVoyagerConnection() {
		DurstVoyagerHyacinthSync.logger.info("Testing connection to Voyager...");
		Connection testConn = getNewVoyagerConnection(); //This will end the program if the connection fails
		
		//Sleep for a couple of seconds before disconnecting because rapid connect+disconnects to voyager might be tripping things up
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			DurstVoyagerHyacinthSync.logger.error("Error: Could not establish test connection to Voyager.");
			e1.printStackTrace();
			System.exit(DurstVoyagerHyacinthSync.EXIT_CODE_ERROR);
		}
		
		try {
			testConn.close();
			DurstVoyagerHyacinthSync.logger.info("Successfully connected to Voyager! Closing test connection...");
			
			//Sleep for a second after disconnecting because rapid connect+disconnects to voyager might be tripping things up
			try {
				Thread.sleep(2000);
				DurstVoyagerHyacinthSync.logger.info("Test Voyager connection closed.");
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
		} catch (SQLException e) {
			DurstVoyagerHyacinthSync.logger.error("Error: Could not close test Voyager connection.");
			e.printStackTrace();
			System.exit(DurstVoyagerHyacinthSync.EXIT_CODE_ERROR);
		}
	}
	
	public static Connection getNewVoyagerConnection() {
		return getNewDBConnection(DurstVoyagerHyacinthSync.voyagerOracleDBUrl, DurstVoyagerHyacinthSync.voyagerOracleDBDatabase, DurstVoyagerHyacinthSync.voyagerOracleDBUsername, DurstVoyagerHyacinthSync.voyagerOracleDBPassword);
	}
	
	
	public static void openStaticConnection() {
		staticVoyagerConnection = getNewVoyagerConnection();
	}
	
	public static void closeStaticConnection() {
		try {
			staticVoyagerConnection.close();
		} catch (SQLException e) {
			DurstVoyagerHyacinthSync.logger.error("Error: Could not close Voyager connection.");
			e.printStackTrace();
		}
		System.out.println("Closed connection to Voyager database.");
	}
	
	public static Connection getNewDBConnection(String url, String database, String username, String password) {

		Connection newConn = null;

		//Step 1: Load MySQL Driver
		try {
            // This is a test to check that the driver is available.
			// The newInstance() call is a work around for some broken Java implementations.
            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
        } catch (Exception ex) { System.err.println("Could not load the ODBC driver!"); }

		//Step 2: Establish connection
		
		String connectionUrl = "jdbc:oracle:thin:@" + url + ":" + database;
		try {
			newConn = DriverManager.getConnection(connectionUrl, username, password);
		} catch (SQLException ex) {
			DurstVoyagerHyacinthSync.logger.error(
				"Error: Could not connect to Voyager Oracle Database at url:" + connectionUrl + "\n" +
				"SQLException: " + ex.getMessage() + "\n" +
		    	"SQLState: " + ex.getSQLState() + "\n" +
		    	"VendorError: " + ex.getErrorCode()
		    );
		    System.exit(DurstVoyagerHyacinthSync.EXIT_CODE_ERROR);
		}

		System.out.println("Successfuly connected to Voyager database: " + database);
		
		return newConn;
	}
	
	public static ArrayList<String> getBarcodesFromItemRecordForBibKey(String clioBibKey) throws SQLException {
		ArrayList<String> barcodes = new ArrayList<String>();
		HashMap<String, String> holdingsData = getRawMarcHoldingsDataForBibKey(clioBibKey);
		
		if(holdingsData.size() > 0) { 
			String itemIdQuery = "select item_id from mfhd_item where mfhd_id = ?";
			PreparedStatement itemIdPstmt = staticVoyagerConnection.prepareStatement(itemIdQuery);
			ResultSet itemIdResultSet;
			
			String barcodeQuery = "select item_barcode from item_barcode where item_id = ? and barcode_status = 1";
			PreparedStatement barcodePstmt = staticVoyagerConnection.prepareStatement(barcodeQuery);
			ResultSet barcodeResultSet;
			
			for(String holdingId : holdingsData.keySet()) {
				
				ArrayList<String> itemIds = new ArrayList<String>();
				
				itemIdPstmt.setString(1, holdingId);
				itemIdResultSet = itemIdPstmt.executeQuery();
				while(itemIdResultSet.next()) {
					itemIds.add(itemIdResultSet.getString(1));
				}
				itemIdResultSet.close();
				
				if(itemIds.size() == 0) {
					continue;
				} else {
					for(String itemId : itemIds) {
						barcodePstmt.setString(1, itemId);
						barcodeResultSet = barcodePstmt.executeQuery();
						while(barcodeResultSet.next()) {
							barcodes.add(barcodeResultSet.getString(1));
						}
						barcodeResultSet.close();
					}
				}
			}
			
			itemIdPstmt.close();
			barcodePstmt.close();
		}
		
		return barcodes;
	}
	
	/**
	 * Returns a HashMap containing the raw data for one or more DURST-ONLY holdings, mapping holdings ids to holdings values.
	 * A durst holding can be identified by holding field 541 $a, with an exact value of "Seymour Durst"
	 * Returns null if there are no holdings.
	 * @param clioBibKey
	 * @return
	 * @throws SQLException
	 * @throws IOException 
	 */
	public static HashMap<String, String> getRawMarcDurstOnlyHoldingsDataForBibKey(String clioBibKey) throws SQLException, IOException {
		HashMap<String, String> allHoldings = getRawMarcHoldingsDataForBibKey(clioBibKey);
		HashMap<String, String> durstHoldings = new HashMap<String, String>();
		
		//Check if this holding contains a value of "Seymour Durst" in field 541 $a
		for(Map.Entry<String, String> entry: allHoldings.entrySet()) {
			String holdingKey = entry.getKey();
			String holdingData = entry.getValue();
			
			InputStream is = IOUtils.toInputStream(holdingData, "UTF-8");
			//System.out.println(marcHoldingsFile.getAbsolutePath());
			MarcReader reader = new MarcStreamReader(is);
	        while (reader.hasNext()) { 
	            Record record = reader.next();
	            List<DataField> dataFields = (List<DataField>)(record.getDataFields()); //Data fields: 010-999
			    for(DataField dataField : dataFields) {
			    	//Get title field
					if(dataField.getTag().equals("541")) {
						// Technically we should only be looking for an exact match of "Seymour Durst",
						// but I've seen other values like "Seymour Durst;", so I'm using contains() instead of equals(). 
						if(dataField.getSubfield('a').getData().contains("Seymour Durst")) {
							durstHoldings.put(holdingKey, holdingData);	
						}
					}
				}
	        }
	        is.close();
		}
		
		return durstHoldings;
	}
	
	
	/**
	 * Returns a HashMap containing the raw data for one or more holdings, mapping holdings ids to holdings values.
	 * @param clioBibKey
	 * @return
	 * @throws SQLException
	 */
	public static HashMap<String, String> getRawMarcHoldingsDataForBibKey(String clioBibKey) throws SQLException {
		
		String query;
		PreparedStatement pstmt;
		
		query = "select mfhd_id from bib_mfhd where bib_id = ?";
		pstmt = staticVoyagerConnection.prepareStatement(query);
		pstmt.setString(1, clioBibKey);
		
		ResultSet resultSet = pstmt.executeQuery();
		
		ArrayList<String> holdingsKeys = new ArrayList<String>(); 
		HashMap<String, String> holdingsKeysToRecords = new HashMap<String, String>(); 
		
		while(resultSet.next()) {
			holdingsKeys.add(resultSet.getString(1));
		}
		
		resultSet.close();
		pstmt.close();
		
		// Now we have one or more holdingsKeys for this record.
		// We'll need to iterate through these keys, look up
		// each of the holdings, figure out which holding is
		// the one that we want, and then get the 866 and 950 fields from it.
		// Note: We'll be getting raw MARC back for the holdings records.
		// Note: Holdings records are stored in 300 character segments, so
		// if a query for a specific record gives us multiple results, we
		// need to concatenate those results into a single holdings string.
		
		query = "select record_segment from mfhd_data where mfhd_id = ? order by seqnum";
		
		for(String singleHoldingsKey : holdingsKeys) {
			
			pstmt = staticVoyagerConnection.prepareStatement(query);
			
			pstmt.setString(1, singleHoldingsKey);
			resultSet = pstmt.executeQuery();
			
			String holdingsResultData = ""; 
			
			while(resultSet.next()) {
				holdingsResultData += resultSet.getString(1);
			}
			
			holdingsKeysToRecords.put(singleHoldingsKey, holdingsResultData);
			
			resultSet.close();
			pstmt.close();
		}
		
		return holdingsKeysToRecords;
	}
	
	
	
	
	
	
	
	/*
	//This method isn't getting these values in the best way. We should read the MARC instead of capturing
	//patterns with a regular expression matcher.
	public static ArrayList<ArrayList<String>> getValuesFor866And950Fields(String clioBibKey) throws SQLException {
		
		ArrayList<String> valuesFor866Fields = new ArrayList<String>();
		ArrayList<String> valuesFor950Fields = new ArrayList<String>();
		
		String query;
		PreparedStatement pstmt;
		
		query = "select mfhd_id from bib_mfhd where bib_id = ?";
		pstmt = staticVoyagerConnection.prepareStatement(query);
		pstmt.setString(1, clioBibKey);
		
		ResultSet resultSet = pstmt.executeQuery();
		
		ArrayList<String> holdingsKeys = new ArrayList<String>(); 
		
		while(resultSet.next()) {
			holdingsKeys.add(resultSet.getString(1));
		}
		
		resultSet.close();
		pstmt.close();
		
		// Now we have one or more holdingsKeys for this record.
		// We'll need to iterate through these keys, look up
		// each of the holdings, figure out which holding is
		// the one that we want, and then get the 866 and 950 fields from it.
		// Note: We'll be getting raw MARC back for the holdings records.
		// Note: Holdings records are stored in 300 character segments, so
		// if a query for a specific record gives us multiple results, we
		// need to concatenate those results into a single holdings string.
		
		query = "select record_segment from mfhd_data where mfhd_id = ? order by seqnum";
		
		for(String singleHoldingsKey : holdingsKeys) {
			
			pstmt = staticVoyagerConnection.prepareStatement(query);
			
			pstmt.setString(1, singleHoldingsKey);
			resultSet = pstmt.executeQuery();
			
			String holdingsResultData = ""; 
			
			while(resultSet.next()) {
				holdingsResultData += resultSet.getString(1);
			}
			
			Matcher matcher;
			
			//Find DIM id
			matcher = dimsIdPattern.matcher(holdingsResultData);
			if(matcher.matches()) {
				valuesFor950Fields.add(matcher.group(1));
			}
			
			//Find Enumeration and Chronology
			matcher = enumerationAndChronologyPattern.matcher(holdingsResultData);
			if(matcher.matches()) {
				valuesFor866Fields.add(matcher.group(1));
			}
			
			resultSet.close();
			pstmt.close();
			
			//System.out.println("Holdings Result Data: " + holdingsResultData);
		}
		
		ArrayList<ArrayList<String>> arrToReturn = new ArrayList<ArrayList<String>>();
		arrToReturn.add(0, valuesFor866Fields);
		arrToReturn.add(1, valuesFor950Fields);
		
		return arrToReturn;
	}
	*/
	
}

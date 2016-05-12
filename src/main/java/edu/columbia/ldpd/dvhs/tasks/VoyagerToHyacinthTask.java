package edu.columbia.ldpd.dvhs.tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import edu.columbia.ldpd.dvhs.DurstRecord;
import edu.columbia.ldpd.dvhs.DurstVoyagerHyacinthSync;
import edu.columbia.ldpd.dvhs.VoyagerOracleDBHelper;
import edu.columbia.ldpd.dvhs.exceptions.MultipleRecordsException;
import edu.columbia.ldpd.dvhs.tasks.workers.VoyagerToHyacinthWorker;
import edu.columbia.ldpd.dvhs.utils.HyacinthUtils;
import edu.columbia.ldpd.marc.z3950.MARCFetcher;

public class VoyagerToHyacinthTask extends AbstractTask {
	
	public VoyagerToHyacinthTask() {
		
	}

	@Override
	public void taskImpl() {
		
		//Get data from Voyager MARC records
		
		//Download latest version (unless we're reusing the latest downloaded set)
		if( DurstVoyagerHyacinthSync.reuseLatestDownloadedVoyagerData ) {
			DurstVoyagerHyacinthSync.logger.info("Reusing already-downloaded MARC files (instead of re-downloading the latest set).");
		} else {
			DurstVoyagerHyacinthSync.logger.info("Clearing old MARC data downloads (bib, holdings, barcodes)...");
			clearDownloadedVoyagerContent();
			downloadEverythingFromVoyager(DurstVoyagerHyacinthSync.DURST_965_MARKERS);
		}
		
		// Collect list of marcXmlFiles in File array   
		File dir = new File(DurstVoyagerHyacinthSync.MARC_BIB_DOWNLOAD_DIR);
		File[] marcXmlFiles = dir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".xml");
		    }
		});
		
		processFiles(marcXmlFiles);
        
		DurstVoyagerHyacinthSync.logger.info("Done.");
	}
	
	public void processFiles(File[] marcXmlFiles) {
		int numberOfFilesToProcess = marcXmlFiles.length;
		DurstVoyagerHyacinthSync.logger.info("Now processing " + numberOfFilesToProcess + " MARC files...");
		
		// Set up print and electronic record lists
		ArrayList<DurstRecord> printRecords = new ArrayList<DurstRecord>();
		ArrayList<DurstRecord> electronicRecords = new ArrayList<DurstRecord>();
		// Set up ocolc mappings
		HashMap<String, ArrayList<DurstRecord>> electronicRecordOcolcValuesToRecords = new HashMap<String, ArrayList<DurstRecord>>();
		HashMap<String, ArrayList<DurstRecord>> printRecordOcolcValuesToRecords = new HashMap<String, ArrayList<DurstRecord>>();
		
		// Build records from data sources
		int counter = 1;
		for(File marcXmlFile : marcXmlFiles) {
			
			if(DurstVoyagerHyacinthSync.maxNumberOfRecordsToSync != -1 && counter > DurstVoyagerHyacinthSync.maxNumberOfRecordsToSync) {
            	DurstVoyagerHyacinthSync.logger.info("Only preparing " + DurstVoyagerHyacinthSync.maxNumberOfRecordsToSync + " MARC records because of limit imposed by command line arg max_number_of_records_to_sync.");
            	break;
            }
			
			try {
				//Get bib id from Marc file filename
				String bibId = marcXmlFile.getName().replace(".xml", "");
				
				//Read in associated holdings
				File[] holdingsFiles = new File(DurstVoyagerHyacinthSync.MARC_HOLDINGS_DOWNLOAD_DIR + "/" + bibId).listFiles(new FilenameFilter() {
				    public boolean accept(File dir, String name) {
				        return name.toLowerCase().endsWith(".mrc");
				    }
				});
				
				//Read in associated barcodes
				String[] barcodes = null;
				File possibleBarcodeFile = new File(DurstVoyagerHyacinthSync.MARC_BARCODES_DOWNLOAD_DIR + "/" + bibId + ".barcodes");
				if( possibleBarcodeFile.exists() ) {
					barcodes = FileUtils.readFileToString(possibleBarcodeFile).split(",");
				}
				
				DurstRecord record = new DurstRecord(marcXmlFile, holdingsFiles, barcodes);
				
				if(record.isElectronicRecord()) {
					electronicRecords.add(record);
					// Use 776 value  (if present) for electronic record ocolc value
					for(String value776 : record.getOcolc776FieldValues()) {
						if( !electronicRecordOcolcValuesToRecords.containsKey(value776) ) {
							electronicRecordOcolcValuesToRecords.put(value776, new ArrayList<DurstRecord>());
						}
						electronicRecordOcolcValuesToRecords.get(value776).add(record);
					}
				} else {
					printRecords.add(record);
					// Use 035 value  (if present) for electronic record ocolc value
					for(String value035 : record.getOcolc035FieldValues()) {
						if( !printRecordOcolcValuesToRecords.containsKey(value035) ) {
							printRecordOcolcValuesToRecords.put(value035, new ArrayList<DurstRecord>());
						}
						printRecordOcolcValuesToRecords.get(value035).add(record);
					}
					
					// If this is a PRINT record, check to see if Hyacinth
					// record exists for this Durst voyager record, based on
					// CLIO id. If so, assign that PID to this record so that we
					// do an UPDATE operation instead of CREATE.
					// Note: We don't need to do this for electronic records
					// because they're always merged into print records based on
					// 035/776 matching, and we CAN'T do this for electronic
					// records because the same electronic record could be
					// merged into multiple print records, so we wouldn't know
					// which pid to choose for a given electronic record.
					String pid = HyacinthUtils.getPidForClioIdentifier(record.getClioIdentifiers().get(0));
					if(pid != null) {
						record.setPid(pid);
					}
					
				}
				
			} catch (FileNotFoundException e) {
				DurstVoyagerHyacinthSync.logger.error(
					"Could not find file: " + marcXmlFile.getAbsolutePath() + "\n" +
					"Message: " + e.getMessage()
				);
				System.exit(DurstVoyagerHyacinthSync.EXIT_CODE_ERROR);
				return;
			} catch (IOException e) {
				DurstVoyagerHyacinthSync.logger.error(
					"IOException encountered while processing file: " + marcXmlFile.getAbsolutePath() + "\n" +
					"Message: " + e.getMessage()
				);
				System.exit(DurstVoyagerHyacinthSync.EXIT_CODE_ERROR);
				return;
			} catch (MultipleRecordsException | JSONException e) {
				// TODO Auto-generated catch block
				DurstVoyagerHyacinthSync.logger.error(e.getClass().getName() + ": " + e.getMessage());
				System.exit(DurstVoyagerHyacinthSync.EXIT_CODE_ERROR);
				return;
			}
			DurstVoyagerHyacinthSync.logger.info("Prepared " + counter + " of " + numberOfFilesToProcess + " Voyager records.");
			counter++;
		}
		
		// Merge records based on ocolc code
		
		// Here's what we want:
		// - All physical (965durst) records get their own Hyacinth records.
		// - Electronic (965eDurst) records that match to physical records are only merged into those physical records.
		// - Update from meeting on 2015-06-02: We are no longer importing electronic records that aren't tied to print records.
		
		ArrayList<DurstRecord> finalizedRecordsToImport = new ArrayList<DurstRecord>();
		HashSet<DurstRecord> electronicRecordsThatWereMergedIn = new HashSet<DurstRecord>(); // Just for debugging
		ArrayList<String> mergeErrors = new ArrayList<String>();
		
		for(DurstRecord printRecord : printRecords) {
			// Get electronic durst records that should be linked to this print one
			ArrayList<DurstRecord> electronicRecordsToMergeIn = new ArrayList<DurstRecord>();
			
			// Determine which electronic records (if any) should be merged into this print record
			if(printRecord.getOcolc035FieldValues().size() > 0) {
				for(String marc035Value : printRecord.getOcolc035FieldValues()) {
					if(electronicRecordOcolcValuesToRecords.containsKey(marc035Value)) {
						for(DurstRecord electronicRecord : electronicRecordOcolcValuesToRecords.get(marc035Value)) {
							electronicRecordsToMergeIn.add(electronicRecord);
							electronicRecordsThatWereMergedIn.add(electronicRecord);
						}						
					}
				}
			}
			
			// Merge electronic records (if present) into this print record
			String mergeError = null;
			try {
				mergeError = DurstRecord.mergeElectronicRecordDataIntoPrintRecord(printRecord, electronicRecordsToMergeIn);
			} catch (JSONException e) {
				ArrayList<String> electronicRecordClioIdentifiers = new ArrayList<String>();
				for(DurstRecord record : electronicRecordsToMergeIn) {
					electronicRecordClioIdentifiers.addAll(record.getClioIdentifiers());
				}
				DurstVoyagerHyacinthSync.logger.error("Problem encountered while merging electronic records " + StringUtils.join(electronicRecordClioIdentifiers, ",") + " into print record " + printRecord.getClioIdentifiers().get(0));
				System.exit(DurstVoyagerHyacinthSync.EXIT_CODE_ERROR);
			}
			if(mergeError != null) {
				mergeErrors.add(mergeError);
			} else {
				finalizedRecordsToImport.add(printRecord);
			}
		}
		
		//Check for merge errors
		if(mergeErrors.size() > 0) {
			DurstVoyagerHyacinthSync.logger.error("Cancelling Voyager to Hyacinth update process because merge errors were encountered:\n" + StringUtils.join(mergeErrors, "\n"));
			System.exit(DurstVoyagerHyacinthSync.EXIT_CODE_ERROR);
		}
		
		sendRecordsToHyacinth(finalizedRecordsToImport);
		
		DurstVoyagerHyacinthSync.logger.info("--------------------------------");
		DurstVoyagerHyacinthSync.logger.info("Print records found: " + printRecords.size());
		DurstVoyagerHyacinthSync.logger.info("Electronic records found: " + electronicRecords.size());
		DurstVoyagerHyacinthSync.logger.info("Print record ocolc values found: " + printRecordOcolcValuesToRecords.size());
		DurstVoyagerHyacinthSync.logger.info("Electronic record ocolc values found: " + electronicRecordOcolcValuesToRecords.size());
		DurstVoyagerHyacinthSync.logger.info("Electronic records merged in: " + electronicRecordsThatWereMergedIn.size());
		DurstVoyagerHyacinthSync.logger.info("Number of merged records sent to Hyacinth: " + finalizedRecordsToImport.size());
		DurstVoyagerHyacinthSync.logger.info("--------------------------------");
	}
	
	private void sendRecordsToHyacinth(ArrayList<DurstRecord> finalizedRecordsToImport) {
		// If we made it here, we're ready to send the records to Hyacinth
		DurstVoyagerHyacinthSync.logger.info("Preparing to send records to Hyacinth...");
		DurstVoyagerHyacinthSync.logger.info("Starting thread pool of size: " + DurstVoyagerHyacinthSync.maxNumberOfThreads);
		ExecutorService executor = Executors.newFixedThreadPool(DurstVoyagerHyacinthSync.maxNumberOfThreads);
		int i = 1;
		int total = finalizedRecordsToImport.size();
		for(DurstRecord record : finalizedRecordsToImport) {
			Runnable worker = new VoyagerToHyacinthWorker(record, i, total);
            executor.execute(worker);
            DurstVoyagerHyacinthSync.logger.info("Queued " + i + " of " + total);
            i++;
		}
		executor.shutdown();
        while (!executor.isTerminated()) {
        	try {
				Thread.sleep(500);
			} catch (InterruptedException e) { e.printStackTrace(); }
        }
        DurstVoyagerHyacinthSync.logger.info("Thread pool has been shut down. All jobs are complete.");
	}

	public void clearDownloadedVoyagerContent() {
		try {
			FileUtils.deleteDirectory(new File(DurstVoyagerHyacinthSync.MARC_BIB_DOWNLOAD_DIR));
			FileUtils.deleteDirectory(new File(DurstVoyagerHyacinthSync.MARC_HOLDINGS_DOWNLOAD_DIR));
			FileUtils.deleteDirectory(new File(DurstVoyagerHyacinthSync.MARC_BARCODES_DOWNLOAD_DIR));
		} catch (IOException e) {
			DurstVoyagerHyacinthSync.logger.error("Could not delete all downloaded voyager content for some reason. Tried to delete directory: " + DurstVoyagerHyacinthSync.MARC_BIB_DOWNLOAD_DIR);
			e.printStackTrace();
		}
	}
	
	public void downloadEverythingFromVoyager(String[] field965MarkersToRetrieve) {
		// Verify that voyager connection works (for downloading holdings and barcode info). We don't want to go through all
		// the trouble of generating thousands of MARC XML files if we can't
		// actually process them properly.
		VoyagerOracleDBHelper.testVoyagerConnection();
		
		// Create download paths on filesystem
		new File(DurstVoyagerHyacinthSync.MARC_BIB_DOWNLOAD_DIR).mkdirs();
		new File(DurstVoyagerHyacinthSync.MARC_HOLDINGS_DOWNLOAD_DIR).mkdirs();
		new File(DurstVoyagerHyacinthSync.MARC_BARCODES_DOWNLOAD_DIR).mkdirs();
		
		// Perform MARC bib record download
		for(int i = 0; i < DurstVoyagerHyacinthSync.DURST_965_MARKERS.length; i++) {
			String marc965Value = DurstVoyagerHyacinthSync.DURST_965_MARKERS[i];
			
			DurstVoyagerHyacinthSync.logger.info("Fetching new MARC records with 965 marker [marker " + (i+1) + " of " + (DurstVoyagerHyacinthSync.DURST_965_MARKERS.length) + "]: " + marc965Value);
			MARCFetcher marcFetcher = new MARCFetcher(new File(DurstVoyagerHyacinthSync.MARC_BIB_DOWNLOAD_DIR));
			marcFetcher.fetch(1, 9000, marc965Value);
			DurstVoyagerHyacinthSync.logger.info("Done fetching new MARC records with 965 marker: " + marc965Value);
		}
		
		// Collect file handles for MARC XML bib record files
		File dir = new File(DurstVoyagerHyacinthSync.MARC_BIB_DOWNLOAD_DIR);
		File[] marcXmlFiles = dir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".xml");
		    }
		});
		
		// Open connection to Voyager Oracle DB 
		VoyagerOracleDBHelper.openStaticConnection();
		
		//Get holdings and barcodes for each MARC bib record
		for(int i = 0; i < marcXmlFiles.length; i++) {
			File marcXmlFile = marcXmlFiles[i];
			String bibKey = FilenameUtils.removeExtension(marcXmlFile.getName());
			DurstVoyagerHyacinthSync.logger.info("Downloading holdings and barcodes for bib key " + i + " of " + marcXmlFiles.length + " (" + bibKey + ")");
			
			try {
				//Get holdings
				HashMap<String, String> holdingsData = VoyagerOracleDBHelper.getRawMarcDurstOnlyHoldingsDataForBibKey(bibKey);
				if(holdingsData.size() > 0) {
					for(Entry<String, String> entry : holdingsData.entrySet()) {
						String key = entry.getKey();
						String value = entry.getValue();
						try {
							FileUtils.writeStringToFile(new File(DurstVoyagerHyacinthSync.MARC_HOLDINGS_DOWNLOAD_DIR + "/" + bibKey + "/" + key + ".mrc"), value);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				
				//Get barcodes from the "Item Record" (which is different from the "Bib Record"
				ArrayList<String> barcodes = VoyagerOracleDBHelper.getBarcodesFromItemRecordForBibKey(bibKey);
				if (barcodes.size() > 0) {
					FileUtils.writeStringToFile(new File(DurstVoyagerHyacinthSync.MARC_BARCODES_DOWNLOAD_DIR + "/" + bibKey + ".barcodes"), StringUtils.join(barcodes, ","));					
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
		
		// Close connection to Voyager Oracle DB 
		VoyagerOracleDBHelper.closeStaticConnection();
	}
	
}

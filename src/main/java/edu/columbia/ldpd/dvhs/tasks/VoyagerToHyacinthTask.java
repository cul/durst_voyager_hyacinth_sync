package edu.columbia.ldpd.dvhs.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import edu.columbia.ldpd.dvhs.DurstVoyagerHyacinthSync;
import edu.columbia.ldpd.dvhs.VoyagerOracleDBHelper;
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
		int numberOfFilesToProcess = marcXmlFiles.length;
		
		DurstVoyagerHyacinthSync.logger.info("Now processing " + numberOfFilesToProcess + " MARC files...");
		
		//Process records
		int counter = 1;
		for(File marcXmlFile : marcXmlFiles) {
			try {
				FileInputStream fis = new FileInputStream(marcXmlFile);
				
				
				
				//TODO: Do something with fis
				
				
				
				fis.close();
			} catch (FileNotFoundException e) {
				DurstVoyagerHyacinthSync.logger.error(
					"Could not find file: " + marcXmlFile.getAbsolutePath() + "\n" +
					"Message: " + e.getMessage()
				);
			} catch (IOException e) {
				DurstVoyagerHyacinthSync.logger.error(
					"IOException encountered while processing file: " + marcXmlFile.getAbsolutePath() + "\n" +
					"Message: " + e.getMessage()
				);
			}
			System.out.println("Processed " + counter + " of " + numberOfFilesToProcess + " Site records.");
			counter++;
		}
        
        System.out.println("Done.");
	}
	
	public void clearDownloadedVoyagerContent() {
		try {
			FileUtils.deleteDirectory(new File(DurstVoyagerHyacinthSync.MARC_BIB_DOWNLOAD_DIR));
			FileUtils.deleteDirectory(new File(DurstVoyagerHyacinthSync.MARC_HOLDINGS_DOWNLOAD_DIR));
			FileUtils.deleteDirectory(new File(DurstVoyagerHyacinthSync.MARC_BARCODES_DOWNLOAD_DIR));
		} catch (IOException e) {
			System.out.println("Could not delete all downloaded voyager content for some reason. Tried to delete directory: " + DurstVoyagerHyacinthSync.MARC_BIB_DOWNLOAD_DIR);
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
			System.out.println("Downloading holdings and barcodes for bib key " + i + " of " + marcXmlFiles.length + " (" + bibKey + ")");
			
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

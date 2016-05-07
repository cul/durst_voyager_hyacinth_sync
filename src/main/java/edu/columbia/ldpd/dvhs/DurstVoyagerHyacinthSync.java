package edu.columbia.ldpd.dvhs;

import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.ldpd.dvhs.tasks.AbstractTask;
import edu.columbia.ldpd.dvhs.tasks.TestTask;
import edu.columbia.ldpd.dvhs.tasks.VoyagerConnectionTestTask;
import edu.columbia.ldpd.dvhs.tasks.VoyagerToHyacinthTask;

public class DurstVoyagerHyacinthSync {

	public static final int EXIT_CODE_SUCCESS = 0;
	public static final int EXIT_CODE_ERROR = 1;
	public static final int BYTES_IN_A_MEGABYTE = 1048576;
	
	public static final long APP_START_TIME_IN_MILLIS = System.currentTimeMillis();
	public static final long MAX_AVAILABLE_MEMORY_IN_BYTES = Runtime.getRuntime().maxMemory();
	
	public static final String MARC_BIB_DOWNLOAD_DIR = "tmp/marcxml-download";
	public static final String MARC_HOLDINGS_DOWNLOAD_DIR = "tmp/marc-holdings-download";
	public static final String MARC_BARCODES_DOWNLOAD_DIR = "tmp/marc-barcodes-download";
	
	public static final String MARC_965_DURST_MARKER = "965durst";
	public static final String MARC_965_E_DURST_MARKER = "965eDurst";
	public static final String[] DURST_965_MARKERS = {MARC_965_E_DURST_MARKER, MARC_965_DURST_MARKER};
	
	public static final Logger logger = LoggerFactory.getLogger(DurstVoyagerHyacinthSync.class);

	// Options from command line
	public static String hyacinthAppUrl;
	public static String hyacinthUserEmail;
	public static String hyacinthUserPassword;
	public static String voyagerOracleDBUrl;
	public static String voyagerOracleDBDatabase;
	public static String voyagerOracleDBUsername;
	public static String voyagerOracleDBPassword;
	
	public static boolean reuseLatestDownloadedVoyagerData;
	public static boolean publishAfterSave;
	public static boolean doTestSaveOnly;
	
	public static boolean runTaskVoyagerToHyacinth;
	public static boolean runTaskVoyagerConnectionTest;
	public static boolean runTaskTest;
	
	public static int maxNumberOfRecordsToSync; //only used during development

	public static int maxNumberOfThreads;
	public static long minAvailableMemoryInBytesForNewProcess;
	
	public static void main(String[] args) {
		
		logger.info("Starting Durst Voyager Hyacinth Sync run.");
		logger.debug("Logger logging in debug mode.");
		
		setupAndParseCommandLineOptions(args);
		
		logger.info(
			"Run configuration:" + "\n" +
			"\n" +
			"- Hyacinth App URL: " + DurstVoyagerHyacinthSync.hyacinthAppUrl + "\n" +
			"- Hyacinth User Email : " + DurstVoyagerHyacinthSync.hyacinthUserEmail + "\n" +
			"- Hyacinth User Password : " + (DurstVoyagerHyacinthSync.hyacinthUserPassword == null ? "[Not provided]" : "********") + "\n" +
			"\n" +
			"- Voyager Oracle DB URL: " + DurstVoyagerHyacinthSync.voyagerOracleDBUrl + "\n" +
			"- Voyager Oracle DB Database Name: " + DurstVoyagerHyacinthSync.voyagerOracleDBDatabase + "\n" +
			"- Voyager Oracle DB Username: " + DurstVoyagerHyacinthSync.voyagerOracleDBUsername + "\n" +
			"- Voyager Oracle DB Password : " + (DurstVoyagerHyacinthSync.voyagerOracleDBPassword == null ? "[Not provided]" : "********") + "\n" +
			"\n" +
			"- Max number of threads: " + DurstVoyagerHyacinthSync.maxNumberOfThreads + "\n" +
			"- Min available memory (in bytes) for new process: " + DurstVoyagerHyacinthSync.minAvailableMemoryInBytesForNewProcess + " (" + (DurstVoyagerHyacinthSync.minAvailableMemoryInBytesForNewProcess/BYTES_IN_A_MEGABYTE) + " MB)" +
			"\n"
		);
		
		ArrayList<AbstractTask> tasksToRun = new ArrayList<AbstractTask>();
		
		//Proper task order defined below
		if(DurstVoyagerHyacinthSync.runTaskVoyagerToHyacinth){ tasksToRun.add(new VoyagerToHyacinthTask()); }
		if(DurstVoyagerHyacinthSync.runTaskVoyagerConnectionTest) { tasksToRun.add(new VoyagerConnectionTestTask()); }
		if(DurstVoyagerHyacinthSync.runTaskTest) { tasksToRun.add(new TestTask()); }
		
		for(AbstractTask task : tasksToRun) {
			task.runTask();
		}
		
		System.out.println("DurstVoyagerHyacinthSync run complete!");

	}

	public static void setupAndParseCommandLineOptions(String[] args) {
		// create the Options
		Options options = new Options();

		// Boolean options
		options.addOption("help", false, "Usage information.");
		// Options with values
		options.addOption("hyacinth_app_url", true,
				"URL of the Hyacinth app where that you want to sync data to.");
		options.addOption("hyacinth_user_email", true,
				"Email address used to log into Hyacinth.");
		options.addOption("hyacinth_user_password", true,
				"Password for the email address used to log into Hyacinth.");
		options.addOption("voyager_oracle_db_url", true,
				"URL of the Voyager Oracle instance that you want to connect to.");
		options.addOption("voyager_oracle_db_database", true,
				"Name of the Voyager Orable database that you want to connect to.");
		options.addOption("voyager_oracle_db_username", true,
				"Username used to log into the specified Voyager Oracle database.");
		options.addOption("voyager_oracle_db_password", true,
				"Password for the username used to log into the specified Voyager Oracle database.");
		options.addOption("reuse_latest_downloaded_marc_data", false,
				"Reuse latest copy of downloaded marc data rather than downloading the latest version.");
		options.addOption("publish_after_save", false,
				"Publish records after saving them in Hyacinth (applies to the Voyager to Hyacinth sync task).");
		options.addOption("do_test_save_only", false,
				"Send the test param when saving Hyacinth records so that they're not actually saved (applies to the Voyager to Hyacinth sync task).");
		// Task options
		options.addOption("run_task_voyager_to_hyacinth", false,
				"Process site data from Voyager (and related hosts file) and save it to Hyacinth.");
		options.addOption("run_task_voyager_connection_test", false,
				"Test connection to voyager.");
		options.addOption("run_task_test", false,
				"Test task. Actual function varies. Only used during development).");
		//For Testing
		options.addOption("max_number_of_records_to_sync", true,
				"Maximum number of records to sync (applies to the Voyager to Hyacinth sync task, only used during development).");
		// Thread/Memory options
		options.addOption("max_number_of_threads", true,
				"Maximum number of threads to use for concurrent processing (when applicable).");
		options.addOption("min_available_memory_in_bytes_for_new_process", true,
				"Minimum number of free bytes of memory required for starting a new processing job (when applicable).");
		
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmdLine = parser.parse(options, args);

			if (args.length == 0 || cmdLine.hasOption("help")) {
				// Show help
				HelpFormatter hf = new HelpFormatter();
				hf.printHelp("durst_voyager_hyacinth_sync [options]", options);
				System.exit(DurstVoyagerHyacinthSync.EXIT_CODE_SUCCESS);
			} else {
				// Handle actual options
				DurstVoyagerHyacinthSync.hyacinthAppUrl = cmdLine.getOptionValue("hyacinth_app_url", null);
				DurstVoyagerHyacinthSync.hyacinthUserEmail = cmdLine.getOptionValue("hyacinth_user_email", null);
				DurstVoyagerHyacinthSync.hyacinthUserPassword = cmdLine.getOptionValue("hyacinth_user_password", null);
				
				DurstVoyagerHyacinthSync.voyagerOracleDBUrl = cmdLine.getOptionValue("voyager_oracle_db_url", null);
				DurstVoyagerHyacinthSync.voyagerOracleDBDatabase = cmdLine.getOptionValue("voyager_oracle_db_database", null);
				DurstVoyagerHyacinthSync.voyagerOracleDBUsername = cmdLine.getOptionValue("voyager_oracle_db_username", null);
				DurstVoyagerHyacinthSync.voyagerOracleDBPassword = cmdLine.getOptionValue("voyager_oracle_db_password", null);
				
				DurstVoyagerHyacinthSync.reuseLatestDownloadedVoyagerData = cmdLine.hasOption("reuse_latest_downloaded_marc_data");
				DurstVoyagerHyacinthSync.publishAfterSave = cmdLine.hasOption("publish_after_save");
				DurstVoyagerHyacinthSync.doTestSaveOnly = cmdLine.hasOption("do_test_save_only");
				DurstVoyagerHyacinthSync.runTaskVoyagerToHyacinth = cmdLine.hasOption("run_task_voyager_to_hyacinth");
				DurstVoyagerHyacinthSync.runTaskVoyagerConnectionTest = cmdLine.hasOption("run_task_voyager_connection_test");
				DurstVoyagerHyacinthSync.runTaskTest = cmdLine.hasOption("run_task_test");
				
				DurstVoyagerHyacinthSync.maxNumberOfRecordsToSync = Integer.parseInt(cmdLine.getOptionValue("max_number_of_records_to_sync", "-1"));
				DurstVoyagerHyacinthSync.maxNumberOfThreads = Integer.parseInt(cmdLine.getOptionValue("max_number_of_threads", "1"));
				
				if(cmdLine.hasOption("min_available_memory_in_bytes_for_new_process")) {
					DurstVoyagerHyacinthSync.minAvailableMemoryInBytesForNewProcess = Long.parseLong(cmdLine.getOptionValue("min_available_memory_in_bytes_for_new_process"));
				} else {
					DurstVoyagerHyacinthSync.minAvailableMemoryInBytesForNewProcess = (MAX_AVAILABLE_MEMORY_IN_BYTES/4L);
				}
			}

		} catch (ParseException e) {
			logger.error("Error parsing command line args: " + e.getMessage());
			e.printStackTrace();
			System.exit(DurstVoyagerHyacinthSync.EXIT_CODE_ERROR);
		}
	}
	
	public static String getCurrentAppMemoryUsageMessage() {
		return "Current memory usage: " + (getCurrentAppMemoryUsageInBytes()/BYTES_IN_A_MEGABYTE) + "/" + (MAX_AVAILABLE_MEMORY_IN_BYTES/BYTES_IN_A_MEGABYTE) + " MB";
	}
	
	public static String getCurrentAppRunTime() {
		return "Current run time: " + ((System.currentTimeMillis() - DurstVoyagerHyacinthSync.APP_START_TIME_IN_MILLIS)/1000) + "seconds";
	}
	
	public static long getCurrentAppMemoryUsageInBytes() {
		return Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
	}
	
	public static long getFreeMemoryInBytes() {
		return Runtime.getRuntime().freeMemory();
	}

}

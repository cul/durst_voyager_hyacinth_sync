package edu.columbia.ldpd.dvhs.tasks.workers;

import edu.columbia.ldpd.dvhs.DurstRecord;
import edu.columbia.ldpd.dvhs.DurstVoyagerHyacinthSync;

public class VoyagerToHyacinthWorker implements Runnable {
	
	private DurstRecord record;
	private int jobNumber;
	private int totalNumberOfJobs;
	
	public VoyagerToHyacinthWorker(DurstRecord record, int jobNumber, int totalNumberOfJobs) {
		this.record = record;
		this.jobNumber = jobNumber;
		this.totalNumberOfJobs = totalNumberOfJobs;
	}

	@Override
	public void run() {
		
		if(this.record.pidExistsInHyacinth()) {
			DurstVoyagerHyacinthSync.logger.info("Sending " + (record.getPid() == null ? "new record" : ("record with pid=" + record.getPid())) + " to Hyacinth. (" + jobNumber +  " of " + totalNumberOfJobs + ")");
		}
		
//		if(DurstVoyagerHyacinthSync.updateAllVoyagerRecords || record.recordMarc005ValueIsDifferentThanHyacinthValue()) {
//			record.sendRecordToHyacinth(true, false); // Change to true, false for real sending				
//		} else {
//			DurstVoyagerHyacinthSync.logger.info("Skipping " + record.pid + " update because 005 value is unchanged.");
//		}
		
	}

}

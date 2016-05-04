package edu.columbia.ldpd.dvhs.tasks.workers;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import edu.columbia.ldpd.dvhs.DurstRecord;
import edu.columbia.ldpd.dvhs.DurstVoyagerHyacinthSync;
import edu.columbia.ldpd.dvhs.utils.HyacinthUtils;

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
		try {
			if(HyacinthUtils.recordHasSpecificMarc005Value(record.getPid(), record.getMarc005Value())) {
				// Skipping record update because this record hasn't changed (based on marc005 value)
				DurstVoyagerHyacinthSync.logger.info("Skipping " + record.getPid() + " update because 005 value is unchanged.");
			} else {
				// Perform record update
				DurstVoyagerHyacinthSync.logger.info("Sending " + (record.getPid() == null ? "new record" : ("record with pid=" + record.getPid())) + " to Hyacinth. (" + jobNumber +  " of " + totalNumberOfJobs + ")");
				HyacinthUtils.sendDurstRecordToHyacinth(this.record, DurstVoyagerHyacinthSync.publishAfterSave, DurstVoyagerHyacinthSync.doTestSaveOnly);
			}
		} catch (JSONException | IOException e) {
			DurstVoyagerHyacinthSync.logger.error(e.getClass().getName() + " for record with pid " + record.getPid() + " and clio identifiers " + StringUtils.join(record.getClioIdentifiers(), ",") + ": " + e.getMessage() + "\n" + StringUtils.join(e.getStackTrace(), "\n"));
		}
	}

}

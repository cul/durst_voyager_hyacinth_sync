package edu.columbia.ldpd.dvhs.tasks;

import edu.columbia.ldpd.dvhs.DurstVoyagerHyacinthSync;

public abstract class AbstractTask {
	
	long startTime;
	
	public abstract void taskImpl();
	
	public void runTask() {
		beginTask();
		taskImpl();
		endTask();
	}
	
	
	public void beginTask() {
		this.startTime = System.currentTimeMillis() / 1000L;
		DurstVoyagerHyacinthSync.logger.info("Started Task: " + this.getClass().getSimpleName());
	}
	
	public void endTask() {
		DurstVoyagerHyacinthSync.logger.info("Finished Task: " + this.getClass().getSimpleName() + " in " + ((System.currentTimeMillis() / 1000L) - this.startTime) + " seconds");
	}
	
}

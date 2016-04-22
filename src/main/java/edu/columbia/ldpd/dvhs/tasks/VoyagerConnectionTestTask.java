package edu.columbia.ldpd.dvhs.tasks;

import edu.columbia.ldpd.dvhs.VoyagerOracleDBHelper;

public class VoyagerConnectionTestTask extends AbstractTask {
	
	public VoyagerConnectionTestTask() {
		
	}

	@Override
	public void taskImpl() {
		VoyagerOracleDBHelper.testVoyagerConnection();
	}
	
}

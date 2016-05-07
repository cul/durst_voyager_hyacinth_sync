package edu.columbia.ldpd.dvhs.tasks;

import java.io.IOException;

import org.json.JSONException;

import edu.columbia.ldpd.dvhs.exceptions.MultipleRecordsException;
import edu.columbia.ldpd.dvhs.utils.HyacinthUtils;

public class TestTask extends AbstractTask {
	
	public TestTask() {
		
	}

	@Override
	public void taskImpl() {
		try {
			String pid = HyacinthUtils.getPidForClioIdentifier("2468");
			System.out.println("GOT PID! " + pid);
			
			String expectedMarc005Value = "1.21";
			boolean foundExpectedMarc005FieldValue = HyacinthUtils.recordHasSpecificMarc005Value(pid, expectedMarc005Value);
			System.out.println("Found expected MARC 005 value of " + expectedMarc005Value + "? " + foundExpectedMarc005FieldValue);
			
		} catch (MultipleRecordsException | IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

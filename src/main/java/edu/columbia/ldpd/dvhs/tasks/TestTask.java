package edu.columbia.ldpd.dvhs.tasks;

import java.util.ArrayList;

import edu.columbia.ldpd.dvhs.DurstRecord;
import edu.columbia.ldpd.dvhs.exceptions.UnhandledCoordinateFormatException;

public class TestTask extends AbstractTask {
	
	public TestTask() {
		
	}

	@Override
	public void taskImpl() {
//		try {
//			String pid = HyacinthUtils.getPidForClioIdentifier("2468");
//			System.out.println("GOT PID! " + pid);
//			
//			String expectedMarc005Value = "1.21";
//			boolean foundExpectedMarc005FieldValue = HyacinthUtils.recordHasSpecificMarc005Value(pid, expectedMarc005Value);
//			System.out.println("Found expected MARC 005 value of " + expectedMarc005Value + "? " + foundExpectedMarc005FieldValue);
//			
//		} catch (MultipleRecordsException | IOException | JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		System.out.println("----------------------------------");
		System.out.println("----- Pass 1: Pre-conversion -----");
		System.out.println("----------------------------------");
		ArrayList<String> convertedValues = new ArrayList<String>(); 
		for (String nonDecimalCoordinate : nonDecimalCoordinates()) {
			String converted = nonDecimalCoordinate;
			try {
				converted = DurstRecord.normalizeCoordinatesToDecimal(nonDecimalCoordinate);
			} catch (UnhandledCoordinateFormatException e) {
				//System.out.println(e.getMessage());
			}
			System.out.println("Before: " + nonDecimalCoordinate + ", after: " + converted);
			convertedValues.add(converted);
		}
		
		System.out.println("-----------------------------------");
		System.out.println("----- Pass 2: Post-conversion -----");
		System.out.println("-----------------------------------");
		for (String convertedCoordinate : convertedValues) {
			try {
				DurstRecord.normalizeCoordinatesToDecimal(convertedCoordinate);
			} catch (UnhandledCoordinateFormatException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public ArrayList<String> nonDecimalCoordinates() {
		ArrayList<String> nonDecimalCoordinates = new ArrayList<String>();
		
		nonDecimalCoordinates.add("W738070--W738070/N417176--N417176");
		nonDecimalCoordinates.add("(W 73°80ʹ--W 73°80ʹ/N 41°71ʹ--N 41°71ʹ)");
		nonDecimalCoordinates.add("W0735755--W0735755/N404656--N404656");
		nonDecimalCoordinates.add("(W 73°57ʹ--W 73°57ʹ/N 40°46ʹ--N 40°46ʹ)");
		nonDecimalCoordinates.add("W0742000--W0734500/N0410500--N0403000");
		nonDecimalCoordinates.add("(W 74°20ʹ--W 73°45ʹ/N 41°05ʹ--N 40°30ʹ)");
		nonDecimalCoordinates.add("W0722959--W0722959/N0430001--N0430001");
		nonDecimalCoordinates.add("(W 72°29ʹ--W 72°29ʹ/N 43°00ʹ--N 43°00ʹ)");
		nonDecimalCoordinates.add("W0735758--W0735758/N0404700--N0404700");
		nonDecimalCoordinates.add("(40°47ʹ00ʺN 073°57ʹ58ʺW)");
		nonDecimalCoordinates.add("W0735758--W0735758/N0404700--N0404700");
		nonDecimalCoordinates.add("(40°47ʹ00ʺN 073°57ʹ58ʺW)");
		nonDecimalCoordinates.add("W0735643--W0735643/N404828--N404828");
		nonDecimalCoordinates.add("(40°48ʹ28ʺN 073°56ʹ43ʺW)");
		nonDecimalCoordinates.add("W0740042--W0740042/N0404228--N0404228");
		nonDecimalCoordinates.add("(W 74°0ʹ42ʺ /N 40°42ʹ28ʺ)");
		nonDecimalCoordinates.add("W0735000--W0735000/N0404000--N0404000");
		nonDecimalCoordinates.add("(W 73°50ʹ--N 40°40ʹ)");
		nonDecimalCoordinates.add("-74.0705---73.7512/40.9163--40.544");
		nonDecimalCoordinates.add("(W 74.0705--W 73.7512/N 40.9163--N 40.544)");
		nonDecimalCoordinates.add("W074.000000--W074.000000/N040.710000--N040.710000");
		nonDecimalCoordinates.add("(W 74.00°--W 74.00°/N 40.71°--N 40.71°)");
		nonDecimalCoordinates.add("W0740100--W0735500/N0405300--N0404200");
		nonDecimalCoordinates.add("(W 74°01ʹ--W 73°55ʹ/N 40°53ʹ--N 40°42ʹ)");
		nonDecimalCoordinates.add("W0740400--W0735500/N0404900--N0404100");
		nonDecimalCoordinates.add("(W 74°04ʹ--W 73°55ʹ/N 40°49ʹ--N 40°41ʹ)");
		nonDecimalCoordinates.add("W0740300--W0735100/N0405500--N0404100");
		nonDecimalCoordinates.add("(W 74°03ʹ--W 73°51ʹ/N 40°55ʹ--N 40°41ʹ)");
		nonDecimalCoordinates.add("W0740800--W0735200/N0405400--N0403300");
		nonDecimalCoordinates.add("(W 74°08ʹ--W 73°52ʹ/N 40°54ʹ--N 40°33ʹ)");
		nonDecimalCoordinates.add("W0740100--W0735600/N0404700--N0404200");
		nonDecimalCoordinates.add("(W 74°01ʹ--W 73°56ʹ/N 40°47ʹ--N 40°42ʹ)");
		nonDecimalCoordinates.add("W0735900--W0735900/N0404340--N0404340");
		nonDecimalCoordinates.add("(W 73°59ʹ00ʺ/N 40°43ʹ40ʺ)");
		nonDecimalCoordinates.add("W0740100--W0735500/N0405300--N0404200");
		nonDecimalCoordinates.add("(W 74°01ʹ--W 73°55ʹ/N 40°53ʹ--N 40°42ʹ)");
		nonDecimalCoordinates.add("W0740300--W0735000/N0405200--N0404000");
		nonDecimalCoordinates.add("(W 74°03ʹ--W 73°50ʹ/N 40°52ʹ--N 40°40ʹ)");
		nonDecimalCoordinates.add("W0740400--W0735700/N0404600--N0403900");
		nonDecimalCoordinates.add("(W 74°04ʹ--W 73°57ʹ/N 40°46ʹ--N 40°39ʹ)");
		nonDecimalCoordinates.add("W0740000--W0735230/N0411500--N0410730");
		nonDecimalCoordinates.add("(W 74°00ʹ00ʺ--W 73°52ʹ30ʺ/N 41°15ʹ00ʺ--N 41°07ʹ30ʺ)");
		nonDecimalCoordinates.add("W0733730--W0733000/N0411500--N0410730");
		nonDecimalCoordinates.add("(W 73°37ʹ30ʺ--W 73°30ʹ/N 41°15ʹ--N 41°07ʹ30ʺ)");
		nonDecimalCoordinates.add("W1290000--W0640000/N0520000--N0240000");
		nonDecimalCoordinates.add("(W 129°--W 64°/N 52°--N 24°)");
		nonDecimalCoordinates.add("W1270000--W0650000/N0470000--N0240000");
		nonDecimalCoordinates.add("(W 127°--W 65°/N 47°--N 24°)");
		nonDecimalCoordinates.add("W0804000--W0721000/N0450000--N0404000");
		nonDecimalCoordinates.add("(W 80°40--W 72°10ʹ/N 45°00ʹ--N 40°40ʹ)");
		nonDecimalCoordinates.add("W0035000--E0062000/N0445000--N0403000");
		nonDecimalCoordinates.add("(W 3°50ʹ--E 6°20ʹ/N 44°50ʹ--N 40°30ʹ)");
		nonDecimalCoordinates.add("W0030000--E0045000/N0450000--N0403000");
		nonDecimalCoordinates.add("(W 3°00ʹ--E 4°50ʹ/N 45°00ʹ--N 40°30ʹ)");
		nonDecimalCoordinates.add("W0735500--W0734800/N0406000--N0405200");
		nonDecimalCoordinates.add("(W 73°55ʹ--W 73°48ʹ/N 40°60ʹ--N 40°52ʹ)");
		nonDecimalCoordinates.add("W0735015--W0735015/N0405445--N0405445");
		nonDecimalCoordinates.add("(W 73°50ʹ15ʺ/N 40°54ʹ45ʺ)");
		nonDecimalCoordinates.add("W0735357--W0735357/N0405552--N0405552");
		nonDecimalCoordinates.add("(W 73°53ʹ57ʺ/N 40°55ʹ52ʺ)");
		nonDecimalCoordinates.add("W0735119--W0735119/N0405050--N0405050");
		nonDecimalCoordinates.add("(W 73°51ʹ19ʺ/N 40°50ʹ50ʺ)");
		nonDecimalCoordinates.add("W0735110--W0735110/N0405352--N0405352");
		nonDecimalCoordinates.add("(W 73°51ʹ10ʺ/N 40°53ʹ52ʺ)");
		nonDecimalCoordinates.add("-073.788---073.788/041.007--041.007");
		nonDecimalCoordinates.add("-073.765---073.765/040.984--040.984");
		nonDecimalCoordinates.add("W0740100--W0735300/N0404400--N0403900");
		nonDecimalCoordinates.add("(W 74°01ʹ--W 73°53ʹ/N 40°44ʹ--N 40°39ʹ)");
		nonDecimalCoordinates.add("W0735400--W0735400/N0405100--N0405100");
		nonDecimalCoordinates.add("(W 73°54ʹ/N 40°51ʹ)");
		nonDecimalCoordinates.add("W0735600--W0735200/N0405300--N0404700");
		nonDecimalCoordinates.add("(W 73°56ʹ--W 73°52ʹ/N 40°53ʹ--N 40°47ʹ)");
		nonDecimalCoordinates.add("W0735900--W0735700/N0404800--N0404550");
		nonDecimalCoordinates.add("(W 73°59ʹ00ʺ--W 73°79ʹ00ʺ/N 40°48ʹ00ʺ--N 40°45ʹ50ʺ)");
		nonDecimalCoordinates.add("W073.955000--W073.908000/N040.880000--N040.828000");
		nonDecimalCoordinates.add("(W 73.955000--W 73.908000/N 40.880000--N 40.828000)");
		nonDecimalCoordinates.add("W0735900--W0735650/N0404810--N0404550");
		nonDecimalCoordinates.add("(W 73°59ʹ00ʺ--W 73°56ʹ50ʺ/N 40°48ʹ10ʺ--N 40°45ʹ50ʺ)");
		nonDecimalCoordinates.add("W0740100--W0735500/N0405300--N0404200");
		nonDecimalCoordinates.add("(W 74.p0.s01ʹ--W 73.p0.s55ʹ/N 40.p0.s53ʹ--N 40.p0.s42ʹ)");
		nonDecimalCoordinates.add("W0740120--W0735420/N0405220--N0404200");
		nonDecimalCoordinates.add("(W 74°01ʹ20ʺ--W 73°54ʹ20ʺ/N 40°52ʹ20ʺ--N 40°42ʹ00ʺ)");
		nonDecimalCoordinates.add("W0740100--W0735700/N0404600--N0404000");
		nonDecimalCoordinates.add("(W 74°01ʹ--W 73°57ʹ/N 40°46ʹ--N 40°40ʹ)");
		nonDecimalCoordinates.add("W0740100--W0735700/N0404500--N0404100");
		nonDecimalCoordinates.add("(W 74°01ʹ--W 73°57ʹ/N 40°45ʹ--N 40°41ʹ)");
		nonDecimalCoordinates.add("W073.950000--W073.950000/N040.650000--N040.650000");
		nonDecimalCoordinates.add("(W 73.95°--W 73.95°/N 40.65°--N 40.65°)");
		nonDecimalCoordinates.add("W0740300--W0732400/N0410500--N0404000");
		nonDecimalCoordinates.add("(W 74°03ʹ--W 73°24ʹ/N 41°05ʹ--N 40°40ʹ)");
		nonDecimalCoordinates.add("W0740400--W0735400/N0405000--N0403800");
		nonDecimalCoordinates.add("(W 74°04ʹ--W 73°54ʹ/N 40°50ʹ--N 40°38ʹ)");
		nonDecimalCoordinates.add("W074.080000--W073.870000/N040.880000--N040.640000");
		nonDecimalCoordinates.add("(W 74.08°--W 73.87°/N 40.88°--N 40.64°)");
		nonDecimalCoordinates.add("W0740200--W0735700/N0404700--N0404200");
		nonDecimalCoordinates.add("(W 74°02ʹ--W 73°57ʹ/N 40°47ʹ--N 40°42ʹ)");
		nonDecimalCoordinates.add("W0735818--W0735818/N0404628--N0404628");
		nonDecimalCoordinates.add("(W 73°58ʹ18ʺ/N 40°46ʹ28ʺ)");
		nonDecimalCoordinates.add("(W 74°02ʹ--W 73°54ʹ/N 40°53ʹ--N 40°41ʹ)");
		nonDecimalCoordinates.add("W0735400--W0735400/N0405100--N0405100");
		nonDecimalCoordinates.add("(W 73°54ʹ/N 40°51ʹ)");
		nonDecimalCoordinates.add("W0735600--W0735200/N0405300--N0404700");
		nonDecimalCoordinates.add("(W 73°56ʹ--W 73°52ʹ/N 40°53ʹ--N 40°47ʹ)");
		nonDecimalCoordinates.add("W0750000--W0724900/N0420100--N0395700");
		nonDecimalCoordinates.add("(W 75°00ʹ--W 72°49ʹ/N 41°01ʹ--N 39°57ʹ)");
		nonDecimalCoordinates.add("W0735119--W0735119/N0405050--N0405050");
		nonDecimalCoordinates.add("(W 73°51ʹ19ʺ/N 40°50ʹ50ʺ)");
		nonDecimalCoordinates.add("W0732600--W0712800/N0450000--N0424300");
		nonDecimalCoordinates.add("(W 73°26ʹ--W 71°28ʹ/N 45°00ʹ--N 42°43ʹ)");
		nonDecimalCoordinates.add("W0734500--W0714500/N0420300--N0410000");
		nonDecimalCoordinates.add("(W 73°45ʹ--W 71°45ʹ/N 42°03ʹ--N 41°00ʹ)");
		nonDecimalCoordinates.add("W0735222--W0735222/N0405318--N0405318");
		nonDecimalCoordinates.add("(W 73°52ʹ22ʺ/N 40°53ʹ18ʺ)");
		nonDecimalCoordinates.add("W0035000--E0062000/N0445000--N0403000");
		nonDecimalCoordinates.add("(W 3°50ʹ--E 6°20ʹ/N 44°50ʹ--N 40°30ʹ)");
		nonDecimalCoordinates.add("W0735500--W0734800/N0406000--N0405200");
		nonDecimalCoordinates.add("(W 73°55ʹ--W 73°48ʹ/N 40°60ʹ--N 40°52ʹ)");
		nonDecimalCoordinates.add("W0735015--W0735015/N0405445--N0405445");
		nonDecimalCoordinates.add("(W 73°50ʹ15ʺ/N 40°54ʹ45ʺ)");
		nonDecimalCoordinates.add("W0735357--W0735357/N0405552--N0405552");
		nonDecimalCoordinates.add("(W 73°53ʹ57ʺ/N 40°55ʹ52ʺ)");
		nonDecimalCoordinates.add("W0735110--W0735110/N0405352--N0405352");
		nonDecimalCoordinates.add("(W 73°51ʹ10ʺ/N 40°53ʹ52ʺ)");
		nonDecimalCoordinates.add("W0740300--W0735100/N0405500--N0404100");
		nonDecimalCoordinates.add("(W 74°03ʹ--W 73°51ʹ/N 40°55ʹ--N 40°41ʹ)");
		nonDecimalCoordinates.add("W0740100--W0735500/N0405300--N0404200");
		nonDecimalCoordinates.add("(W 74.p0.s01ʹ--W 73.p0.s55ʹ/N 40.p0.s53ʹ--N 40.p0.s42ʹ)");
		nonDecimalCoordinates.add("W0740300--W0735000/N0405200--N0404000");
		nonDecimalCoordinates.add("(W 74°03ʹ--W 73°50ʹ/N 40°52ʹ--N 40°40ʹ)");
		nonDecimalCoordinates.add("W0740100--W0735500/N0405300--N0404200");
		nonDecimalCoordinates.add("(W 74°01ʹ--W 73°55ʹ/N 40°53ʹ--N 40°42ʹ)");
		nonDecimalCoordinates.add("W0734500--W0741500/N0403000--N0410000");
		nonDecimalCoordinates.add("(W 73°45ʹ--W 74°15ʹ/N 40°30ʹ--N 41°)");
		nonDecimalCoordinates.add("W0740400--W0735500/N0404900--N0404100");
		nonDecimalCoordinates.add("(W 74°04ʹ--W 73°55ʹ/N 40°49ʹ--N 40°41ʹ)");
		nonDecimalCoordinates.add("W0740800--W0735200/N0405400--N0403300");
		nonDecimalCoordinates.add("(W 74°08ʹ--W 73°52ʹ/N 40°54ʹ--N 40°33ʹ)");
		nonDecimalCoordinates.add("W0740100--W0735600/N0404700--N0404200");
		nonDecimalCoordinates.add("(W 74°01ʹ--W 73°56ʹ/N 40°47ʹ--N 40°42ʹ)");
		nonDecimalCoordinates.add("W0735900--W0735900/N0404340--N0404340");
		nonDecimalCoordinates.add("(W 73°59ʹ00ʺ/N 40°43ʹ40ʺ)");
		nonDecimalCoordinates.add("W0740100--W0735500/N0405300--N0404200");
		nonDecimalCoordinates.add("(W 74°01ʹ--W 73°55ʹ/N 40°53ʹ--N 40°42ʹ)");
		nonDecimalCoordinates.add("W0740400--W0735700/N0404600--N0403900");
		nonDecimalCoordinates.add("(W 74°04ʹ--W 73°57ʹ/N 40°46ʹ--N 40°39ʹ)");
		nonDecimalCoordinates.add("W0740000--W0735230/N0411500--N0410730");
		nonDecimalCoordinates.add("(W 74°00ʹ00ʺ--W 73°52ʹ30ʺ/N 41°15ʹ00ʺ--N 41°07ʹ30ʺ)");
		nonDecimalCoordinates.add("W0733730--W0733000/N0411500--N0410730");
		nonDecimalCoordinates.add("(W 73°37ʹ30ʺ--W 73°30ʹ/N 41°15ʹ--N 41°07ʹ30ʺ)");
		nonDecimalCoordinates.add("W0733730--W0733000/N0412230--N0411500");
		nonDecimalCoordinates.add("(W 73°37ʹ30ʺ--W 73°30ʹ00ʺ/N 41°22ʹ30ʺ--N 41°15ʹ00ʺ)");
		nonDecimalCoordinates.add("W1290000--W0640000/N0520000--N0240000");
		nonDecimalCoordinates.add("(W 129°--W 64°/N 52°--N 24°)");
		nonDecimalCoordinates.add("W1270000--W0650000/N0470000--N0240000");
		nonDecimalCoordinates.add("(W 127°--W 65°/N 47°--N 24°)");
		nonDecimalCoordinates.add("W0804000--W0721000/N0450000--N0404000");
		nonDecimalCoordinates.add("(W 80°40--W 72°10ʹ/N 45°00ʹ--N 40°40ʹ)");
		nonDecimalCoordinates.add("W0030000--E0045000/N0450000--N0403000");
		nonDecimalCoordinates.add("(W 3°00ʹ--E 4°50ʹ/N 45°00ʹ--N 40°30ʹ)");
		nonDecimalCoordinates.add("W0740100--W0735300/N0404400--N0403900");
		nonDecimalCoordinates.add("(W 74°01ʹ--W 73°53ʹ/N 40°44ʹ--N 40°39ʹ)");
		nonDecimalCoordinates.add("W0735900--W0735700/N0404800--N0404550");
		nonDecimalCoordinates.add("(W 73°59ʹ00ʺ--W 73°79ʹ00ʺ/N 40°48ʹ00ʺ--N 40°45ʹ50ʺ)");
		nonDecimalCoordinates.add("W073.955000--W073.908000/N040.880000--N040.828000");
		nonDecimalCoordinates.add("(W 73.955000--W 73.908000/N 40.880000--N 40.828000)");
		nonDecimalCoordinates.add("W0735900--W0735650/N0404810--N0404550");
		nonDecimalCoordinates.add("(W 73°59ʹ00ʺ--W 73°56ʹ50ʺ/N 40°48ʹ10ʺ--N 40°45ʹ50ʺ)");
		nonDecimalCoordinates.add("W0741500--W0734500/N0410000--N0403000");
		nonDecimalCoordinates.add("(W 73⁰45ʹ--W 74⁰15ʹ/N 40⁰30ʹ--N 41⁰00ʹ)");
		nonDecimalCoordinates.add("W0742000--W0734500/N0410500--N0403000");
		nonDecimalCoordinates.add("(W 74°20ʹ--W 73°45ʹ/N 41°05ʹ--N 40°30ʹ)");
		nonDecimalCoordinates.add("-74.044---73.7471/40.9163--40.5419");
		nonDecimalCoordinates.add("(W 74.044--W 73.7471/N 40.9163--N 40.5419)");
		
		return nonDecimalCoordinates;
	}
	
}

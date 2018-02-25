package com.ibm.app.test.utils.app;

import java.sql.Timestamp;
import java.util.ArrayList;

import com.ibm.app.test.utils.RTCClientConstants;
import com.ibm.app.test.utils.rtc.workitem.RTCDefect;



/** 
 * CCSSD project - Defects from cookbook check by Berkshelf
 * @author ruifengm
 * @since 2017-Mar-17
 */

public class BerksAutoDefect extends RTCDefect {
	
	// Constructor
	public BerksAutoDefect(String summary, String description, String fileAgainstComponent, String owner, String severity,
			String foundInActivity, String plannedForSprint, Timestamp dueDate, ArrayList<String> subscriberList) {
		super();
		this.defectType = RTCClientConstants.RTC_DEFECT_TYPE_BUILD; 
		this.filedAgainst = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_CONTENT;
		this.userImpact = RTCClientConstants.RTC_DEFECT_USER_IMPACT_FUNC;
		this.subscriberList = subscriberList;
		this.summary = summary;
		this.fileAgainstComponent = fileAgainstComponent;
		this.description = description;
		this.foundInActivity = foundInActivity;
		this.owner = owner;
		this.severity = severity;
		this.plannedForSprint = plannedForSprint;
		this.dueDate = dueDate;
	}
}

package com.ibm.app.test.utils.app;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Set;

import com.ibm.app.test.utils.RTCClientConstants;
import com.ibm.app.test.utils.rtc.workitem.RTCDefect;



/** 
 * CCSSD project - Test Automation defect for Cucumber/Kitchen etc.
 * @author ruifengm
 * @since 2017-Apr-29
 */

public class TestAutoDefect extends RTCDefect {
	
	// Constructor
	public TestAutoDefect(String summary, String description, String filedAgainst, String fileAgainstComponent, String owner, String severity,
			String foundInActivity, String plannedForSprint, Timestamp dueDate, ArrayList<String> subscriberList,
			Set<String> attachmentLinkSet, String comment) {
		super();
		this.defectType = RTCClientConstants.RTC_DEFECT_TYPE_AUTO; 
		this.filedAgainst = filedAgainst;
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
		this.attachmentLinkSet = attachmentLinkSet;
		this.comment = comment;
	}
}

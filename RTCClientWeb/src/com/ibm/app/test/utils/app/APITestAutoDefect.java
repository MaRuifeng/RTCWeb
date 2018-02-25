package com.ibm.app.test.utils.app;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Set;

import com.ibm.app.test.utils.RTCClientConstants;
import com.ibm.app.test.utils.rtc.workitem.RTCDefect;

/**
 * GPO Health Check project - API Regression Test Automation defect
 * 
 * @author ruifengm
 * @since 2015-Nov-27
 */

public class APITestAutoDefect extends RTCDefect {

	// Constructor
	public APITestAutoDefect(String summary, String description,
			String fileAgainstComponent, String owner, String severity,
			String foundInActivity, String plannedForSprint, Timestamp dueDate,
			ArrayList<String> subscriberList, Set<String> attachmentLinkSet) {
		super();
		this.defectType = RTCClientConstants.RTC_DEFECT_TYPE_AUTO;
		this.filedAgainst = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_CUKE;
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
	}
}

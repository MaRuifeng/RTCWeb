package com.ibm.app.test.utils.rtc.workitem;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Set;

/** 
 * Define customized RTC defect for GPO Health Check project
 * @author ruifengm
 * @since 2015-Nov-27
 */

public abstract class RTCDefect {
	protected String summary;
	protected String description; 
	protected String defectType;
	protected String filedAgainst; 
	protected String fileAgainstComponent; 
	protected String foundInActivity; 
	protected String userImpact; 
	protected String owner; 
	protected String severity; 
	protected String plannedForSprint;
	protected Timestamp dueDate; 
	protected String comment;
	protected ArrayList<String> subscriberList;
	protected Set<String> attachmentLinkSet;
	
	// Constructor
	public RTCDefect() {
		super();
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return defectType;
	}

	public void setType(String type) {
		this.defectType = type;
	}

	public String getFiledAgainst() {
		return filedAgainst;
	}

	public void setFiledAgainst(String filedAgainst) {
		this.filedAgainst = filedAgainst;
	}

	public String getFileAgainstComponent() {
		return fileAgainstComponent;
	}

	public void setFileAgainstComponent(String fileAgainstComponent) {
		this.fileAgainstComponent = fileAgainstComponent;
	}

	public String getFoundInActivity() {
		return foundInActivity;
	}

	public void setFoundInActivity(String foundInActivity) {
		this.foundInActivity = foundInActivity;
	}

	public String getUserImpact() {
		return userImpact;
	}

	public void setUserImpact(String userImpact) {
		this.userImpact = userImpact;
	}

	public String getOwnedBy() {
		return owner;
	}

	public void setOwnedBy(String ownedBy) {
		this.owner = ownedBy;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getPlannedForSprint() {
		return plannedForSprint;
	}

	public void setPlannedForSprint(String plannedForSprint) {
		this.plannedForSprint = plannedForSprint;
	}

	public Timestamp getDueDate() {
		return dueDate;
	}

	public void setDueDate(Timestamp dueDate) {
		this.dueDate = dueDate;
	}

	public ArrayList<String> getSubscriberList() {
		return subscriberList;
	}

	public void setSubscriberList(ArrayList<String> subscriberList) {
		this.subscriberList = subscriberList;
	}

	public Set<String> getAttachmentLinkSet() {
		return attachmentLinkSet;
	}

	public void setAttachmentLinkSet(Set<String> attachmentLinkSet) {
		this.attachmentLinkSet = attachmentLinkSet;
	}
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "RTCDefect [summary=" + summary + ", description=" + description
				+ ", defectType=" + defectType + ", filedAgainst="
				+ filedAgainst + ", fileAgainstComponent="
				+ fileAgainstComponent + ", foundInActivity=" + foundInActivity
				+ ", userImpact=" + userImpact + ", owner=" + owner
				+ ", severity=" + severity + ", plannedForSprint="
				+ plannedForSprint + ", dueDate=" + dueDate
				+ ", comment=" + comment
				+ ", subscriberList=" + subscriberList
				+ ", attachmentLinkList=" + attachmentLinkSet + "]";
	}
}

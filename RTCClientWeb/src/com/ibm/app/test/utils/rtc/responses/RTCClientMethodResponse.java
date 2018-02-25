package com.ibm.app.test.utils.rtc.responses;

import com.ibm.json.java.JSONObject;

/**
 * Define RTC work item response returned by the RTC client methods
 * This class can be extended to define client method responses for RTC defect/task/feature etc.
 * @author ruifengm
 * @since 2015-Dec-9
 */
public class RTCClientMethodResponse {
	
	private int itemNum;
	private String itemSummary; 
	private String itemStatus;
	private String itemLink; 
	
	// constructor
	public RTCClientMethodResponse() {
		super();
	}

	public int getItemNum() {
		return itemNum;
	}

	public String getItemSummary() {
		return itemSummary;
	}

	public String getItemStatus() {
		return itemStatus;
	}

	public void setItemNum(int itemNum) {
		this.itemNum = itemNum;
	}

	public void setItemSummary(String itemSummary) {
		this.itemSummary = itemSummary;
	}

	public void setItemStatus(String itemStatus) {
		this.itemStatus = itemStatus;
	}
	
	public String getItemLink() {
		return itemLink;
	}

	public void setItemLink(String itemLink) {
		this.itemLink = itemLink;
	}
	
	@Override
	public String toString() {
		return "RTCClientMethodResponse [itemNum=" + itemNum + ", itemSummary="
				+ itemSummary + ", itemStatus=" + itemStatus + ", itemLink="
				+ itemLink + "]";
	}

	public JSONObject toJSONObject() { 
		JSONObject returnObj = new JSONObject(); 
		
		returnObj.put("WorkItemNumber", this.itemNum);
		returnObj.put("WorkItemSummary", this.itemSummary);
		returnObj.put("WorkItemStatus", this.itemStatus);
		returnObj.put("WorkItemLink", this.itemLink);
		
		return returnObj;
	}
}

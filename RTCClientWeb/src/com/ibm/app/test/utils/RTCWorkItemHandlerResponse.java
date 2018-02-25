package com.ibm.app.test.utils;

import com.ibm.app.test.utils.rtc.responses.RTCClientMethodResponse;
import com.ibm.json.java.JSONObject;


/**
 * Define method response for the RTC work item handler
 * @author ruifengm
 * @since 2015-Dec-9
 */
public class RTCWorkItemHandlerResponse {
	
	private RTCClientMethodResponse clientMethodResponse; 
	private String identifier; 
	private String errorMessage;
	
	// constructor
	public RTCWorkItemHandlerResponse() {
		super();
	}

	public RTCClientMethodResponse getClientMethodResponse() {
		return clientMethodResponse;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getErrorMsg() {
		return errorMessage;
	}

	public void setClientMethodResponse(RTCClientMethodResponse clientMethodResponse) {
		this.clientMethodResponse = clientMethodResponse;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMessage = errorMsg;
	}

	@Override
	public String toString() {
		return "RTCWorkItemHandlerResponse [clientMethodResponse="
				+ clientMethodResponse + ", identifier=" + identifier
				+ ", errorMsg=" + errorMessage + "]";
	} 
	
	public JSONObject toJSONObject() {
		JSONObject returnObj = new JSONObject(); 
		
		if (clientMethodResponse == null) {
			returnObj.put(RTCClientConstants.JSON_KEY_RESULT, null);
		}
		else returnObj.put(RTCClientConstants.JSON_KEY_RESULT, this.clientMethodResponse.toJSONObject());
		returnObj.put(RTCClientConstants.JSON_KEY_IDENTIFIER, this.identifier);
		if (null != this.errorMessage && !this.errorMessage.isEmpty()) returnObj.put(RTCClientConstants.JSON_KEY_ERROR_MSG, this.errorMessage);
		
		return returnObj;
	}
}

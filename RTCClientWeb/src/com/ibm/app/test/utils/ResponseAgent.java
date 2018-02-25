package com.ibm.app.test.utils;

import java.util.ArrayList;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.app.test.utils.rtc.responses.RTCClientMethodResponse;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;


/**
 * Define common objects that is returned by the 
 * --- RTC work item handler APIs
 * --- GPO test results handler APIs
 * @author ruifengm
 * @Since 2015-Dec-01
 */
public class ResponseAgent {
	protected String identifier;
	protected String errorMessage;
	protected RTCClientMethodResponse response;
	protected ArrayList<RTCClientMethodResponse> responseList;
	protected JSONObject resultJson;
	protected JSONArray resultJsonArr;
	
	public ResponseAgent() {
		super();
	}
	
	public Response getRTCItemHandlerResponse(RTCClientMethodResponse response, String identifier, String errorMsg) {
		this.response = response;
		this.identifier = identifier;
		this.errorMessage = errorMsg; 
		
		RTCWorkItemHandlerResponse handlerResponse = new RTCWorkItemHandlerResponse(); 
		handlerResponse.setClientMethodResponse(response);
		handlerResponse.setIdentifier(identifier);
		if (errorMsg != null && !errorMsg.isEmpty()) {
			handlerResponse.setErrorMsg(errorMsg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(handlerResponse.toJSONObject().toString()).build();
		}
	
		else return Response.ok(handlerResponse.toJSONObject().toString()).build();
	}
	
	public Response getRTCItemHandlerResponseList(ArrayList<RTCClientMethodResponse> responseList, String identifier, String errorMsg) {
		this.responseList = responseList;
		this.identifier = identifier;
		this.errorMessage = errorMsg; 
		
		JSONObject returnObj = new JSONObject(); 
		JSONArray returnArr = new JSONArray(); 
		for (RTCClientMethodResponse response: responseList) {
			returnArr.add(response.toJSONObject());
		}
		
		returnObj.put(RTCClientConstants.JSON_KEY_RESULT, returnArr);
		returnObj.put(RTCClientConstants.JSON_KEY_IDENTIFIER, identifier);
		if (errorMsg != null && !errorMsg.isEmpty()) {
			returnObj.put(RTCClientConstants.JSON_KEY_ERROR_MSG, errorMsg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(returnObj.toString()).build();
		}
		
		else return Response.ok(returnObj.toString()).build();
	}
	
	public Response getRTCItemHandlerJsonResponse(JSONObject resultJson, String identifier, String errorMsg) {
		this.resultJson = resultJson;
		this.identifier = identifier;
		this.errorMessage = errorMsg; 
		
		JSONObject returnObj = new JSONObject(); 
		returnObj.put(RTCClientConstants.JSON_KEY_RESULT, resultJson);
		returnObj.put(RTCClientConstants.JSON_KEY_IDENTIFIER, identifier);
		if (errorMsg != null && !errorMsg.isEmpty()) {
			returnObj.put(RTCClientConstants.JSON_KEY_ERROR_MSG, errorMsg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(returnObj.toString()).build();
		}
		
		else return Response.ok(returnObj.toString()).build();
	}
	
	public Response getRTCItemHandlerJsonArrayResponse(JSONArray resultJsonArr, String identifier, String errorMsg) {
		this.resultJsonArr = resultJsonArr;
		this.identifier = identifier;
		this.errorMessage = errorMsg; 
		
		JSONObject returnObj = new JSONObject(); 
		returnObj.put(RTCClientConstants.JSON_KEY_RESULT, resultJsonArr);
		returnObj.put(RTCClientConstants.JSON_KEY_IDENTIFIER, identifier);
		if (errorMsg != null && !errorMsg.isEmpty()) {
			returnObj.put(RTCClientConstants.JSON_KEY_ERROR_MSG, errorMsg);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(returnObj.toString()).build();
		}
		
		else return Response.ok(returnObj.toString()).build();
	}
	
	public Response getAppTestResultsHandlerJsonResponse(JSONObject resultJson, String identifier, Exception e) {
		this.resultJson = resultJson;
		this.identifier = identifier;
		if (e != null) this.errorMessage = e.getMessage();
		
		JSONObject returnObj = new JSONObject(); 
		returnObj.put(RTCClientConstants.JSON_KEY_RESULT, resultJson);
		returnObj.put(RTCClientConstants.JSON_KEY_IDENTIFIER, identifier);
		if (e != null) {
			returnObj.put(RTCClientConstants.JSON_KEY_ERROR_MSG, e.getClass().getName() + ": " + e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(returnObj.toString()).build();
		}
		
		else return Response.ok()
				// allow calling from JavaScript
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods",
						"GET, POST, PUT, DELETE")
		        .header("Access-Control-Allow-Headers",
		        		MediaType.APPLICATION_JSON).entity(returnObj)
		        .build();
	}
	
	public Response getNullParamResponse(String paramName) {
		RTCWorkItemHandlerResponse handlerResponse = new RTCWorkItemHandlerResponse(); 
		handlerResponse.setClientMethodResponse(null);
		handlerResponse.setIdentifier("NullParameterError");
		handlerResponse.setErrorMsg("Null parameter " + paramName + " received.");
		return Response.status(Response.Status.BAD_REQUEST).entity(handlerResponse.toJSONObject().toString()).build();
	}
	
	public Response getInvalidIntParamResponse(String paramName) {
		RTCWorkItemHandlerResponse handlerResponse = new RTCWorkItemHandlerResponse(); 
		handlerResponse.setClientMethodResponse(null);
		handlerResponse.setIdentifier("InvalidIntParameterError");
		handlerResponse.setErrorMsg("Invalid integer parameter " + paramName + " received.");
		return Response.status(Response.Status.BAD_REQUEST).entity(handlerResponse.toJSONObject().toString()).build();
	}
	
	public Response getInvalidParamTypeResponse(String paramName, String correctType, String wrongType) {
		RTCWorkItemHandlerResponse handlerResponse = new RTCWorkItemHandlerResponse(); 
		handlerResponse.setClientMethodResponse(null);
		handlerResponse.setIdentifier("InvalidParamTypeError");
		handlerResponse.setErrorMsg("Wrong type " + wrongType + " received for " + paramName + ". Correct type should be " + correctType + ".");
		return Response.status(Response.Status.BAD_REQUEST).entity(handlerResponse.toJSONObject().toString()).build();
	}
}

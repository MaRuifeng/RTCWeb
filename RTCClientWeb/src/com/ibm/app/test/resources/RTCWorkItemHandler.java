package com.ibm.app.test.resources;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.QueryParam;

import com.ibm.app.test.utils.RTCClientConstants;
import com.ibm.app.test.utils.ResponseAgent;
import com.ibm.app.test.utils.rtc.RTCClient;
import com.ibm.app.test.utils.rtc.responses.RTCClientMethodResponse;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.team.repository.common.TeamRepositoryException;

/**
 * Define APIs to handle RTC work items (create, get, update & delete)
 * @author ruifengm
 * @since 2015-Dec-15
 */

@Path("rtc") // path must be set and be unique when multiple resource classes are registered under the JAX-RS application
@Stateless
public class RTCWorkItemHandler {
	public static final String className = RTCWorkItemHandler.class.getName();
	public static final Logger logger = Logger.getLogger(className);
	
	protected RTCClient rtcClient;
	
	/**
	 * Get the current status of a work item
	 * @param workItemNum, input JSON containing RTC config properties
	 * @return RTCClientMethodResponse as a JSONObject
	 * @throws Exception
	 */
	@PUT
	@Path("/getWorkItemStatus")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getWorkItemStatus (			
			@QueryParam(RTCClientConstants.PARAM_WORK_ITEM_NUM) String workItemNum,
			JSONObject rtcConfig) {
		String sourceMethod = "getWorkItemStatus";
		logger.entering(className, sourceMethod); 
		
		ResponseAgent responseAgent = new ResponseAgent();
		RTCClientMethodResponse response = new RTCClientMethodResponse(); 
		
		// check parameter validity
		if (workItemNum == null || workItemNum=="") return responseAgent.getNullParamResponse(RTCClientConstants.PARAM_WORK_ITEM_NUM);
		int itemId = -1; 
		try {
			itemId = Integer.parseInt(workItemNum); 
		} catch (Exception e) {
			responseAgent.getInvalidIntParamResponse(RTCClientConstants.PARAM_WORK_ITEM_NUM);
		}
		
		// parse input JSON
		JSONObject config = (JSONObject) rtcConfig.get("RTCConfig"); 
		
		// establish RTC client
		try {
			establishRTCClient(config);
		} catch (Exception e) {
		    return responseAgent.getRTCItemHandlerResponse(null, "establishRTCClient", 
		    		e.getMessage());
		}
		
		try {
			response = rtcClient.getWorkItemStatus(itemId); 
		} catch (TeamRepositoryException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		e.getMessage());
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		e.getMessage());
		}
		logger.exiting(className, sourceMethod); 
		return responseAgent.getRTCItemHandlerResponse(response, "Work Item Status", null);
	}
	
	/**
	 * API to exit established RTC instance
	 * Note: this API is mandatory after all other RTC Web Client APIs are called, to make sure the project area information gets updated on next log in,
	 *       otherwise when invoking other APIs, the login session cannot be used, and explicit login has to be carried out each time.
	 * @param rtcConfig
	 * @return
	 */
	@PUT
	@Path("/exitRTC")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response exitRTC (JSONObject rtcConfig) {
		String sourceMethod = "exitRTC";
		logger.entering(className, sourceMethod); 
		
		ResponseAgent responseAgent = new ResponseAgent();
		
		// parse input JSON
		JSONObject config = (JSONObject) rtcConfig.get("RTCConfig"); 
		
		// establish RTC client
		try {
			establishRTCClient(config);
		} catch (Exception e) {
		    return responseAgent.getRTCItemHandlerResponse(null, "establishRTCClient", 
		    		e.getMessage());
		}
		
		try {
			rtcClient.logout();
			rtcClient.shutdown();
		} catch (TeamRepositoryException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		e.getMessage());
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		e.getMessage());
		}
		logger.exiting(className, sourceMethod); 
		return responseAgent.getRTCItemHandlerResponse(null, "Exit RTC", null);
	}
	
	/**
	 * Establish RTC client with the provided config properties
	 * @param [JSONObject]rtcConfig
	 * @throws Exception
	 */
	public void establishRTCClient(JSONObject rtcConfig) throws TeamRepositoryException, Exception {
		String sourceMethod = "establishRTCClient";
		logger.entering(className, sourceMethod);
		
		// config properties 
		String repositoryURI = (String) rtcConfig.get("RepositoryURI");
		String userId = (String) rtcConfig.get("UserId"); 
		String password = (String) rtcConfig.get("Password"); 
		String projectAreaName = (String) rtcConfig.get("ProjectArea"); 
		String developmentLineId = (String) rtcConfig.get("DevLineId");
		JSONArray subscribers = (JSONArray) rtcConfig.get("Subscribers"); 
		ArrayList<String> subscriberList = new ArrayList<String>();
		if (null != subscribers) {
			for (Object obj: subscribers){
				String subscriber = (String) obj; 
				subscriberList.add(subscriber);
			}
		}

		// check parameter validity
		if (repositoryURI == null || repositoryURI.equalsIgnoreCase("")) throw new Exception("Repository URI is not specified!");
		if (userId == null || userId.equalsIgnoreCase("")) throw new Exception("User ID is not specified!");
		if (password == null || password.equalsIgnoreCase("")) throw new Exception("Password (encrypted) is not specified!");
		if (projectAreaName == null || projectAreaName.equalsIgnoreCase("")) throw new Exception("Project Area is not specified!");
		if (developmentLineId == null || developmentLineId.equalsIgnoreCase("")) throw new Exception("Development Line ID is not specified!");
		if (subscriberList.isEmpty()) throw new Exception("At least one non-robot RTC user should be given as a subscriber!");
		
		// construct & launch RTC client
		try {
			this.rtcClient = new RTCClient(repositoryURI, userId, password, projectAreaName, developmentLineId, subscriberList); 
			if(!this.rtcClient.loggedIn()) this.rtcClient.login(); // log in check
			this.rtcClient.setUp(); 
		} catch (TeamRepositoryException teamRepoExc){
			StringWriter sw = new StringWriter();
			teamRepoExc.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
			throw teamRepoExc;
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
			throw e;
		}
		logger.exiting(className, sourceMethod); 
	}
}

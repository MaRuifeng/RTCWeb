package com.ibm.app.test.resources;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.QueryParam;

import com.ibm.app.test.ejbs.BerksDefectService;
import com.ibm.app.test.entities.BerksDefect;
import com.ibm.app.test.utils.NoRecordException;
import com.ibm.app.test.utils.RTCClientConstants;
import com.ibm.app.test.utils.ResponseAgent;
import com.ibm.app.test.utils.app.BerksAutoDefect;
import com.ibm.app.test.utils.rtc.responses.RTCClientMethodResponse;
import com.ibm.json.java.JSONObject;
import com.ibm.team.repository.common.TeamRepositoryException;

/**
 * Define APIs to handle Berkshelf defects in RTC (create, get, update & delete)
 * @author ruifengm
 * @since 2017-Mar-17
 */

@Path("berks/rtc") // path must be set and be unique when multiple resource classes are registered under the JAX-RS application
@Stateless
public class BerksDefectHandler extends RTCWorkItemHandler {
	public static final String className = BerksDefectHandler.class.getName();
	public static final Logger logger = Logger.getLogger(className);
	
	@EJB
	private BerksDefectService BerksDefectService; 

	/**
	 * This API creates a defect for failed dependency check by Berkshelf of a cookbook
	 */
	@POST
	@Path("/createBerksDefect")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createBerksDefect(
			@QueryParam(RTCClientConstants.PARAM_SEVERITY) String defectSeverity,
			JSONObject input) {
		String sourceMethod = "createBerksDefect";
		logger.entering(className, sourceMethod);
		
		ResponseAgent responseAgent = new ResponseAgent(); 
		
		// parse input JSON
		JSONObject rtcConfig = (JSONObject) input.get("RTCConfig");  
		
		// establish RTC client
		try {
			establishRTCClient(rtcConfig);
		} catch (Exception e) {
		    return responseAgent.getRTCItemHandlerResponse(null, "establishRTCClient", 
		    		e.getMessage());
		}
		
		// get Berks result
		String owner = input.get("owner").toString(); 
		String cookbook = input.get("cookbook").toString(); 
		String pullRequest = input.get("pr").toString();
		String sourceBranch = input.get("source").toString();
		String targetBranch = input.get("target").toString(); 
		String prMerged = input.get("merged").toString();
		String prUrl = input.get("pr_url").toString();
		String berksMessage = input.get("berks_message").toString();
		
		Timestamp dueDate = setDueDate();
	
		// check parameter invalidity
		Response handlerResponse = checkParameterInvalidity(cookbook, targetBranch, defectSeverity, responseAgent);
		if (handlerResponse != null) return handlerResponse;
		
		// construct RTC defect content
		String summary = "[Berkshelf] Cookbook " + cookbook + " failed dependency check with pull request '" + pullRequest + "' to branch " + targetBranch; 
		
		StringBuilder descBuilder = new StringBuilder(); 
		descBuilder.append("Cookbook: " + cookbook + "\n");
		descBuilder.append("Pull Request: " + pullRequest + "\n");
		descBuilder.append("Pull Request Link: " + prUrl + "\n");
		descBuilder.append("Source Branch: " + sourceBranch + "\n");
		descBuilder.append("Target Branch: " + targetBranch + "\n");
		descBuilder.append("Merged: " + prMerged + "\n\n\n");
		descBuilder.append("Berks Output:\n\n " + berksMessage );
		String description = descBuilder.toString();
		
		String fileAgainstComponent = null;
		
		String foundInActivity = RTCClientConstants.RTC_DEFECT_FOUND_IN_ACTIVITY_REVIEW;
		String severity = defectSeverity; 
		
		BerksAutoDefect defect = new BerksAutoDefect(summary, description, fileAgainstComponent, 
				owner, severity, foundInActivity, null, dueDate, this.rtcClient.getSubscriberList()); 

		// create defect
		RTCClientMethodResponse response = new RTCClientMethodResponse();
		
		try {
			// RTC
			response = rtcClient.createDefect(defect);
			// TEST DB
			BerksDefectService.createRTCDefect(response.getItemNum(), response.getItemSummary(), 
					cookbook, targetBranch, prUrl, response.getItemStatus(), response.getItemLink());
		} catch (TeamRepositoryException e) {
			//logger.logp(Level.SEVERE, rtcClient.getClass().getName(), e.getStackTrace()[0].getMethodName(), e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		"Unable to create defect for cookbook " + cookbook + "! Cause: " + e.getMessage());
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		"Unable to create defect for cookbook " + cookbook + "! Cause: " + e.getCause());
		}
		
		logger.exiting(className, sourceMethod);
		return responseAgent.getRTCItemHandlerResponse(response, cookbook + "-" + targetBranch, null);
	}
	
	/**
	 * This API updates a defect for failed dependency check by Berkshelf of a cookbook
	 */
	@POST
	@Path("/updateBerksDefect")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateBerksDefect(
			@QueryParam(RTCClientConstants.PARAM_DEFECT_NUM) String defectNum,
			@QueryParam(RTCClientConstants.PARAM_SEVERITY) String defectSeverity,
			JSONObject input) {
		String sourceMethod = "updateBerksDefect";
		logger.entering(className, sourceMethod);
		
		ResponseAgent responseAgent = new ResponseAgent(); 
		
		// parse input JSON
		JSONObject rtcConfig = (JSONObject) input.get("RTCConfig");  
		
		// establish RTC client
		try {
			establishRTCClient(rtcConfig);
		} catch (Exception e) {
		    return responseAgent.getRTCItemHandlerResponse(null, "establishRTCClient", 
		    		e.getMessage());
		}
		
		// get Berks result
		String cookbook = input.get("cookbook").toString(); 
		String pullRequest = input.get("pr").toString();
		String sourceBranch = input.get("source").toString();
		String targetBranch = input.get("target").toString(); 
		String prMerged = input.get("merged").toString();
		String prUrl = input.get("pr_url").toString();
		String berksMessage = input.get("berks_message").toString();
		
		// check parameter invalidity
		Response handlerResponse = checkParameterInvalidity(cookbook, targetBranch, defectSeverity, responseAgent);
		if (handlerResponse != null) return handlerResponse;
		if (defectNum == null || defectNum=="") return responseAgent.getNullParamResponse(RTCClientConstants.PARAM_DEFECT_NUM);
		int defectId = -1; 
		try {
			defectId = Integer.parseInt(defectNum); 
		} catch (Exception e) {
			responseAgent.getInvalidIntParamResponse(RTCClientConstants.PARAM_DEFECT_NUM);
		}
		
		// construct RTC defect content
		String summary = "[Berkshelf] Cookbook " + cookbook + " failed dependency check with pull request '" + pullRequest + "' to branch " + targetBranch; 
		
		StringBuilder descBuilder = new StringBuilder(); 
		descBuilder.append("Cookbook: " + cookbook + "\n");
		descBuilder.append("Pull Request: " + pullRequest + "\n");
		descBuilder.append("Pull Request Link: " + prUrl + "\n");
		descBuilder.append("Source Branch: " + sourceBranch + "\n");
		descBuilder.append("Target Branch: " + targetBranch + "\n");
		descBuilder.append("Merged: " + prMerged + "\n\n\n");
		descBuilder.append("Berks Output:\n\n " + berksMessage );
		String description = descBuilder.toString();
		
		String comment = "Updated defect content upon changes to pull request " + prUrl + ".";

		// update defect
		RTCClientMethodResponse response = new RTCClientMethodResponse();
		
		try {
			// RTC
			response = rtcClient.updateDefect(defectId, comment, summary, description, defectSeverity, null);
			// TEST DB
			BerksDefectService.updateRTCDefect(defectId, response.getItemSummary(), response.getItemStatus(), targetBranch, prUrl);
		} catch (TeamRepositoryException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		"Unable to update defect " + defectNum + "!");
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		"Unable to update defect " + defectNum + "!");
		}
		
		logger.exiting(className, sourceMethod);
		return responseAgent.getRTCItemHandlerResponse(response, cookbook + "-" + targetBranch, null);
	}
	
	/**
	 * Close a Berkshelf defect via the 'Resolve --> Verify' steps with a comment
	 */
	@PUT
	@Path("/closeBerksDefect")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response closeBerksDefect(			
			@QueryParam(RTCClientConstants.PARAM_COMMENT) String comment,
			@QueryParam(RTCClientConstants.PARAM_DEFECT_NUM) String defectNum,
			JSONObject input) {
		String sourceMethod = "closeBerksDefect";
		logger.entering(className, sourceMethod);
		
		ResponseAgent responseAgent = new ResponseAgent(); 
		RTCClientMethodResponse response = new RTCClientMethodResponse(); 
		
		// check parameter validity
		if (defectNum == null || defectNum=="") return responseAgent.getNullParamResponse(RTCClientConstants.PARAM_DEFECT_NUM);
		int defectId = -1; 
		try {
			defectId = Integer.parseInt(defectNum); 
		} catch (Exception e) {
			responseAgent.getInvalidIntParamResponse(RTCClientConstants.PARAM_DEFECT_NUM);
		}
		
		// parse input JSON
		JSONObject rtcConfig = (JSONObject) input.get("RTCConfig"); 
		
		// establish RTC client
		try {
			establishRTCClient(rtcConfig);
		} catch (Exception e) {
		    return responseAgent.getRTCItemHandlerResponse(null, "establishRTCClient", 
		    		e.getMessage());
		}
		
		try {
			// RTC
			response = rtcClient.closeDefect(defectId, comment);
			// TEST DB
			BerksDefectService.updateRTCDefect(defectId, response.getItemSummary(), response.getItemStatus(), null, null);
		}catch (TeamRepositoryException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		"Unable to close defect " + defectNum + "!");
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		"Unable to close defect " + defectNum + "!");
		}
		logger.exiting(className, sourceMethod);
		return responseAgent.getRTCItemHandlerResponse(response, "Close Berks Defect", null);
	}
	
	/**
	 * Sync status of all RTC defects
	 */
	@PUT
	@Path("/syncRTCDefectStatus")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response syncRTCDefectStatus(JSONObject rtcConfig) {
		String sourceMethod = "syncRTCDefectStatus";
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
	
		// sync 
		ArrayList<RTCClientMethodResponse> responseList = new ArrayList<RTCClientMethodResponse>();
		try {
			// get defect list from DB
			List<BerksDefect> defectList = BerksDefectService.getAllDefects();
			for (BerksDefect defect: defectList) {
				// sync against RTC records
				String currentStatusInDB = defect.getDefectStatus(); 
				String latestStatusInRTC = rtcClient.getWorkItemStatus(defect.getDefectNum()).getItemStatus();
				if (!currentStatusInDB.equalsIgnoreCase(latestStatusInRTC)) {
					BerksDefectService.updateRTCDefect(defect.getDefectNum(), null, latestStatusInRTC, null, null);
					responseList.add(rtcClient.getWorkItemStatus(defect.getDefectNum()));
				}
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		e.getMessage());
		}
		logger.exiting(className, sourceMethod);
		return responseAgent.getRTCItemHandlerResponseList(responseList, "Sync Test Defect Status", null);
	}
	
	/**
	 * Get open RTC defect for the given cookbook from the TEST DB (only retrieve the latest record)
	 */
	@PUT
	@Path("/getOpenBerksDefect")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getOpenBerksDefect (			
			@QueryParam(RTCClientConstants.PARAM_COOKBOOK) String cookbook,
			JSONObject rtcConfig) {
		String sourceMethod = "getOpenBerksDefect";
		logger.entering(className, sourceMethod); 
		
		ResponseAgent responseAgent = new ResponseAgent();
		
		// check parameter validity
		if (cookbook == null || cookbook =="") return responseAgent.getNullParamResponse(RTCClientConstants.PARAM_COOKBOOK);
		
		// parse input JSON
		JSONObject config = (JSONObject) rtcConfig.get("RTCConfig"); 
		
		// establish RTC client
		try {
			establishRTCClient(config);
		} catch (Exception e) {
		    return responseAgent.getRTCItemHandlerResponse(null, "establishRTCClient", 
		    		e.getMessage());
		}
		
		BerksDefect openDefect = new BerksDefect(); 
		RTCClientMethodResponse response = new RTCClientMethodResponse(); 
		try {
			//TEST DB
			openDefect = BerksDefectService.getOpenRTCDefectByCookbook(cookbook);
			response = rtcClient.getWorkItemStatus(openDefect.getDefectNum());
		} catch (NoRecordException getExc) {
			if (getExc.getClass().getName().equalsIgnoreCase(NoRecordException.class.getName())) {
				return responseAgent.getRTCItemHandlerJsonResponse(null, "No Open Defect", null);
			}
			else {
				StringWriter sw = new StringWriter();
				getExc.printStackTrace(new PrintWriter(sw));
				logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
			    return responseAgent.getRTCItemHandlerJsonResponse(null, sourceMethod, 
			    		getExc.getMessage());
			}
		} catch (TeamRepositoryException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerJsonResponse(null, sourceMethod, 
		    		e.getMessage());
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerJsonResponse(null, sourceMethod, 
		    		e.getMessage());
		}
		if (openDefect.getDefectStatus().equalsIgnoreCase(response.getItemStatus())){
			logger.exiting(className, sourceMethod); 
			return responseAgent.getRTCItemHandlerJsonResponse(openDefect.toJSONObject(), "Open Defect for Berks", null);
		}
		else return responseAgent.getRTCItemHandlerJsonResponse(null, "Out of Sync", "TEST DB defect status record does not tally with RTC. Sync first."); 
	}
	
	/**
	 * Due date should be set {RTCClientConstants.DUE_WORKING_DAYS} working days after the defect filing date
	 */
	public Timestamp setDueDate() {
		Calendar cal = Calendar.getInstance();
		// add only business working days
		for (int i=0; i<RTCClientConstants.DUE_WORKING_DAYS; i++){
			do {
				cal.add(Calendar.DAY_OF_MONTH, 1); 
			} while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
					 cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
		}
		return new Timestamp(cal.getTimeInMillis());
	}

	/**
	 * Check parameter invalidity
	 * @param project
	 * @param build
	 * @param defectSeverity
	 * @param responseAgent
	 * @return
	 */
	private Response checkParameterInvalidity(String project, String build, String defectSeverity, ResponseAgent responseAgent) {
		
		// check parameter validity 
		if (project == null || project == "") return responseAgent.getNullParamResponse(RTCClientConstants.PARAM_PROJECT);
		if (build == null || build == "") return responseAgent.getNullParamResponse(RTCClientConstants.PARAM_BUILD_NAME);
		if (defectSeverity == null || defectSeverity == "") return responseAgent.getNullParamResponse(RTCClientConstants.PARAM_SEVERITY);
		
		if (!defectSeverity.equalsIgnoreCase(RTCClientConstants.RTC_DEFECT_SEVERIY_BLOCKER)
				&& !defectSeverity.equalsIgnoreCase(RTCClientConstants.RTC_DEFECT_SEVERIY_MAJOR)
				&& !defectSeverity.equalsIgnoreCase(RTCClientConstants.RTC_DEFECT_SEVERIY_NORMAL)
				&& !defectSeverity.equalsIgnoreCase(RTCClientConstants.RTC_DEFECT_SEVERIY_MINOR)) return responseAgent.getRTCItemHandlerResponse(null,
				"checkParameterValidity", "Defect severity value can only be " + 
				RTCClientConstants.RTC_DEFECT_SEVERIY_BLOCKER + " or " + 
				RTCClientConstants.RTC_DEFECT_SEVERIY_MAJOR + " or " + 
				RTCClientConstants.RTC_DEFECT_SEVERIY_NORMAL + " or " + 
				RTCClientConstants.RTC_DEFECT_SEVERIY_MINOR + "!" );
		
		return null;
	}
}

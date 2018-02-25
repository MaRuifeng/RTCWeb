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

import com.ibm.app.test.ejbs.BrakemanDefectService;
import com.ibm.app.test.entities.BrakemanDefect;
import com.ibm.app.test.utils.NoRecordException;
import com.ibm.app.test.utils.RTCClientConstants;
import com.ibm.app.test.utils.ResponseAgent;
import com.ibm.app.test.utils.app.SecurityAutoDefect;
import com.ibm.app.test.utils.rtc.responses.RTCClientMethodResponse;
import com.ibm.json.java.JSONObject;
import com.ibm.team.repository.common.TeamRepositoryException;

/**
 * Define APIs to handle Brakeman defects in RTC (create, get, update & delete)
 * @author ruifengm
 * @since 2016-Nov-17
 */

@Path("brakeman/rtc") // path must be set and be unique when multiple resource classes are registered under the JAX-RS application
@Stateless
public class BrakemanDefectHandler extends RTCWorkItemHandler {
	public static final String className = BrakemanDefectHandler.class.getName();
	public static final Logger logger = Logger.getLogger(className);
	
	@EJB
	private BrakemanDefectService brakemanDefectService; 

	/**
	 * This API creates a defect for Brakeman warnings of a Rails project
	 */
	@POST
	@Path("/createBrakemanDefect")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createBrakemanDefect(
			@QueryParam(RTCClientConstants.PARAM_PROJECT) String project,
			@QueryParam(RTCClientConstants.PARAM_BUILD_NAME) String build,
			@QueryParam(RTCClientConstants.PARAM_SEVERITY) String defectSeverity,
			JSONObject input) {
		String sourceMethod = "createBrakemanDefect";
		logger.entering(className, sourceMethod);
		
		ResponseAgent responseAgent = new ResponseAgent(); 
		
		// check parameter invalidity
		Response handlerResponse = checkParameterInvalidity(project, build, defectSeverity, responseAgent);
		if (handlerResponse != null) return handlerResponse;
		
		// parse input JSON
		JSONObject rtcConfig = (JSONObject) input.get("RTCConfig"); 
		JSONObject brakemanResult = (JSONObject) input.get("BrakemanResult"); 
		
		// establish RTC client
		try {
			establishRTCClient(rtcConfig);
		} catch (Exception e) {
		    return responseAgent.getRTCItemHandlerResponse(null, "establishRTCClient", 
		    		e.getMessage());
		}
		
		// get Brakeman result
		String appPath = brakemanResult.get("app_path").toString(); 
		String railsVer = brakemanResult.get("rails_version").toString(); 
		String rubyVer = brakemanResult.get("ruby_version").toString();
		String brakemanVer = brakemanResult.get("brakeman_version").toString();
		String startTime = brakemanResult.get("start_time").toString(); 
		String endTime = brakemanResult.get("end_time").toString();
		int controllerCount = Integer.parseInt(brakemanResult.get("number_of_controllers").toString());
		int modelCount = Integer.parseInt(brakemanResult.get("number_of_models").toString());
		int templateCount = Integer.parseInt(brakemanResult.get("number_of_templates").toString());
		int warningCount = Integer.parseInt(brakemanResult.get("security_warnings").toString());
		Timestamp dueDate = setDueDate();
		
		String htmlLink = input.get("html_link").toString();
		String owner = input.get("owner").toString();
		
		// construct RTC defect content
		String summary = "[Brakeman] " + warningCount + " Security warnings found in project " + project +
				" during Jenkins build";
		
		StringBuilder descBuilder = new StringBuilder(); 
		descBuilder.append("Rails Application Path: " + appPath + "\n");
		descBuilder.append("Ruby Version: " + rubyVer + "\n");
		descBuilder.append("Rails Version: " + railsVer + "\n");
		descBuilder.append("Brakeman Version: " + brakemanVer + "\n");
		descBuilder.append("Build: " + build + "\n");
		descBuilder.append("Start Time: " + startTime + "\n");
		descBuilder.append("End Time: " + endTime + "\n\n");
		descBuilder.append("Number of Warnings: " + warningCount + "\n");
		descBuilder.append("Number of Models: " + modelCount + "\n");
		descBuilder.append("Number of Controllers: " + controllerCount + "\n");
		descBuilder.append("Number of Templates: " + templateCount + "\n");
		descBuilder.append("Detailed Report: " + htmlLink + "\n");
		String description = descBuilder.toString();
		
		String fileAgainstComponent = null;
		
		String foundInActivity = RTCClientConstants.RTC_DEFECT_FOUND_IN_ACTIVITY_SECURITY;
		String severity = defectSeverity; 
		
		SecurityAutoDefect defect = new SecurityAutoDefect(summary, description, fileAgainstComponent, 
				owner, severity, foundInActivity, null, dueDate, this.rtcClient.getSubscriberList()); 

		// create defect
		RTCClientMethodResponse response = new RTCClientMethodResponse();
		
		try {
			// RTC
			response = rtcClient.createDefect(defect);
			// TEST DB
			brakemanDefectService.createRTCDefect(response.getItemNum(), response.getItemSummary(), 
					project, response.getItemStatus(), response.getItemLink(), build);
		} catch (TeamRepositoryException e) {
			//logger.logp(Level.SEVERE, rtcClient.getClass().getName(), e.getStackTrace()[0].getMethodName(), e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		"Unable to create defect for project " + project + "! Cause: " + e.getMessage());
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		"Unable to create defect for project " + project + "! Cause: " + e.getCause());
		}
		
		logger.exiting(className, sourceMethod);
		return responseAgent.getRTCItemHandlerResponse(response, project + "-" + build, null);
	}
	
	/**
	 * This API updates a defect for Brakeman warnings of a Rails project
	 */
	@POST
	@Path("/updateBrakemanDefect")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateBrakemanDefect(		
			@QueryParam(RTCClientConstants.PARAM_PROJECT) String project,
			@QueryParam(RTCClientConstants.PARAM_DEFECT_NUM) String defectNum,
			@QueryParam(RTCClientConstants.PARAM_BUILD_NAME) String build,
			@QueryParam(RTCClientConstants.PARAM_SEVERITY) String defectSeverity,
			JSONObject input) {
		String sourceMethod = "updateBrakemanDefect";
		logger.entering(className, sourceMethod);
		
		ResponseAgent responseAgent = new ResponseAgent(); 
		
		// check parameter invalidity
		Response handlerResponse = checkParameterInvalidity(project, build, defectSeverity, responseAgent);
		if (handlerResponse != null) return handlerResponse;
		if (defectNum == null || defectNum=="") return responseAgent.getNullParamResponse(RTCClientConstants.PARAM_DEFECT_NUM);
		int defectId = -1; 
		try {
			defectId = Integer.parseInt(defectNum); 
		} catch (Exception e) {
			responseAgent.getInvalidIntParamResponse(RTCClientConstants.PARAM_DEFECT_NUM);
		}
		
		// parse input JSON
		JSONObject rtcConfig = (JSONObject) input.get("RTCConfig"); 
		JSONObject brakemanResult = (JSONObject) input.get("BrakemanResult"); 
		
		// establish RTC client
		try {
			establishRTCClient(rtcConfig);
		} catch (Exception e) {
		    return responseAgent.getRTCItemHandlerResponse(null, "establishRTCClient", 
		    		e.getMessage());
		}
		
		// get Brakeman result
		String appPath = brakemanResult.get("app_path").toString(); 
		String railsVer = brakemanResult.get("rails_version").toString(); 
		String rubyVer = brakemanResult.get("ruby_version").toString();
		String brakemanVer = brakemanResult.get("brakeman_version").toString();
		String startTime = brakemanResult.get("start_time").toString(); 
		String endTime = brakemanResult.get("end_time").toString();
		int controllerCount = Integer.parseInt(brakemanResult.get("number_of_controllers").toString());
		int modelCount = Integer.parseInt(brakemanResult.get("number_of_models").toString());
		int templateCount = Integer.parseInt(brakemanResult.get("number_of_templates").toString());
		int warningCount = Integer.parseInt(brakemanResult.get("security_warnings").toString());
		
		String htmlLink = input.get("html_link").toString();
		
		// construct RTC defect content
		String summary = "[Brakeman] " + warningCount + " Security warnings found in project " + project +
				" during Jenkins build";
		
		StringBuilder descBuilder = new StringBuilder(); 
		descBuilder.append("Rails Application Path: " + appPath + "\n");
		descBuilder.append("Ruby Version: " + rubyVer + "\n");
		descBuilder.append("Rails Version: " + railsVer + "\n");
		descBuilder.append("Brakeman Version: " + brakemanVer + "\n");
		descBuilder.append("Build: " + build + "\n");
		descBuilder.append("Start Time: " + startTime + "\n");
		descBuilder.append("End Time: " + endTime + "\n\n");
		descBuilder.append("Number of Warnings: " + warningCount + "\n");
		descBuilder.append("Number of Models: " + modelCount + "\n");
		descBuilder.append("Number of Controllers: " + controllerCount + "\n");
		descBuilder.append("Number of Templates: " + templateCount + "\n");
		descBuilder.append("Detailed Report: " + htmlLink + "\n");
		String description = descBuilder.toString();
		
		String comment = "Updated defect content upon build " + build + ".\nNumber of Warnings: " + warningCount;

		// update defect
		RTCClientMethodResponse response = new RTCClientMethodResponse();
		
		try {
			// RTC
			response = rtcClient.updateDefect(defectId, comment, summary, description, defectSeverity, null);
			// TEST DB
			brakemanDefectService.updateRTCDefect(defectId, response.getItemSummary(), response.getItemStatus(), build);
		} catch (TeamRepositoryException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		"Unable to update defect " + defectNum + " for build " + build + "!");
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		"Unable to update defect " + defectNum + " for build " + build + "!");
		}
		
		logger.exiting(className, sourceMethod);
		return responseAgent.getRTCItemHandlerResponse(response, project + "-" + build, null);
	}
	
	/**
	 * Close a Brakeman defect via the 'Resolve --> Verify' steps with a comment
	 */
	@PUT
	@Path("/closeBrakemanDefect")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response closeBrakemanDefect(			
			@QueryParam(RTCClientConstants.PARAM_COMMENT) String comment,
			@QueryParam(RTCClientConstants.PARAM_DEFECT_NUM) String defectNum,
			@QueryParam(RTCClientConstants.PARAM_BUILD_NAME) String build,
			JSONObject input) {
		String sourceMethod = "closeBrakemanDefect";
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
			brakemanDefectService.updateRTCDefect(defectId, response.getItemSummary(), response.getItemStatus(), build);
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
		return responseAgent.getRTCItemHandlerResponse(response, "Close Brakeman Defect", null);
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
			List<BrakemanDefect> defectList = brakemanDefectService.getAllDefects();
			for (BrakemanDefect defect: defectList) {
				// sync against RTC records
				String currentStatusInDB = defect.getDefectStatus(); 
				String latestStatusInRTC = rtcClient.getWorkItemStatus(defect.getDefectNum()).getItemStatus();
				if (!currentStatusInDB.equalsIgnoreCase(latestStatusInRTC)) {
					brakemanDefectService.updateRTCDefect(defect.getDefectNum(), null, latestStatusInRTC, null);
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
	 * Get open RTC defect for the given project from the TEST DB (only retrieve the latest record)
	 */
	@PUT
	@Path("/getOpenBrakemanDefect")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getOpenBrakemanDefect (			
			@QueryParam(RTCClientConstants.PARAM_PROJECT) String project,
			JSONObject rtcConfig) {
		String sourceMethod = "getOpenBrakemanDefect";
		logger.entering(className, sourceMethod); 
		
		ResponseAgent responseAgent = new ResponseAgent();
		
		// check parameter validity
		if (project == null || project=="") return responseAgent.getNullParamResponse(RTCClientConstants.PARAM_PROJECT);
		
		// parse input JSON
		JSONObject config = (JSONObject) rtcConfig.get("RTCConfig"); 
		
		// establish RTC client
		try {
			establishRTCClient(config);
		} catch (Exception e) {
		    return responseAgent.getRTCItemHandlerResponse(null, "establishRTCClient", 
		    		e.getMessage());
		}
		
		BrakemanDefect openDefect = new BrakemanDefect(); 
		RTCClientMethodResponse response = new RTCClientMethodResponse(); 
		try {
			//TEST DB
			openDefect = brakemanDefectService.getOpenRTCDefectByProject(project);
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
			return responseAgent.getRTCItemHandlerJsonResponse(openDefect.toJSONObject(), "Open Defect for Brakeman", null);
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

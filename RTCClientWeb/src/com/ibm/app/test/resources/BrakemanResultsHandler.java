package com.ibm.app.test.resources;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.QueryParam;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

import com.ibm.app.test.ejbs.BrakemanWarningService;
import com.ibm.app.test.utils.RTCClientConstants;
import com.ibm.app.test.utils.ResponseAgent;


/**
 * Define APIs to handle Brakeman scan results received in JSON format from Jenkins (add into the database)
 * @author ruifengm
 * @since 2016-Nov-17
 */

@Path("brakeman/result") // path must be set and be unique when multiple resource classes are registered under the JAX-RS application
@Stateless
public class BrakemanResultsHandler {
	public static final String className = BrakemanResultsHandler.class.getName();
	public static final Logger logger = Logger.getLogger(className);
	
	@EJB
	private BrakemanWarningService brakemanWarningService; 
	
	/**
	 * Add Brakeman warnings to the TESTDB
	 * @return Response
	 */
	@POST
	@Path("/addWarnings")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addWarnings(
			@QueryParam(RTCClientConstants.PARAM_DEFECT_NUM) String defectNumberStr,
			@QueryParam(RTCClientConstants.PARAM_BUILD_NAME) String buildName,
			JSONArray warningList) {
		String sourceMethod = "addWarnings";
		logger.entering(className, sourceMethod);
		
		ResponseAgent responseAgent = new ResponseAgent(); 
		// check parameter nullability
		if (defectNumberStr == null || defectNumberStr=="") return responseAgent.getNullParamResponse(RTCClientConstants.PARAM_DEFECT_NUM);
		int defectNumber = -1; 
		try {
			defectNumber = Integer.parseInt(defectNumberStr); 
		} catch (Exception e) {
			responseAgent.getInvalidIntParamResponse(RTCClientConstants.PARAM_DEFECT_NUM);
		}
		if (buildName == null || buildName == "") return responseAgent.getNullParamResponse(RTCClientConstants.PARAM_BUILD_NAME);
		
		int warningsCount = 0; 
		// add 
		for (Object obj: warningList){
			JSONObject warning = (JSONObject) obj;
			String warningType = (String)warning.get("warning_type");
			String fileName = (String)warning.get("file");
			String message = (String)warning.get("message");
			String confidence = (String)warning.get("confidence");
			try {
				brakemanWarningService.createBrakemanWarning(warningType, fileName, message, confidence, buildName, defectNumber);
				logger.fine(">>>>> Warning from build " + buildName + " with defect " + defectNumber + " successfully added into the TEST DB.");
				warningsCount++; 
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
				return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, e);
			}
		}
		
		JSONObject returnObject = new JSONObject(); 
		returnObject.put("Warnings Count", warningsCount);
		logger.exiting(className, sourceMethod);
		return responseAgent.getAppTestResultsHandlerJsonResponse(returnObject, sourceMethod + " completed", null);
	}
	
}

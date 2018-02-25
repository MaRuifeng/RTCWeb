package com.ibm.app.test.resources;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.QueryParam;

import com.ibm.app.test.ejbs.interfaces.local.RTCDefectServiceLocal;
import com.ibm.app.test.entities.RTCDefect;
import com.ibm.app.test.utils.NoRecordException;
import com.ibm.app.test.utils.RTCClientConstants;
import com.ibm.app.test.utils.ResponseAgent;
import com.ibm.app.test.utils.app.TestAutoDefect;
import com.ibm.app.test.utils.rtc.responses.RTCClientMethodResponse;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.team.repository.common.TeamRepositoryException;

/**
 * Define APIs to handle RTC test automation defects of the GPO project (create, get, update & delete)
 * @author ruifengm
 * @since 2015-Dec-15
 */

@Path("rtc") // path must be set and be unique when multiple resource classes are registered under the JAX-RS application
@Stateless
public class RTCTestAutoDefectHandler extends RTCWorkItemHandler {
	public static final String className = RTCTestAutoDefectHandler.class.getName();
	public static final Logger logger = Logger.getLogger(className);
	
	@EJB
	private RTCDefectServiceLocal rtcDefectService; 

	/**
	 * This API creates a defect for a test suite automation execution with failures or errors
	 * @param testCategory (accepts only with case ignored: "API", "GUI")
	 * @param testPhase (accepts only with case ignored: "BVT", "SMT", "Regression Test")
	 * @param severity (accepts only with case ignored: "Blocker (Sev 1)", "Major (Sev 2)", "Normal (Sev 3)", "Minor (Sev 4)")
	 * @return RTCWorkItemHandlerResponse as a JSON object
	 */
	@POST
	@Path("/createTestAutoDefect")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createTestAutoDefect(
			@QueryParam(RTCClientConstants.PARAM_TEST_CATEGORY) String testCategory,
			@QueryParam(RTCClientConstants.PARAM_TEST_PHASE) String testPhase,
			@QueryParam(RTCClientConstants.PARAM_SEVERITY) String defectSeverity,
			JSONObject input) {
		String sourceMethod = "createTestAutoDefect";
		logger.entering(className, sourceMethod);
		
		ResponseAgent responseAgent = new ResponseAgent(); 
		
		// check parameter invalidity
		Response handlerResponse = checkParameterInvalidity(testCategory, testPhase, defectSeverity, responseAgent);
		if (handlerResponse != null) return handlerResponse;
		
		// parse input JSON
		JSONObject rtcConfig = (JSONObject) input.get("RTCConfig"); 
		JSONObject testSuiteResult = (JSONObject) input.get("TestSuiteResult"); 
		
		// establish RTC client
		try {
			establishRTCClient(rtcConfig);
		} catch (Exception e) {
		    return responseAgent.getRTCItemHandlerResponse(null, "establishRTCClient", 
		    		e.getMessage());
		}
		
		// get test suite result
		String testSuiteOwner = testSuiteResult.get("Owner").toString(); 
		String testSuiteName = testSuiteResult.get("TestSuiteName").toString(); 
		String testSuitePackage = testSuiteResult.get("TestPackage").toString();
		String executionTimestamp = testSuiteResult.get("ExecutionTimestamp").toString();
		String executionTime = testSuiteResult.get("ExecutionTimeInSeconds").toString() + "s";
		String buildName = ((JSONObject)testSuiteResult.get("Build")).get("BuildName").toString();
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		try {
			cal.setTime(dateFormatter.parse(testSuiteResult.get("DueDate").toString()));
		} catch (ParseException e) {
			logger.logp(Level.SEVERE, className, "testSuiteResult.getDueDate", "Unable to parse the due date to timestamp!");
		    return responseAgent.getRTCItemHandlerResponse(null, "parseDueDate", 
		    		"Unable to parse the due date of " + testSuiteName + " to timestamp!");
		}
		Timestamp dueDate = new Timestamp(cal.getTimeInMillis());
		int testCount = Integer.parseInt(testSuiteResult.get("TestCount").toString());
		int errorCount = Integer.parseInt(testSuiteResult.get("ErrorCount").toString());
		int failureCount = Integer.parseInt(testSuiteResult.get("FailureCount").toString());
		NumberFormat percentFormat = NumberFormat.getPercentInstance(); 
		percentFormat.setMaximumFractionDigits(1);
		String passRate = percentFormat.format( 1 - (double)(errorCount + failureCount)/testCount); 
		JSONArray testCaseResultList = (JSONArray) testSuiteResult.get("TestCaseResults");
		
		// construct RTC defect content
		String summary = null; // sample: [TEST AUTO API]InventoryRegressionJUnitSuite: pass rate - 97.5% 
		if (testCategory.equalsIgnoreCase("API")) summary = RTCClientConstants.RTC_DEFECT_SUMMARY_PREFIX_API; 
		else if (testCategory.equalsIgnoreCase("GUI")) summary = RTCClientConstants.RTC_DEFECT_SUMMARY_PREFIX_GUI; 
		else summary = RTCClientConstants.RTC_DEFECT_SUMMARY_PREFIX + " " + testCategory; 
		summary = summary + " - " + testSuiteName + ": pass rate " + passRate; 
		
		StringBuilder descBuilder = new StringBuilder(); 
		descBuilder.append("Test Suite (Feature) Name: " + testSuiteName + "\n");
		descBuilder.append("Test Suite Package: " + testSuitePackage + "\n");
		descBuilder.append("Execution Date: " + executionTimestamp + "\n");
		descBuilder.append("Execution Duration: " + executionTime + "\n");
		descBuilder.append("Build: " + buildName + "\n");
		descBuilder.append("JUnit Test Count: " + testCount + "\n");
		descBuilder.append("Error Count: " + errorCount + "\n");
		descBuilder.append("Failure Count: " + failureCount + "\n\n");
		descBuilder.append("******** Failure/Error Details ********\n\n");
		Set<String> testLogLinkSet = new HashSet<String>(); // using Set to filter out duplicates
		int index=1; 
		for (Object obj: testCaseResultList){
			JSONObject testCaseResult = (JSONObject) obj;
			String status = testCaseResult.get("Status").toString();
			if(!status.equalsIgnoreCase("SUCCESS")) {
				// description
				descBuilder.append("<" + index + ">\n");
				descBuilder.append("Test Case (Scenario) Name: " + testCaseResult.get("TestCaseName").toString() + "\n");
				// descBuilder.append("Test Case Class Path: " + testCaseResult.get("TestCaseClassPath").toString() + "\n");
				 
				descBuilder.append("Status: " + status + "\n");
				if (status.equalsIgnoreCase("FAILURE")) {
					descBuilder.append("Failure Type: " + testCaseResult.get("FailureType").toString() + "\n");
					// get only the first line of the message
//					String[] msgArr = ((String)testCaseResult.get("FailureMessage")).split("\\n"); 
//					String msg = ""; 
//					for (int i=0; i<msgArr.length; i++) {
//						if (!msgArr[i].isEmpty()) {
//							msg = msgArr[i]; 
//							break;
//						}
//					}
					String msg = (String)testCaseResult.get("FailureMessage");
					if (msg.length() > RTCClientConstants.MSG_LENGTH_THRESHOLD) msg = msg.substring(0, RTCClientConstants.MSG_LENGTH_THRESHOLD) +
							"...[Too long, not fully displayed, check log file]";
					descBuilder.append("Failure Message: " + msg.trim() + "\n\n");
				}
				if (status.equalsIgnoreCase("ERROR")) {
					descBuilder.append("Error Type: " + testCaseResult.get("ErrorType").toString() + "\n");
					// get only the first line of the message
//					String[] msgArr = ((String)testCaseResult.get("FailureMessage")).split("\\n"); 
//					String msg = ""; 
//					for (int i=0; i<msgArr.length; i++) {
//						if (!msgArr[i].isEmpty()) {
//							msg = msgArr[i]; 
//							break;
//						}
//					}
					String msg = (String)testCaseResult.get("ErrorMessage");
					if (msg.length() > RTCClientConstants.MSG_LENGTH_THRESHOLD) msg = msg.substring(0, RTCClientConstants.MSG_LENGTH_THRESHOLD) +
							"...[Too long, not fully displayed, check log file]";
					descBuilder.append("Error Message: " + msg.trim() + "\n\n");
				}
				index++; 
				
				// test log links
				if (testCaseResult.get("TestLogLink") instanceof JSONArray) {
					JSONArray testLogLinkArr = (JSONArray) testCaseResult.get("TestLogLink"); 
					for (Object testLogLink: testLogLinkArr) testLogLinkSet.add((String)testLogLink);
				}
				else if (testCaseResult.get("TestLogLink") instanceof String) {
					testLogLinkSet.add((String)testCaseResult.get("TestLogLink"));
				}
				else {
					logger.logp(Level.SEVERE, className, "testSuiteResult.getTestLogLink", "Test log link format not recognized! Accetp only JSONArray<String> or String.");
				    return responseAgent.getRTCItemHandlerResponse(null, "getTestLogLink", 
				    		"Test log link format not recognized! Accetp only JSONArray<String> or String.");
				}

			}
		}
		String description = descBuilder.toString();
		
		String fileAgainstComponent = null; 
//		if (testSuiteName.equalsIgnoreCase("AccountsRegressionJUnitSuite")) fileAgainstComponent = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_COMP_API_ACT_CONFIG; 
//		if (testSuiteName.equalsIgnoreCase("CheckListRegressionJUnitSuite")) fileAgainstComponent = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_COMP_API_CHKLST_VAL; 
//		if (testSuiteName.equalsIgnoreCase("DeviationRegressionJUnitSuite")) fileAgainstComponent = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_COMP_API_DEVIATION; 
//		if (testSuiteName.equalsIgnoreCase("InventoryRegressionJUnitSuite")) fileAgainstComponent = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_COMP_API_INV_VAL; 
//		if (testSuiteName.equalsIgnoreCase("MHCRegressionJUnitSuite")) fileAgainstComponent = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_COMP_API_MHC; 
//		if (testSuiteName.equalsIgnoreCase("SecExcRegressionJUnitSuite")) fileAgainstComponent = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_COMP_API_SEC_EXEC; 
//		if (testSuiteName.equalsIgnoreCase("CiratsRegressionJUnitSuite")) fileAgainstComponent = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_COMP_API_DEVIATION; 
//		if (testSuiteName.equalsIgnoreCase("ReportRegressionJUnitSuite")) fileAgainstComponent = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_COMP_API_ECM_RPT; 
//		if (testSuiteName.equalsIgnoreCase("HCRegressionJUnitSuite")) fileAgainstComponent = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_COMP_API_HC_CYC; 
//		if (testSuiteName.equalsIgnoreCase("AdapterConfigRegressionJUnitSuite")) fileAgainstComponent = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_COMP_API_INV_VAL; 
//		if (testSuiteName.equalsIgnoreCase("ConfigRegressionJUnitSuite")) fileAgainstComponent = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_COMP_API_INV_VAL; 
//		if (testSuiteName.equalsIgnoreCase("ObjStoreArtifactRegressionJUnitSuite")) fileAgainstComponent = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_COMP_API_OBJ_STORE; 
//		if (testSuiteName.equalsIgnoreCase("ScaRelayUploadRegressionJUnitSuite")) fileAgainstComponent = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_COMP_API_SCA_RELAY; 
//		if (testSuiteName.equalsIgnoreCase("ObjStoreRegressionJUnitSuite")) fileAgainstComponent = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_COMP_API_OBJ_STORE; 
//		if (testSuiteName.equalsIgnoreCase("ReportDashboardRegressionJUnitSuite")) fileAgainstComponent = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_COMP_API_RPT_DASHBD; 
//		if (testSuiteName.equalsIgnoreCase("AdapterRegressionJUnitSuite")) fileAgainstComponent = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_COMP_API_SCA_ADP; 

		
		String foundInActivity = testPhase; 
		String severity = defectSeverity; 
		String filedAgainst = "";
		if (testCategory.contains("Console")) {
			filedAgainst = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_CC;
		} else if (testCategory.contains("Kitchen")) {
			filedAgainst = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_CONTENT;
		} else {
			filedAgainst = RTCClientConstants.RTC_DEFECT_FILED_AGAINST_CUKE;
		}
		
		// comment
		String testCatUrl = ""; 
		if (testCategory.contains("Console")) testCatUrl = "/cucumber-result/console/summary.html";
		if (testCategory.contains("UI")) testCatUrl = "/cucumber-result/ui/summary.html";
		if (testCategory.contains("Kitchen")) {
			if (testCategory.contains("Windows")) testCatUrl = "/kitchen-result/windows/" + testCategory + "-summary.html";
			if (testCategory.contains("AIX")) testCatUrl = "/kitchen-result/aix/" + testCategory + "-summary.html";
			if (testCategory.contains("Linux")) testCatUrl = "/kitchen-result/linux/" + testCategory + "-summary.html";
		}
				
		String htmlReportLink = "http://9.51.163.190/ccssd-test/" + buildName + "/" + testPhase.toLowerCase() + testCatUrl;
		String comment = "Created defect content upon build " + buildName + ".\nReport: " + htmlReportLink;
		
		TestAutoDefect defect = new TestAutoDefect(summary, description, filedAgainst, fileAgainstComponent, 
				testSuiteOwner, severity, foundInActivity, null, dueDate, this.rtcClient.getSubscriberList(), testLogLinkSet, comment); 

		// create defect
		RTCClientMethodResponse response = new RTCClientMethodResponse();
		
		try {
			// RTC
			response = rtcClient.createDefect(defect);
			// TEST DB
			rtcDefectService.createRTCDefect(response.getItemNum(), response.getItemSummary(), 
					testSuiteName, response.getItemStatus(), response.getItemLink(), buildName);
		} catch (TeamRepositoryException e) {
			//logger.logp(Level.SEVERE, rtcClient.getClass().getName(), e.getStackTrace()[0].getMethodName(), e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		"Unable to create defect for test suite " + testSuiteName + "! Cause: " + e.getMessage());
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		"Unable to create defect for test suite " + testSuiteName + "! Cause: " + e.getCause());
		}
		
		logger.exiting(className, sourceMethod);
		return responseAgent.getRTCItemHandlerResponse(response, testCategory + "-" + testPhase, null);
	}
	
	/**
	 * This API updates a defect for a test suite automation execution with failures or errors
	 * @param defectNum
	 * @param testCategory (accepts only with case ignored: "API", "GUI")
	 * @param testPhase (accepts only with case ignored: "BVT", "SMT", "Regression Test")
	 * @param severity (accepts only with case ignored: "Blocker (Sev 1)", "Major (Sev 2)", "Normal (Sev 3)", "Minor (Sev 4)")
	 * @return RTCWorkItemHandlerResponse as a JSON object
	 */
	@POST
	@Path("/updateTestAutoDefect")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateTestAutoDefect(		
			@QueryParam(RTCClientConstants.PARAM_DEFECT_NUM) String defectNum,
			@QueryParam(RTCClientConstants.PARAM_TEST_CATEGORY) String testCategory,
			@QueryParam(RTCClientConstants.PARAM_TEST_PHASE) String testPhase,
			@QueryParam(RTCClientConstants.PARAM_SEVERITY) String defectSeverity,
			JSONObject input) {
		String sourceMethod = "updateTestAutoDefect";
		logger.entering(className, sourceMethod);
		
		ResponseAgent responseAgent = new ResponseAgent(); 
		
		// check parameter invalidity
		Response handlerResponse = checkParameterInvalidity(testCategory, testPhase, defectSeverity, responseAgent);
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
		JSONObject testSuiteResult = (JSONObject) input.get("TestSuiteResult"); 
		
		// establish RTC client
		try {
			establishRTCClient(rtcConfig);
		} catch (Exception e) {
		    return responseAgent.getRTCItemHandlerResponse(null, "establishRTCClient", 
		    		e.getMessage());
		}
		
		// parse input JSON
		String testSuiteName = testSuiteResult.get("TestSuiteName").toString(); 
		String testSuitePackage = testSuiteResult.get("TestPackage").toString();
		String executionTimestamp = testSuiteResult.get("ExecutionTimestamp").toString();
		String executionTime = testSuiteResult.get("ExecutionTimeInSeconds").toString() + "s";
		int testCount = Integer.parseInt(testSuiteResult.get("TestCount").toString());
		int errorCount = Integer.parseInt(testSuiteResult.get("ErrorCount").toString());
		int failureCount = Integer.parseInt(testSuiteResult.get("FailureCount").toString());
		String buildName = ((JSONObject)testSuiteResult.get("Build")).get("BuildName").toString();
		NumberFormat percentFormat = NumberFormat.getPercentInstance(); 
		percentFormat.setMaximumFractionDigits(1);
		String passRate = percentFormat.format( 1 - (double)(errorCount + failureCount)/testCount); 
		JSONArray testCaseResultList = (JSONArray) testSuiteResult.get("TestCaseResults");
		
		// construct RTC defect content
		String summary = null; // sample: [TEST AUTO API]InventoryRegressionJUnitSuite: pass rate - 97.5% 
		if (testCategory.equalsIgnoreCase("API")) summary = RTCClientConstants.RTC_DEFECT_SUMMARY_PREFIX_API; 
		else if (testCategory.equalsIgnoreCase("GUI")) summary = RTCClientConstants.RTC_DEFECT_SUMMARY_PREFIX_GUI; 
		else summary = RTCClientConstants.RTC_DEFECT_SUMMARY_PREFIX + " " + testCategory; 
		summary = summary + " - " + testSuiteName + ": pass rate " + passRate; 
		
		StringBuilder descBuilder = new StringBuilder(); 
		descBuilder.append("Test Suite (Feature) Name: " + testSuiteName + "\n");
		descBuilder.append("Test Suite Package: " + testSuitePackage + "\n");
		descBuilder.append("Execution Date: " + executionTimestamp + "\n");
		descBuilder.append("Execution Duration: " + executionTime + "\n");
		descBuilder.append("Build: " + buildName + "\n");
		descBuilder.append("JUnit Test Count: " + testCount + "\n");
		descBuilder.append("Error Count: " + errorCount + "\n");
		descBuilder.append("Failure Count: " + failureCount + "\n\n");
		descBuilder.append("******** Failure/Error Details ********\n\n");
		Set<String> testLogLinkSet = new HashSet<String>(); // using Set to filter out duplicates
		int index=1; 
		for (Object obj: testCaseResultList){
			JSONObject testCaseResult = (JSONObject) obj;
			String status = testCaseResult.get("Status").toString();
			if(!status.equalsIgnoreCase("SUCCESS")) {
				// description
				descBuilder.append("<" + index + ">\n");
				descBuilder.append("Test Case (Scenario) Name: " + testCaseResult.get("TestCaseName").toString() + "\n");
				// descBuilder.append("Test Case Class Path: " + testCaseResult.get("TestCaseClassPath").toString() + "\n");
				 
				descBuilder.append("Status: " + status + "\n");
				if (status.equalsIgnoreCase("FAILURE")) {
					descBuilder.append("Failure Type: " + testCaseResult.get("FailureType").toString() + "\n");
					// get only the first line of the message
//					String[] msgArr = ((String)testCaseResult.get("FailureMessage")).split("\\n"); 
//					String msg = ""; 
//					for (int i=0; i<msgArr.length; i++) {
//						if (!msgArr[i].isEmpty()) {
//							msg = msgArr[i]; 
//							break;
//						}
//					}
					String msg = (String)testCaseResult.get("FailureMessage");
					if (msg.length() > RTCClientConstants.MSG_LENGTH_THRESHOLD) msg = msg.substring(0, RTCClientConstants.MSG_LENGTH_THRESHOLD) +
							"...[Too long, not fully displayed, check log file]";
					descBuilder.append("Failure Message: " + msg.trim() + "\n\n");
				}
				if (status.equalsIgnoreCase("ERROR")) {
					descBuilder.append("Error Type: " + testCaseResult.get("ErrorType").toString() + "\n");
					// get only the first line of the message
//					String[] msgArr = ((String)testCaseResult.get("FailureMessage")).split("\\n"); 
//					String msg = ""; 
//					for (int i=0; i<msgArr.length; i++) {
//						if (!msgArr[i].isEmpty()) {
//							msg = msgArr[i]; 
//							break;
//						}
//					}
					String msg = (String)testCaseResult.get("ErrorMessage");
					if (msg.length() > RTCClientConstants.MSG_LENGTH_THRESHOLD) msg = msg.substring(0, RTCClientConstants.MSG_LENGTH_THRESHOLD) +
							"...[Too long, not fully displayed, check log file]";
					descBuilder.append("Error Message: " + msg.trim() + "\n\n");
				}
				index++; 
				
				// test log links
				if (testCaseResult.get("TestLogLink") instanceof JSONArray) {
					JSONArray testLogLinkArr = (JSONArray) testCaseResult.get("TestLogLink"); 
					for (Object testLogLink: testLogLinkArr) testLogLinkSet.add((String)testLogLink);
				}
				else if (testCaseResult.get("TestLogLink") instanceof String) {
					testLogLinkSet.add((String)testCaseResult.get("TestLogLink"));
				}
			}
		}
		String description = descBuilder.toString();
		
		// String comment = "Updated defect content on test automation built and run at " + executionTimestamp + ".";
		String testCatUrl = ""; 
		if (testCategory.contains("Console")) testCatUrl = "/cucumber-result/console/summary.html";
		if (testCategory.contains("UI")) testCatUrl = "/cucumber-result/ui/summary.html";
		if (testCategory.contains("Kitchen")) {
			if (testCategory.contains("Windows")) testCatUrl = "/kitchen-result/windows/" + testCategory + "-summary.html";
			if (testCategory.contains("AIX")) testCatUrl = "/kitchen-result/aix/" + testCategory + "-summary.html";
			if (testCategory.contains("Linux")) testCatUrl = "/kitchen-result/linux/" + testCategory + "-summary.html";
		}
				
		String htmlReportLink = "http://9.51.163.190/ccssd-test/" + buildName + "/" + testPhase.toLowerCase() + testCatUrl;
		String comment = "Updated defect content upon build " + buildName + ".\nReport: " + htmlReportLink;

		// update defect
		RTCClientMethodResponse response = new RTCClientMethodResponse();
		
		try {
			// RTC
			response = rtcClient.updateDefect(defectId, comment, summary, description, defectSeverity, testLogLinkSet);
			// TEST DB
			rtcDefectService.updateRTCDefect(defectId, response.getItemSummary(), response.getItemStatus(), buildName);
		} catch (TeamRepositoryException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		"Unable to update defect " + defectNum + " for test suite " + testSuiteName + "!");
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		"Unable to update defect " + defectNum + " for test suite " + testSuiteName + "!");
		}
		
		logger.exiting(className, sourceMethod);
		return responseAgent.getRTCItemHandlerResponse(response, testCategory + "-" + testPhase, null);
	}
	
	/**
	 * Close a test automation defect via the 'Resolve --> Verify' steps with a comment
	 * @param defectNum
	 * @param comment (optional)
	 * @return RTCWorkItemHandlerResponse as a JSON Object
	 */
	@PUT
	@Path("/closeTestAutoDefect")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response closeTestAutoDefect(			
			@QueryParam(RTCClientConstants.PARAM_DEFECT_NUM) String defectNum,
			@QueryParam(RTCClientConstants.PARAM_COMMENT) String comment,
			@QueryParam(RTCClientConstants.PARAM_BUILD_NAME) String buildName,
			JSONObject input) {
		String sourceMethod = "closeTestAutoDefect";
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
		
		// Overwrite comment	
		String htmlReportLink = "http://9.51.163.190/ccssd-test/" + buildName;
		comment = "Closed defect upon build " + buildName + ".\nReport: " + htmlReportLink;
		
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
			rtcDefectService.updateRTCDefect(defectId, response.getItemSummary(), response.getItemStatus(), buildName);
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
		return responseAgent.getRTCItemHandlerResponse(response, "Close Test Automation Defect", null);
	}
	
	/**
	 * Sync status of the latest RTC defects of all test suites
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
			// get defect list from TEST DB
			// List<RTCDefect> defectList = rtcDefectService.getAllDefects();
			List<RTCDefect> defectList = rtcDefectService.getLatestDefectsForAllSuites();
			for (RTCDefect defect: defectList) {
				// sync against RTC records
				String currentStatusInDB = defect.getDefectStatus(); 
				String latestStatusInRTC = rtcClient.getWorkItemStatus(defect.getDefectNum()).getItemStatus();
				if (!currentStatusInDB.equalsIgnoreCase(latestStatusInRTC)) {
					rtcDefectService.updateRTCDefect(defect.getDefectNum(), null, latestStatusInRTC, null);
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
	 * Get open RTC defect for the given test suite from the TEST DB (only retrieve the latest record)
	 * and verify its status against RTC record
	 * @param testSuiteName, input JSON containing RTC config properties
	 * @return RTCClientMethodResponse as a JSONObject
	 * @throws Exception
	 */
	@PUT
	@Path("/getOpenTestAutoDefect")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getOpenTestAutoDefect (			
			@QueryParam(RTCClientConstants.PARAM_TEST_SUITE_NAME) String testSuiteName,
			JSONObject rtcConfig) {
		String sourceMethod = "getOpenTestAutoDefect";
		logger.entering(className, sourceMethod); 
		
		ResponseAgent responseAgent = new ResponseAgent();
		
		// check parameter validity
		if (testSuiteName == null || testSuiteName=="") return responseAgent.getNullParamResponse(RTCClientConstants.PARAM_TEST_SUITE_NAME);
		
		// parse input JSON
		JSONObject config = (JSONObject) rtcConfig.get("RTCConfig"); 
		
		// establish RTC client
		try {
			establishRTCClient(config);
		} catch (Exception e) {
		    return responseAgent.getRTCItemHandlerResponse(null, "establishRTCClient", 
		    		e.getMessage());
		}
		
		RTCDefect openDefect = new RTCDefect(); 
		RTCClientMethodResponse response = new RTCClientMethodResponse(); 
		try {
			//TEST DB
			openDefect = rtcDefectService.getOpenRTCDefectByTestSuiteName(testSuiteName);
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
			return responseAgent.getRTCItemHandlerJsonResponse(openDefect.toJSONObject(), "Open Defect for Test Suite", null);
		}
		else return responseAgent.getRTCItemHandlerJsonResponse(null, "Out of Sync", "TEST DB defect status record does not tally with RTC. Sync first."); 
	}
	
	/**
	 * Get latest RTC defects for all test suites from the TEST DB
	 */
	@GET
	@Path("/getAllLatestRTCDefects")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllLatestRTCDefects() {
		String sourceMethod = "getAllLatestRTCDefects";
		logger.entering(className, sourceMethod);
		
		ResponseAgent responseAgent = new ResponseAgent();

		JSONArray defectArray = new JSONArray(); 
		try {
			// get defect list from TEST DB
			List<RTCDefect> defectList = rtcDefectService.getLatestDefectsForAllSuites();
			for (RTCDefect defect: defectList) {
				defectArray.add(defect.toJSONObject());
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
		    return responseAgent.getRTCItemHandlerResponse(null, sourceMethod, 
		    		e.getMessage());
		}
		logger.exiting(className, sourceMethod);
		return responseAgent.getRTCItemHandlerJsonArrayResponse(defectArray, "Get All Latest Defects", null);
	}
	
	/**
	 * Check parameter invalidity
	 * @param testCategory
	 * @param testPhase
	 * @param defectSeverity
	 * @param responseAgent
	 * @return
	 */
	private Response checkParameterInvalidity(String testCategory, String testPhase, String defectSeverity, ResponseAgent responseAgent) {
		
		// check parameter validity 
		if (testCategory == null || testCategory == "") return responseAgent.getNullParamResponse(RTCClientConstants.PARAM_TEST_CATEGORY);
		if (testPhase == null || testPhase == "") return responseAgent.getNullParamResponse(RTCClientConstants.PARAM_TEST_PHASE);
		if (defectSeverity == null || defectSeverity == "") return responseAgent.getNullParamResponse(RTCClientConstants.PARAM_SEVERITY);
		
//		if (!testCategory.equalsIgnoreCase("API") && !testCategory.equalsIgnoreCase("GUI")) return responseAgent.getRTCItemHandlerResponse(null, 
//				"checkParameterValidity", "Test category can only be 'API' or 'GUI'!");
//		if (!testPhase.equalsIgnoreCase(RTCClientConstants.RTC_DEFECT_FOUND_IN_ACTIVITY_BVT)
//				&& !testPhase.equalsIgnoreCase(RTCClientConstants.RTC_DEFECT_FOUND_IN_ACTIVITY_REG)) return responseAgent.getRTCItemHandlerResponse(null, 
//				"checkParameterValidity", "Test category can only be " + RTCClientConstants.RTC_DEFECT_FOUND_IN_ACTIVITY_BVT +
//						" or " + RTCClientConstants.RTC_DEFECT_FOUND_IN_ACTIVITY_REG + "!");
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

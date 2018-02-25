package com.ibm.app.test.resources;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

import com.ibm.app.test.ejbs.AppBuildService;
import com.ibm.app.test.ejbs.BuildPassRateService;
import com.ibm.app.test.ejbs.TestCaseResultService;
import com.ibm.app.test.ejbs.TestCaseService;
import com.ibm.app.test.ejbs.TestSuiteResultService;
import com.ibm.app.test.ejbs.TestSuiteService;
import com.ibm.app.test.ejbs.interfaces.local.RTCDefectServiceLocal;
import com.ibm.app.test.entities.AppBuild;
import com.ibm.app.test.entities.BuildPassRate;
import com.ibm.app.test.entities.TestSuiteResult;
import com.ibm.app.test.results.BuildDefectCount;
import com.ibm.app.test.utils.NoRecordException;
import com.ibm.app.test.utils.RTCClientConstants;
import com.ibm.app.test.utils.ResponseAgent;
import com.ibm.app.test.utils.TimeStampParser;


/**
 * Define APIs to handle application test results received in JSON format (add, delete, update & sync etc. with the database)
 * @author ruifengm
 * @since 2015-Dec-18
 */

@Path("testResult") // path must be set and be unique when multiple resource classes are registered under the JAX-RS application
@Stateless
public class AppTestResultsHandler {
	public static final String className = AppTestResultsHandler.class.getName();
	public static final Logger logger = Logger.getLogger(className);
	
	@EJB
	private TestSuiteService testSuiteService;
	@EJB
	private TestCaseService testCaseService;
	@EJB
	private TestSuiteResultService testSuiteRstService; 
	@EJB
	private TestCaseResultService testCaseRstService; 
	@EJB
	private AppBuildService appBuildService;
	@EJB
	private BuildPassRateService buildPassRateService;
	@EJB
	private RTCDefectServiceLocal rtcDefectService; 


	
	/**
	 * Sync test suites listed in the test results to the TESTDB
	 * @param testCategory
	 * @param testPhase
	 * @param testSuiteResultList
	 * @return Response
	 */
	@PUT
	@Path("/syncTestSuites")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response syncTestSuites(
			@QueryParam(RTCClientConstants.PARAM_TEST_CATEGORY) String testCategory,
			@QueryParam(RTCClientConstants.PARAM_TEST_PHASE) String testPhase, 
			JSONArray testSuiteResultList) {
		String sourceMethod = "syncTestSuites";
		logger.entering(className, sourceMethod);
		
		ResponseAgent responseAgent = new ResponseAgent(); 
		
		// check parameter nullability
		Response handlerResponse = checkParameterNotNull(testCategory, RTCClientConstants.PARAM_TEST_CATEGORY, responseAgent); 
		if (handlerResponse != null) return handlerResponse;
		
		// check parameter type 
		handlerResponse = checkParameterType(testSuiteResultList, JSONArray.class, "testSuiteResultList", responseAgent); 
		if (handlerResponse != null) return handlerResponse;
		
		JSONArray addedTestSuites = new JSONArray(); 
		// sync 
		for (Object obj: testSuiteResultList){
			JSONObject testSuiteResult = (JSONObject) obj; 
			String testSuiteName = "";
			try {
				testSuiteName = (String)testSuiteResult.get("TestSuiteName");
				testSuiteService.getTestSuiteByName(testSuiteName); 
				logger.fine(">>>>> Test suite " + testSuiteName + " already exits in the TEST DB.");
			} catch (NoRecordException getExc) {
				if (getExc.getClass().getName().equalsIgnoreCase(NoRecordException.class.getName())){
					try {
						testSuiteService.createTestSuite(testSuiteName, (String)testSuiteResult.get("TestPackage"), 
								(String)testSuiteResult.get("Owner"), testCategory);
						logger.fine(">>>>> Test suite " + testSuiteName + " successfully added into the TEST DB.");
						addedTestSuites.add(testSuiteResult);
					} catch (Exception createExc){
						StringWriter sw = new StringWriter();
						createExc.printStackTrace(new PrintWriter(sw));
						logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
						return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, createExc);
					}
				}
				else {
					StringWriter sw = new StringWriter();
					getExc.printStackTrace(new PrintWriter(sw));
					logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
					return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, getExc);
				}
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
				return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, e);
			}
		}
		JSONObject returnObject = new JSONObject(); 
		returnObject.put("Newly Added Test Suites", addedTestSuites);
		logger.exiting(className, sourceMethod);
		return responseAgent.getAppTestResultsHandlerJsonResponse(returnObject, sourceMethod + " completed", null);
	}
	
	/**
	 * Sync test cases listed in the test results to the TESTDB
	 * @return Response
	 */
	@PUT
	@Path("/syncTestCases")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response syncTestCases(JSONArray testSuiteResultList) {
		String sourceMethod = "syncTestCases";
		logger.entering(className, sourceMethod);
		
		ResponseAgent responseAgent = new ResponseAgent(); 
		
		JSONArray addedTestCases = new JSONArray(); 
		// sync 
		for (Object suiteObj: testSuiteResultList){
			JSONObject testSuiteResult = (JSONObject) suiteObj; 
			JSONArray testCaseResultList = (JSONArray) testSuiteResult.get("TestCaseResults");
			for (Object caseObj: testCaseResultList) {
				JSONObject testCaseResult = (JSONObject) caseObj; 
				String testCaseName = (String)testCaseResult.get("TestCaseName");
				String testCasePath = (String)testCaseResult.get("TestCaseClassPath");
				int rqmTestCaseId = Integer.parseInt(testCaseResult.get("RQMTestCaseId").toString());
				try {
					testCaseService.getTestCaseByNameAndPath(testCaseName, testCasePath);
					logger.fine(">>>>> Test case " + testCaseName + " with class path " + testCasePath + " already exits in the TEST DB.");
				} catch (NoRecordException getExc) {
					if (getExc.getClass().getName().equalsIgnoreCase(NoRecordException.class.getName())){
						try {
							testCaseService.createTestCase(testCaseName, testCasePath, (String)testCaseResult.get("TestSuite"), rqmTestCaseId);
							logger.fine(">>>>> Test case " + testCaseName + " with class path " + testCasePath + " successfully added into the TEST DB.");
							addedTestCases.add(testCaseResult);
						} catch (Exception createExc){
							StringWriter sw = new StringWriter();
							createExc.printStackTrace(new PrintWriter(sw));
							logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
							return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, createExc);
						}
					}
					else {
						StringWriter sw = new StringWriter();
						getExc.printStackTrace(new PrintWriter(sw));
						logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
						return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, getExc);
					}
				} catch (Exception e) {
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
					return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, e);
				}
			}
		}
		JSONObject returnObject = new JSONObject(); 
		returnObject.put("Newly Added Test Cases", addedTestCases);
		logger.exiting(className, sourceMethod);
		return responseAgent.getAppTestResultsHandlerJsonResponse(returnObject, sourceMethod + " completed", null);
	}
	
	/**
	 * Sync application builds listed in the test results to the TESTDB
	 * @return Response
	 */
	@PUT
	@Path("/syncAppBuilds")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response syncAppBuilds(JSONArray testSuiteResultList) {
		String sourceMethod = "syncAppBuilds";
		logger.entering(className, sourceMethod);
		
		ResponseAgent responseAgent = new ResponseAgent(); 
		
		JSONArray addedAppBuilds = new JSONArray(); 
		// sync 
		for (Object obj: testSuiteResultList){
			JSONObject testSuiteResult = (JSONObject) obj; 
			JSONObject build = (JSONObject) testSuiteResult.get("Build");
			String buildName = "";
			int buildVersion = 0;
			String gitBranch = "";
			String sprint = "";
			Timestamp buildTimestamp = null;
			// add build if not existing
			try {
				buildName = (String)build.get("BuildName");
				buildVersion = Integer.parseInt((String)build.get("BuildVersion"));
				gitBranch = (String)build.get("GitBranch");
				sprint = (String)build.get("Sprint");
				try {
					buildTimestamp = TimeStampParser.getSQLTimestamp((String)build.get("BuildTimestamp"));
				} catch (ParseException e){
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
					return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, e);
				}
				appBuildService.getAppBuildByName(buildName);
				logger.fine(">>>>> App build " + buildName + " already exits in the TEST DB.");
			} catch (NoRecordException getExc) {
				if (getExc.getClass().getName().equalsIgnoreCase(NoRecordException.class.getName())){
					try {
						appBuildService.createAppBuild(buildName, buildVersion, buildTimestamp, gitBranch, sprint); 
						logger.fine(">>>>> App build " + buildName + " successfully added into the TEST DB.");
						addedAppBuilds.add(build);
					} catch (Exception createExc){
						StringWriter sw = new StringWriter();
						createExc.printStackTrace(new PrintWriter(sw));
						logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
						return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, createExc);
					}
				}
				else {
					StringWriter sw = new StringWriter();
					getExc.printStackTrace(new PrintWriter(sw));
					logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
					return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, getExc);
				}
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
				return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, e);
			}
		}
		JSONObject returnObject = new JSONObject(); 
		returnObject.put("Newly Added Application Builds", addedAppBuilds);
		logger.exiting(className, sourceMethod);
		return responseAgent.getAppTestResultsHandlerJsonResponse(returnObject, sourceMethod + " completed", null);
	}
	
	
	/**
	 * Add test results to the TESTDB
	 * @return Response
	 */
	@POST
	@Path("/addTestResults")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addTestResults(
			@QueryParam(RTCClientConstants.PARAM_TEST_PHASE) String testPhase,
			JSONArray testSuiteResultList) {
		String sourceMethod = "addTestResults";
		logger.entering(className, sourceMethod);
		
		ResponseAgent responseAgent = new ResponseAgent(); 
		// check parameter nullability
		Response handlerResponse = checkParameterNotNull(testPhase, RTCClientConstants.PARAM_TEST_PHASE, responseAgent); 
		if (handlerResponse != null) return handlerResponse;
		
		JSONObject resultsCount = new JSONObject();  
		int testSuiteResultsCount = 0; 
		int testCaseResultsCount = 0; 
		// add 
		for (Object obj: testSuiteResultList){
			JSONObject testSuiteResult = (JSONObject) obj;
			int testSuiteRstId = -1; 
			
			// Add test suite results
			String testSuiteName = (String)testSuiteResult.get("TestSuiteName");
			int testCount = Integer.parseInt(testSuiteResult.get("TestCount").toString());
			int errorCount = Integer.parseInt(testSuiteResult.get("ErrorCount").toString());
			int failureCount = Integer.parseInt(testSuiteResult.get("FailureCount").toString());
			int suiteExecutionTime = Integer.parseInt(testSuiteResult.get("ExecutionTimeInSeconds").toString());
			String buildName = ((JSONObject)testSuiteResult.get("Build")).get("BuildName").toString();
			Timestamp exeTimestamp = null; 
			try {
				exeTimestamp = TimeStampParser.getSQLTimestamp((String)testSuiteResult.get("ExecutionTimestamp"));
			} catch (ParseException e){
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
				return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, e);
			}
			try {
				testSuiteRstId = testSuiteRstService.createTestSuiteResult(testCount, failureCount, errorCount, exeTimestamp, suiteExecutionTime, testSuiteName, testPhase, buildName);
				logger.fine(">>>>> Test result for test suite " + testSuiteName + " successfully added into the TEST DB.");
				testSuiteResultsCount++; 
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
				return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, e);
			}
			
			// Add test case results
			JSONArray testCaseResultList = (JSONArray) testSuiteResult.get("TestCaseResults");
			for (Object caseObj: testCaseResultList) {
				JSONObject testCaseResult = (JSONObject) caseObj; 
				String testCaseName = (String)testCaseResult.get("TestCaseName");
				String testCasePath = (String)testCaseResult.get("TestCaseClassPath");
				String status = (String)testCaseResult.get("Status"); 
				String errorType = (String)testCaseResult.get("ErrorType");
				String errorMsg = (String)testCaseResult.get("ErrorMessage");
				String failureType = (String)testCaseResult.get("FailureType");
				String failureMsg = (String)testCaseResult.get("FailureMessage");
				int caseExecutionTime = Integer.parseInt(testCaseResult.get("ExecutionTimeInSeconds").toString());
				try {
					testCaseRstService.createTestCaseResult(status, errorType, errorMsg, failureType, failureMsg, caseExecutionTime,
							testSuiteRstId, testCaseName, testCasePath, buildName);
					logger.fine(">>>>> Test result for test case " + testCaseName + " with path " + testCasePath + " successfully added into the TEST DB.");
					testCaseResultsCount++; 
				} catch (Exception e) {
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
					return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, e);
				}
			}
		}
		
		resultsCount.put("Number of Test Suite Results Added", testSuiteResultsCount);
		resultsCount.put("Number of Test Case Results Added", testCaseResultsCount);
		JSONObject returnObject = new JSONObject(); 
		returnObject.put("Results Count", resultsCount);
		logger.exiting(className, sourceMethod);
		return responseAgent.getAppTestResultsHandlerJsonResponse(returnObject, sourceMethod + " completed", null);
	}
	

	/**
	 * Compute test pass rate of given build for each test phase under each test category, then save to the TESTDB
	 * @return Response
	 */
	@GET
	@Path("/syncBuildPassRate")
	@Produces(MediaType.APPLICATION_JSON)
	public Response syncBuildPassRate(
			@QueryParam(RTCClientConstants.PARAM_BUILD_NAME) String buildName) {
		String sourceMethod = "syncBuildPassRate";
		logger.entering(className, sourceMethod);
		
		ResponseAgent responseAgent = new ResponseAgent(); 
		
		JSONArray computedBuildPassRates = new JSONArray();
		// sync 
		try {
			// delete existing build pass rates of the build
			buildPassRateService.deleteBuildPassRateByName(buildName);
			// get test suite result counts grouped by test category and test phase for the given build from TEST DB
			List<BuildPassRate> resultList = testSuiteRstService.getTestResultCountsInCategoryAndPhaseByBuild(buildName);
			// compute and save build pass rate for each test phase under each test category
			for (BuildPassRate result: resultList) {
				try {
					// create new
					buildPassRateService.createBuildPassRate(result.getTestCategory(), result.getTestPhase(), result.getTestCount(), 
							result.getErrorCount(), result.getFailureCount(), result.getAppBuild().getBuildName());
					logger.fine(">>>>> Pass rate for build " + result.getAppBuild().getBuildName() + " in test phase " 
							+ result.getTestPhase() + " of test category " + result.getTestCategory() + " successfully added into the TEST DB.");
				} catch (Exception e) {
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
					return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, e);
				}
				computedBuildPassRates.add(result.toJSONObject());
			}

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
			return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, e);
		}
		JSONObject returnObject = new JSONObject(); 
		returnObject.put("Synced Build Pass Rate", computedBuildPassRates);
		logger.exiting(className, sourceMethod);
		return responseAgent.getAppTestResultsHandlerJsonResponse(returnObject, sourceMethod + " completed", null);
	}
	
	/**
	 * Get build pass rates by page number and size from TESTDB
	 * @return Response
	 */
	@GET
	@Path("/getBuildPassRates")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBuildPassRates(
			@QueryParam(RTCClientConstants.PARAM_PAGE_NUMBER) String pageNum,
			@QueryParam(RTCClientConstants.PARAM_PAGE_SIZE) String pageSize, 
			@QueryParam(RTCClientConstants.PARAM_TEST_CATEGORY) String testCategory,
			@QueryParam(RTCClientConstants.PARAM_TEST_PHASE) String testPhase,
			@QueryParam(RTCClientConstants.PARAM_BUILD_NAME) String buildName,
			@QueryParam(RTCClientConstants.PARAM_SPRINT) String sprint) {
		String sourceMethod = "getBuildPassRates";
		logger.entering(className, sourceMethod);
		
		ResponseAgent responseAgent = new ResponseAgent(); 
		
		// check parameter nullability
//		Response handlerResponse = checkParameterNotNull(pageNum, RTCClientConstants.PARAM_PAGE_NUMBER, responseAgent); 
//		if (handlerResponse != null) return handlerResponse;
//		handlerResponse = checkParameterNotNull(pageSize, RTCClientConstants.PARAM_PAGE_SIZE, responseAgent); 
//		if (handlerResponse != null) return handlerResponse;
		// check parameter type
		int num = 1; // default
		int size = 50; // default
		try {
			num = Integer.parseInt(pageNum); 
		} catch (Exception e) {
			responseAgent.getInvalidIntParamResponse(RTCClientConstants.PARAM_PAGE_NUMBER);
		}
		try {
			size = Integer.parseInt(pageSize);
		} catch (Exception e) {
			responseAgent.getInvalidIntParamResponse(RTCClientConstants.PARAM_PAGE_SIZE);
		}
		
		JSONArray obtainedBuildPassRates = new JSONArray();
		long totalCount = 0;
		try {
			// store build pass rates in a new array list as JPA result list is read only
			List<BuildPassRate> resultList = new ArrayList<BuildPassRate>(buildPassRateService.getBuildPassRates(testCategory, testPhase, buildName, sprint, num, size));
//			// sort the list by build version if it can be converted to a number
//			Collections.sort(resultList, new Comparator<BuildPassRate>() {
//				@Override
//				public int compare(BuildPassRate object1, BuildPassRate object2) {
//					long version1 = -1; 
//					long version2 = -1; 
//					try {
//						version1 = Long.parseLong(object1.getAppBuild().getBuildVersion());
//						version2 = Long.parseLong(object2.getAppBuild().getBuildVersion()); 
//						return version1 < version2 ? 1 : -1; // descending order
//					} catch (Exception e) {
//						logger.info("Build version not in number format. No need to sort again."); 
//						return 0;
//					}
//				}
//			});
			totalCount = buildPassRateService.getTotalBuildPassRateCount(testCategory, testPhase, buildName, sprint);
			
			// Use HashSet to get unique build entries
			Set<AppBuild> buildSet = new HashSet<AppBuild>();
			for (BuildPassRate result: resultList) buildSet.add(result.getAppBuild());
			
			// Move unique build entries to a list
			List<AppBuild> buildList = new ArrayList<AppBuild>(buildSet);
			// Sort by build version
			Collections.sort(buildList, new Comparator<AppBuild>() {
				@Override
				public int compare(AppBuild object1, AppBuild object2) {
					int version1 = -1; 
					int version2 = -1; 
					try {
						version1 = object1.getBuildVersion();
						version2 = object2.getBuildVersion(); 
						return version1 > version2 ? 1 : -1; // ascending order
					} catch (Exception e) {
						logger.info("Unable to sort by build version."); 
						return 0;
					}
				}
		    });
			
			for (AppBuild build: buildList) {
				System.out.println(build.toString());
				JSONObject buildResultObj = new JSONObject(); 
				JSONArray resultArr = new JSONArray(); 
				for (BuildPassRate result: resultList) {
					JSONObject resultObj = result.toJSONObject();
					if (result.getAppBuild().equals(build)) {
						resultObj.remove("App Build");
						resultArr.add(resultObj);
					}
				}
				buildResultObj = build.toJSONObject();
				buildResultObj.put("Pass Rates", resultArr);
				// defect counts
				buildName = build.getBuildName();
				List<BuildDefectCount> defectCountList = rtcDefectService.getBuildDefectCountsByStatus(buildName, testCategory);
				JSONObject defectCountObj = new JSONObject();
				long totalDefectCount = 0;  
				for (BuildDefectCount defectCount: defectCountList) {
					defectCountObj.put(defectCount.getDefectStatus(), defectCount.getDefectCount());		
					totalDefectCount = totalDefectCount + defectCount.getDefectCount(); 
				}
				if (testCategory != null && testCategory != "") {
					defectCountObj.put("Test Category", testCategory);
				} else {
					defectCountObj.put("Test Category", "All");
				}
				defectCountObj.put("Total", totalDefectCount);
				buildResultObj.put("Defect Count", defectCountObj);
				obtainedBuildPassRates.add(buildResultObj);
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
			return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, e);
		}
		JSONObject returnObject = new JSONObject(); 
		returnObject.put("Items", obtainedBuildPassRates);
		returnObject.put("Total Item Count", totalCount);
		logger.exiting(className, sourceMethod);
		return responseAgent.getAppTestResultsHandlerJsonResponse(returnObject, sourceMethod + " completed", null);
	}

	/**
	 * Get all sprints from TESTDB
	 * @return Response
	 */
	@GET
	@Path("/getAllSprints")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllSprints() {
		String sourceMethod = "getAllSprints";
		logger.entering(className, sourceMethod);
		
		ResponseAgent responseAgent = new ResponseAgent(); 
		
		JSONArray sprintArray = new JSONArray();
		try {
			List<String> resultList = appBuildService.getAllSprints();
			for (String result: resultList)
				sprintArray.add(result);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
			return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, e);
		}
		JSONObject returnObject = new JSONObject(); 
		returnObject.put("Sprints", sprintArray);
		returnObject.put("Total Count", sprintArray.size());
		logger.exiting(className, sourceMethod);
		return responseAgent.getAppTestResultsHandlerJsonResponse(returnObject, sourceMethod + " completed", null);
	}
	
	/**
	 * Get test results of their respective latest builds from TESTDB
	 * @return Response
	 */
	@GET
	@Path("/getLatestBuildTestResults")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLatestBuildTestResults() {
		String sourceMethod = "getLatestBuildTestResults";
		logger.entering(className, sourceMethod);
		
		ResponseAgent responseAgent = new ResponseAgent(); 
		
		JSONArray obtainedTestResults = new JSONArray();
		try {
			List<TestSuiteResult> resultList = testSuiteRstService.getResultsFromLatestTestSuiteBuilds();

			for (TestSuiteResult result: resultList)
				obtainedTestResults.add(result.toJSONObject());
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
			return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, e);
		}
		JSONObject returnObject = new JSONObject(); 
		returnObject.put("Latest Test Suite Results", obtainedTestResults);
		returnObject.put("Total Count", obtainedTestResults.size());
		logger.exiting(className, sourceMethod);
		return responseAgent.getAppTestResultsHandlerJsonResponse(returnObject, sourceMethod + " completed", null);
	}
	
	/**
	 * Get latest test results (in terms of execution timestamp) of the given build with details (test suite results and defects)
	 * @param testCategory
	 * @param testPhase
	 * @param buildName
	 * @return Response
	 */
	@GET
	@Path("/getBuildLatestTestResults")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBuildTestResults(
			@QueryParam(RTCClientConstants.PARAM_TEST_CATEGORY) String testCategory,
			@QueryParam(RTCClientConstants.PARAM_TEST_PHASE) String testPhase,
			@QueryParam(RTCClientConstants.PARAM_BUILD_NAME) String buildName) {
		String sourceMethod = "getBuildLatestTestResults";
		logger.entering(className, sourceMethod);
		
		ResponseAgent responseAgent = new ResponseAgent(); 
		// check parameter nullability
		Response handlerResponse = checkParameterNotNull(buildName, RTCClientConstants.PARAM_BUILD_NAME, responseAgent); 
		if (handlerResponse != null) return handlerResponse;
		
		JSONArray obtainedTestResults = new JSONArray();
		try {
			List<TestSuiteResult> tsrList = testSuiteRstService.getLatestTestSuiteResultsByBuild(buildName, testCategory, testPhase);
			for (TestSuiteResult tsr: tsrList){
				JSONObject result = tsr.toJSONObject();
				try {
					result.put("Defect", rtcDefectService.getLatestRTCDefectByTestSuiteNameAndBuild(tsr.getTestSuite().getSuiteName(), 
							tsr.getAppBuild().getBuildName()).toJSONObject());
				} catch (NoRecordException getExp) {
					result.put("Defect", null); 
				}
				obtainedTestResults.add(result);
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString());
			return responseAgent.getAppTestResultsHandlerJsonResponse(null, sourceMethod, e);
		}
		JSONObject returnObject = new JSONObject(); 
		returnObject.put("Items", obtainedTestResults);
		returnObject.put("Total Count", obtainedTestResults.size());
		logger.exiting(className, sourceMethod);
		return responseAgent.getAppTestResultsHandlerJsonResponse(returnObject, sourceMethod + " completed", null);
	}
	
	/**
	 * Check non-null params
	 * @param testCategory
	 * @param testPhase
	 * @param responseAgent
	 * @return
	 */
	private Response checkParameterNotNull(Object param, String paramName, ResponseAgent responseAgent) {
		if (param == null || param == "") return responseAgent.getNullParamResponse(paramName);
		return null;
	}
	
	/**
	 * Check param class type
	 * @param testCategory
	 * @param testPhase
	 * @param responseAgent
	 * @return
	 */
	private Response checkParameterType(Object param, Class<?> correctType, String paramName, ResponseAgent responseAgent) {
		if (correctType.isInstance(param)) return null;
		return responseAgent.getInvalidParamTypeResponse(paramName, correctType.getName(), param.getClass().getName());
	}

}

package com.ibm.app.test.ejbs;

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.ibm.app.test.entities.AppBuild;
import com.ibm.app.test.entities.TestCase;
import com.ibm.app.test.entities.TestCaseResult;

/**
 * Service provider class for the entity bean TestCaseResult 
 * @author ruifengm
 * @since 2015-Dec-08
 */
@Stateless
public class TestCaseResultService extends AppTestJPAService {
	private static final String className = TestCaseResultService.class.getName();
	private static final Logger logger = Logger.getLogger(className);
	
	@EJB
	private TestCaseService testCaseService;
	
	@EJB 
	private TestSuiteResultService testSuiteRstService;
	
	@EJB
	private AppBuildService appBuildService;
	
	/**
	 * Create test case result
	 * @param status
	 * @param errorType
	 * @param errorMsg
	 * @param failureType
	 * @param failureMsg
	 * @param executionSeconds
	 * @param testSuiteRstId
	 * @param testCaseName
	 * @param testCasePath
	 * @param buildName
	 * @throws Exception
	 */
	public void createTestCaseResult (String status, String errorType, String errorMsg, 
			String failureType, String failureMsg, int executionSeconds, int testSuiteRstId, String testCaseName, String testCasePath, String buildName) throws Exception {
		String methodName = "createTestCaseResult"; 
		logger.entering(className, methodName);
		
		TestCaseResult testCaseRst = new TestCaseResult(); 
		testCaseRst.setStatus(status);
		testCaseRst.setErrorType(errorType);
		testCaseRst.setErrorMsg(errorMsg);
		testCaseRst.setFailureType(failureType);
		testCaseRst.setFailureMsg(failureMsg);
		testCaseRst.setExecutionSeconds(executionSeconds);
		
		TestCase testCase = testCaseService.getTestCaseByNameAndPath(testCaseName, testCasePath);
		testCaseRst.setTestCase(testCase);
		
		AppBuild appBuild = appBuildService.getAppBuildByName(buildName);
		testCaseRst.setAppBuild(appBuild);
		
		testCaseRst.setTestSuiteRst(testSuiteRstService.getTestSuiteRstById(testSuiteRstId));
		
		persistEntity(testCaseRst);
		
		logger.exiting(className, methodName);
	}
	
	/**
	 * Get test case result by ID
	 * @param testCaseRstId
	 * @return
	 * @throws Exception
	 */
	public TestCaseResult getTestCaseRstById (int testCaseRstId) throws Exception {
		String methodName = "getTestCaseRstById"; 
		logger.entering(className, methodName);
		TestCaseResult testCaseRst = (TestCaseResult) getEntity(TestCaseResult.class, testCaseRstId);
		logger.exiting(className, methodName);
		return testCaseRst;
	}
	
}

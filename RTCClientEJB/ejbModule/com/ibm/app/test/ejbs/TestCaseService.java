package com.ibm.app.test.ejbs;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.ibm.app.test.entities.TestCase;
import com.ibm.app.test.entities.TestSuite;
import com.ibm.app.test.utils.NoRecordException;


/**
 * Service provider class for the entity bean TestCase
 * @author ruifengm
 * @since 2015-Dec-07
 */
@Stateless
public class TestCaseService extends AppTestJPAService {
	
	private static final String className = TestCaseService.class.getName();
	private static final Logger logger = Logger.getLogger(className);
	
	@EJB
	private TestSuiteService testSuiteService;
	
	/**
	 * Create test case
	 * @param testCaseName
	 * @param testCasePath
	 * @param testSuiteName
	 * @throws Exception
	 */
	public void createTestCase (String testCaseName, String testCasePath, String testSuiteName, int rqmTestCaseId) throws Exception {
		String methodName = "createTestCase"; 
		logger.entering(className, methodName);
		
		TestCase testCase = new TestCase(); 
		testCase.setCaseName(testCaseName); 
		testCase.setCasePath(testCasePath);
		testCase.setRqmTestCaseId(rqmTestCaseId);
	
		TestSuite testSuite = testSuiteService.getTestSuiteByName(testSuiteName); 
		
		//testCase.setTestSuite(em.find(TestSuite.class, testSuite.getTestSuiteId()));
		testCase.setTestSuite(testSuite);
		//testCase.setTestSuite(testSuiteService.getTestSuiteById(testSuite.getTestSuiteId()));
		
		persistEntity(testCase);

		logger.exiting(className, methodName);
	}
	
	/**
	 * Get test case by id
	 * @param testCaseId
	 * @return
	 * @throws Exception
	 */
	public TestCase getTestCaseById (int testCaseId) throws Exception {
		String methodName = "getTestCaseById";
		logger.entering(className, methodName);
		TestCase testCase = (TestCase) getEntity(TestCase.class, testCaseId);
		logger.exiting(className, methodName);
		return testCase;
	}
	
	/**
	 * Get test case by name & path
	 * @param caseName
	 * @param casePath
	 * @return
	 * @throws Exception
	 */
	public TestCase getTestCaseByNameAndPath (String caseName, String casePath) throws NoRecordException, Exception {
		String methodName = "getTestCaseByNameAndPath";
		logger.entering(className, methodName);
		TestCase testCase = new TestCase(); 
		
		Query query = em.createNamedQuery("getTestCaseByNameAndPath", TestCase.class); 
		query.setParameter("caseName", caseName);
		query.setParameter("casePath", casePath);
		try {
			testCase = (TestCase) query.getSingleResult(); 
		} catch (NoResultException e) {
			logger.logp(Level.INFO, className, methodName, "No test case named " + caseName + " with path " 
		                          + casePath + " found.", e.getCause());
			throw new NoRecordException("No test case named " + caseName + " with path " + casePath + " found.");
		} catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to get the test case named " + caseName + " with path " 
                    + casePath, e.getCause());
			throw e;
		}
		logger.exiting(className, methodName);
		return testCase;
	}
	

}

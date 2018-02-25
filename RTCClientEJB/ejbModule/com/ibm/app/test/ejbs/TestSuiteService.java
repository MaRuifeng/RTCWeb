package com.ibm.app.test.ejbs;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.ibm.app.test.entities.TestSuite;
import com.ibm.app.test.utils.NoRecordException;


/**
 * Service provider class for the entity bean TestSuite 
 * @author ruifengm
 * @since 2015-Dec-07
 */
@Stateless
public class TestSuiteService extends AppTestJPAService {
	
	private static final String className = TestSuiteService.class.getName();
	private static final Logger logger = Logger.getLogger(className);

	/**
	 * Create test suite
	 * @param testSuiteName
	 * @param testSuitePackage
	 * @param owner
	 * @param testCategory
	 * @throws Exception
	 */
	public void createTestSuite (String testSuiteName, String testSuitePackage, 
			String owner, String testCategory) throws Exception {
		String methodName = "createTestSuite"; 
		logger.entering(className, methodName);
		
		TestSuite testSuite = new TestSuite(); 
		testSuite.setSuiteName(testSuiteName);
		testSuite.setSuitePackage(testSuitePackage);
		testSuite.setOwner(owner);
		testSuite.setTestCategory(testCategory); 
		
		persistEntity(testSuite);
		
		logger.exiting(className, methodName); 
	}
	
	/**
	 * Update test suite
	 * @param testSuiteId
	 * @param testSuiteName
	 * @param testSuitePackage
	 * @param owner
	 * @throws Exception
	 */
	public void updateTestSuite(int testSuiteId, String testSuiteName, String testSuitePackage, 
			String owner) throws Exception {
		String methodName = "updateTestSuite";
		logger.entering(className, methodName);
		
		TestSuite testSuite = (TestSuite) getEntity(TestSuite.class, testSuiteId);
		
		if (testSuiteName != null && !testSuiteName.equalsIgnoreCase("")) testSuite.setSuiteName(testSuiteName);
		if (testSuitePackage != null && !testSuitePackage.equalsIgnoreCase("")) testSuite.setSuitePackage(testSuitePackage);
		if (owner != null && !owner.equalsIgnoreCase("")) testSuite.setOwner(owner);
		
		persistEntity(testSuite);
		
		logger.exiting(className, methodName); 
	}

	/**
	 * Get test suite by id
	 * @param testSuiteId
	 * @return
	 * @throws Exception
	 */
	public TestSuite getTestSuiteById(int testSuiteId) throws Exception {
		String methodName = "getTestSuiteById";
		logger.entering(className, methodName);
		
		TestSuite testSuite = (TestSuite) getEntity(TestSuite.class, testSuiteId);
		logger.exiting(className, methodName);
		return testSuite;
	}
	
	/**
	 * Get test suite by name
	 * @param suiteName
	 * @return
	 * @throws Exception
	 */
	public TestSuite getTestSuiteByName(String suiteName) throws NoRecordException, Exception {
		String methodName = "getTestSuiteByName";
		logger.entering(className, methodName);
		TestSuite testSuite = new TestSuite();
		
		Query query = em.createNamedQuery("getTestSuiteByName", TestSuite.class).setParameter("suiteName", suiteName);
		try {
			testSuite = (TestSuite) query.getSingleResult();
			//testSuite = em.createNamedQuery("getTestSuiteByName", TestSuite.class).setParameter("suiteName", suiteName).getSingleResult();
		} catch (NoResultException e){
			logger.logp(Level.INFO, className, methodName, "No test suite named " + suiteName + " found.", e.getCause());
			throw new NoRecordException("No test suite named " + suiteName + " found.");
		} catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to get the test suite named " + suiteName + ".", e.getCause());
			throw e; 
		}
	
		logger.exiting(className, methodName);
		return testSuite; 
	}
}

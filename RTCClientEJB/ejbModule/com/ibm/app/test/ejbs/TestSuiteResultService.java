package com.ibm.app.test.ejbs;

import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import com.ibm.app.test.entities.AppBuild;
import com.ibm.app.test.entities.BuildPassRate;
import com.ibm.app.test.entities.TestSuite;
import com.ibm.app.test.entities.TestSuiteResult;
import com.ibm.app.test.utils.NoRecordException;

/**
 * Service provider class for the entity bean TestSuiteResult 
 * @author ruifengm
 * @since 2015-Dec-08
 */
@Stateless
public class TestSuiteResultService extends AppTestJPAService {
	private static final String className = TestSuiteResultService.class.getName();
	private static final Logger logger = Logger.getLogger(className);
	
	@EJB
	private TestSuiteService testSuiteService;
	
	@EJB
	private AppBuildService appBuildService;
	
	/**
	 * Create test suite result
	 * @param testCount
	 * @param failureCount
	 * @param errorCount
	 * @param exeTimestamp
	 * @param executionSeconds
	 * @param testSuiteName
	 * @param testPhase
	 * @param buildName
	 * @return test suite result id
	 * @throws Exception
	 */
	public int createTestSuiteResult (int testCount, int failureCount, int errorCount, Timestamp exeTimestamp, int executionSeconds, String testSuiteName, String testPhase, String buildName) throws Exception {
		String methodName = "createTestSuiteResult"; 
		logger.entering(className, methodName);
		
		TestSuiteResult testSuiteRst = new TestSuiteResult(); 
		testSuiteRst.setTestCount(testCount);
		testSuiteRst.setErrorCount(errorCount);
		testSuiteRst.setFailureCount(failureCount);
		testSuiteRst.setExeTimestamp(exeTimestamp);
		testSuiteRst.setExecutionSeconds(executionSeconds);
		testSuiteRst.setTestPhase(testPhase);
		
		TestSuite testSuite = testSuiteService.getTestSuiteByName(testSuiteName); 
		testSuiteRst.setTestSuite(testSuite);
		
		AppBuild appBuild = appBuildService.getAppBuildByName(buildName);
		testSuiteRst.setAppBuild(appBuild);

		persistEntity(testSuiteRst);
		
		logger.exiting(className, methodName);
		return testSuiteRst.getTestSuiteRstId(); 
	}
	
	/**
	 * Get test suite result by ID
	 * @param testSuiteRstId
	 * @return
	 * @throws Exception
	 */
	public TestSuiteResult getTestSuiteRstById (int testSuiteRstId) throws Exception {
		String methodName = "getTestSuiteRstById"; 
		logger.entering(className, methodName);
		TestSuiteResult testSuiteRst = (TestSuiteResult) getEntity(TestSuiteResult.class, testSuiteRstId);
		logger.exiting(className, methodName);
		return testSuiteRst;
	}
	
	/**
	 * Get latest test suite results (in terms of execution timestamp) for given build
	 * @param buildName
	 * @param testCategory
	 * @param testPhase
	 * @return
	 * @throws NoRecordException
	 * @throws Exception
	 */
	public List<TestSuiteResult> getLatestTestSuiteResultsByBuild (String buildName, String testCategory, String testPhase) throws NoRecordException, Exception {
		String methodName = "getTestSuiteResultsByBuild"; 
		logger.entering(className, methodName);
		
		List<TestSuiteResult> resultList = null;
		try {
			CriteriaBuilder queryBuilder = em.getCriteriaBuilder(); 
			// main query
			CriteriaQuery<TestSuiteResult> query = queryBuilder.createQuery(TestSuiteResult.class);
			Root<TestSuiteResult> root = query.from(TestSuiteResult.class);
			Expression<Integer> testSuiteId = root.get("testSuite").get("testSuiteId");  // field to map with subquery
			Expression<Timestamp> exeTimestamp = root.get("exeTimestamp");  // field to map with subquery
			
			// sub query
			Subquery<Timestamp> subQuery = query.subquery(Timestamp.class);
			Root<TestSuiteResult> subRoot = subQuery.from(TestSuiteResult.class);
			Expression<Integer> subTestSuiteId = subRoot.get("testSuite").get("testSuiteId"); // field to map with main query
			Expression<Timestamp> subExeTimestamp = subRoot.get("exeTimestamp");  // field to map with main query
			
			// combine together
			Predicate predicate = queryBuilder.conjunction();
			Predicate subPredicate = queryBuilder.conjunction();
			if (testCategory != null && testCategory != "") {
				//predicate = queryBuilder.and(predicate, queryBuilder.equal(root.get("testSuite").get("testCategory"), testCategory));
				subPredicate = queryBuilder.and(subPredicate, queryBuilder.equal(subRoot.get("testSuite").get("testCategory"), testCategory)); 
			}
			if (testPhase != null && testPhase != "") {
				//predicate = queryBuilder.and(predicate, queryBuilder.equal(root.get("testPhase"), testPhase)); 
				subPredicate = queryBuilder.and(subPredicate, queryBuilder.equal(subRoot.get("testPhase"), testPhase)); 
			}
			if (buildName != null && buildName != "") {
				//predicate = queryBuilder.and(predicate, queryBuilder.equal(root.get("appBuild").get("buildName"), buildName));
				subPredicate = queryBuilder.and(subPredicate, queryBuilder.equal(subRoot.get("appBuild").get("buildName"), buildName)); 
			}
			subPredicate = queryBuilder.and(subPredicate, queryBuilder.equal(subTestSuiteId, testSuiteId));
			subQuery.select(queryBuilder.greatest(subExeTimestamp)).where(subPredicate);
			predicate = queryBuilder.and(predicate, queryBuilder.equal(exeTimestamp, subQuery));
			query.select(root).where(predicate);
			logger.info(em.createQuery(query).toString());
			resultList = em.createQuery(query).getResultList();
		} catch (NoResultException e) {
			logger.logp(Level.INFO, className, methodName, "No test suite result of the given build " + buildName + " found.", e.getCause()); 
			throw new NoRecordException("No test suite result of the given build " + buildName + " found.");
		} catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to get any test suite result for build " + buildName + ".", e.getCause());
			throw e; 
		}
		logger.exiting(className, methodName);
		return resultList;
	}
	
	/**
	 * Get test result counts (total, error and failure) for given build grouped by test category and test phase
	 * @param buildName
	 * @return
	 * @throws NoRecordException
	 * @throws Exception
	 */
	public List<BuildPassRate> getTestResultCountsInCategoryAndPhaseByBuild (String buildName) throws NoRecordException, Exception {
		String methodName = "getTestResultCountsInCategoryAndPhaseByBuild"; 
		logger.entering(className, methodName);
		
		String queryStr="SELECT NEW BuildPassRate(tsr.appBuild," +
				" tsr.testSuite.testCategory, tsr.testPhase," +
				" SUM(tsr.testCount), SUM(tsr.failureCount), SUM(tsr.errorCount))" +
				" FROM TestSuiteResult tsr WHERE tsr.appBuild.buildName = :buildName" +
				" GROUP BY tsr.appBuild, tsr.testSuite.testCategory, tsr.testPhase";

		List<BuildPassRate> resultList = null;
		try {
			TypedQuery<BuildPassRate> query = em.createQuery(queryStr, BuildPassRate.class);
			query.setParameter("buildName", buildName);
			resultList = query.getResultList(); 
		} catch (NoResultException e) {
			logger.logp(Level.INFO, className, methodName, "No test suite result counts of the given build " + buildName + " found.", e.getCause()); 
			throw new NoRecordException("No test suite result counts of the given build " + buildName + " found.");
		} catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to get any test suite result counts for build " + buildName + ".", e.getCause());
			throw e; 
		}
		logger.exiting(className, methodName);
		return resultList;
	}
	
	/**
	 * Get test suite results from their latest builds
	 * @return
	 * @throws NoRecordException
	 * @throws Exception
	 */
	public List<TestSuiteResult> getResultsFromLatestTestSuiteBuilds() throws NoRecordException, Exception {
		String methodName = "getResultsFromLatestTestSuiteBuilds"; 
		logger.entering(className, methodName);
		
		List<TestSuiteResult> tsrList = null;
	
		try {
			// SQL Implementation:
			// SELECT DISTINCT s.SUITE_NAME, b.BUILD_NAME, b.BUILD_VERSION
			// FROM TESTRST.APP_BUILD b, TESTRST.TEST_SUITE s, TESTRST.TEST_SUITE_RESULT sr 
			// WHERE s.TEST_SUITE_ID = sr.TEST_SUITE_ID AND
			//        b.BUILD_VERSION = (SELECT MAX(BIGINT(BUILD_VERSION)) FROM TESTRST.APP_BUILD WHERE BUILD_ID IN 
			//                                   (SELECT BUILD_ID FROM TESTRST.TEST_SUITE_RESULT WHERE TEST_SUITE_ID = sr.TEST_SUITE_ID));
			CriteriaBuilder queryBuilder = em.getCriteriaBuilder(); 
			
			// main query
			CriteriaQuery<TestSuiteResult> query = queryBuilder.createQuery(TestSuiteResult.class);
			Root<TestSuiteResult> root = query.from(TestSuiteResult.class);
			Expression<Integer> buildVersion = queryBuilder.function("INT", Integer.class, 
					root.get("appBuild").get("buildVersion"));  // field to map with subquery1, casted from String to Integer
			Expression<Integer> suiteId = root.get("testSuite").get("testSuiteId").as(Integer.class);
			
			// first-level sub query
			Subquery<Integer> subQuery1 = query.subquery(Integer.class);
			Root<AppBuild> subRoot1 = subQuery1.from(AppBuild.class);
			Path<Integer> buildId1 = subRoot1.get("buildId"); // field to map with subquery2 using SQL keyword 'IN'
			Expression<Integer> buildVersion1 = queryBuilder.function("INT", Integer.class, 
					subRoot1.get("buildVersion"));  // field to map with main query, casted from String to Integer
			
			
			// second-level sub query 
			Subquery<Integer> subQuery2 = subQuery1.subquery(Integer.class);
			Root<TestSuiteResult> subRoot2 = subQuery2.from(TestSuiteResult.class); 
			Expression<Integer> buildId2 = subRoot2.get("appBuild").get("buildId").as(Integer.class); // field to map with subquery1
			Expression<Integer> suiteId2 = subRoot2.get("testSuite").get("testSuiteId").as(Integer.class); // field to map with main query
			
			// combine together
			Predicate predicate2 = queryBuilder.equal(suiteId2, suiteId);
			subQuery2.select(buildId2).where(predicate2); 
			Predicate predicate1 = queryBuilder.in(buildId1).value(subQuery2);
			subQuery1.select(queryBuilder.max(buildVersion1)).where(predicate1);
			Predicate predicate = queryBuilder.equal(buildVersion, subQuery1);
			query.select(root).where(predicate);
			
			// run
			tsrList = em.createQuery(query).getResultList();
		} catch (NoResultException e) {
			logger.logp(Level.INFO, className, methodName, "No test results from latest builds found. DB table empty.", e.getCause()); 
			throw new NoRecordException("No test results from latest builds found. DB table empty.");
		} catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to get any test results from leatest builds.", e.getCause());
			throw e; 
		}
		logger.exiting(className, methodName);
		return tsrList;
	}
}

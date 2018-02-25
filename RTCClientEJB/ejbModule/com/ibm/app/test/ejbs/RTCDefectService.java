package com.ibm.app.test.ejbs;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.ibm.app.test.ejbs.interfaces.local.RTCDefectServiceLocal;
import com.ibm.app.test.entities.AppBuild;
import com.ibm.app.test.entities.RTCDefect;
import com.ibm.app.test.entities.TestSuite;
import com.ibm.app.test.results.BuildDefectCount;
import com.ibm.app.test.utils.Constants;
import com.ibm.app.test.utils.NoRecordException;


/**
 * Session bean implementation
 * Service provider class for the entity bean RTCDefect 
 * @author ruifengm
 * @since 2015-Dec-09
 */
@Stateless
public class RTCDefectService extends AppTestJPAService implements RTCDefectServiceLocal {
	private static final String className = RTCDefectService.class.getName();
	private static final Logger logger = Logger.getLogger(className);
	
	@EJB
	private TestSuiteService testSuiteService; 
	@EJB
	private AppBuildService appBuildService;
	
	/**
	 * Create RTC defect record in the TEST DB
	 * @param DefectNum
	 * @param DefectName
	 * @param testSuiteName
	 * @param defectStatus
	 */
	@Override
	public void createRTCDefect (int defectNum, String defectName, String testSuiteName, String defectStatus, String defectLink, String buildName) throws Exception {
		String methodName = "createRTCDefect"; 
		logger.entering(className, methodName);
		
		Timestamp currentTimestp = new Timestamp(System.currentTimeMillis());
		
		RTCDefect defect = new RTCDefect(); 
		defect.setDefectNum(defectNum);
		defect.setDefectName(defectName);
		defect.setFilingTimestamp(currentTimestp);
		defect.setDefectStatus(defectStatus);
		defect.setDefectLink(defectLink);
	
		TestSuite testSuite = testSuiteService.getTestSuiteByName(testSuiteName);
		defect.setTestSuite(testSuite);
		
		AppBuild appBuild = appBuildService.getAppBuildByName(buildName);
		defect.setAppBuild(appBuild);
		
		persistEntity(defect);
		
		logger.exiting(className, methodName);
	}
	
	/**
	 * Get RTC defect record from the TEST DB by auto-incremented id
	 * @param defectId
	 * @return
	 * @throws Exception
	 */
	@Override
	public RTCDefect getRTCDefectById(int defectId) throws Exception {
		String methodName = "getRTCDefectById"; 
		logger.entering(className, methodName);
		RTCDefect defect = (RTCDefect) getEntity(RTCDefect.class, defectId); 
		logger.exiting(className, methodName);
		return defect; 
	}
	
	/**
	 * Get RTC defect record from the TEST DB by its number in RTC
	 * @param defectNum
	 * @return
	 * @throws Exception
	 */
	@Override
	public RTCDefect getRTCDefectByNumber(int defectNum) throws NoResultException, Exception {
		String methodName = "getRTCDefectByNumber"; 
		logger.entering(className, methodName);
		RTCDefect defect = new RTCDefect();
		
		try {
			defect = em.createNamedQuery("getRtcDefectByNumber", RTCDefect.class)
					.setParameter("defectNum", defectNum)
					.getSingleResult(); 
		} catch (NoResultException e) {
			logger.logp(Level.INFO, className, methodName, "No defect with number " + defectNum + " is found.", e.getCause()); 
			throw e; 
		} catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to get the defect with number " + defectNum + ".", e.getCause()); 
			throw e; 
		}
		logger.exiting(className, methodName);
		return defect;
	}
	
	/**
	 * Update RTC defect name & status record in the TEST DB by its number
	 * @param defectNum
	 * @param defectName
	 * @param defectStatus
	 * @throws Exception
	 */
	@Override
	public void updateRTCDefect (int defectNum, String defectName, String defectStatus, String buildName) throws Exception {
		String methodName = "updateRTCDefect"; 
		logger.entering(className, methodName);
		
		//Timestamp currentTimestp = new Timestamp(System.currentTimeMillis()%1000);
		int modifiCount = -1; 
		RTCDefect defect = getRTCDefectByNumber(defectNum);
		modifiCount = defect.getModifiTimes(); 
		if (modifiCount != -1) {
			if (defectName != null && !defectName.equalsIgnoreCase("")) {
				defect.setDefectName(defectName);
				defect.setModifiTimes(modifiCount + 1);
				if (buildName != null && !buildName.equalsIgnoreCase("")) {
					AppBuild appBuild = appBuildService.getAppBuildByName(buildName);
					defect.setAppBuild(appBuild);
				}
			}
			if (defectStatus != null && !defectStatus.equalsIgnoreCase("")) defect.setDefectStatus(defectStatus);
			// defect.setModifiTimestamp(currentTimestp); // generated by DB by default
		}
		
		persistEntity(defect);
		
		logger.exiting(className, methodName);
	}
	
	/**
	 * Get RTC defect that is not in the 'Resolved' or 'Verified' status for the given test suite
	 * @param testSuiteName
	 * @return RTC defect JPA entity
	 * @throws Exception
	 */
	@Override
	public RTCDefect getOpenRTCDefectByTestSuiteName (String testSuiteName) throws NoRecordException, Exception {
		String methodName = "getOpenRTCDefectByTestSuiteName"; 
		logger.entering(className, methodName);
		
		RTCDefect defect = new RTCDefect();
		
		List<String> closedStatusList = Arrays.asList(Constants.RTC_DEFECT_STATE_RESOLVED, Constants.RTC_DEFECT_STATE_VERIFIED);
		
		try {
			List<RTCDefect> resultList = em.createNamedQuery("getOpenDefectByTestSuiteName", RTCDefect.class)
					.setParameter("testSuiteName", testSuiteName)
					.setParameter("closedStatusList", closedStatusList)
					.getResultList(); 
			if (resultList.size() > 0) defect = resultList.get(resultList.size() - 1 ); // get latest record only
			else throw new NoRecordException("No open defect of the given test suite " + testSuiteName + " is found.");
		} catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to get the open defect of the given test suite " 
		                     + testSuiteName + ".", e.getCause());
			throw e; 
		}
		logger.exiting(className, methodName);
		return defect;
	}
	
	/**
	 * Get latest RTC defect for the given test suite
	 * @param testSuiteName
	 * @return RTC defect JPA entity
	 * @throws Exception
	 */
	@Override
	public RTCDefect getLatestRTCDefectByTestSuiteNameAndBuild (String testSuiteName, String buildName) throws NoRecordException, Exception {
		String methodName = "getLatestRTCDefectByTestSuiteName"; 
		logger.entering(className, methodName);
		RTCDefect defect = new RTCDefect();
		try {
			defect = em.createNamedQuery("getLatestDefectByTestSuiteNameAndBuild", RTCDefect.class)
					.setParameter("testSuiteName", testSuiteName)
					.setParameter("buildName", buildName)
					.getSingleResult();
		} catch (NoResultException e) {
			logger.logp(Level.INFO, className, methodName, "No defect found for test suite " + testSuiteName + " with build " + buildName + ".", e.getCause()); 
			throw new NoRecordException("No defect found for test suite " + testSuiteName + " with build " + buildName + ".");
		} catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to get the open defect of the given test suite " 
		                     + testSuiteName + ".", e.getCause());
			throw e; 
		}
		logger.exiting(className, methodName);
		return defect;
	}
	
	/**
	 * Get all defects 
	 * @return List<RTCDefect>
	 * @throws Exception
	 */
	@Override
	public List<RTCDefect> getAllDefects () throws NoRecordException, Exception {
		String methodName = "getAllDefects"; 
		logger.entering(className, methodName);
		
		List<RTCDefect> defectList = null;
	
		try {
			defectList = em.createNamedQuery("getAllDefects", RTCDefect.class).getResultList(); 
		} catch (NoResultException e) {
			logger.logp(Level.INFO, className, methodName, "No defect found. DB table empty.", e.getCause()); 
			throw new NoRecordException("No defect found. DB table empty.");
		} catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to get any defect.", e.getCause());
			throw e; 
		}
		logger.exiting(className, methodName);
		return defectList;
	}

	/**
	 * Get latest defects for all test suites 
	 * @return List<RTCDefect>
	 * @throws Exception
	 */
	@Override
	public List<RTCDefect> getLatestDefectsForAllSuites() throws NoRecordException, Exception  {
		String methodName = "getLatestDefectsForAllSuites"; 
		logger.entering(className, methodName);
		
		List<RTCDefect> defectList = null;
	
		try {
			defectList = em.createNamedQuery("getLatestDefectsForAllTestSuites", RTCDefect.class).getResultList(); 
		} catch (NoResultException e) {
			logger.logp(Level.INFO, className, methodName, "No defect found. DB table empty.", e.getCause()); 
			throw new NoRecordException("No defect found. DB table empty.");
		} catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to get any defect.", e.getCause());
			throw e; 
		}
		logger.exiting(className, methodName);
		return defectList;
	}
	
	/**
	 * Get defect counts pivoted by defect status for given build, and test category if provided
	 * @param buildName
	 * @param testCategory
	 * @return
	 * @throws NoRecordException
	 * @throws Exception
	 */
	@Override
	public List<BuildDefectCount> getBuildDefectCountsByStatus(String buildName, String testCategory) throws NoRecordException, Exception {
		String methodName = "getBuildDefectCountsByStatus"; 
		logger.entering(className, methodName);
	
		List<BuildDefectCount> resultList = null;
		try {
			String qStr = "";
			if (testCategory != null && testCategory != "") {
				 qStr = "SELECT NEW com.ibm.app.test.results.BuildDefectCount(rd.testSuite.testCategory, rd.appBuild.buildName, rd.defectStatus, COUNT(rd.defectStatus))" +
						" FROM RTCDefect rd" +
						" WHERE rd.appBuild.buildName = :buildName AND rd.testSuite.testCategory = :testCategory" +
						" GROUP BY rd.testSuite.testCategory, rd.appBuild.buildName, rd.defectStatus";
				TypedQuery<BuildDefectCount> query = em.createQuery(qStr, BuildDefectCount.class);
				resultList = query.setParameter("buildName", buildName).setParameter("testCategory", testCategory).getResultList();
			} else {
				qStr = "SELECT NEW com.ibm.app.test.results.BuildDefectCount(rd.appBuild.buildName, rd.defectStatus, COUNT(rd.defectStatus))" +
						" FROM RTCDefect rd WHERE rd.appBuild.buildName = :buildName " +
						" GROUP BY rd.appBuild.buildName, rd.defectStatus";
				TypedQuery<BuildDefectCount> query = em.createQuery(qStr, BuildDefectCount.class);
				resultList = query.setParameter("buildName", buildName).getResultList();
			}


		} catch (NoResultException e) {
			logger.logp(Level.INFO, className, methodName, "No defect found for given build " + buildName + ".", e.getCause()); 
			throw new NoRecordException("No defect found for given build " + buildName + ".");
		} catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to get any defect for given build " + buildName + ".", e.getCause());
			throw e; 
		}
		logger.exiting(className, methodName);
		return resultList;
	}
}
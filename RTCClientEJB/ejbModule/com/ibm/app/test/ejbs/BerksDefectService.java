package com.ibm.app.test.ejbs;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import com.ibm.app.test.entities.BerksDefect;
import com.ibm.app.test.utils.Constants;
import com.ibm.app.test.utils.NoRecordException;


/**
 * Session bean implementation
 * Service provider class for the entity bean BerksDefect 
 * @author ruifengm
 * @since 2016-Mar-17
 */
@Stateless
public class BerksDefectService extends JenkinsJPAService {
	private static final String className = BerksDefectService.class.getName();
	private static final Logger logger = Logger.getLogger(className);
	

	/**
	 * Create RTC defect record in the TEST DB
	 * @param defectNum
	 * @param defectName
	 * @param cookbook
	 * @param gitBranch
	 * @param gitPRLink
	 * @param defectStatus
	 * @param defectLink
	 * @throws Exception
	 */
	public void createRTCDefect (int defectNum, String defectName, String cookbook, String gitBranch, 
			String gitPRLink, String defectStatus, String defectLink) throws Exception {
		String methodName = "createRTCDefect"; 
		logger.entering(className, methodName);
		
		Timestamp currentTimestp = new Timestamp(System.currentTimeMillis());
		
		BerksDefect defect = new BerksDefect(); 
		defect.setDefectNum(defectNum);
		defect.setDefectName(defectName);
		defect.setFilingTimestamp(currentTimestp);
		defect.setDefectStatus(defectStatus);
		defect.setDefectLink(defectLink);
		defect.setCookbook(cookbook);
		defect.setGitBranch(gitBranch);
		defect.setGitPRLink(gitPRLink);
		
		persistEntity(defect);
		
		logger.exiting(className, methodName);
	}
	
	/**
	 * Get RTC defect record from the TEST DB by auto-incremented id
	 * @param defectId
	 * @return
	 * @throws Exception
	 */
	public BerksDefect getRTCDefectById(int defectId) throws Exception {
		String methodName = "getRTCDefectById"; 
		logger.entering(className, methodName);
		BerksDefect defect = (BerksDefect) getEntity(BerksDefect.class, defectId); 
		logger.exiting(className, methodName);
		return defect; 
	}
	
	/**
	 * Get RTC defect record from the TEST DB by its number in RTC
	 * @param defectNum
	 * @return
	 * @throws Exception
	 */
	public BerksDefect getRTCDefectByNumber(int defectNum) throws NoResultException, Exception {
		String methodName = "getRTCDefectByNumber"; 
		logger.entering(className, methodName);
		BerksDefect defect = new BerksDefect();
		
		try {
			defect = em.createNamedQuery("getBerksDefectByNumber", BerksDefect.class)
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
	 * @param gitBranch
	 * @param gitPRLink
	 * @throws Exception
	 */
	public void updateRTCDefect (int defectNum, String defectName, String defectStatus, String gitBranch, String gitPRLink) throws Exception {
		String methodName = "updateRTCDefect"; 
		logger.entering(className, methodName);
		
		//Timestamp currentTimestp = new Timestamp(System.currentTimeMillis()%1000);
		int modifiCount = -1; 
		BerksDefect defect = getRTCDefectByNumber(defectNum);
		modifiCount = defect.getModifiTimes(); 
		if (modifiCount != -1) {
			if (defectName != null && !defectName.equalsIgnoreCase("")) {
				defect.setDefectName(defectName);
				defect.setModifiTimes(modifiCount + 1);
				if (gitBranch != null && !gitBranch.equalsIgnoreCase("")) {
					defect.setGitBranch(gitBranch);
				}
				if (gitPRLink != null && !gitPRLink.equalsIgnoreCase("")) {
					defect.setGitPRLink(gitPRLink);
				}
			}
			if (defectStatus != null && !defectStatus.equalsIgnoreCase("")) defect.setDefectStatus(defectStatus);
			// defect.setModifiTimestamp(currentTimestp); // generated by DB by default
		}
		
		persistEntity(defect);
		
		logger.exiting(className, methodName);
	}
	
	/**
	 * Get RTC defect that is not in the 'Resolved' or 'Verified' status for the given cookbook project
	 * @param cookbooks
	 * @return RTC defect JPA entity
	 * @throws Exception
	 */
	public BerksDefect getOpenRTCDefectByCookbook (String cookbook) throws NoRecordException, Exception {
		String methodName = "getOpenRTCDefectByCookbook"; 
		logger.entering(className, methodName);
		
		BerksDefect defect = new BerksDefect();
		
		List<String> closedStatusList = Arrays.asList(Constants.RTC_DEFECT_STATE_RESOLVED, Constants.RTC_DEFECT_STATE_VERIFIED);
		
		try {
			List<BerksDefect> resultList = em.createNamedQuery("getOpenDefectByCookbook", BerksDefect.class)
					.setParameter("cookbook", cookbook)
					.setParameter("closedStatusList", closedStatusList)
					.getResultList(); 
			if (resultList.size() > 0) defect = resultList.get(resultList.size() - 1 ); // get latest record only
			else throw new NoRecordException("No open defect of the given cookbook" + cookbook + " is found.");
		} catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to get the open defect of the given cookbook " 
		                     + cookbook + ".", e.getCause());
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
	public List<BerksDefect> getAllDefects () throws NoRecordException, Exception {
		String methodName = "getAllDefects"; 
		logger.entering(className, methodName);
		
		List<BerksDefect> defectList = null;
	
		try {
			defectList = em.createNamedQuery("getAllBerksDefects", BerksDefect.class).getResultList(); 
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
}

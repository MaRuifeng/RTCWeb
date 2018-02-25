package com.ibm.app.test.ejbs;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.ibm.app.test.entities.AppBuild;
import com.ibm.app.test.entities.BuildPassRate;
import com.ibm.app.test.utils.NoRecordException;


/**
 * Service provider class for the entity bean BuildPassRate 
 * @author ruifengm
 * @since 2016-Jul-14
 */
@Stateless
public class BuildPassRateService extends AppTestJPAService {
	
	private static final String className = BuildPassRateService.class.getName();
	private static final Logger logger = Logger.getLogger(className);
	
	@EJB
	private AppBuildService appBuildService;


	public void createBuildPassRate (String testCategory, String testPhase, 
			int testCount, int errorCount, int failureCount, String buildName) throws Exception {
		String methodName = "createBuildPassRate"; 
		logger.entering(className, methodName);
		
		BuildPassRate buildPassRate = new BuildPassRate();
		buildPassRate.setTestCategory(testCategory);
		buildPassRate.setTestPhase(testPhase);
		buildPassRate.setTestCount(testCount);
		buildPassRate.setErrorCount(errorCount);
		buildPassRate.setFailureCount(failureCount);
		
		AppBuild appBuild = appBuildService.getAppBuildByName(buildName);
		buildPassRate.setAppBuild(appBuild);
		
		persistEntity(buildPassRate);
		
		logger.exiting(className, methodName); 
	}


	/**
	 * Get build pass rate by id
	 * @param buildPassRateId
	 * @return
	 * @throws Exception
	 */
	public BuildPassRate getBuildPassRateById(int buildPassRateId) throws Exception {
		String methodName = "getBuildPassRateById";
		logger.entering(className, methodName);
		
		BuildPassRate buildPassRate = (BuildPassRate) getEntity(BuildPassRate.class, buildPassRateId);
		logger.exiting(className, methodName);
		return buildPassRate;
	}
	
	/**
	 * Delete build pass rates by build name
	 * @param appBuildName
	 * @return number of deletions
	 * @throws NoRecordException
	 * @throws Exception
	 */
	public int deleteBuildPassRateByName(String appBuildName) throws Exception {
		String methodName = "deleteBuildPassRateByName";
		logger.entering(className, methodName);
		
		int deleteCount = 0;
		Query query = em.createQuery("DELETE FROM BuildPassRate bpr WHERE bpr.appBuild.buildName = :buildName").setParameter("buildName", appBuildName);
		try {
			deleteCount = query.executeUpdate();
		}  catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to delete build pass rates for build " + appBuildName + ".", e.getCause());
			throw e; 
		}
	
		logger.exiting(className, methodName);
		return deleteCount; 
	}
	
	
	/**
	 * Get total build pass rate count with parameters if supplied
	 * @param testCategory
	 * @param testPhase
	 * @return
	 * @throws Exception
	 */
	public long getTotalBuildPassRateCount(String testCategory, String testPhase, String buildName, String sprint) throws Exception {
		String methodName = "getTotalBuildPassRateCount"; 
		logger.entering(className, methodName);
		
		try {
			CriteriaBuilder queryBuilder = em.getCriteriaBuilder(); 
			CriteriaQuery<Long> query = queryBuilder.createQuery(Long.class);
			Root<BuildPassRate> root = query.from(BuildPassRate.class);
			Predicate predicate = queryBuilder.conjunction(); 
			if (testCategory != null && testCategory != "") {
				predicate = queryBuilder.and(predicate, queryBuilder.equal(root.get("testCategory"), testCategory)); 
			}
			if (testPhase != null && testPhase != "") {
				predicate = queryBuilder.and(predicate, queryBuilder.equal(root.get("testPhase"), testPhase)); 
			}
			if (sprint != null && sprint != "") {
				predicate = queryBuilder.and(predicate, queryBuilder.equal(root.get("appBuild").get("sprint"), sprint)); 
			}
			if (buildName != null && buildName != "") {
				predicate = queryBuilder.and(predicate, queryBuilder.equal(root.get("appBuild").get("buildName"), buildName)); 
			}
			query.select(queryBuilder.count(root)).where(predicate);
			return em.createQuery(query).getSingleResult();
		} catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to get total build pass rate count.", e.getCause());
			throw e; 
		}
	}
	
	/**
	 * Get build pass rates by page number and size with other parameters if supplied
	 * @param testCategory
	 * @param testPhase
	 * @param pageNum
	 * @param pageSize
	 * @return
	 * @throws NoRecordException
	 * @throws Exception
	 */
	public List<BuildPassRate> getBuildPassRates (String testCategory, String testPhase, String buildName, String sprint,
			int pageNum, int pageSize) throws NoRecordException, Exception {
		String methodName = "getBuildPassRates"; 
		logger.entering(className, methodName);
		
		List<BuildPassRate> bprList = null;
	
		try {
			CriteriaBuilder queryBuilder = em.getCriteriaBuilder(); 
			CriteriaQuery<BuildPassRate> query = queryBuilder.createQuery(BuildPassRate.class);
			Root<BuildPassRate> root = query.from(BuildPassRate.class);
			Predicate predicate = queryBuilder.conjunction(); 
			if (testCategory != null && testCategory != "") {
				predicate = queryBuilder.and(predicate, queryBuilder.equal(root.get("testCategory"), testCategory)); 
			}
			if (testPhase != null && testPhase != "") {
				predicate = queryBuilder.and(predicate, queryBuilder.equal(root.get("testPhase"), testPhase)); 
			}
			if (buildName != null && buildName != "") {
				predicate = queryBuilder.and(predicate, queryBuilder.equal(root.get("appBuild").get("buildName"), buildName)); 
			}
			if (sprint != null && sprint != "") {
				predicate = queryBuilder.and(predicate, queryBuilder.equal(root.get("appBuild").get("sprint"), sprint)); 
			}
			query.select(root).where(predicate).orderBy(queryBuilder.desc(root.get("appBuild").get("buildVersion")));
			bprList = em.createQuery(query)
					.setFirstResult((pageNum -1) * pageSize)
					.setMaxResults(pageSize)
					.getResultList();
		} catch (NoResultException e) {
			logger.logp(Level.INFO, className, methodName, "No build pass rate found. DB table empty.", e.getCause()); 
			throw new NoRecordException("No build pass rate found. DB table empty.");
		} catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to get any build pass rate.", e.getCause());
			throw e; 
		}
		logger.exiting(className, methodName);
		return bprList;
	}
}

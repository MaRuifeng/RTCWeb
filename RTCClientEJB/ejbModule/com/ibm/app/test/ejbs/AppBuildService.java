package com.ibm.app.test.ejbs;

import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.ibm.app.test.entities.AppBuild;
import com.ibm.app.test.utils.NoRecordException;


/**
 * Service provider class for the entity bean AppBuild 
 * @author ruifengm
 * @since 2016-Jul-14
 */
@Stateless
public class AppBuildService extends AppTestJPAService {
	
	private static final String className = AppBuildService.class.getName();
	private static final Logger logger = Logger.getLogger(className);


	/**
	 * Create app build
	 * @param buildName
	 * @param buildVersion
	 * @param buildTimestamp
	 * @param gitBranch
	 * @param sprint
	 * @throws Exception
	 */
	public void createAppBuild (String buildName, int buildVersion, 
			Timestamp buildTimestamp, String gitBranch, String sprint ) throws Exception {
		String methodName = "createAppBuild"; 
		logger.entering(className, methodName);
		
		AppBuild appBuild = new AppBuild(); 
		appBuild.setBuildName(buildName);
		appBuild.setBuildVersion(buildVersion);
		appBuild.setBuildTimestamp(buildTimestamp);
		appBuild.setGitBranch(gitBranch);
		appBuild.setSprint(sprint);
		
		persistEntity(appBuild);
		
		logger.exiting(className, methodName); 
	}

	/**
	 * Get app build by id
	 * @param appBuildId
	 * @return
	 * @throws Exception
	 */
	public AppBuild getAppBuildById(int appBuildId) throws Exception {
		String methodName = "getAppBuildById";
		logger.entering(className, methodName);
		
		AppBuild appBuild = (AppBuild) getEntity(AppBuild.class, appBuildId);
		logger.exiting(className, methodName);
		return appBuild;
	}
	

	/**
	 * Get app build by name
	 * @param appBuildName
	 * @return
	 * @throws NoRecordException
	 * @throws Exception
	 */
	public AppBuild getAppBuildByName(String appBuildName) throws NoRecordException, Exception {
		String methodName = "getAppBuildByName";
		logger.entering(className, methodName);
		AppBuild appBuild = new AppBuild();
		
		Query query = em.createNamedQuery("getAppBuildByName", AppBuild.class).setParameter("buildName", appBuildName);
		try {
			appBuild = (AppBuild) query.getSingleResult();
			//testSuite = em.createNamedQuery("getTestSuiteByName", TestSuite.class).setParameter("suiteName", suiteName).getSingleResult();
		} catch (NoResultException e){
			logger.logp(Level.INFO, className, methodName, "No app build named " + appBuildName + " found.", e.getCause());
			throw new NoRecordException("No app build named " + appBuildName + " found.");
		} catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to get the app build named " + appBuildName + ".", e.getCause());
			throw e; 
		}
	
		logger.exiting(className, methodName);
		return appBuild; 
	}
	
	/**
	 * Get sprint list
	 * @return
	 * @throws Exception
	 */
	public List<String> getAllSprints() throws Exception {
		String methodName = "getAllSprints"; 
		logger.entering(className, methodName);
		
		try {
			CriteriaBuilder queryBuilder = em.getCriteriaBuilder(); 
			CriteriaQuery<String> query = queryBuilder.createQuery(String.class);
			Root<AppBuild> root = query.from(AppBuild.class);
			query.select(root.get("sprint").as(String.class)).distinct(true).orderBy(queryBuilder.desc(root.get("sprint")));;
			return em.createQuery(query).getResultList();
		} catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to get any sprint.", e.getCause());
			throw e; 
		}
	}
	
}

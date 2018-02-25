package com.ibm.app.test.ejbs;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Parent class for JPA services for Jenkins results
 * @author ruifengm
 * @since 2016-Nov-17
 */
public abstract class JenkinsJPAService {
	private static final String className = JenkinsJPAService.class.getName();
	private static final Logger logger = Logger.getLogger(className);
	
	/* 
	 * Transaction-scoped persistence context --> classical scenario for stateless session beans
	 * When first accessing the entity manager, it checks if current JTA transaction has a context attached, and a new context is 
	 * created if not.
	 * The the entity is read from the database or cache and placed into the context.
	 * When transaction ends (commit or rollback), the context becomes invalid and whatever entities in it become detached. 
	 */ 
	@PersistenceContext(unitName="JenkinsJPA")
	protected EntityManager em;
	
	/*
	 * Application-managed persistence context. 
	 * begin() and close() are required for the entityManager and entityManagerFactory for persist (DB CREATE, INSERT & UPDATE etc.) request
	 * Both entityManager and entityManagerFactory need to be set static such that they can be used across all service providers
	 */
	//protected static EntityManagerFactory emFactory = Persistence.createEntityManagerFactory("TestResultsHandlerJPA"); 
	//protected static EntityManager em = emFactory.createEntityManager(); 
	
	/**
	 * Begin DB entity manager
	 * @throws Exception
	 */
	//public static void begin() {
		//em.getTransaction().begin(); // begin entity manager
	//}
	
	/**
	 * Commit changes and close DB entity manager
	 */
	//public static void close() {
		//em.getTransaction( ).commit( );
		//em.close(); 
		//emFactory.close();
	//}
	
	/**
	 * Persist entities to the database
	 */
	public void persistEntity(Object entity) throws Exception {
		String methodName = "persistEntity"; 
		logger.entering(className, methodName);
		try {
			logger.logp(Level.INFO, className, methodName, "Persisting entity {0}", new Object[]{entity.getClass().getName()});
			em.persist(entity);
			em.flush();
			logger.logp(Level.INFO, className, methodName, "Successfully persisted entity {0}", new Object[]{entity.getClass().getName()});
		} catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to persist entity " + entity.getClass().getName(), e.getCause());
			throw e; 
		} finally {
			logger.exiting(className, methodName);
		}
	}
	
	/**
	 * Get entities from the database by unique ID 
	 */
	public Object getEntity(Class<?> entityClass, int id) throws Exception {
		String methodName = "getEntity"; 
		logger.entering(className, methodName);
		Object entity = new Object(); 
		try {
			logger.logp(Level.INFO, className, methodName, "Getting entity " + entityClass.getName());
			entity = em.find(entityClass, id);
			logger.logp(Level.INFO, className, methodName, "Successfully got entity "+ entityClass.getName());
		} catch (Exception e) {
			logger.logp(Level.SEVERE, className, methodName, "Unable to get entity " + entityClass.getName(), e.getCause());
			throw e;
		} finally {
			logger.exiting(className, methodName);
		}
		return entity;
	}
}

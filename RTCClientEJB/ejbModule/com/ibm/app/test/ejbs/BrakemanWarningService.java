package com.ibm.app.test.ejbs;

import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import com.ibm.app.test.entities.BrakemanDefect;
import com.ibm.app.test.entities.BrakemanWarning;


/**
 * Session bean implementation
 * Service provider class for the entity bean BrakemanWarning
 * @author ruifengm
 * @since 2016-Nov-17
 */
@Stateless
public class BrakemanWarningService extends JenkinsJPAService {
	private static final String className = BrakemanWarningService.class.getName();
	private static final Logger logger = Logger.getLogger(className);
	
	@EJB
	private BrakemanDefectService brakemanDefectService; 
	
	/**
	 * Create Brakeman warning record in the TEST DB
	 * @param warningType
	 * @param fileName
	 * @param message
	 * @param confidence
	 * @param build
	 * @param defectNumber
	 * @throws Exception
	 */
	public void createBrakemanWarning (String warningType, String fileName, String message, String confidence, String build, int defectNumber) throws Exception {
		String methodName = "createBrakemanWarning"; 
		logger.entering(className, methodName);
		
		BrakemanWarning warning = new BrakemanWarning(); 
		warning.setWarningType(warningType);
		warning.setFileName(fileName);
		warning.setMessage(message);
		warning.setConfidence(confidence);
		warning.setBuild(build);
		
		BrakemanDefect defect = brakemanDefectService.getRTCDefectByNumber(defectNumber);
		warning.setBrakemanDefect(defect);
		
		System.out.println(defect.getDefectId());
		System.out.println(warning.toJSONObject().toString());
		
		persistEntity(warning);
		
		logger.exiting(className, methodName);
	}
}

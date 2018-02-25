package com.ibm.app.test.ejbs.interfaces.local;

import java.util.List;

import com.ibm.app.test.entities.RTCDefect;
import com.ibm.app.test.results.BuildDefectCount;

/**
 * A local interface that exposes business methods to local EJB client that runs in the same 
 * deployment/application environment as the EJB. 
 * @author ruifengm
 * @since 2015-Dec-17
 */
public interface RTCDefectServiceLocal {
	public void createRTCDefect (int defectNum, String defectName, String testSuiteName, String defectStatus, String defectLink, String buildName) throws Exception;
	public RTCDefect getRTCDefectById(int defectId) throws Exception; 
	public RTCDefect getRTCDefectByNumber(int defectNum) throws Exception; 
	public void updateRTCDefect (int defectNum, String defectName, String defectStatus, String buildName) throws Exception; 
	public RTCDefect getOpenRTCDefectByTestSuiteName (String testSuiteName) throws Exception; 
	public RTCDefect getLatestRTCDefectByTestSuiteNameAndBuild (String testSuiteName, String buildName) throws Exception; 
	public List<RTCDefect> getAllDefects () throws Exception; 
	public List<RTCDefect> getLatestDefectsForAllSuites () throws Exception; 
	public List<BuildDefectCount> getBuildDefectCountsByStatus(String buildName, String testCategory) throws Exception;
}

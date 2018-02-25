package com.ibm.app.test.utils;

/**
 * Define operational constants for the RTC client 
 * @author ruifengm
 * @since 2015-Nov-27
 */

public interface RTCClientConstants {
	// RTC defect - work item name & attributes etc. (found in project area management, might vary across different RTC repositories)
	public static final String RTC_WORK_ITEM_DEFECT                               = "defect"; 
	public static final String RTC_DEFECT_TYPE                                    = "Defect_Type";
	public static final String RTC_DEFECT_FILED_AGAINST_COMP                      = "fileAgainstComponent";
	public static final String RTC_DEFECT_FOUND_IN_ACTIVITY                       = "foundInActivity";
	public static final String RTC_DEFECT_USER_IMPACT                             = "User_Impact";
	public static final String RTC_DEFECT_SEVERIY                                 = "internalSeverity";
	public static final String RTC_DEFECT_PRIORITY                                = "internalPriority";
	
	
	// RTC task - work item name & attributes etc. (found in project area management)
	public static final String RTC_WORK_ITEM_TASK                                 = "task";

    
    // attribute values of RTC defect found in test automation
	public static final String RTC_DEFECT_TYPE_AUTO                               = "Automation";
	public static final String RTC_DEFECT_TYPE_SECU                               = "Security";
	public static final String RTC_DEFECT_TYPE_BUILD                              = "Build";
	public static final String RTC_DEFECT_SUMMARY_PREFIX_API                      = "[TEST AUTO API]";
	public static final String RTC_DEFECT_SUMMARY_PREFIX_GUI                      = "[TEST AUTO GUI]";
	public static final String RTC_DEFECT_SUMMARY_PREFIX                          = "[TEST AUTO]";
	
//	public static final String RTC_DEFECT_FILED_AGAINST_API                       = "BPM (CCM)/Healthcheck/Rudolf";  // category path: BPM (CCM)/DevOps/Rudolf
//	public static final String RTC_DEFECT_FILED_AGAINST_COMP_API_ACT_CONFIG       = "HC API / Account Onboarding";
//	public static final String RTC_DEFECT_FILED_AGAINST_COMP_API_OBJ_STORE        = "HC API / Object Storage";
//	public static final String RTC_DEFECT_FILED_AGAINST_COMP_API_CHKLST_VAL       = "HC API / Checklist Validation";
//	public static final String RTC_DEFECT_FILED_AGAINST_COMP_API_DEVIATION        = "HC API / Deviation";
//	public static final String RTC_DEFECT_FILED_AGAINST_COMP_API_ECM_RPT          = "HC API / ECM Report";
//	public static final String RTC_DEFECT_FILED_AGAINST_COMP_API_HC_CYC           = "HC API / HC Cycle";
//	public static final String RTC_DEFECT_FILED_AGAINST_COMP_API_HC_SCHE          = "HC API / HC Schedule";
//	public static final String RTC_DEFECT_FILED_AGAINST_COMP_API_INV_VAL          = "HC API / Inventory Validation";
//	public static final String RTC_DEFECT_FILED_AGAINST_COMP_API_MHC              = "HC API / Manual HC";
//	public static final String RTC_DEFECT_FILED_AGAINST_COMP_API_REMEDIATION      = "HC API / Remediation";
//	public static final String RTC_DEFECT_FILED_AGAINST_COMP_API_RPT_DASHBD       = "HC API / Report Dashboard";
//	public static final String RTC_DEFECT_FILED_AGAINST_COMP_API_SCA_RELAY        = "HC API / SCA Relay";
//	public static final String RTC_DEFECT_FILED_AGAINST_COMP_API_SEC_EXEC         = "HC API / Security Exception";
//	public static final String RTC_DEFECT_FILED_AGAINST_COMP_API_USER_CLAS        = "HC API / User Classification";
//	public static final String RTC_DEFECT_FILED_AGAINST_COMP_API_SCA_ADP          = "HC Adapter / Scan Tool / SCA";
	
	public static final String RTC_DEFECT_FILED_AGAINST_CUKE                      = "CC Development Team/Integration Test";
	public static final String RTC_DEFECT_FILED_AGAINST_CC                        = "CC Development Team/Compliance Console";
	public static final String RTC_DEFECT_FILED_AGAINST_CONTENT                   = "CC Development Team/Content";
	
	public static final String RTC_DEFECT_FOUND_IN_ACTIVITY_BVT                   = "BVT";
	public static final String RTC_DEFECT_FOUND_IN_ACTIVITY_REG                   = "Regression Test";
	public static final String RTC_DEFECT_FOUND_IN_ACTIVITY_SECURITY              = "Security Test";
	public static final String RTC_DEFECT_FOUND_IN_ACTIVITY_REVIEW                = "Code Review";
	public static final String RTC_DEFECT_USER_IMPACT_FUNC                        = "Functionality";
	public static final String RTC_DEFECT_USER_IMPACT_SECU                        = "Security";
	public static final String RTC_DEFECT_SEVERIY_BLOCKER                         = "Critical";
	public static final String RTC_DEFECT_SEVERIY_MAJOR                           = "Major";
	public static final String RTC_DEFECT_SEVERIY_NORMAL                          = "Moderate";
	public static final String RTC_DEFECT_SEVERIY_MINOR                           = "Minor";
	public static final String RTC_DEFECT_ACTION_VERIFY                           = "Verify";
	public static final String RTC_DEFECT_STATE_VERIFIED                          = "Verified";
	
	// attribute values of RTC task
	// TODO 
	
	// method params
	public static final String PARAM_TEST_CATEGORY                  ="testCategory";
	public static final String PARAM_TEST_PHASE                     ="testPhase";
	public static final String PARAM_SEVERITY                       ="defectSeverity";
	public static final String PARAM_DEFECT_NUM                     ="defectNumber";
	public static final String PARAM_COMMENT                        ="comment";
	public static final String PARAM_WORK_ITEM_NUM                  ="workItemNumber";
	public static final String PARAM_TEST_SUITE_NAME                ="testSuiteName";
	public static final String PARAM_BUILD_NAME                     ="buildName";
	public static final String PARAM_PAGE_NUMBER                    ="pageNumber";
	public static final String PARAM_PAGE_SIZE                      ="pageSize";
	public static final String PARAM_SPRINT                         ="sprint";
	public static final String PARAM_PROJECT                        ="project";
	public static final String PARAM_COOKBOOK                       ="cookbook";
	public static final String PARAM_GIT_BRANCH                     ="gitBranch";
	
	// input JSON field keys
	// TODO
	
	// response JSON field keys
	public static final String JSON_KEY_RESULT                  ="Result";
	public static final String JSON_KEY_IDENTIFIER              ="Identifier";
	public static final String JSON_KEY_ERROR_MSG               ="Error Message";

	// TEST automation failure/error message length threshold (not displayed in RTC work item description if exceeded) 
	public static final int MSG_LENGTH_THRESHOLD = 200; 
	
	// Due working days
	public static final int DUE_WORKING_DAYS = 5; 
}


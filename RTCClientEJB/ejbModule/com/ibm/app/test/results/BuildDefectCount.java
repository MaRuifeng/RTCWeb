package com.ibm.app.test.results;

import com.ibm.json.java.JSONObject;

/**
 * Custom result class for SQL queries with multiple SELECT expressions, an object oriented alternative to representing results as Object[] elements
 *  - Defect counts of a given build pivoted by defect status 

 * @author ruifengm
 * @since 2016-Jul-20
 */

public class BuildDefectCount {
	private String testCategory;
	private String buildName;
	private String defectStatus; 
	private long defectCount;
	
	// constructors
	public BuildDefectCount(String testCategory, String buildName, String defectStatus,
			long defectCount) {
		super();
		this.testCategory = testCategory;
		this.buildName = buildName;
		this.defectStatus = defectStatus;
		this.defectCount = defectCount;
	}
	
	public BuildDefectCount(String buildName, String defectStatus,
			long defectCount) {
		super();
		this.testCategory = "All";
		this.buildName = buildName;
		this.defectStatus = defectStatus;
		this.defectCount = defectCount;
	}
	
	// getters
	public String getBuildName() {
		return buildName;
	}
	
	public String getTestCategory() {
		return testCategory;
	}

	public String getDefectStatus() {
		return defectStatus;
	}

	public long getDefectCount() {
		return defectCount;
	}

	@Override
	public String toString() {
		return "BuildDefectCount [testCategory=" + testCategory + ", buildName=" + buildName + ", defectStatus="
				+ defectStatus + ", defectCount=" + defectCount + "]";
	} 
	
	public JSONObject toJSONObject() {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("Test Category", this.testCategory);
		jsonObj.put("Build Name", this.buildName);
		jsonObj.put("Defect Status", this.defectStatus);
		jsonObj.put("Defect Count", this.defectCount);
		return jsonObj;
	}

}

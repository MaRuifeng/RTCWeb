package com.ibm.app.test.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.ibm.json.java.JSONObject;

/**
 * Persistence class for the TESTRST.TEST_SUITE table of the TESTDB. 
 * @author ruifengm
 * @since 2015-Dec-07
 */

@Entity
@Table(name="TEST_SUITE")

@NamedQueries({
	@NamedQuery(name="getTestSuiteByName", query="SELECT ts FROM TestSuite ts WHERE ts.suiteName = :suiteName")
})

public class TestSuite implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="TEST_SUITE_ID")
	private int testSuiteId; 
	
	@Column(name="\"SUITE_NAME\"", nullable=false, length=100)
	private String suiteName; 
	@Column(name="\"SUITE_PACKAGE\"", nullable=false, length=500)
	private String suitePackage; 
	@Column(name="\"OWNER\"", nullable=false, length=100)
	private String owner;
	@Column(name="\"TEST_CATEGORY\"", nullable=false, length=50)
	private String testCategory;
	
	// constructor
	public TestSuite() {
		super();
	}

	public int getTestSuiteId() {
		return testSuiteId;
	}

	public void setTestSuiteId(int testSuiteId) {
		this.testSuiteId = testSuiteId;
	}

	public String getSuiteName() {
		return suiteName;
	}

	public String getSuitePackage() {
		return suitePackage;
	}

	public String getOwner() {
		return owner;
	}

	public void setSuiteName(String suiteName) {
		this.suiteName = suiteName;
	}

	public void setSuitePackage(String suitePackage) {
		this.suitePackage = suitePackage;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getTestCategory() {
		return testCategory;
	}


	public void setTestCategory(String testCategory) {
		this.testCategory = testCategory;
	}

	@Override
	public String toString() {
		return "TestSuite [suiteName=" + suiteName + ", suitePackage="
				+ suitePackage + ", owner=" + owner + ", testCategory="
				+ testCategory + "]";
	}
	
	public JSONObject toJSONObject() {
		JSONObject returnObj = new JSONObject(); 
		returnObj.put("Name", this.suiteName);
		returnObj.put("Package", this.suitePackage);
		returnObj.put("Owner", this.owner);
		returnObj.put("Test Category", this.testCategory);
		return returnObj;
	}
}

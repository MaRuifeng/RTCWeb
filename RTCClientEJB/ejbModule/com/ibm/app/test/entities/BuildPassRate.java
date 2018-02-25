package com.ibm.app.test.entities;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Column;

import com.ibm.json.java.JSONObject;



/**
 * Persistence class for the TESTRST.BUILD_PASS_RATE table of the TESTDB. 
 * @author ruifengm
 * @since 2016-Jul-14
 */

@Entity
@Table(name="BUILD_PASS_RATE")

@NamedQueries({
	@NamedQuery(name="getAllBuildPassRates", query="SELECT bpr FROM BuildPassRate bpr")
})

public class BuildPassRate implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="BUILD_PASS_RATE_ID")
	private int buildPassRateId;
	
	@Column(name="\"TEST_CATEGORY\"", nullable=false, length=50)
	private String testCategory;
	@Column(name="\"TEST_PHASE\"", nullable=false, length=50)
	private String testPhase;
	@Column(name="\"TEST_COUNT\"", nullable=false)
	private int testCount; 
	@Column(name="\"ERROR_COUNT\"", nullable=false)
	private int errorCount; 
	@Column(name="\"FAILURE_COUNT\"", nullable=false)
	private int failureCount; 
	@Column(name="\"PASS_RATE\"", nullable=false, updatable=false, insertable=false)
	private double passRate; // GENERATED ALWAYS FOR EACH ROW ON UPDATE
	
	//uni-directional many-to-one foreign key association to TestSuite
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name="BUILD_ID", referencedColumnName="BUILD_ID", nullable=false, insertable=true, updatable=true)
	private AppBuild appBuild;

	// no-argument constructor
	public BuildPassRate() {
		super();
	}
	
	// constructor with arguments (for SQL result use)
	public BuildPassRate(AppBuild appBuild, String testCategory, String testPhase, long testCount,
			long failureCount, long errorCount) {
		super();
		this.testCategory = testCategory;
		this.testPhase = testPhase;
		this.testCount = (int) testCount;
		this.errorCount = (int) errorCount;
		this.failureCount = (int) failureCount;
		this.appBuild = appBuild;
		this.passRate = 1 - (double)(errorCount + failureCount) / testCount;
	}


	public int getBuildPassRateId() {
		return buildPassRateId;
	}

	public String getTestCategory() {
		return testCategory;
	}

	public String getTestPhase() {
		return testPhase;
	}

	public double getPassRate() {
		return passRate;
	}

	public AppBuild getAppBuild() {
		return appBuild;
	}

	public void setTestCategory(String testCategory) {
		this.testCategory = testCategory;
	}

	public void setTestPhase(String testPhase) {
		this.testPhase = testPhase;
	}

	public void setAppBuild(AppBuild appBuild) {
		this.appBuild = appBuild;
	}

	public int getTestCount() {
		return testCount;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public int getFailureCount() {
		return failureCount;
	}

	public void setTestCount(int testCount) {
		this.testCount = testCount;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	public void setFailureCount(int failureCount) {
		this.failureCount = failureCount;
	}

	
	@Override
	public String toString() {
		return "BuildPassRate [buildPassRateId=" + buildPassRateId
				+ ", testCategory=" + testCategory + ", testPhase=" + testPhase
				+ ", testCount=" + testCount + ", errorCount=" + errorCount
				+ ", failureCount=" + failureCount + ", passRate=" + passRate
				+ ", appBuild=" + appBuild + "]";
	}

	public JSONObject toJSONObject() {
		JSONObject returnObj = new JSONObject(); 
		returnObj.put("App Build", this.appBuild.toJSONObject());
		returnObj.put("Test Category", this.testCategory); 
		returnObj.put("Test Phase", this.testPhase); 
		returnObj.put("Test Count", this.testCount); 
		returnObj.put("Error Count", this.errorCount); 
		returnObj.put("Failure Count", this.failureCount); 
		returnObj.put("Pass Rate", this.passRate); 
		
		return returnObj;
	}
}

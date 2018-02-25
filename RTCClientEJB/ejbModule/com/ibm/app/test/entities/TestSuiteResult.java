package com.ibm.app.test.entities;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.ibm.json.java.JSONObject;


/**
 * Persistence class for the TESTRST.TEST_SUITE_RESULT table of the TESTDB. 
 * @author ruifengm
 * @since 2015-Dec-07
 */

@Entity
@Table(name="TEST_SUITE_RESULT")

@NamedQueries({
	@NamedQuery(name="getTestSuiteResultsByBuild", query="SELECT tsr FROM TestSuiteResult tsr WHERE tsr.appBuild.buildName = :buildName"),
	@NamedQuery(name="getTestSuiteResultsByBuildAndCategory", query="SELECT tsr FROM TestSuiteResult tsr WHERE tsr.appBuild.buildName = :buildName " +
			"AND tsr.testSuite.testCategory = :testCategory " +
			"AND tsr.testPhase = :testPhase")
})


public class TestSuiteResult implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="TEST_SUITE_RESULT_ID")
	private int testSuiteRstId; 
	
	@Column(name="\"TEST_COUNT\"", nullable=false)
	private int testCount; 
	@Column(name="\"ERROR_COUNT\"", nullable=false)
	private int errorCount; 
	@Column(name="\"FAILURE_COUNT\"", nullable=false)
	private int failureCount; 
	@Column(name="\"EXECUTION_TIMESTAMP\"", nullable=false)
	private Timestamp exeTimestamp; 
	@Column(name="\"EXECUTION_SECONDS\"", nullable=true)
	private int executionSeconds; 
	@Column(name="\"PASS_RATE\"", nullable=false, updatable=false, insertable=false)
	private double passRate; // GENERATED ALWAYS FOR EACH ROW ON UPDATE
	@Column(name="\"TEST_PHASE\"", nullable=false, length=50)
	private String testPhase;
	
	//uni-directional many-to-one foreign key association to TestSuite
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name="TEST_SUITE_ID", referencedColumnName="TEST_SUITE_ID", nullable=false, insertable=true, updatable=true)
	private TestSuite testSuite;
	
	//uni-directional many-to-one foreign key association to TestSuite
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name="BUILD_ID", referencedColumnName="BUILD_ID", nullable=false, insertable=true, updatable=true)
	private AppBuild appBuild;


	//constructor
	public TestSuiteResult() {
		super();
	}

	public int getExecutionSeconds() {
		return executionSeconds;
	}

	public void setExecutionSeconds(int executionSeconds) {
		this.executionSeconds = executionSeconds;
	}

	public AppBuild getAppBuild() {
		return appBuild;
	}

	public void setAppBuild(AppBuild appBuild) {
		this.appBuild = appBuild;
	}

	public int getTestSuiteRstId() {
		return testSuiteRstId;
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



	public Timestamp getExeTimestamp() {
		return exeTimestamp;
	}



	public TestSuite getTestSuite() {
		return testSuite;
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



	public void setExeTimestamp(Timestamp exeTimestamp) {
		this.exeTimestamp = exeTimestamp;
	}



	public void setTestSuite(TestSuite testSuite) {
		this.testSuite = testSuite;
	}


	public String getTestPhase() {
		return testPhase;
	}

	public void setTestPhase(String testPhase) {
		this.testPhase = testPhase;
	}

	public double getPassRate() {
		return passRate;
	}

	@Override
	public String toString() {
		return "TestSuiteResult [testSuiteRstId=" + testSuiteRstId
				+ ", testCount=" + testCount + ", errorCount=" + errorCount
				+ ", failureCount=" + failureCount + ", exeTimestamp="
				+ exeTimestamp + ", executionSeconds=" + executionSeconds
				+ ", passRate=" + passRate + ", testPhase=" + testPhase
				+ ", testSuite=" + testSuite + ", appBuild=" + appBuild + "]";
	}
	
	public JSONObject toJSONObject() {
		JSONObject returnObj = new JSONObject(); 
		returnObj.put("Test Suite", this.testSuite.toJSONObject());
		returnObj.put("Build", this.appBuild.toJSONObject());
		returnObj.put("Test Phase", this.testPhase);
		returnObj.put("Test Count", this.testCount);
		returnObj.put("Error Count", this.errorCount);
		returnObj.put("Failure Count", this.failureCount);
		returnObj.put("Execution Timestamp", this.exeTimestamp.toString());
		returnObj.put("Execution Time(s)", this.executionSeconds);
		returnObj.put("Pass Rate", this.passRate);
		return returnObj;
	}
}

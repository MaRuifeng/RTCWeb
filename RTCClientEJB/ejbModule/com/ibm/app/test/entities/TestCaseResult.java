package com.ibm.app.test.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Persistence class for the TESTRST.TEST_CASE_RESULT table of the TESTDB. 
 * @author ruifengm
 * @since 2015-Dec-07
 */

@Entity
@Table(name="TEST_CASE_RESULT")

public class TestCaseResult implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="TEST_CASE_RESULT_ID")
	private int testCaseRstId;

	
	@Column(name="\"STATUS\"", nullable=false, length=20)
	private String status; 
	@Column(name="\"ERROR_TYPE\"", nullable=false, length=1000)
	private String errorType; 
	@Column(name="\"ERROR_MESSAGE\"", nullable=false, length=10000)
	private String errorMsg; 
	@Column(name="\"FAILURE_TYPE\"", nullable=false, length=1000)
	private String failureType; 
	@Column(name="\"FAILURE_MESSAGE\"", nullable=false, length=10000)
	private String failureMsg; 
	@Column(name="\"EXECUTION_SECONDS\"", nullable=true)
	private int executionSeconds; 
	
	//uni-directional many-to-one foreign key association to TestCase
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name="TEST_CASE_ID", referencedColumnName="TEST_CASE_ID", nullable=false, insertable=true, updatable=true)
	private TestCase testCase;
	
	//uni-directional many-to-one foreign key association to TestSuiteResult
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name="TEST_SUITE_RESULT_ID", referencedColumnName="TEST_SUITE_RESULT_ID", nullable=false, insertable=true, updatable=true)
	private TestSuiteResult testSuiteRst;
	
	//uni-directional many-to-one foreign key association to TestSuite
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name="BUILD_ID", referencedColumnName="BUILD_ID", nullable=false, insertable=true, updatable=true)
	private AppBuild appBuild;

	// constructor
	public TestCaseResult() {
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

	public int getTestCaseRstId() {
		return testCaseRstId;
	}

	public String getStatus() {
		return status;
	}

	public String getErrorType() {
		return errorType;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public String getFailureType() {
		return failureType;
	}

	public String getFailureMsg() {
		return failureMsg;
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public TestSuiteResult getTestSuiteRst() {
		return testSuiteRst;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public void setFailureType(String failureType) {
		this.failureType = failureType;
	}

	public void setFailureMsg(String failureMsg) {
		this.failureMsg = failureMsg;
	}

	public void setTestCase(TestCase testCase) {
		this.testCase = testCase;
	}

	public void setTestSuiteRst(TestSuiteResult testSuiteRst) {
		this.testSuiteRst = testSuiteRst;
	}

	@Override
	public String toString() {
		return "TestCaseResult [testCaseRstId=" + testCaseRstId + ", status="
				+ status + ", errorType=" + errorType + ", errorMsg="
				+ errorMsg + ", failureType=" + failureType + ", failureMsg="
				+ failureMsg + ", executionSeconds=" + executionSeconds
				+ ", testCase=" + testCase.toString() + ", testSuiteRst="
				+ testSuiteRst.toString() + ", appBuild=" + appBuild.toString() 
				+ "]";
	}
}

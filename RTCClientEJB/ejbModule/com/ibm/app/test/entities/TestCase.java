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


/**
 * Persistence class for the TESTRST.TEST_CASE table of the TESTDB. 
 * @author ruifengm
 * @since 2015-Dec-07
 */

@Entity
@Table(name="TEST_CASE")
@NamedQueries({
	@NamedQuery(name="getTestCaseByNameAndPath", query="SELECT tc from TestCase tc WHERE tc.caseName = :caseName AND tc.casePath = :casePath")
})
public class TestCase implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="TEST_CASE_ID")
	private int testCaseId; 
	
	@Column(name="\"TEST_CASE_NAME\"", nullable=false, length=100)
	private String caseName; 
	@Column(name="\"TEST_CASE_PATH\"", nullable=false, length=500)
	private String casePath; 
	@Column(name="\"RQM_TEST_CASE_ID\"")
	private int rqmTestCaseId; 

	//uni-directional many-to-one foreign key association to TestSuite
	@ManyToOne(targetEntity=TestSuite.class,cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name="TEST_SUITE_ID", referencedColumnName="TEST_SUITE_ID", nullable=false, insertable=true, updatable=true)
	private TestSuite testSuite;

	// constructor
	public TestCase() {
		super();
	}

	public int getRqmTestCaseId() {
		return rqmTestCaseId;
	}

	public void setRqmTestCaseId(int rqmTestCaseId) {
		this.rqmTestCaseId = rqmTestCaseId;
	}
	
	public int getTestCaseId() {
		return testCaseId;
	}

	public String getCaseName() {
		return caseName;
	}

	public String getCasePath() {
		return casePath;
	}

	public TestSuite getTestSuite() {
		return testSuite;
	}

	public void setTestCaseId(int testCaseId) {
		this.testCaseId = testCaseId;
	}

	public void setCaseName(String caseName) {
		this.caseName = caseName;
	}

	public void setCasePath(String casePath) {
		this.casePath = casePath;
	}

	public void setTestSuite(TestSuite testSuite) {
		this.testSuite = testSuite;
	}

	@Override
	public String toString() {
		return "TestCase [testCaseId=" + testCaseId + ", caseName=" + caseName
				+ ", casePath=" + casePath + ", rqmTestCaseId=" + rqmTestCaseId
				+ ", testSuite=" + testSuite.toString() + "]";
	} 
}

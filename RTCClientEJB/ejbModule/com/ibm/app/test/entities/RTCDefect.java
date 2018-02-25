package com.ibm.app.test.entities;

import java.io.Serializable;
import java.sql.Timestamp;

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
 * Persistence class for the TESTRST.RTC_DEFECT table of the TESTDB. 
 * @author ruifengm
 * @since 2015-Dec-07
 */

@Entity
@Table(name="RTC_DEFECT")

@NamedQueries({
	@NamedQuery(name="getRtcDefectByNumber", query="SELECT rd FROM RTCDefect rd WHERE rd.defectNum = :defectNum"),
	@NamedQuery(name="getAllDefects", query="SELECT rd FROM RTCDefect rd"),
	@NamedQuery(name="getOpenDefectByTestSuiteName", query="SELECT rd FROM RTCDefect rd WHERE rd.testSuite.suiteName = :testSuiteName AND rd.defectStatus NOT IN :closedStatusList"),
	@NamedQuery(name="getLatestDefectsForAllTestSuites", query="SELECT rd FROM RTCDefect rd WHERE rd.filingTimestamp = (SELECT MAX(rd1.filingTimestamp) FROM RTCDefect rd1 WHERE rd1.testSuite.testSuiteId = rd.testSuite.testSuiteId)"), 
	@NamedQuery(name="getLatestDefectByTestSuiteNameAndBuild", query="SELECT rd FROM RTCDefect rd WHERE " +
			"rd.testSuite.suiteName = :testSuiteName AND " +
			"rd.appBuild.buildName = :buildName AND " +
			"rd.filingTimestamp = (SELECT MAX(rd1.filingTimestamp) FROM RTCDefect rd1 WHERE rd1.testSuite.testSuiteId = rd.testSuite.testSuiteId)")
})

public class RTCDefect implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="DEFECT_ID")
	private int defectId;
	
	@Column(name="\"DEFECT_NAME\"", nullable=false, length=500)
	private String defectName; 
	@Column(name="\"DEFECT_NUMBER\"", nullable=false)
	private int defectNum; 
	@Column(name="\"DEFECT_STATUS\"", nullable=false, length=500)
	private String defectStatus; 
	@Column(name="\"FILING_TIMESTAMP\"", nullable=false)
	private Timestamp filingTimestamp; 
	@Column(name="\"DEFECT_LINK\"", nullable=false, length=500)
	private String defectLink; 
	
	//@GeneratedValue(strategy=GenerationType.AUTO)  // GENERATED ALWAYS FOR EACH ROW ON UPDATE AS ROW CHANGE TIMESTAMP
	@Column(name="\"MODIFICATION_TIMESTAMP\"", nullable=false, updatable=false, insertable=false)
	private Timestamp modifiTimestamp; 
	

	@Column(name="\"MODIFICATION_TIMES\"", nullable=false)
	private int modifiTimes; 
	
	//uni-directional many-to-one foreign key association to TestSuite
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name="TEST_SUITE_ID", referencedColumnName="TEST_SUITE_ID", nullable=false, insertable=true, updatable=true)
	private TestSuite testSuite;
	
	//uni-directional many-to-one foreign key association to TestSuite
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name="BUILD_ID", referencedColumnName="BUILD_ID", nullable=false, insertable=true, updatable=true)
	private AppBuild appBuild;

	public AppBuild getAppBuild() {
		return appBuild;
	}

	public void setAppBuild(AppBuild appBuild) {
		this.appBuild = appBuild;
	}

	//constructor
	public RTCDefect() {
		super();
	}

	public int getDefectId() {
		return defectId;
	}

	public String getDefectName() {
		return defectName;
	}

	public String getDefectStatus() {
		return defectStatus;
	}

	public Timestamp getFilingTimestamp() {
		return filingTimestamp;
	}

	public Timestamp getModifiTimestamp() {
		return modifiTimestamp;
	}

	public int getModifiTimes() {
		return modifiTimes;
	}

	public TestSuite getTestSuite() {
		return testSuite;
	}

	public void setDefectName(String defectName) {
		this.defectName = defectName;
	}

	public void setDefectStatus(String defectStatus) {
		this.defectStatus = defectStatus;
	}

	public void setFilingTimestamp(Timestamp filingTimestamp) {
		this.filingTimestamp = filingTimestamp;
	}

	public void setModifiTimes(int modifiTimes) {
		this.modifiTimes = modifiTimes;
	}

	public void setTestSuite(TestSuite testSuite) {
		this.testSuite = testSuite;
	}

	public int getDefectNum() {
		return defectNum;
	}

	public void setDefectNum(int defectNum) {
		this.defectNum = defectNum;
	}
	
	public String getDefectLink() {
		return defectLink;
	}

	public void setDefectLink(String defectLink) {
		this.defectLink = defectLink;
	}
	
	@Override
	public String toString() {
		return "RTCDefect [defectId=" + defectId + ", defectName=" + defectName
				+ ", defectNum=" + defectNum + ", defectStatus=" + defectStatus
				+ ", filingTimestamp=" + filingTimestamp + ", defectLink="
				+ defectLink + ", modifiTimestamp=" + modifiTimestamp
				+ ", modifiTimes=" + modifiTimes + ", testSuite=" + testSuite.toString()
				+ ", appBuild=" + appBuild.toString()
				+ "]";
	}

	public JSONObject toJSONObject() {
		JSONObject returnObj = new JSONObject(); 
		returnObj.put("Defect Number", defectNum);
		returnObj.put("Defect Summary", defectName);
		returnObj.put("Defect Status", defectStatus);
		returnObj.put("Defect Link", defectLink);
		returnObj.put("Defect Filed Timestamp", filingTimestamp.toString());
		returnObj.put("Defect Modified Timestamp", modifiTimestamp.toString());
		returnObj.put("Defect Modification Times", modifiTimes);
		returnObj.put("Test Suite", testSuite.getSuiteName());
		returnObj.put("App Build", appBuild.toJSONObject());
		
		return returnObj;
	}
	
}

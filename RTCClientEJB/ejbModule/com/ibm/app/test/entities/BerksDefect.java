package com.ibm.app.test.entities;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Column;

import com.ibm.json.java.JSONObject;



/**
 * Persistence class for the JENKINS.BERKS_DEFECT table of the TESTDB. 
 * Defects are created in RTC.
 * @author ruifengm
 * @since 2017-Mar-17
 */

@Entity
@Table(name="BERKS_DEFECT")

@NamedQueries({
	@NamedQuery(name="getBerksDefectByNumber", query="SELECT bd FROM BerksDefect bd WHERE bd.defectNum = :defectNum"),
	@NamedQuery(name="getAllBerksDefects", query="SELECT bd FROM BerksDefect bd"),
	@NamedQuery(name="getOpenDefectByCookbook", query="SELECT bd FROM BerksDefect bd WHERE bd.cookbook = :cookbook AND bd.defectStatus NOT IN :closedStatusList")
})

public class BerksDefect implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="DEFECT_ID")
	private int defectId;
	
	@Column(name="\"DEFECT_NAME\"", nullable=false, length=500)
	private String defectName; 
	@Column(name="\"DEFECT_NUMBER\"", nullable=false)
	private int defectNum;
	@Column(name="\"DEFECT_STATUS\"", nullable=false, length=10)
	private String defectStatus; 
	@Column(name="\"FILING_TIMESTAMP\"", nullable=false)
	private Timestamp filingTimestamp; 
	@Column(name="\"DEFECT_LINK\"", nullable=false, length=250)
	private String defectLink; 
	@Column(name="\"COOKBOOK\"", nullable=false, length=100)
	private String cookbook; 
	@Column(name="\"GIT_BRANCH\"", nullable=false, length=100)
	private String gitBranch;
	@Column(name="\"GIT_PR_LINK\"", nullable=true, length=800)
	private String gitPRLink; 
	
	//@GeneratedValue(strategy=GenerationType.AUTO)  // GENERATED ALWAYS FOR EACH ROW ON UPDATE AS ROW CHANGE TIMESTAMP
	@Column(name="\"MODIFICATION_TIMESTAMP\"", nullable=false, updatable=false, insertable=false)
	private Timestamp modifiTimestamp; 

	@Column(name="\"MODIFICATION_TIMES\"", nullable=false)
	private int modifiTimes; 

	public BerksDefect() {
		super();
	}
	
	
	public int getDefectId() {
		return defectId;
	}
	
	public String getDefectName() {
		return defectName;
	}



	public void setDefectName(String defectName) {
		this.defectName = defectName;
	}



	public int getDefectNum() {
		return defectNum;
	}



	public void setDefectNum(int defectNum) {
		this.defectNum = defectNum;
	}



	public String getDefectStatus() {
		return defectStatus;
	}



	public void setDefectStatus(String defectStatus) {
		this.defectStatus = defectStatus;
	}



	public Timestamp getFilingTimestamp() {
		return filingTimestamp;
	}



	public void setFilingTimestamp(Timestamp filingTimestamp) {
		this.filingTimestamp = filingTimestamp;
	}



	public String getDefectLink() {
		return defectLink;
	}



	public void setDefectLink(String defectLink) {
		this.defectLink = defectLink;
	}

	public String getCookbook() {
		return cookbook;
	}


	public void setCookbook(String cookbook) {
		this.cookbook = cookbook;
	}


	public String getGitBranch() {
		return gitBranch;
	}


	public void setGitBranch(String gitBranch) {
		this.gitBranch = gitBranch;
	}


	public String getGitPRLink() {
		return gitPRLink;
	}


	public void setGitPRLink(String gitPRLink) {
		this.gitPRLink = gitPRLink;
	}


	public Timestamp getModifiTimestamp() {
		return modifiTimestamp;
	}



	public void setModifiTimestamp(Timestamp modifiTimestamp) {
		this.modifiTimestamp = modifiTimestamp;
	}



	public int getModifiTimes() {
		return modifiTimes;
	}



	public void setModifiTimes(int modifiTimes) {
		this.modifiTimes = modifiTimes;
	}



	@Override
	public String toString() {
		return "RTCDefect [defectName=" + defectName
				+ ", defectNum=" + defectNum + ", defectStatus=" + defectStatus
				+ ", filingTimestamp=" + filingTimestamp + ", defectLink="
				+ defectLink + ", modifiTimestamp=" + modifiTimestamp
				+ ", modifiTimes=" + modifiTimes + ", cookbook=" + cookbook
				+ ", gitBranch=" + gitBranch
				+ ", gitPRLink=" + gitPRLink
				+ "]";
	}

	public JSONObject toJSONObject() {
		JSONObject returnObj = new JSONObject(); 
		returnObj.put("DefectNumber", defectNum);
		returnObj.put("DefectSummary", defectName);
		returnObj.put("DefectStatus", defectStatus);
		returnObj.put("DefectLink", defectLink);
		returnObj.put("DefectFiled Timestamp", filingTimestamp.toString());
		returnObj.put("DefectModified Timestamp", modifiTimestamp.toString());
		returnObj.put("DefectModification Times", modifiTimes);
		returnObj.put("Cookbook", cookbook);
		returnObj.put("GitBranch", gitBranch);
		returnObj.put("GitPRLink", gitPRLink);
		
		return returnObj;
	}
	
}

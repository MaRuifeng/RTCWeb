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
 * Persistence class for the JENKINS.BRAKEMAN_DEFECT table of the TESTDB. 
 * Defects are created in RTC.
 * @author ruifengm
 * @since 2016-Nov-17
 */

@Entity
@Table(name="BRAKEMAN_DEFECT")

@NamedQueries({
	@NamedQuery(name="getBrakemanDefectByNumber", query="SELECT bd FROM BrakemanDefect bd WHERE bd.defectNum = :defectNum"),
	@NamedQuery(name="getAllBrakemanDefects", query="SELECT bd FROM BrakemanDefect bd"),
	@NamedQuery(name="getOpenDefectByProject", query="SELECT bd FROM BrakemanDefect bd WHERE bd.project = :project AND bd.defectStatus NOT IN :closedStatusList")
})

public class BrakemanDefect implements Serializable{
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
	@Column(name="\"PROJECT\"", nullable=false, length=100)
	private String project; 
	@Column(name="\"BUILD\"", nullable=false, length=250)
	private String build; 
	
	//@GeneratedValue(strategy=GenerationType.AUTO)  // GENERATED ALWAYS FOR EACH ROW ON UPDATE AS ROW CHANGE TIMESTAMP
	@Column(name="\"MODIFICATION_TIMESTAMP\"", nullable=false, updatable=false, insertable=false)
	private Timestamp modifiTimestamp; 

	@Column(name="\"MODIFICATION_TIMES\"", nullable=false)
	private int modifiTimes; 

	public BrakemanDefect() {
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



	public String getProject() {
		return project;
	}



	public void setProject(String project) {
		this.project = project;
	}



	public String getBuild() {
		return build;
	}



	public void setBuild(String build) {
		this.build = build;
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
				+ ", modifiTimes=" + modifiTimes + ", project=" + project
				+ ", build=" + build
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
		returnObj.put("Build", build);
		returnObj.put("Project", project);
		
		return returnObj;
	}
	
}

package com.ibm.app.test.entities;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Column;

import com.ibm.json.java.JSONObject;



/**
 * Persistence class for the JENKINS.BRAKEMAN_WARNING table of the TESTDB. 
 * @author ruifengm
 * @since 2016-Nov-17
 */

@Entity
@Table(name="BRAKEMAN_WARNING")

public class BrakemanWarning implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="WARNING_ID")
	private int warningID;
	
	@Column(name="\"WARNING_TYPE\"", nullable=false, length=250)
	private String warningType; 
	@Column(name="\"FILE_NAME\"", nullable=false, length=250)
	private String fileName; 
	@Column(name="\"MESSAGE\"", nullable=false, length=5000)
	private String message; 
	@Column(name="\"CONFIDENCE\"", nullable=false, length=50)
	private String confidence; 
	@Column(name="\"BUILD\"", nullable=false, length=250)
	private String build; 

	//uni-directional many-to-one foreign key association to BrakemanDefect
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name="DEFECT_ID", referencedColumnName="DEFECT_ID", nullable=false, insertable=true, updatable=true)
	private BrakemanDefect brakemanDefect;
	
	
	public BrakemanWarning() {
		super();
	}

	
	public String getWarningType() {
		return warningType;
	}


	public void setWarningType(String warningType) {
		this.warningType = warningType;
	}


	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public String getConfidence() {
		return confidence;
	}


	public void setConfidence(String confidence) {
		this.confidence = confidence;
	}


	public String getBuild() {
		return build;
	}


	public void setBuild(String build) {
		this.build = build;
	}


	public BrakemanDefect getBrakemanDefect() {
		return brakemanDefect;
	}


	public void setBrakemanDefect(BrakemanDefect brakemanDefect) {
		this.brakemanDefect = brakemanDefect;
	}

	public JSONObject toJSONObject() {
		JSONObject returnObj = new JSONObject(); 
		returnObj.put("Warning Type", warningType);
		returnObj.put("File Name", fileName);
		returnObj.put("Message", message);
		returnObj.put("Confidence", confidence);
		returnObj.put("Build", build);
		returnObj.put("Defect", brakemanDefect.toJSONObject());
		return returnObj;
	}
	
}

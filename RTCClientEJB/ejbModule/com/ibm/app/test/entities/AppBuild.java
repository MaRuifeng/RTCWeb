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
 * Persistence class for the TESTRST.APP_BUILD table of the TESTDB. 
 * @author ruifengm
 * @since 2016-Jul-14
 */

@Entity
@Table(name="APP_BUILD")

@NamedQueries({
	@NamedQuery(name="getAppBuildByName", query="SELECT ab FROM AppBuild ab WHERE ab.buildName = :buildName")
})

public class AppBuild implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="BUILD_ID")
	private int buildId;
	
	@Column(name="\"BUILD_NAME\"", nullable=false, length=100)
	private String buildName; 
	@Column(name="\"BUILD_VERSION\"", nullable=false)
	private int buildVersion; 
	@Column(name="\"BUILD_TIMESTAMP\"", nullable=false)
	private Timestamp buildTimestamp; 
	@Column(name="\"GIT_BRANCH\"", nullable=false, length=100)
	private String gitBranch; 
	@Column(name="\"SPRINT\"", nullable=false, length=100)
	private String sprint; 
	
	//constructor
	public AppBuild() {
		super();
	}

	public int getBuildId() {
		return buildId;
	}
	
	public String getBuildName() {
		return buildName;
	}

	public int getBuildVersion() {
		return buildVersion;
	}

	public Timestamp getBuildTimestamp() {
		return buildTimestamp;
	}

	public String getGitBranch() {
		return gitBranch;
	}

	public String getSprint() {
		return sprint;
	}

	public void setBuildName(String buildName) {
		this.buildName = buildName;
	}

	public void setBuildVersion(int buildVersion) {
		this.buildVersion = buildVersion;
	}

	public void setBuildTimestamp(Timestamp buildTimestamp) {
		this.buildTimestamp = buildTimestamp;
	}

	public void setGitBranch(String gitBranch) {
		this.gitBranch = gitBranch;
	}

	public void setSprint(String sprint) {
		this.sprint = sprint;
	}

	@Override
	public String toString() {
		return "AppBuild [buildId=" + buildId + ", buildName=" + buildName
				+ ", buildVersion=" + buildVersion + ", buildTimestamp=" + buildTimestamp
				+ ", gitBranch=" + gitBranch + ", sprint=" + sprint
				+ "]";
	}

	public JSONObject toJSONObject() {
		JSONObject returnObj = new JSONObject(); 
		returnObj.put("Build Name", buildName);
		returnObj.put("Build Version", buildVersion);
		returnObj.put("Build Timestamp", buildTimestamp.toString());
		returnObj.put("Git Branch", gitBranch);
		returnObj.put("Sprint", sprint);
		return returnObj;
	}
	
}

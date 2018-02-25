package com.ibm.app.test.utils.rtc;

import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler.ILoginInfo;

/**
 * Customized RTC login handler
 * @author ruifengm
 * @since 2015-Nov-27
 */

public class RTCLoginHandler implements ILoginHandler, ILoginInfo {
	
	private String userId; 
	private String password; 
	
	// constructor
	public RTCLoginHandler(String userId, String password) {
		super();
		this.userId = userId;
		this.password = password;
	}

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		return password;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public ILoginInfo challenge(ITeamRepository repository) {
		return this;
	}

}

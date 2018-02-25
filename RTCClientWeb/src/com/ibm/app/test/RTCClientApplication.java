package com.ibm.app.test;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.ibm.app.test.resources.AppTestResultsHandler;
import com.ibm.app.test.resources.BerksDefectHandler;
import com.ibm.app.test.resources.BrakemanDefectHandler;
import com.ibm.app.test.resources.BrakemanResultsHandler;
import com.ibm.app.test.resources.RTCTestAutoDefectHandler;

/**
 * This class contains REST services provided by the RTC client resources 
 * @author ruifengm
 * @since 2015-Dec-15
 */

public class RTCClientApplication extends Application {
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>(); 
		// register resource classes
		classes.add(AppTestResultsHandler.class);
		classes.add(RTCTestAutoDefectHandler.class);
		classes.add(BrakemanDefectHandler.class);
		classes.add(BrakemanResultsHandler.class);
		classes.add(BerksDefectHandler.class);
		return classes; 
	}

}

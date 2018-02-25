package com.ibm.app.test.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A customized time stamp parser that specializes in parsing time stamp info in below string format 
 *    "yyyy-MM-dd'T'HH:mm:ss"   e.g. "2015-11-19T12:41:52"
 * @author ruifengm
 * @since 2015-Dec-08
 */
public class TimeStampParser {
	
	/** 
	 * Get SQL timestamp from a given string in "yyyy-MM-dd'T'HH:mm:ss" format
	 * @param timestampStr
	 * @return SQL timestamp
	 * @throws ParseException
	 */
	public static Timestamp getSQLTimestamp(String timestampStr) throws ParseException {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date date = dateFormatter.parse(timestampStr);
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(date);
		return new Timestamp(cal.getTimeInMillis());
	}
}

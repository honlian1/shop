package com.xxdhy.util;


import java.text.ParseException;
import java.util.Date;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *     时间类型的转换
 *     
	  //jodat->time
	  //str->Date
	  //Date->str
 * @author User
 *
 */

public class DateTimeUtil {
      
	public static final String STANDARD_FORMAT="yyyy-MM-dd HH:mm:ss";
	  //jodat->time
	
	 //str->Date
	  /**
	   *    
	   * @param dateTimeStr
	   * @param formatStr
	   * @return
	   * @throws ParseException
	   *    
	   *   String--->Date
	   *   混乱了我？？？？（方法暂定）
	   */
	public static Date strToDate(String dateTimeStr,String formatStr) throws ParseException {
		DateTimeFormatter dateTimeFormatter= DateTimeFormat.forPattern(formatStr);
	     DateTime dateTime=dateTimeFormatter.parseDateTime(dateTimeStr);

	     return dateTime.toDate();
	}
	public static Date strToDate(String dateTimeStr) throws ParseException {
		DateTimeFormatter dateTimeFormatter= DateTimeFormat.forPattern(STANDARD_FORMAT);
		DateTime dateTime=dateTimeFormatter.parseDateTime(dateTimeStr);

		return dateTime.toDate();
	}

	
	public static String dateToStr(Date date,String formatStr) {
		if(date==null) {
			return StringUtils.EMPTY;
		}
		DateTime dateTime=new DateTime(date);
		return dateTime.toString(formatStr);
	}
	public static String dateToStr(Date date) {
		if(date==null) {
			return StringUtils.EMPTY;
		}
		DateTime dateTime=new DateTime(date);
		return dateTime.toString(STANDARD_FORMAT);
	}
	

	public static void main(String[] args) throws ParseException {
		System.out.println(DateTimeUtil.dateToStr(new Date(),DateTimeUtil.STANDARD_FORMAT));
		System.out.println(DateTimeUtil.strToDate("2011-11-02 11:11:11",DateTimeUtil.STANDARD_FORMAT));

	}
}

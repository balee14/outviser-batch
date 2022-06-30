package com.enliple.outviserbatch.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.time.FastDateFormat;

public class DateUtils {
    
    public static String getDate(java.util.Date date, String format) {
        if (date==null || format == null)
                return "";

        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        return sdf.format(date);
    }
    
    /**
     * 해당 1일을 구하는 함수. 예] 20210901
     * @return
     */
    public static String getCurrentMonth01() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());       
        int field = calendar.getActualMinimum(Calendar.DATE);
        return getDate(new Date(), "yyyyMM")+(field < 10? "0"+field:String.valueOf(field));
    }
    
    /**
     * 해당 마지막 날을 구하는 함수. 예] 20210930
     * @return
     */
    public static String getCurrentMonth31() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());       
        return getDate(new Date(), "yyyyMM")+calendar.getActualMaximum(Calendar.DATE);
    }
    
    /**
     * format에 따라 결과 반환 
     * 예] "yyyyMMddHHmmss" = 20210902110351, "yyyy-MM-dd HH:mm:ss" = 2021-09-02 11:05:23
     * @param format
     * @return
     */
    public static String getCurrentDate(String format) {
        return getDate(new Date(), format);
    }
    
    public static String getCurrentDate() {
        return getDate(new Date(), "yyyyMMdd");
    }
    
    public static String getCurrentDateFormFolder() {
        return getDate(new Date(), "yyyy/MM/dd");
    }
    
    public static String getDate(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, day);
        return getDate(calendar.getTime(), "yyyyMMdd");
    }
    
    public static String getDate(int day, String format) {
        Calendar calendar = Calendar.getInstance();
        
        calendar.add(Calendar.DATE, day);
        return getDate(calendar.getTime(), format);
    }
    
    /**
	 * yyyy-MM-dd ?��?��?�� ?��?��?��?���? ?��?�� ?��?��?�� �??�� ?��?�� 감산?��?��
	 * ?��) DateUtil.addDate("2010-01-03", Calendar.YEAR, 1);
	 */
	
    public static String addDate(String strDate, int amount) {
    	
    	return addDate(strDate, Calendar.DAY_OF_MONTH, amount, "yyyy-MM-dd");
    }
    
    public static String addDate(String strDate, int field, int amount) {
		return addDate(strDate, field, amount, "yyyy-MM-dd");
	}
	
    public static String addMonth(String strDate, int amount) {
    	
    	return addMonth(strDate, Calendar.DAY_OF_MONTH, amount, "yyyy-MM");
    }
    
    /**
	 * ?��?��?��?���? ?��?�� ?��?��?�� �?�? 처리
	 */
	public static String addMonth(String strDate, int field, int amount, String pattern) {
    	
    	Date date = convertStringToDate(strDate, pattern);
    	if (date == null)
    		return "";
    	
    	org.apache.commons.lang.time.DateUtils.addMonths(date, amount);
        
    	Calendar calendar = Calendar.getInstance();
        calendar.setTime(org.apache.commons.lang.time.DateUtils.addMonths(date, amount));
    	
        return FastDateFormat.getInstance(pattern).format(calendar);
    }
	
	/**
	 * ?��?��?��?���? ?��?�� ?��?��?�� �?�? 처리
	 */
	public static String addDate(String strDate, int field, int amount, String pattern) {
    	
    	Date date = convertStringToDate(strDate, pattern);
    	if (date == null)
    		return "";
    	
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(field, amount);
        
        return FastDateFormat.getInstance(pattern).format(calendar);
    }
	
	/**
     * String???��?�� ?���?(strDate)�? pattern?�� 맞는 Date객체�? �??��
     */
    public static Date convertStringToDate(String strDate, String pattern) {
    	
		SimpleDateFormat 	df 	 = null;
		Date 				date = null;
		  
		df = new SimpleDateFormat(pattern);
		
		try {
			date = df.parse(strDate);
		} catch(ParseException pe) {
			return null;
		}
		
		return date;
    }
    
    /**
     * ?��?�� ?��?��
     * @return
     */
    public static int getWeekNum() {    	
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 문자열에 시간값을 밀리세컨드로 변환
     * @param strDate
     * @param pattern
     * @return
     */
	public static long convertStringToMillisecond(String strDate, String pattern) {

		long result = 0L;

		DateFormat dFormat = new SimpleDateFormat(pattern);
		try {
			result = dFormat.parse(strDate).getTime();
		} catch (Exception e) {}

		return result;
	}

}

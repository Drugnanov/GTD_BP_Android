package cz.slama.android.gtd.utils;


import cz.slama.android.gtd.exceptions.GtdException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by Drugnanov on 29.3.2015.
 */
public class Converter {

  private final static String DATE_TIME_FORMAT = "dd/MM/yyyy hh:mm aaa";

  //Loading from DB
  public static String getDateTimeStringFromMinutes(Long miliseconds) {
    if (miliseconds == null){
      return "";
    }
    String timeString = getDate(miliseconds);
    return timeString;
  }

  //Prepare date to DB save
  public static Long getMillisecondsFromString(String dateFormat) throws GtdException {
    if (Compare.isNullOrEmpty(dateFormat)){
      return null;
    }
    DateFormat df = new SimpleDateFormat(DATE_TIME_FORMAT);
    df.setTimeZone(TimeZone.getDefault());
    DateFormat sqlDf = new SimpleDateFormat(DATE_TIME_FORMAT);
    sqlDf.setTimeZone(TimeZone.getTimeZone("UTC"));
    GregorianCalendar gc = (GregorianCalendar) sqlDf.getCalendar();
    try {
      gc.setTime(df.parse(dateFormat));
    }
    catch (ParseException e) {
      throw new GtdException("Cannot parse string "+dateFormat + " to date", e);
    }
    return gc.getTimeInMillis();
  }

  public static Calendar getCalendarFromString(String dateTimeFormat) throws GtdException {
    if (Compare.isNullOrEmpty(dateTimeFormat)){
      return null;
    }
    DateFormat df = new SimpleDateFormat(DATE_TIME_FORMAT);
    df.setTimeZone(TimeZone.getDefault());
    GregorianCalendar gc = (GregorianCalendar) df.getCalendar();
    try {
      gc.setTime(df.parse(dateTimeFormat));
    }
    catch (ParseException e) {
      throw new GtdException("Cannot parse string "+dateTimeFormat + " to date", e);
    }
    return gc;
  }

  private static String getDate(Long milliSeconds) {
    if (milliSeconds == null){
      return "";
    }

    TimeZone timeZone = TimeZone.getDefault();

    SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT);
    formatter.setTimeZone(TimeZone.getDefault());
    GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    calendar.setTimeInMillis(milliSeconds);
    return formatter.format(calendar.getTime());
  }

  public static String getStringFromCalendar(Calendar calendar) {
    if (calendar == null){
      return "";
    }
    SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT);
    calendar.setTimeZone(TimeZone.getDefault());
    return formatter.format(calendar.getTime());
  }
}

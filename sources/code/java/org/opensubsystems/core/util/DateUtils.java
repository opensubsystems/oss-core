/*
 * Copyright (C) 2003 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
 * 
 * This file is part of OpenSubsystems.
 *
 * OpenSubsystems is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package org.opensubsystems.core.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.opensubsystems.core.error.OSSInvalidDataException;

/**
 * Collection of useful utilities to work with dates. The provided methods allow
 * - comparisons of various elements (e.g. just date or just time) of Date 
 *   instances
 * - parsing of dates and timestamps with safe handling of milliseconds
 * - date expressions such as now + 3h - 1m + 4d. 
 * 
 * @author bastafidli
 */
public final class DateUtils extends OSSObject
{
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * One second in milliseconds.
    */
   public static final long ONE_SECOND = 1000L;
   
   /**
    * One minute in milliseconds.
    */
   public static final long ONE_MINUTE = ONE_SECOND * 60L;
   
   /**
    * One hour in milliseconds.
    */
   public static final long ONE_HOUR = ONE_MINUTE * 60L;
   
   /**
    * One day in milliseconds.
    */
   public static final long ONE_DAY = ONE_HOUR * 24L;
   
   /**
    * Separator we used to separate time from the nanosecond portion of the 
    * timestamp when converted to string.
    */
   public static final char NANO_SEPARATOR = ':';
   
   /**
    * Constant for timing type
    */
   public static final int TIMING_NEVER = 0;

   /**
    * Constant for timing type
    */
   public static final int TIMING_MINUTES = 1;

   /**
    * Constant for timing type
    */
   public static final int TIMING_HOURS = 2;

   /**
    * Constant for timing type
    */
   public static final int TIMING_DAYS = 3;

   /**
    * Constant for timing type
    */
   public static final int TIMING_WEEKS = 4;

   /**
    * Constant for timing type
    */
   public static final int TIMING_MONTHS = 5;

   /**
    * Constant for timing type
    */
   public static final int TIMING_YEARS = 6;
   
   /**
    * Constant for timing type
    */
   public static final int TIMING_NONE = 7;

   /**
    * Constant for current date code used in date/time formulas 
    */
   public static final String CURRENT_DATE_CODE = "now";
   
   /**
    * Constant for dynamic date code used in date/time formulas
    */
   public static final char YEAR_CODE = 'y';
   
   /**
    * Constant for dynamic date code used in date/time formulas
    */
   public static final char MONTH_CODE = 'M';

   /**
    * Constant for dynamic date code used in date/time formulas
    */
   public static final char WEEK_CODE = 'w';

   /**
    * Constant for dynamic date code used in date/time formulas
    */
   public static final char DAY_CODE = 'd';

   /**
    * Constant for dynamic date code used in date/time formulas
    */
   public static final char HOUR_CODE = 'h';

   /**
    * Constant for dynamic date code used in date/time formulas
    */
   public static final char MINUTE_CODE = 'm';
   
   /**
    * Constant for dynamic date code used in date/time formulas
    */
   public static final char SECOND_CODE = 's';

   /**
    * constant for date type DATE
    */
   public static final int DATE_TYPE_DATE = 1;

   /**
    * constant for date type TIME
    */
   public static final int DATE_TYPE_TIME = 2;

   /**
    * constant for date type DATETIME
    */
   public static final int DATE_TYPE_DATETIME = 3;
   
   // Constants for period start types /////////////////////////////////////////

// TODO: For Miro: Remove this code once all the code which referred to these
// constants was fixed
//   /**
//    * constant for period type
//    */
//   public static final int PERIOD_START_TYPE_NONE = 0;
//
//   /**
//    * constant for period type
//    */
//   public static final int PERIOD_START_TYPE_CREATION = 1;
//
//   /**
//    * constant for period type
//    */
//   public static final int PERIOD_START_TYPE_COMPLETION = 2;
//
//   /**
//    * constant for period type
//    */
//   public static final int PERIOD_START_TYPE_APPROVAL = 3;
//
//   /**
//    * constant for period type
//    */
//   public static final int PERIOD_START_TYPE_ACTIVATION = 4;
//
//   /**
//    * constant for period type
//    */
//   public static final int PERIOD_START_TYPE_INACTIVATION = 5;
//   
//   /**
//    * constant for period type
//    */
//   public static final int PERIOD_START_TYPE_DYNAMIC = 6;
//
//   /**
//    * constant for period type code
//    */
//   public static final int PERIOD_TYPE_CODE = 99;
//
//   /**
//    * constant for period type object
//    */
//   public static final Integer PERIOD_TYPE_OBJ = new Integer(PERIOD_TYPE_CODE);

   // Cached variables /////////////////////////////////////////////////////////
   
   /**
    * static SimpleDateFormat for date format to display on UI and in messages.
    */
   public static final SimpleDateFormat DATE_FORMAT 
                          = (SimpleDateFormat)DateFormat.getDateInstance(
                                                            DateFormat.SHORT);
   
   /**
    * static SimpleDateFormat for time format to display on UI and in messages.
    */
   public static final SimpleDateFormat TIME_FORMAT 
                          = (SimpleDateFormat)DateFormat.getTimeInstance(
                                                            DateFormat.MEDIUM);
   
   /**
    * static SimpleDateFormat for datetime format to display on UI and in messages.
    */
   public static final SimpleDateFormat DATETIME_FORMAT 
                          = (SimpleDateFormat)DateFormat.getDateTimeInstance(
                                                           DateFormat.SHORT, 
                                                           DateFormat.MEDIUM); 

   /**
    * static SimpleDateFormat for date format to store date as string so that
    * it is stored consistently.
    */
   public static final SimpleDateFormat DATE_STORE_FORMAT
                          = new SimpleDateFormat("MM/dd/yyyy");
   
   /**
    * static SimpleDateFormat for time format to store time as string so that
    * it is stored consistently.
    */
   public static final SimpleDateFormat TIME_STORE_FORMAT 
                          = new SimpleDateFormat("HH:mm:ss");
   
   /**
    * static SimpleDateFormat for datetime format to store date and time as 
    * string so that it is stored consistently.
    */
   public static final SimpleDateFormat DATETIME_STORE_FORMAT 
                          = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");


   /**
    * static SimpleDateFormat for date format for sql date
    */
   public static final SimpleDateFormat DATE_SQL_FORMAT
                          = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
   
   /**
    * static SimpleDateFormat for time format for sql time
    */
   public static final SimpleDateFormat TIME_SQL_FORMAT 
                          = new SimpleDateFormat("1970-01-01 HH:mm:ss");
   
   /**
    * static SimpleDateFormat for datetime format for sql date and time
    */
   public static final SimpleDateFormat DATETIME_SQL_FORMAT 
                          = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private DateUtils(
   )
   {
      // Do nothing
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Check if two dates equals regardless of the time. Two null dates are equal. 
    * Null and not null dates are not equal.
    * 
    * @param  dtFirst - first date to compare, can be null
    * @param  dtSecond - second date to compare, can be null 
    * @return boolean - true if two dates equals regardless of what the time is 
    */
   public static boolean dateEquals(
      Date dtFirst,
      Date dtSecond
   )
   {
      boolean  bReturn;
      
      // If they are the same object, they are equals
      bReturn = (dtFirst == dtSecond);
      if (!bReturn)
      {
         if (dtFirst == null)
         {
            // Two null dates are the same
            bReturn = (dtSecond == null);
         }
         else
         {
            if (dtSecond != null)
            {
               Calendar compCalendar;
               int      iEra;
               int      iYear;
               int      iMonth;
               int      iDay;
               
               compCalendar = Calendar.getInstance();
               compCalendar.setTime(dtFirst);
               iEra   = compCalendar.get(Calendar.ERA);
               iYear  = compCalendar.get(Calendar.YEAR);
               iMonth = compCalendar.get(Calendar.MONTH);
               iDay   = compCalendar.get(Calendar.DATE);
               compCalendar.setTime(dtSecond);
         
               bReturn = ((iEra == compCalendar.get(Calendar.ERA))
                         && (iYear == compCalendar.get(Calendar.YEAR))
                         && (iMonth == compCalendar.get(Calendar.MONTH))
                         && (iDay == compCalendar.get(Calendar.DATE)));
            }
         }
      } 
            
      return bReturn;            
   }
   
   /**
    * Check if two times equals regardless of the date. Two null times are equal. 
    * Null and not null times are not equal.
    * 
    * @param  dtFirst - first time to compare, can be null
    * @param  dtSecond - second time to compare, can be null
    * @param  bIgnoreMilliseconds - if true milliseconds will be ignored in comparison
    * @return boolean - true if two time equals regardless of what the date is 
    */
   public static boolean timeEquals(
      Date    dtFirst,
      Date    dtSecond,
      boolean bIgnoreMilliseconds
   )
   {
      boolean  bReturn;
      
      // If they are the same object, they are equals
      bReturn = (dtFirst == dtSecond);
      if (!bReturn)
      {
         if (dtFirst == null)
         {
            // Two null dates are the same
            bReturn = (dtSecond == null);
         }
         else
         {
            if (dtSecond != null)
            {
               Calendar compCalendar;
               int      iHour;
               int      iMinute;
               int      iSecond;
               int      iMili;
               
               compCalendar = Calendar.getInstance();
               compCalendar.setTime(dtFirst);
               iHour   = compCalendar.get(Calendar.HOUR_OF_DAY);
               iMinute = compCalendar.get(Calendar.MINUTE);
               iSecond = compCalendar.get(Calendar.SECOND);
               iMili   = compCalendar.get(Calendar.MILLISECOND);
               compCalendar.setTime(dtSecond);
         
               bReturn = ((iHour == compCalendar.get(Calendar.HOUR_OF_DAY))
                         && (iMinute == compCalendar.get(Calendar.MINUTE))
                         && (iSecond == compCalendar.get(Calendar.SECOND))
                         && ((bIgnoreMilliseconds) 
                            || (iMili == compCalendar.get(Calendar.MILLISECOND))));
            }
         }
      } 
            
      return bReturn;            
   }

   /**
    * Check if two dates and times are equal. Two null dates are equal. Null 
    * and not null dates are not equal.
    * 
    * @param  dtFirst - first date time to compare, can be null
    * @param  dtSecond - second date time to compare, can be null
    * @return boolean - true if two date and times are equal 
    */
   public static boolean dateAndTimeEquals(
      Date dtFirst,
      Date dtSecond
   )
   {
      boolean bReturn;
      
      // If they are the same object, they are equals
      bReturn = (dtFirst == dtSecond);
      if (!bReturn)
      {
         if (dtFirst == null)
         {
            // Two null dates are the same
            bReturn = (dtSecond == null);
         }
         else
         {
            if (dtSecond != null)
            {
               // They are both not null so they have to match to millisecond
               // (actually to nanosecond since the getTime takes nanoseconds
               // into account)
               bReturn = (dtFirst.getTime() == dtSecond.getTime());                               
            }
         }
      }
            
      return bReturn;            
   }

   /**
    * Check if String representing date is function or date. Date is a function
    * (formula) if it starts with the current date/time variable which can be 
    * followed by expression describing period from current date.
    *
    * @param strValue - string representation of date or date function
    * @return boolean - date function flag
    */
   public static boolean isFunction(
      String   strValue
   )
   {
      boolean bReturn = false;

      if ((strValue != null) && (strValue.length() > 0) 
         && (strValue.trim().startsWith(CURRENT_DATE_CODE)))
      {
         bReturn = true;
      }
      
      return bReturn;
   }
   
   /**
    * Parse date time value from given string resolving any functions or formulas
    * the string can contain. This method  can be therefore used if the passed 
    * string contains string representation date, time or timestamp or a formula
    * such as now + 3h - 1m + 4d. 
    *
    * @param strValue - string representation of date or date function
    * @param iDateType - date type code, one of the DATE_TYPE_XXX constants
    * @param stored - flag if Date should be parsed using format used for 
    *                 storage or for display
    * @return Timestamp - parsed date or null if date was null
    * @throws OSSInvalidDataException - error during parsing
    */
   public static Timestamp parseDateTime(
      String   strValue,
      int      iDateType,
      boolean  stored
   ) throws OSSInvalidDataException
   {
      Timestamp tsReturn = null;
      Calendar workCal = GregorianCalendar.getInstance();
      
      if (strValue != null && strValue.length() > 0)
      {
         strValue = strValue.trim();
         if (strValue.startsWith(CURRENT_DATE_CODE))
         {
            strValue = strValue.replaceAll("[ ]", "");

            // If the user specified "UseCurrent", then substitute the
            // current date/time in the value
            workCal.setTime(new Date());

//            Log.getInstance().debug("Parsing current date " + strValue);

            // Parse the date math
            int iBeginIndex = CURRENT_DATE_CODE.length();
            int iMaxLength = strValue.length();
            int iSign = 1;
            int iNumberIndex;
            int iValue;
            char cChar = ' ';

            while (iBeginIndex < iMaxLength)
            {
               // This has to be sign
               if (strValue.charAt(iBeginIndex) == '+')
               {
                  iSign = 1;
               }
               else if (strValue.charAt(iBeginIndex) == '-')
               {
                  iSign = -1;
               }
               else
               {
                  // Incorrect String
                  throw new OSSInvalidDataException(
                           "Date function is in incorrect format: "
                           + strValue + " at " + strValue.substring(iBeginIndex));
               }
               iBeginIndex++;

               // Now we have to have number
               iNumberIndex = iBeginIndex;
               
               while (((iBeginIndex == iNumberIndex) || Character.isDigit(cChar)) 
                     && (iBeginIndex < iMaxLength))
               {
                  cChar = strValue.charAt(iBeginIndex++);
               }

               // We have to go one back because we should stop on modifier (e.g 1m)
               iBeginIndex--;

               try
               {
                  iValue = Integer.parseInt(strValue.substring(iNumberIndex, iBeginIndex));
               }
               catch (NumberFormatException nmeExc)
               {
                  // Incorrect String
                  throw new OSSInvalidDataException(
                           "Date function is in incorrect format: "
                           + strValue + " at " + strValue.substring(iNumberIndex));
               }

               // This has to be modifier: y - year, M - month, w - week, 
               // d - day, h - hour, m - minute, s - second
               cChar = strValue.charAt(iBeginIndex);
               switch(cChar)
               {
                  case(YEAR_CODE):
                  {
                     if (iDateType == DATE_TYPE_TIME)
                     {
                        throw new OSSInvalidDataException(
                           "Date function is in incorrect format: " +
                           "used YEAR modifier for TIME type");
                     }
                     workCal.add(Calendar.YEAR, iSign * iValue);
                     break;
                  }
                  case(MONTH_CODE):
                  {
                     if (iDateType == DATE_TYPE_TIME)
                     {
                        throw new OSSInvalidDataException(
                           "Date function is in incorrect format: " +
                           "used MONTH modifier for TIME type");
                     }
                     workCal.add(Calendar.MONTH, iSign * iValue);
                     break;
                  }
                  case(WEEK_CODE):
                  {
                     if (iDateType == DATE_TYPE_TIME)
                     {
                        throw new OSSInvalidDataException(
                           "Date function is in incorrect format: " +
                           "used WEEK modifier for TIME type");
                     }
                     workCal.add(Calendar.WEEK_OF_YEAR, iSign * iValue);
                     break;
                  }
                  case(DAY_CODE):
                  {
                     if (iDateType == DATE_TYPE_TIME)
                     {
                        throw new OSSInvalidDataException(
                           "Date function is in incorrect format: " +
                           "used DAY modifier for TIME type");
                     }
                     workCal.add(Calendar.DATE, iSign * iValue);
                     break;
                  }
                  case(HOUR_CODE):
                  {
                     if (iDateType == DATE_TYPE_DATE)
                     {
                        throw new OSSInvalidDataException(
                           "Date function is in incorrect format: " +
                           "used HOUR modifier for DATE type");
                     }
                     workCal.add(Calendar.HOUR, iSign * iValue);
                     break;
                  }
                  case(MINUTE_CODE):
                  {
                     if (iDateType == DATE_TYPE_DATE)
                     {
                        throw new OSSInvalidDataException(
                           "Date function is in incorrect format: " +
                           "used MINUTE modifier for DATE type");
                     }
                     workCal.add(Calendar.MINUTE, iSign * iValue);
                     break;
                  }
                  case(SECOND_CODE):
                  {
                     if (iDateType == DATE_TYPE_DATE)
                     {
                        throw new OSSInvalidDataException(
                           "Date function is in incorrect format: " +
                           "used SECOND modifier for DATE type");
                     }
                     workCal.add(Calendar.SECOND, iSign * iValue);
                     break;
                  }
                  default:
                  {
                     // Incorrect String
                     throw new OSSInvalidDataException(
                           "Date function is in incorrect format: "
                           + strValue + " at " + strValue.substring(iBeginIndex));
                  }
               }

               iBeginIndex++;
            }
            
            tsReturn = new Timestamp(workCal.getTimeInMillis());
            
         }
         else
         {
            try
            {
               if (stored)
               {
                  switch (iDateType)
                  {
                     case (DATE_TYPE_DATE) :
                     {
                        tsReturn = new Timestamp(DATE_STORE_FORMAT.parse(strValue).getTime());
                        break;   
                     }
                     case (DATE_TYPE_TIME) :
                     {
                        tsReturn = new Timestamp(TIME_STORE_FORMAT.parse(strValue).getTime());
                        break;   
                     }
                     case (DATE_TYPE_DATETIME) :
                     {
                        tsReturn = new Timestamp(DATETIME_STORE_FORMAT.parse(strValue).getTime());
                        break;   
                     }
                     default:
                     {
                        assert false : "Unknown date type " + iDateType;
                     }
                  }                  
               }
               else
               {
                  switch (iDateType)
                  {
                     case (DATE_TYPE_DATE) :
                     {
                        tsReturn = new Timestamp(DATE_FORMAT.parse(strValue).getTime());
                        break;   
                     }
                     case (DATE_TYPE_TIME) :
                     {
                        tsReturn = new Timestamp(TIME_FORMAT.parse(strValue).getTime());
                        break;   
                     }
                     case (DATE_TYPE_DATETIME) :
                     {
                        tsReturn = new Timestamp(DATETIME_FORMAT.parse(strValue).getTime());
                        break;   
                     }
                     default:
                     {
                        assert false : "Unknown date type " + iDateType;
                     }                  
                  }                  
               }
            }
            catch (ParseException peExc)
            {
               throw new OSSInvalidDataException(
                     "Date is in incorrect format. Problems with parsing.",
                     peExc);
            }
         }
      }

      return tsReturn;
   }
   
   /**
    * Parse the specified period into string displaying number of days the 
    * period represents. 
    * 
    * @param lPeriod - period in milliseconds
    * @return String - period in format 'x day(s)' or '' if not valid period
    */
   public static String parseDayPeriod(
      long lPeriod
   )
   {
      StringBuilder sbReturn = new StringBuilder();
      long lDays;
      
      if (lPeriod > 0)
      {
         // we will count each started day as counted day 
         lPeriod = lPeriod + DateUtils.ONE_DAY - 1;
         
         lDays = lPeriod / DateUtils.ONE_DAY;
         sbReturn.append(lDays);
         if (lDays == 1L)
         {
            sbReturn.append(" day");
         }
         else
         {
            sbReturn.append(" days");
         }
      }
      else
      {
         sbReturn.append("0 days");
      }

      return sbReturn.toString();
   }
   
   /**
    * Parse the specified period into string displaying date and time the 
    * period represents. 
    * 
    * @param lPeriod - preiod in milliseconds
    * @return String - period in format 'x day(s) y hour(s) z minute(s)' 
    *                  or '' if not valid period
    */
   public static String parseDayTimePeriod(
      long lPeriod
   )
   {
      StringBuilder sbReturn = new StringBuilder();
      long lHelp;
      
      
      if (lPeriod > 0)
      {
         lPeriod = lPeriod + DateUtils.ONE_MINUTE - 1;
         // we will count each started day as counted day 
         lHelp = lPeriod / DateUtils.ONE_DAY;
         if (lHelp > 0)
         {
            sbReturn.append(lHelp);
            if (lHelp == 1L)
            {
               sbReturn.append(" d ");
            }
            else
            {
               sbReturn.append(" d ");
            }
         }
         lPeriod = lPeriod % DateUtils.ONE_DAY;
         lHelp = lPeriod / DateUtils.ONE_HOUR;
         if (lHelp > 0 || sbReturn.length() > 0)
         {
            sbReturn.append(lHelp);
            if (lHelp == 1L)
            {
               sbReturn.append(" h ");
            }
            else
            {
               sbReturn.append(" h ");
            }
         }
         lPeriod = lPeriod % DateUtils.ONE_HOUR;
         lHelp = lPeriod / DateUtils.ONE_MINUTE;
         if (lHelp > 0 || sbReturn.length() > 0)
         {
            sbReturn.append(lHelp);
            if (lHelp == 1L)
            {
               sbReturn.append(" min");
            }
            else
            {
               sbReturn.append(" min");
            }
         }
      }
      else
      {
         sbReturn.append("0 min");
      }
      return sbReturn.toString();
   }
   
// TODO: For Miro: Remove this code once all the code which referred to these
// was fixed. These should be moved to a GUI related class.
//   /**
//    * Method for list of timing types.
//    * 
//    * @return List - list of timing types
//    */
//   public static List getTimingTypes(
//   )
//   { 
//      List lstTimingTypes = new ArrayList();
//      
//      lstTimingTypes.add(new SelectOption(Integer.toString(DateUtils.TIMING_MINUTES), 
//                                                           "Minute(s)"));
//      lstTimingTypes.add(new SelectOption(Integer.toString(DateUtils.TIMING_HOURS), "Hour(s)"));
//      lstTimingTypes.add(new SelectOption(Integer.toString(DateUtils.TIMING_DAYS), "Day(s)"));
//      lstTimingTypes.add(new SelectOption(Integer.toString(DateUtils.TIMING_WEEKS), "Week(s)"));
//      lstTimingTypes.add(new SelectOption(Integer.toString(DateUtils.TIMING_MONTHS), "Month(s)"));
//      lstTimingTypes.add(new SelectOption(Integer.toString(DateUtils.TIMING_YEARS), "Year(s)"));
//      lstTimingTypes.add(new SelectOption(Integer.toString(DateUtils.TIMING_NEVER), "Never"));
//      
//      return lstTimingTypes;
//   }

//   /**
//    * Method for list of timing types with None option.
//    * 
//    * @return List - list of timing types
//    */
//   public static List getTimingTypesWithNone(
//   )
//   { 
//      List lstTimingTypes = new ArrayList();
//      
//      lstTimingTypes.add(new SelectOption(Integer.toString(DateUtils.TIMING_NONE), "None"));
//      lstTimingTypes.add(new SelectOption(Integer.toString(DateUtils.TIMING_MINUTES), 
//                         "Minute(s)"));
//      lstTimingTypes.add(new SelectOption(Integer.toString(DateUtils.TIMING_HOURS), "Hour(s)"));
//      lstTimingTypes.add(new SelectOption(Integer.toString(DateUtils.TIMING_DAYS), "Day(s)"));
//      lstTimingTypes.add(new SelectOption(Integer.toString(DateUtils.TIMING_WEEKS), "Week(s)"));
//      lstTimingTypes.add(new SelectOption(Integer.toString(DateUtils.TIMING_MONTHS), "Month(s)"));
//      lstTimingTypes.add(new SelectOption(Integer.toString(DateUtils.TIMING_YEARS), "Year(s)"));
//      lstTimingTypes.add(new SelectOption(Integer.toString(DateUtils.TIMING_NEVER), "Never"));
//      
//      return lstTimingTypes;
//   }

//   /**
//    * Method for getting string name of the timing type.
//    * 
//    * @param iTimingType - timing type constant
//    * @return String - string name of timing type
//    */
//   public static String getTimingTypeName(
//      int iTimingType
//   )
//   {
//      String outTimingTypeName = "Never"; 
//      switch (iTimingType)      
//      {
//         case (DateUtils.TIMING_NEVER):
//         {
//            outTimingTypeName = "Never";
//            break;
//         }
//         case (DateUtils.TIMING_MINUTES):
//         {
//            outTimingTypeName = "Minute(s)";
//            break;
//         }
//         case (DateUtils.TIMING_HOURS):
//         {
//            outTimingTypeName = "Hour(s)";
//            break;
//         }
//         case (DateUtils.TIMING_DAYS):
//         {
//            outTimingTypeName = "Day(s)";
//            break;
//         }
//         case (DateUtils.TIMING_WEEKS):
//         {
//            outTimingTypeName = "Week(s)";
//            break;
//         }
//         case (DateUtils.TIMING_MONTHS):
//         {
//            outTimingTypeName = "Month(s)";
//            break;
//         }
//         case (DateUtils.TIMING_YEARS):
//         {
//            outTimingTypeName = "Year(s)";
//            break;
//         }
//         case (DateUtils.TIMING_NONE):
//         {
//            outTimingTypeName = "None";
//            break;
//         }
//      }
//
//      return outTimingTypeName;
//   }
   
   /**
    * Get expiration timestamp from start date, period type and duration. For 
    * example if the start date is now, period type is hour and duration is 2
    * then the result will be timestamp representing now + 2 hours.  
    * 
    * @param tsStartDate - start date of period counting
    * @param iPeriodType - one of the period type constant TIMING_XXX 
    * @param iPeriodDuration - period duration, number of time units specified 
    *                          by period type
    * @return Timestamp - date of period expiration or null if any problem
    */
   public static Timestamp getPeriodExpiration(
      Timestamp tsStartDate,
      int       iPeriodType,
      int       iPeriodDuration
   )
   {
      Timestamp tsReturn = null;
      Calendar calHelp;
      if (tsStartDate != null && iPeriodDuration > 0 
            && iPeriodType > TIMING_NEVER && iPeriodType < TIMING_NONE)
      {
         calHelp = Calendar.getInstance();
         calHelp.setTime(tsStartDate);
         
         switch (iPeriodType)
         {
            case (TIMING_MINUTES) :
            {
               calHelp.add(Calendar.MINUTE, iPeriodDuration);
               break;
            }
            case (TIMING_HOURS) :
            {
               calHelp.add(Calendar.HOUR, iPeriodDuration);
               break;
            }
            case (TIMING_DAYS) :
            {
               calHelp.add(Calendar.DATE, iPeriodDuration);
               break;
            }
            case (TIMING_WEEKS) :
            {
               calHelp.add(Calendar.WEEK_OF_YEAR, iPeriodDuration);
               break;
            }
            case (TIMING_MONTHS) :
            {
               calHelp.add(Calendar.MONTH, iPeriodDuration);
               break;
            }
            case (TIMING_YEARS) :
            {
               calHelp.add(Calendar.YEAR, iPeriodDuration);
               break;
            }
            default :
            {
               assert false : "Not supported Timing type " + iPeriodType;
            } 
         }
         tsReturn = new Timestamp(calHelp.getTimeInMillis());
      }
      
      return tsReturn;
   }
   
   /**
    * Method to compare time periods
    * 
    * @param iPeriodType1 - first period type, one of the period type constant 
    *                       TIMING_XXX
    * @param iPeriodDuration1 - first period duration
    * @param iPeriodType2 - second period type, one of the period type constant 
    *                       TIMING_XXX
    * @param iPeriodDuration2 - second period duration
    * @return int - 1 - first period is longer
    *               0 - periods are same
    *              -1 - first period is shorter
    */
   public static int comparePeriods(
      int iPeriodType1,
      int iPeriodDuration1,
      int iPeriodType2,
      int iPeriodDuration2
   )
   {
      int iReturn = 0;
      
      if ((iPeriodType1 != TIMING_NEVER) && (iPeriodType1 != TIMING_NONE) 
         && (iPeriodType2 != TIMING_NEVER) && (iPeriodType2 != TIMING_NONE))
      {
         Timestamp tsTimestamp1 = getPeriodExpiration(
               new Timestamp(0), iPeriodType1, iPeriodDuration1);
         Timestamp tsTimestamp2 = getPeriodExpiration(
               new Timestamp(0), iPeriodType2, iPeriodDuration2);
         
         // TODO: Improve: When would any of these be null?
         if ((tsTimestamp1 != null) && (tsTimestamp2 != null))
         {
            if (tsTimestamp1.after(tsTimestamp2))
            {
               iReturn = 1;
            }
            else if (tsTimestamp2.after(tsTimestamp1))
            {
               iReturn = -1;
            }
         }
      }
      else
      {
         if (iPeriodType1 != iPeriodType2)
         {
            if (iPeriodType1 == TIMING_NEVER)
            {
               iReturn = 1;
            }
            else if (iPeriodType1 == TIMING_NONE)
            {
               iReturn = -1;
            }
            else if (iPeriodType2 == TIMING_NEVER)
            {
               iReturn = -1;
            }
            else if (iPeriodType2 == TIMING_NONE)
            {
               iReturn = 1;
            }
         }
      }

      return iReturn;      
   }
   
   /**
    * Convert timestamp to string including it's nanosecond portion so that it 
    * can be safely stored in variable of web page.
    * 
    * @param tsTimestamp - timestamp to convert
    * @return String - text containing time and nanosecond portion of timestamp
    */
   public static String getTimestampAsString(
      Timestamp tsTimestamp
   )
   {
      StringBuilder sbTimestamp = new StringBuilder();
      
      sbTimestamp.append(tsTimestamp.getTime());
      sbTimestamp.append(NANO_SEPARATOR);
      sbTimestamp.append(tsTimestamp.getNanos());
      
      return sbTimestamp.toString();
   }
   
   /**
    * Convert string to timestamp including if available it's nanosecond portion 
    * so that it can be safely restored from variable in web page.
    * 
    * @param strTimestamp - timestamp to convert
    * @return Timestamp - restored timestamp
    * @throws NumberFormatException - problem parsing the string
    */
   public static Timestamp parseTimestamp(
      String strTimestamp
   ) throws NumberFormatException
   {
      long      lTime;
      int       iNanos;

      if ("0".equals(strTimestamp))
      {
         lTime = 0L;
         iNanos = 0;
      }
      else
      {
         int       iIndex;
         
         iIndex = strTimestamp.indexOf(NANO_SEPARATOR);
         if (iIndex == -1)
         {
            throw new NumberFormatException(
                         "The timestamp string doesn't contain nanosecond separator: "
                         + strTimestamp);
         }
         
         lTime = Long.parseLong(strTimestamp.substring(0, iIndex));
         iNanos = Integer.parseInt(strTimestamp.substring(iIndex + 1));
      }
      
      return new TimestampCopy(lTime, iNanos);
   }

   /**
    * Function returns time string in the form MM:SS.MS from the input specified 
    * in milliseconds. 
    * 
    * @param lTimeInMiliseconds - time in milliseconds
    * @return String - string representation of milliseconds in the form MM:SS.MS
    */
   public static String getStringTime(
      long lTimeInMiliseconds
   )
   {
      long lTotalMS   = lTimeInMiliseconds;
      long lMS        = lTotalMS % 1000;
      long lTotalSecs = lTotalMS / 1000;
      long lSecs      = lTotalSecs % 60;
      long lTotalMins = lTotalSecs / 60;
      long lMinutes   = lTotalMins % 60;
      long lHours     = lTotalMins / 60;
      StringBuilder sbBuffer = new StringBuilder();

      if (lHours > 0)
      {
         sbBuffer.append(lHours);
         sbBuffer.append(":");
         sbBuffer.append(lMinutes);
         sbBuffer.append(":");
         sbBuffer.append(lSecs);
         sbBuffer.append(".");
         sbBuffer.append(lMS);
      }
      else if (lMinutes > 0)
      {
         sbBuffer.append(lMinutes);
         sbBuffer.append(":");
         sbBuffer.append(lSecs);
         sbBuffer.append(".");
         sbBuffer.append(lMS);
      }
      else if (lSecs > 0)
      {
         sbBuffer.append(lSecs);
         sbBuffer.append(".");
         sbBuffer.append(lMS);
         sbBuffer.append(" seconds");
      }
      else
      {
         sbBuffer.append(lMS);
         sbBuffer.append(" ms");
      }
      
      return sbBuffer.toString();
   }

// TODO: For Miro: Remove this code once all the code which referred to these
// was fixed. These should be moved to a GUI or business logic related class.
//   /**
//    * Method to check if valid period settings
//    * 
//    * @param iPeriod - period length
//    * @param iPeriodType - period type
//    * @param iPeriodStartType - period start type
//    * @param iAttributeId - attribute ID for dynamic period start type
//    * @param bPeriodException - period exception flag
//    * @param strPeriodName - period name used for exception message
//    * @param bAdvancePeriodType - flag if advanced period type (includes also start type)
//    * @param bfideException - invalid data exception
//    */
//   public static void validatePeriod(
//      int iPeriod,
//      int iPeriodType,
//      int iPeriodStartType,
//      int iAttributeId,
//      boolean bPeriodException,
//      String strPeriodName,
//      boolean bAdvancePeriodType,
//      OSSInvalidDataException messageException
//   ) 
//   {
//      if ((iPeriod > 0) 
//         || ((iPeriodType != TIMING_NONE) && (iPeriodType != TIMING_NEVER)) 
//         || (bPeriodException) || (iPeriodStartType != PERIOD_START_TYPE_NONE))
//      {
//         if (iPeriod <= 0)
//         {
//            if (messageException == null)
//            {
//               messageException = new OSSInvalidDataException();
//            }
//            messageException.getErrorMessages().addMessage(
//               PERIOD_TYPE_OBJ, 
//               "You have to set valid period length for " + strPeriodName + " type."
//            );
//         }
//         else if ((iPeriodType == TIMING_NONE) || (iPeriodType == TIMING_NEVER))
//         {
//            if (messageException == null)
//            {
//               messageException = new OSSInvalidDataException();
//            }
//            messageException.getErrorMessages().addMessage(
//               PERIOD_TYPE_OBJ, 
//               "You have to set valid period type for " + strPeriodName + " type."
//            );
//         }
//         else if ((bAdvancePeriodType) && (iPeriodStartType == PERIOD_START_TYPE_NONE))
//         {
//            if (messageException == null)
//            {
//               messageException = new OSSInvalidDataException();
//            }
//            messageException.getErrorMessages().addMessage(
//               PERIOD_TYPE_OBJ, 
//               "You have to set valid period start type for " + strPeriodName + " type."
//            );
//         }
//         else if ((bAdvancePeriodType) 
//                 && (iPeriodStartType == PERIOD_START_TYPE_DYNAMIC) 
//                 && (iAttributeId == DataObject.NEW_ID))
//         {
//            if (messageException == null)
//            {
//               messageException = new OSSInvalidDataException();
//            }
//            messageException.getErrorMessages().addMessage(
//               PERIOD_TYPE_OBJ, 
//               "You have to set valid period dynamic start attribute for " 
//               + strPeriodName + " type."
//            );
//         }
//      }
//   }
}

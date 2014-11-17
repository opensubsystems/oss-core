/*
 * Copyright (C) 2012 - 2014 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Collection of methods to manipulate JSON values.
 * 
 * TODO: Improve: Consider eliminating this class and use gson instead
 * 
 * @author bastafidli
 */
public class JSONUtils 
{
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Commons logger variable used to log runtime information.
    */
   private static Logger s_logger = Log.getInstance(JSONUtils.class);

   // Constructors /////////////////////////////////////////////////////////////
    
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private JSONUtils(
   )
   {
      // Do nothing
   }
   
   // Public methods ///////////////////////////////////////////////////////////

   /**
    * Parse JSON String value
    * 
    * @param mpParams - map containing the parameter
    * @param strParameter - parameter identifying the String value
    * @param lstParsingErrors - list to collect any parsing errors
    * @param bMandatory - if false the parameter is not mandatory and doesn't
    *                     have to exist
    * @return String - string value or null if it cannot be parsed
    */
   public static String parseJSONString(
      Map          mpParams,
      String       strParameter,
      List<String> lstParsingErrors,
      boolean      bMandatory
   )
   {
      Object objTemp;
      String strValue = null;
      
      objTemp = mpParams.get(strParameter);
      if (objTemp == null)
      {
         if (bMandatory)
         {
            lstParsingErrors.add(strParameter 
                                 + " parameter not specified in the request");
         }
      }
      else if (objTemp instanceof String)
      {
         strValue = (String)objTemp;
      }
      else
      {
         // Do not report error and rather convert to string since the underlying
         // framework sometimes automatically converts numbers to string based
         // on how they are formatted (e.g. with or without '')
         strValue = StringUtils.valueIfNotNull(objTemp);
         /*
         lstParsingErrors.add(strParameter
            + " parameter specified in the task is not String."
            + " It is instead " + objTemp.getClass().getName());
         */
      }
      
      return strValue;
   }

   /**
    * Add JSON String value
    * 
    * @param mpParams - map to add the parameter to
    * @param strParameter - parameter identifying the String value
    * @param strValue - string value 
    */
   public static void addJSONString(
      Map    mpParams,
      String strParameter,
      String strValue
   )
   {
       mpParams.put(strParameter, StringUtils.valueIfNotNull(strValue, ""));
   }

   /**
    * Parse JSON Long value
    * 
    * @param mpParams - map containing the parameter
    * @param strParameter - parameter identifying the Long value
    * @param lstParsingErrors - list to collect any parsing errors
    * @param bMandatory - if false the parameter is not mandatory and doesn't
    *                     have to exist
    * @return Long - long value or null if it cannot be parsed
    */
   public static Long parseJSONLong(
      Map          mpParams,
      String       strParameter,
      List<String> lstParsingErrors,
      boolean      bMandatory
   )
   {
      String strValue;
      Long   lValue = null;
      
      strValue = parseJSONString(mpParams, strParameter, lstParsingErrors, 
                                 bMandatory);
      if ((strValue != null) && (!strValue.isEmpty()))
      {
         try
         {
            lValue  = Long.parseLong(strValue);
         }
         catch (NumberFormatException ex)
         {
            lstParsingErrors.add(strParameter
               + " parameter specified in the task is not Long."
               + " It is instead " + strValue);
         }
      }
         
      return lValue;
   }
   
   /**
    * Add JSON Long value
    * 
    * @param mpParams - map to add the parameter to
    * @param strParameter - parameter identifying the String value
    * @param lValue - long value 
    */
   public static void addJSONLong(
      Map    mpParams,
      String strParameter,
      Long   lValue
   )
   {
      if (lValue != null)
      {
          mpParams.put(strParameter, lValue);
      }
      else
      {
          mpParams.put(strParameter, "");
      }
   }
   
   /**
    * Parse JSON Integer value
    * 
    * @param mpParams - map containing the parameter
    * @param strParameter - parameter identifying the Integer value
    * @param lstParsingErrors - list to collect any parsing errors
    * @param bMandatory - if false the parameter is not mandatory and doesn't
    *                     have to exist
    * @return Integer - integer value or null if it cannot be parsed
    */
   public static Integer parseJSONInteger(
      Map          mpParams,
      String       strParameter,
      List<String> lstParsingErrors,
      boolean      bMandatory
   )
   {
      String  strValue;
      Integer iValue = null;
      
      strValue = parseJSONString(mpParams, strParameter, lstParsingErrors, 
                                 bMandatory);
      if (strValue != null) 
      {
         try
         {
            iValue  = Integer.parseInt(strValue);
         }
         catch (NumberFormatException ex)
         {
            lstParsingErrors.add(strParameter
               + " parameter specified in the task is not Integer."
               + " It is instead " + strValue);
         }
      }
         
      return iValue;
   }
   
   /**
    * Add JSON Integer value
    * 
    * @param mpParams - map to add the parameter to
    * @param strParameter - parameter identifying the String value
    * @param iValue - integer value 
    */
   public static void addJSONInteger(
      Map     mpParams,
      String  strParameter,
      Integer iValue
   )
   {
      if (iValue != null)
      {
          mpParams.put(strParameter, iValue);
      }
      else
      {
          mpParams.put(strParameter, "");
      }
   }
   
   /**
    * Parse JSON Boolean value
    * 
    * @param mpParams - map containing the parameter
    * @param strParameter - parameter identifying the Boolean value
    * @param lstParsingErrors - list to collect any parsing errors
    * @param bMandatory - if false the parameter is not mandatory and doesn't
    *                     have to exist
    * @return Boolean - boolean value or null if it cannot be parsed
    */
   public static Boolean parseJSONBoolean(
      Map          mpParams,
      String       strParameter,
      List<String> lstParsingErrors,
      boolean      bMandatory
   )
   {
      String  strValue;
      Boolean bValue = null;
      
      strValue = parseJSONString(mpParams, strParameter, lstParsingErrors, 
                                 bMandatory);
      if (strValue != null) 
      {
         try
         {
            bValue  = Boolean.parseBoolean(strValue);
         }
         catch (NumberFormatException ex)
         {
            lstParsingErrors.add(strParameter
               + " parameter specified in the task is not Boolean."
               + " It is instead " + strValue);
         }
      }
         
      return bValue;
   }
   
   /**
    * Add JSON Boolean value
    * 
    * @param mpParams - map to add the parameter to
    * @param strParameter - parameter identifying the String value
    * @param bValue - boolean value 
    */
   public static void addJSONBoolean(
      Map     mpParams,
      String  strParameter,
      Boolean bValue
   )
   {
      if (bValue != null)
      {
          mpParams.put(strParameter, bValue);
      }
      else
      {
          mpParams.put(strParameter, "");
      }
   }
   
   /**
    * Parse JSON List value
    * 
    * @param mpParams - map containing the parameter
    * @param strParameter - parameter identifying the List value
    * @param lstParsingErrors - list to collect any parsing errors
    * @param bMandatory - if false the parameter is not mandatory and doesn't
    *                     have to exist
    * @return List - List value or null if it cannot be parsed
    */
   public static List parseJSONList(
      Map          mpParams,
      String       strParameter,
      List<String> lstParsingErrors,
      boolean      bMandatory
   )
   {
      Object objTemp;
      List   lstValue = null;
      
      objTemp = mpParams.get(strParameter);
      if (objTemp == null)
      {
         if (bMandatory)
         {
            lstParsingErrors.add(strParameter 
                                 + " parameter not specified in the request");
         }
      }
      else if (objTemp instanceof List)
      {
         lstValue = (List)objTemp;
      }
      else
      {
         lstParsingErrors.add(strParameter
            + " parameter specified in the task is not List."
            + " It is instead " + objTemp.getClass().getName());
      }
      
      return lstValue;
   }

   /**
    * Add JSON List value
    * 
    * @param mpParams - map to add the parameter to
    * @param strParameter - parameter identifying the String value
    * @param lstValue - list value 
    */
   public static void addJSONList(
      Map    mpParams,
      String strParameter,
      List   lstValue
   )
   {
      if (lstValue != null)
      {
          mpParams.put(strParameter, lstValue);
      }
      else
      {
          mpParams.put(strParameter, "");
      }
   }
}

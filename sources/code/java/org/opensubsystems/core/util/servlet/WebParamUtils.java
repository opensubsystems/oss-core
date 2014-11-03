/*
 * Copyright (C) 2003 - 2014 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

package org.opensubsystems.core.util.servlet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import org.opensubsystems.core.error.OSSInvalidDataException;

import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.OSSObject;

/**
 * Collection of useful methods to work with servlet parameters.
 *
 * @author bastafidli
 */
public final class WebParamUtils extends OSSObject
{
   // Configuration settings ///////////////////////////////////////////////////
   
   /** 
    * Name of the property for size of the buffer used to serve files.
    * @see #WEBFILE_BUFFER_DEFAULT_SIZE
    */   
   public static final String WEBUTILS_WEBFILE_BUFFER_SIZE 
                                 = "oss.webserver.servebuffer.size";   

   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Default value for the WEBUTILS_WEBFILE_BUFFER_SIZE.
    * @see #WEBUTILS_WEBFILE_BUFFER_SIZE
    */
   public static final int WEBFILE_BUFFER_DEFAULT_SIZE = 40960;

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(WebParamUtils.class);

   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private WebParamUtils(
   )
   {
      // Do nothing
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Get parameter as a String and if it is not present provide user friendly 
    * error message.
    * 
    * @param strLogPrefix - log prefix used for all log output to tie together
    *                       the same invocations
    * @param request - request to get parameter from
    * @param strParamName  - parameter name to get
    * @param strDisplayName - display name used for this parameter
    * @param bRequired - is this value required or not
    * @return String - value of the parameter
    * @throws OSSInvalidDataException
    */
   public static String getParameterAsString(
      String             strLogPrefix,
      HttpServletRequest request,
      String             strParamName,
      String             strDisplayName,
      boolean            bRequired
   ) throws OSSInvalidDataException
   {
      String strValue;
      
      strValue = request.getParameter(strParamName);
      if (strValue == null)
      {
         // Not a user error, use strParamName
         throw new OSSInvalidDataException(strLogPrefix, strParamName 
                                + " parameter is missing in the request");
      }
      else if ((bRequired) && (strValue.trim().length() == 0))
      {
         throw new OSSInvalidDataException(strLogPrefix, strDisplayName + " wasn't specified");
      }
      
      return strValue;
   }
   
   /**
    * Get parameter as a Integer.
    * 
    * @param strLogPrefix - log prefix used for all log output to tie together
    *                       the same invocations
    * @param request - request to get parameter from
    * @param strParamName  - parameter name to get
    * @return Integer - value of the parameter or null if it doesn't exist
    * @throws OSSInvalidDataException
    */
   public static Integer getParameterAsInteger(
      String             strLogPrefix,
      HttpServletRequest request,
      String             strParamName
   ) throws OSSInvalidDataException
   {
      String  strValue;
      Integer iValue = null;
      
      strValue = request.getParameter(strParamName);
      if ((strValue != null) && (strValue.trim().length() != 0))
      {
         try
         {
            iValue = Integer.parseInt(strValue);
         }
         catch (NumberFormatException nfeExc)
         {
            throw new OSSInvalidDataException(strLogPrefix, 
                                   "Incorrecly formatted value of parameter " 
                                   + strParamName);
         }
      }
      
      return iValue;
   }

   /**
    * Get parameter as a Integer and if it is not present provide user friendly 
    * error message.
    * 
    * @param strLogPrefix - log prefix used for all log output to tie together
    *                       the same invocations
    * @param request - request to get parameter from
    * @param strParamName  - parameter name to get
    * @param strDisplayName - display name used for this parameter
    * @param bRequired - is this value required or not
    * @return Integer - value of the parameter
    * @throws OSSInvalidDataException
    */
   public static Integer getParameterAsInteger(
      String             strLogPrefix,
      HttpServletRequest request,
      String             strParamName,
      String             strDisplayName,
      boolean            bRequired
   ) throws OSSInvalidDataException
   {
      String  strValue;
      Integer iValue = null;
      
      strValue = request.getParameter(strParamName);
      if (strValue == null)
      {
         // Not a user error, use strParamName
         throw new OSSInvalidDataException(strLogPrefix, strParamName 
                                + " parameter is missing in the request");
      }
      else 
      {
         try
         {
            iValue = Integer.parseInt(strValue);
         }
         catch (NumberFormatException nfeExc)
         {
            // Not a user error, use strParamName
            throw new OSSInvalidDataException(strLogPrefix, strParamName 
                                   + " parameter is not valid number: "
                                   + strValue);
         }
         // TODO: Verify if it is a valid number, e.g. from a list of valid
         // number and if it is not use the strDisplayName to provide user 
         // friendly message/
      }
      
      return iValue;
   }

   /**
    * Get parameter as a Long.
    * 
    * @param strLogPrefix - log prefix used for all log output to tie together
    *                       the same invocations
    * @param request - request to get parameter from
    * @param strParamName  - parameter name to get
    * @return Long - value of the parameter or null if it doesn't exist
    * @throws OSSInvalidDataException
    */
   public static Long getParameterAsLong(
      String             strLogPrefix,
      HttpServletRequest request,
      String             strParamName
   ) throws OSSInvalidDataException
   {
      String  strValue;
      Long    lValue = null;
      
      strValue = request.getParameter(strParamName);
      if ((strValue != null) && (strValue.trim().length() != 0))
      {
         try
         {
            lValue = Long.parseLong(strValue);
         }
         catch (NumberFormatException nfeExc)
         {
            throw new OSSInvalidDataException(strLogPrefix, 
                                   "Incorrecly formatted value of parameter " 
                                   + strParamName);
         }
      }
      
      return lValue;
   }

   /**
    * Get parameter as a Long and if it is not present provide user friendly 
    * error message.
    * 
    * @param strLogPrefix - log prefix used for all log output to tie together
    *                       the same invocations
    * @param request - request to get parameter from
    * @param strParamName  - parameter name to get
    * @param strDisplayName - display name used for this parameter
    * @param bRequired - is this value required or not
    * @return Long - value of the parameter
    * @throws OSSInvalidDataException
    */
   public static Long getParameterAsLong(
      String             strLogPrefix,
      HttpServletRequest request,
      String             strParamName,
      String             strDisplayName,
      boolean            bRequired
   ) throws OSSInvalidDataException
   {
      String strValue;
      Long   lValue = null;
      
      strValue = request.getParameter(strParamName);
      if (strValue == null)
      {
         // Not a user error, use strParamName
         throw new OSSInvalidDataException(strLogPrefix, strParamName 
                                + " parameter is missing in the request");
      }
      else 
      {
         try
         {
            lValue = Long.parseLong(strValue);
         }
         catch (NumberFormatException nfeExc)
         {
            // Not a user error, use strParamName
            throw new OSSInvalidDataException(strLogPrefix, strParamName 
                                   + " parameter is not valid number: "
                                   + strValue);
         }
         // TODO: Verify if it is a valid number, e.g. from a list of valid
         // number and if it is not use the strDisplayName to provide user 
         // friendly message/
      }
      
      return lValue;
   }

   /**
    * Get parameter as a Collection of Long, Values and if it is not present 
    * provide user friendly  error message.
    * 
    * @param strLogPrefix - log prefix used for all log output to tie together
    *                       the same invocations
    * @param request - request to get parameter from
    * @param strParamName  - parameter name to get
    * @param strDisplayName - display name used for this parameter
    * @param bRequired - is this value required or not
    * @return Collection<Long> - value of the parameter
    * @throws OSSInvalidDataException
    */
   public static Collection<Long> getParameterAsLongCollection(
      String             strLogPrefix,
      HttpServletRequest request,
      String             strParamName,
      String             strDisplayName,
      boolean            bRequired
   ) throws OSSInvalidDataException
   {
      String[]         arValue;
      Collection<Long> colValues = null;
      
      arValue = request.getParameterValues(strParamName);
      if (arValue == null)
      {
         // Not a user error, use strParamName
         throw new OSSInvalidDataException(strLogPrefix, strParamName 
                                + " parameter is missing in the request");
      }
      else if (arValue.length == 0)
      {
         // Not a user error, use strParamName
         throw new OSSInvalidDataException(strLogPrefix, strParamName 
                                + " parameter contains no values.");
      }
      else 
      {
         Long lValue = null;
         colValues = new ArrayList<>(arValue.length);
         
         for (String strValue : arValue)
         {
            try
            {
               lValue = Long.parseLong(strValue);
               colValues.add(lValue);
            }
            catch (NumberFormatException nfeExc)
            {
               // Not a user error, use strParamName
               throw new OSSInvalidDataException(strLogPrefix, strParamName 
                                      + " parameter is not valid number: "
                                      + strValue);
            }
            // TODO: Verify if it is a valid number, e.g. from a list of valid
            // number and if it is not use the strDisplayName to provide user 
            // friendly message/
         }
      }
      
      return colValues;
   }

   /**
    * Get parameter as a Boolean and if it is not present provide user friendly 
    * error message.
    * 
    * @param strLogPrefix - log prefix used for all log output to tie together
    *                       the same invocations
    * @param request - request to get parameter from
    * @param strParamName  - parameter name to get
    * @param strDisplayName - display name used for this parameter
    * @param bRequired - is this value required or not
    * @return Boolean - value of the parameter
    * @throws OSSInvalidDataException
    */
   public static Boolean getParameterAsBoolean(
      String             strLogPrefix,
      HttpServletRequest request,
      String             strParamName,
      String             strDisplayName,
      boolean            bRequired
   ) throws OSSInvalidDataException
   {
      String  strValue;
      Boolean bValue = null;
      
      strValue = request.getParameter(strParamName);
      if (strValue == null)
      {
         // Not a user error, use strParamName
         throw new OSSInvalidDataException(strLogPrefix, strParamName 
                                + " parameter is missing in the request");
      }
      else 
      {
         bValue = Boolean.parseBoolean(strValue);

         // TODO: Verify if it is a valid boolean, e.g. from a list of valid
         // booleans and if it is not use the strDisplayName to provide user 
         // friendly message/
      }
      
      return bValue;
   }

   /**
    * Get parameter as a List of Strings.
    * 
    * @param request - request to get parameter from
    * @param strParamName  - parameter name to get
    * @param bFilterEmptyStrings - if true then empty strings will be removed
    * @return Long - value of the parameter or null if it doesn't exist
    * @throws OSSInvalidDataException
    */
   public static List<String> getParameterAsList(
      HttpServletRequest request,
      String             strParamName,
      boolean            bFilterEmptyStrings
   ) throws OSSInvalidDataException
   {
      String[]     strValues;
      List<String> lstValues = null;
      
      strValues = request.getParameterValues(strParamName);
      if (strValues != null)
      {
         lstValues = Arrays.asList(strValues);
          if (bFilterEmptyStrings)
          {
             // The above list is read only so we need to reinitialize it
             lstValues = new ArrayList<>(lstValues);
             for (Iterator<String> values = lstValues.iterator();  values.hasNext();)
             {
                if (values.next().trim().length() == 0)
                {
                   values.remove();
                }
             }
          }
      }
      
      return lstValues;
   }

   /**
    * Get parameter as a file stored on a disk and if it is not present provide 
    * user friendly error message.
    * 
    * @param strLogPrefix - log prefix used for all log output to tie together
    *                       the same invocations
    * @param request - request to get parameter from
    * @param strParamName  - parameter name to get
    * @param strDisplayName - display name used for this parameter
    * @param bRequired - is this value required or not
    * @return File - value of the parameter
    * @throws OSSInvalidDataException
    */
   public static File getParameterAsDiskFile(
      String             strLogPrefix,
      HttpServletRequest request,
      String             strParamName,
      String             strDisplayName,
      boolean            bRequired
   ) throws OSSInvalidDataException
   {
      File fileValue;
      
      fileValue = request.getParameter(strParamName);
      if (fileValue == null)
      {
         // Not a user error, use strParamName
         throw new OSSInvalidDataException(strLogPrefix, strParamName 
                                + " parameter is missing in the request");
      }
      else if ((bRequired) && (fileValue.trim().length() == 0))
      {
         throw new OSSInvalidDataException(strLogPrefix, strDisplayName + " wasn't specified");
      }
      
      return fileValue;
   }
   
}

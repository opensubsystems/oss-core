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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.FileCleanerCleanup;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileCleaningTracker;
import org.opensubsystems.core.error.OSSInvalidDataException;
import org.opensubsystems.core.util.Config;
import org.opensubsystems.core.util.FileUtils;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.OSSObject;
import org.opensubsystems.core.util.PropertyUtils;
import org.opensubsystems.core.util.TwoElementStruct;

/**
 * Collection of useful methods to work with servlet parameters.
 *
 * @author bastafidli
 */
public final class WebParamUtils extends OSSObject
{
   // Configuration settings ///////////////////////////////////////////////////
   
   /** 
    * Name of the property to specify maximal size of files that can be uploaded.
    * @see #REQUEST_UPLOAD_MAX_SIZE_DEFAULT
    */   
   public static final String REQUEST_UPLOAD_MAX_SIZE = "oss.upload.max.size";   

   /**
    * Name of the property to specify threshold for uploaded files so that if the
    * files are smaller than this value, they will be kept in memory.
    * @see #REQUEST_UPLOAD_MEMORY_THRESHOLD_DEFAULT
    */
   public static final String REQUEST_UPLOAD_MEMORY_THRESHOLD = "oss.upload.memory.threshold";
   
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Default value for the REQUEST_UPLOAD_MEMORY_THRESHOLD.
    * @see #REQUEST_UPLOAD_MAX_SIZE
    */
   public static final int REQUEST_UPLOAD_MAX_SIZE_DEFAULT = 1000000;

   /**
    * Default value for the REQUEST_UPLOAD_MEMORY_THRESHOLD.
    * @see #REQUEST_UPLOAD_MEMORY_THRESHOLD
    */
   public static final int REQUEST_UPLOAD_MEMORY_THRESHOLD_DEFAULT = 4096;

   /**
    * Name of attribute used to store parsed parameters passed in via multipart 
    * request keyed by the name of the parameter
    */
   public static final String REQUEST_PARAMS_MAP = "ossrequestparamsmap";
   
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
      
      strValue = getParameter(strLogPrefix, request, strParamName);
      if (strValue == null)
      {
         // Not a user error, use strParamName
         throw new OSSInvalidDataException(strLogPrefix, strParamName 
                                + " parameter is missing in the request");
      }
      else if ((bRequired) && (strValue.trim().length() == 0))
      {
         throw new OSSInvalidDataException(strLogPrefix, strDisplayName 
                                           + " wasn't specified");
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
      
      strValue = getParameter(strLogPrefix, request, strParamName);
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
      
      strValue = getParameter(strLogPrefix, request, strParamName);
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
      
      strValue = getParameter(strLogPrefix, request, strParamName);
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
      
      strValue = getParameter(strLogPrefix, request, strParamName);
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
      
      arValue = getParameterValues(strLogPrefix, request, strParamName);
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
      
      strValue = getParameter(strLogPrefix, request, strParamName);
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
    * @param strLogPrefix - log prefix used for all log output to tie together
    *                       the same invocations
    * @param request - request to get parameter from
    * @param strParamName  - parameter name to get
    * @param bFilterEmptyStrings - if true then empty strings will be removed
    * @return Long - value of the parameter or null if it doesn't exist
    * @throws OSSInvalidDataException
    */
   public static List<String> getParameterAsList(
      String             strLogPrefix,
      HttpServletRequest request,
      String             strParamName,
      boolean            bFilterEmptyStrings
   ) throws OSSInvalidDataException
   {
      String[]     strValues;
      List<String> lstValues = null;
      
      strValues = getParameterValues(strLogPrefix, request, strParamName);
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
      File                  fileValue;
      FileItem              uploadedFile;
      Object                temp;
      Map<String, FileItem> mpFiles;
      TwoElementStruct<Map<String, Object>, Map<String, FileItem>> params;

      temp = request.getAttribute(REQUEST_PARAMS_MAP);
      if (temp == null)
      {
         try 
         {
            params = parseMultipartRequest(strLogPrefix, request);
         } 
         catch (FileUploadException exc) 
         {
            throw new OSSInvalidDataException(strLogPrefix, 
                         "Cannot parse multipart request", exc);
         }
         request.setAttribute(REQUEST_PARAMS_MAP, params);
      }
      else
      {
         params = (TwoElementStruct<Map<String, Object>, Map<String, FileItem>>)temp;
      }

      mpFiles = params.getSecond();
      uploadedFile = mpFiles.get(strParamName);
      
      if (uploadedFile == null)
      {
         if (bRequired)
         {
            throw new OSSInvalidDataException(strLogPrefix, strDisplayName 
                                              + " wasn't specified");
         }
         fileValue = null;
      }
      else
      {
         // TODO: Improve: Consider calling 
         // FileUtils.createTemporarySubdirectory
         // as done in legacy Formature.DocumentTemplateServlet.getFormToProcess
         // to store the temporary files per session and request
         String strTempDir = FileUtils.getTemporaryDirectory();
         
         try 
         {
            fileValue = File.createTempFile("oss", "upload", new File(strTempDir));
            try 
            {         
               uploadedFile.write(fileValue);
            } 
            catch (Exception exc) 
            {
               throw new OSSInvalidDataException(strLogPrefix, 
                            "Unable to save the uploaded file to disk.", exc);
            }
         } 
         catch (IOException exc) 
         {
            throw new OSSInvalidDataException(strLogPrefix, 
                         "Unable to generate temporary file to save the uploaded"
                         + " file to disk.", exc);
         }
      }
      
      return fileValue;
   }

   /**
    * Get single parameter value correctly handling regular and multipart requests.
    * Returns the value of a request parameter as a String, or null if the parameter 
    * does not exist. Request parameters are extra information sent with the request. 
    * For HTTP servlets, parameters are contained in the query string or posted 
    * form data. You should only use this method when you are sure the parameter 
    * has only one value. If the parameter might have more than one value, use 
    * getParameterValues(java.lang.String). If you use this method with a 
    * multivalued parameter, the value returned is equal to the first value in 
    * the array returned by getParameterValues.
    * 
    * @param strLogPrefix - log prefix used for all log output to tie together
    *                       the same invocations
    * @param request - request to get parameter from
    * @param strParamName  - parameter name to get
    * @return String - a single (first) value of the parameter or null if the 
    *                  parameter values were not specified
    * @throws OSSInvalidDataException - an error has occurred
    */
   public static String getParameter(
      String             strLogPrefix,
      HttpServletRequest request,
      String             strParamName
   ) throws OSSInvalidDataException 
   {
      String  strValue;
      boolean bIsMultipart = ServletFileUpload.isMultipartContent(request);
      
      if (!bIsMultipart)
      {
         strValue = request.getParameter(strParamName);
      }
      else
      {
         Object temp;
         
         temp = getMultipartParameterValueAsObject(strLogPrefix, request, strParamName);
         if (temp != null)
         {
            if (temp instanceof String)
            {
               strValue = (String)temp;
            }
            else
            {
               List<String> lstValues;
               
               lstValues = (List<String>)temp;
               strValue = lstValues.get(0);
            }
         }
         else
         {
            strValue = null;
         }
      }
      
      return strValue;
   }

   /**
    * Get all parameter value correctly handling regular and multipart requests.
    * Returns an array of String objects containing all of the values the given 
    * request parameter has, or null if the parameter does not exist. If the 
    * parameter has a single value, the array has a length of 1.
    * 
    * @param strLogPrefix - log prefix used for all log output to tie together
    *                       the same invocations
    * @param request - request to get parameter from
    * @param strParamName  - parameter name to get
    * @return String[] - an array of String objects containing the parameter's 
    *                    values or null if the parameter values were not specified
    * @throws OSSInvalidDataException - an error has occurred
    */
   public static String[] getParameterValues(
      String             strLogPrefix,
      HttpServletRequest request,
      String             strParamName
   ) throws OSSInvalidDataException 
   {
      String[] arValues;
      boolean  bIsMultipart = ServletFileUpload.isMultipartContent(request);
      
      if (!bIsMultipart)
      {
         arValues = request.getParameterValues(strParamName);
      }
      else
      {
         Object temp;
         
         temp = getMultipartParameterValueAsObject(strLogPrefix, request, strParamName);
         if (temp != null)
         {
            if (temp instanceof String)
            {
               arValues = new String[1];
               arValues[1] = (String)temp;
            }
            else
            {
               List<String> lstValues;
               
               lstValues = (List<String>)temp;
               arValues = lstValues.toArray(new String[lstValues.size()]);
            }
         }
         else
         {
            arValues = null;
         }
      }
      
      return arValues;
   }
   
   /**
    * Parse multipart request and separate regular parameters and files. The
    * files names are also stored as values of the parameters that are used to 
    * upload them.
    * 
    * @param strLogPrefix - log prefix used for all log output to tie together
    *                       the same invocations
    * @param request - request to get parameter from
    * @return TwoElementStruct<Map<String, String>, Map<String, FileItem>> - the
    *                  first element is map of parameter names and their values. 
    *                  For uploaded files the files names are also stored here as 
    *                  values of the parameters that are used to upload them.
    *                  If there is only one value of the parameter then the value
    *                  is stored directly as String. If there are multiple values
    *                  then the values are stored as List<String>.
    *                  The second element is map of parameter names and the files
    *                  that are uploaded as these parameters.
    * @throws FileUploadException - an error has occurred
    */
   public static TwoElementStruct<Map<String, Object>, Map<String, FileItem>> parseMultipartRequest(
      String             strLogPrefix,
      HttpServletRequest request
   ) throws FileUploadException 
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert ServletFileUpload.isMultipartContent(request) 
                : "Specified request is not multipart";
      }
      
      TwoElementStruct<Map<String, Object>, Map<String, FileItem>> returnValue;
      FileCleaningTracker fileCleaningTracker;
      String              strTempDir;
      DiskFileItemFactory factory;
      Properties          prpSettings;
      int                 iMaxInMemorySize;
      int                 iMaxSize;
      ServletFileUpload   upload;
      List<FileItem>      items;

      // TODO: Improve: Consider calling 
      // FileUtils.createTemporarySubdirectory
      // as done in legacy Formature.DocumentTemplateServlet.getFormToProcess
      // to store the temporary files per session and request
      strTempDir = FileUtils.getTemporaryDirectory();

      prpSettings = Config.getInstance().getProperties();
      iMaxInMemorySize = PropertyUtils.getIntPropertyInRange(
                                 prpSettings, REQUEST_UPLOAD_MEMORY_THRESHOLD, 
                                 REQUEST_UPLOAD_MEMORY_THRESHOLD_DEFAULT, 
                                 "Maximal size of uploaded file that is kept in memory", 
                                 1, // 0 is allowed 
                                 Integer.MAX_VALUE);
      iMaxSize = PropertyUtils.getIntPropertyInRange(
                                 prpSettings, REQUEST_UPLOAD_MAX_SIZE, 
                                 REQUEST_UPLOAD_MAX_SIZE_DEFAULT, 
                                 "Maximal size of uploaded file", 
                                 1, // 0 is allowed 
                                 Integer.MAX_VALUE);
      
      fileCleaningTracker = FileCleanerCleanup.getFileCleaningTracker(
                                                  request.getServletContext());
   
      // Create a factory for disk-based file items
      factory = new DiskFileItemFactory();
      factory.setFileCleaningTracker(fileCleaningTracker);
      // Set factory constraints
      factory.setSizeThreshold(iMaxInMemorySize);
      factory.setRepository(new File(strTempDir));

      // Create a new file upload handler
      upload = new ServletFileUpload(factory);
      // Set overall request size constraint
      upload.setSizeMax(iMaxSize);

      // Parse the request
      items = upload.parseRequest(request);
      if ((items != null) && (!items.isEmpty()))
      {
         Map          mpParams;
         Map          mpFiles;
         String       strParamName;
         String       strValue;
         Object       temp;
         List<String> lstValues;
               
         mpParams = new HashMap(items.size());
         mpFiles = new HashMap();

         returnValue = new TwoElementStruct(mpParams, mpFiles);
         for (FileItem item : items) 
         {
            strParamName = item.getFieldName();
            if (item.isFormField()) 
            {
               strValue = item.getString();
            } 
            else 
            {
               strValue = item.getName();
               mpFiles.put(strParamName, item);
            }
            
            temp = mpParams.put(strParamName, strValue);
            if (temp != null)
            {
               // There was already an value so convert it to list of values
               if (temp instanceof String)
               {
                  // There are currently exactly two values
                  lstValues = new ArrayList<>();
                  lstValues.add((String)temp);
                  mpParams.put(strParamName, lstValues);
               }
               else
               {
                  // There are currently more than two values
                  lstValues = (List<String>)temp;
               }
               lstValues.add(strValue);
            }
         }
      }
      else
      {
         returnValue = new TwoElementStruct(Collections.emptyMap(), 
                                            Collections.emptyMap());
      }
      
      return returnValue;
   }
   
   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Get all parameter value correctly handling regular and multipart requests.
    * Returns Object that represents either a single String value of the parameter 
    * or an array of String objects containing all of the values the given 
    * request parameter has, or null if the parameter does not exist. If the 
    * parameter has a single value, the array has a length of 1.
    * 
    * @param strLogPrefix - log prefix used for all log output to tie together
    *                       the same invocations
    * @param request - request to get parameter from
    * @param strParamName  - parameter name to get
    * @return Object - an array of String objects or a String containing the 
    *                  parameter's values or null if the parameter values were 
    *                  not specified
    * @throws OSSInvalidDataException - an error has occurred
    */
   protected static Object getMultipartParameterValueAsObject(
      String             strLogPrefix,
      HttpServletRequest request,
      String             strParamName
   ) throws OSSInvalidDataException
   {
      Object              temp;
      Map<String, Object> mpParams;
      TwoElementStruct<Map<String, Object>, Map<String, FileItem>> params;

      temp = request.getAttribute(REQUEST_PARAMS_MAP);
      if (temp == null)
      {
         try 
         {
            params = parseMultipartRequest(strLogPrefix, request);
         } 
         catch (FileUploadException exc) 
         {
            throw new OSSInvalidDataException(strLogPrefix, 
                         "Cannot parse multipart request", exc);
         }
         request.setAttribute(REQUEST_PARAMS_MAP, params);
      }
      else
      {
         params = (TwoElementStruct<Map<String, Object>, Map<String, FileItem>>)temp;
      }

      mpParams = params.getFirst();
      temp = mpParams.get(strParamName);

      return temp;
   }
}

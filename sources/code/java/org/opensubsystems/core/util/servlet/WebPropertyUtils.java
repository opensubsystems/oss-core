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

package org.opensubsystems.core.util.servlet;

import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.PropertyUtils;
import static org.opensubsystems.core.util.PropertyUtils.printConfigMessage;

/**
 * Utility methods to access configuration settings for various servlet related 
 * objects.
 * 
 * @author bastafidli
 */
public final class WebPropertyUtils 
{
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Commons logger variable used to log runtime information.
    */
   private static Logger s_logger = Log.getInstance(WebPropertyUtils.class);
   
   // Constructors /////////////////////////////////////////////////////////////
    
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private WebPropertyUtils(
   )
   {
      // Do nothing
   }
   
   // Public methods ///////////////////////////////////////////////////////////

   /**
    * Read property from from different configuration locations.
    * 1st try to read from servlet config (init-param within web.xml)
    * 2nd try to read from servlet context (context-param within web.xml)
    * 
    * @param scConfig - servlet config to search (application configuration file
    *                    is known by other mean)
    * @param strProperty - name of the property to read
    * @param strDefaultValue - default value of the property returned in case
    *                           the value is not found, null is allows
    * @param strDisplayName - user friendly name of the property  
    * @param bAllowEmpty - is empty string valid value of the property
    * @param bPrintMessage - if true then message about what value was read will
    *                        be printed into the log, if false nothing will be
    * @return String - value of the property
    */
   public static String readProperty(
      ServletConfig scConfig,
      String        strProperty,
      String        strDefaultValue,
      String        strDisplayName,
      boolean       bAllowEmpty,
      boolean       bPrintMessage
   )
   {
      String strValue;

      strValue = scConfig.getInitParameter(strProperty);

      if ((strValue == null) 
         || ((strValue.length() == 0) && (!bAllowEmpty)))
      {
         // didn't found, try to read property from context-param within web.xml
         strValue = scConfig.getServletContext().getInitParameter(
                                                           strProperty);
         if ((strValue == null) 
            || ((strValue.length() == 0) && (!bAllowEmpty)))
         {
            if ((strDefaultValue != null) && (bPrintMessage))
            {
               PropertyUtils.printConfigMessage(strProperty, strDefaultValue,  
                                                strDisplayName + " is not set in property "
                                                + strProperty + ", using default value " 
                                                + strDefaultValue);
            }
            // still didn't found, set up default value
            strValue = strDefaultValue;
         }
      }
      
      if ((strValue != null) && (bPrintMessage))
      {
         printConfigMessage(strProperty, strValue, null);  
      }

      return strValue;
   }

   /**
    * Read property from servlet context (context-param within web.xml)
    * 
    * @param scContext - servlet context to search (application configuration file
    *                    is known by other mean)
    * @param strProperty - name of the property to read
    * @param strDefaultValue - default value of the property returned in case
    *                          the value is not found, null is allows
    * @param strDisplayName - user friendly name of the property  
    * @param bAllowEmpty - is empty string valid value of the property
    * @param bPrintMessage - if true then message about what value was read will
    *                        be printed into the log, if false nothing will be
    * @return String - value of the property
    */
   public static String readProperty(
      ServletContext scContext,
      String         strProperty,
      String         strDefaultValue,
      String         strDisplayName,
      boolean        bAllowEmpty,
      boolean        bPrintMessage
   )
   {
      String strValue;

      strValue = scContext.getInitParameter(strProperty);
      if ((strValue == null) || ((strValue.length() == 0) && (!bAllowEmpty)))
      {
         if ((strDefaultValue != null) && (bPrintMessage))
         {
            PropertyUtils.printConfigMessage(strProperty, strDefaultValue,  
                                             strDisplayName + " is not set in property "
                                             + strProperty + ", using default value " 
                                             + strDefaultValue);
         }
         // still didn't found, set up default value
         strValue = strDefaultValue;
      }
      
      if ((strValue != null) && (bPrintMessage))
      {
         printConfigMessage(strProperty, strValue, null);  
      }
      
      return strValue;
   }
}

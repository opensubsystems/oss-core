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
 
package org.opensubsystems.core.www.servlet;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensubsystems.core.application.Module;
import org.opensubsystems.core.application.impl.ApplicationImpl;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.WebConstants;
import org.opensubsystems.core.www.WebModule;

/**
 * This is a simple servlet which doesn't do anything else but redirects the
 * request to specified JSP page or some other destination. Since it is derived 
 * from WebUIServlet/WebSessionServlet it is ensured that the security is taken
 * care of before the page is displayed. This is useful if we have some pages 
 * which are very simple and doesn't require logic or pages, for which the logic
 * doesn't exist yet, for example when demo application is being created. 
 *  
 * @author bastafidli
 */
public class WebUIDispatchServlet extends WebUIServlet
{
   // Configuration settings ///////////////////////////////////////////////////

   /** 
    * Name of the property for page which should be displayed upon request.
    */   
   public static final String DISPATCH_PAGE = "dispatch.page";
   
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Names of the current domain parameter.
    */
   public static final String ACTIVE_MODULE_NAME = "activemodule";

   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 322547463691937622L;

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   public String getServletInfo(
   )
   {
      return this.getClass().getName();
   }

   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected void init(
      ServletConfig scConfig,
      Properties    prpSettings
   ) throws ServletException,
            OSSException
   {
      super.init(scConfig, prpSettings);

      // Load UI page for the main screen display
      cacheUIPath(prpSettings, DISPATCH_PAGE, "Page to display");
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void doGet(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws ServletException, 
            IOException
   {
      String strModuleName = null;
      // try to find out from actual request path, what module is active 
      // and set up parameter 'activemodule' into the request
      try
      {
         strModuleName = getModuleIdentifierFromURL(hsrqRequest.getServletPath());
      }
      catch (OSSException osseExc)
      {
         throw new ServletException("An unexpected exception has occurred " +
                                    "while getting web module name from URL.", 
                                    osseExc);
      }
      
      if ((strModuleName != null) && (strModuleName.length() > 0))
      {
         // set up active module name into the request parameter
         hsrqRequest.setAttribute(ACTIVE_MODULE_NAME, strModuleName);
      }
      
      // Right now there is no action
      displayUI(DISPATCH_PAGE, hsrqRequest, hsrpResponse);
   }

   /**
    * Get web module identifier from URL.
    * 
    * @param strURL - URL the module identifier has to be found for
    * @return String - module identifier for specified URL
    * @throws OSSException - an error has occurred
    */
   protected String getModuleIdentifierFromURL(
      String strURL
   ) throws OSSException
   {
      String strReturn = null;
      Map    mpModules = ApplicationImpl.getInstance().getModules();
      
      if ((mpModules != null) && (!mpModules.isEmpty()))
      {
         Iterator  itDefinitions;
         Module    module;
         WebModule webModule;
         
         for (itDefinitions = mpModules.values().iterator();
             itDefinitions.hasNext();)
         {
            module = (Module)itDefinitions.next();
            // There can be all kinds of modules present in the application so 
            // take into account only web modules 
            if (module instanceof WebModule)
            {
               webModule = (WebModule)module;
               // If strURL starts with url separator then remove it from there
               if (strURL.startsWith(WebConstants.URL_SEPARATOR))
               {
                  strURL = strURL.substring(
                                     WebConstants.URL_SEPARATOR.length()); 
               }
               
               // We want to do starts with (not equals) because there can be 
               // something like activetab or other attributes attached
               if (strURL.startsWith(webModule.getURL()))
               {
                  // set up active module name into the return parameter
                  strReturn = webModule.getIdentifier();
               }
            }
         }
      }
      
      return strReturn;
   }
}

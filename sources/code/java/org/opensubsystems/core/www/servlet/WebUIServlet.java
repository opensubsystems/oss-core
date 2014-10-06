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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.CallContext;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.PropertyUtils;
import org.opensubsystems.core.util.StopWatch;
import org.opensubsystems.core.util.WebConstants;
import org.opensubsystems.core.util.servlet.WebSessionUtils;
import org.opensubsystems.core.util.servlet.WebUtils;
import org.opensubsystems.core.www.jsp.MessageTag;

/**
 * Base class for all servlets developed as part of this  project, which perform 
 * web form processing or execute an action based on request from the user 
 * interface. It's main responsibility is to provide default implementation if 
 * functionality for form processing is required. 
 *
 * @author bastafidli
 */
public class WebUIServlet extends WebSessionServlet
{
   // Configuration settings ///////////////////////////////////////////////////

   /** 
    * Property used to specify page to display messages to user for example 
    * in case of error.
    */   
   public static final String WEBUI_MESSAGEBOX_PAGE 
                                 = "oss.webui.messagebox.page";

   /** 
    * Property used to specify style sheet used to display messages to user for 
    * example in case of error.
    */   
   public static final String WEBUI_MESSAGEBOX_STYLE_SHEET 
                                 = "oss.webui.messagebox.css";
   
//   /** 
//    * TODO: For Miro: What is this for?
//    */   
//   public static final String CLOSE_PREVIEW_PAGE = "oss.webui.closepreview.page";

   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Name of the generic attribute used to pass some string representation of 
    * the current request to the message box page in case it wants to display 
    * some troubleshooting information.
    */
   public static final String ORIGINAL_REQUEST_INFO_REQUEST_PARAM = "originalrequest";

   /**
    * Name of the generic attribute used to pass some data to the rendering page.
    * Any additional data can be passed in attributed named "databean2", "databean3", 
    * etc.
    */
   public static final String DATA_ATTRIBUTE_REQUEST_PARAM = "databean";

   /**
    * Name of the attribute used to pass page title to the rendering pages.
    */
   public static final String PAGE_TITLE_REQUEST_PARAM = "pagetitle";

   /**
    * Name of the attribute used to pass flag if the user is logged in.
    */
   public static final String LOGGEDIN_REQUEST_PARAM = "loggedin";
   
   /**
    * Name of the attribute used to pass flag if the dialog has to be maximized.
    */
   public static final String FORCE_MAXIMIZE_PARAM = "forcemaximize";

   /**
    * Parameter used to identify form to be processed.
    * @see #FORM_UNKNOWN_ID
    * @see #getFormToProcess
    */
   public static final String FORM_NAME_REQUEST_PARAM = "FORM_NAME";

   /**
    * Constants for forms recognized by this servlet
    * @see #FORM_NAME_REQUEST_PARAM
    * @see #getFormToProcess
    */
   public static final int FORM_UNKNOWN_ID = -1;

   /**
    * Constants for number of forms recognized by this servlet
    */
   public static final int FORM_COUNT_WEBUI = 0;

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(WebUIServlet.class);

   /**
    * Style sheet used for message boxes if any.
    */
   protected String m_strMessageStyleSheet;
   
   /**
    * Cache where paths to individual UI pages will be stored once initialized 
    * from configuration file.
    * We can use not synchronized HashMap instead of synchronized Hashtable
    * because once the map in initialized, it will be read only.
    * This cache cannot be static since every instance of this servlet
    * can be configured to use different UI.
    */
   private final Map m_mpUIPathCache = new HashMap();
   
   /**
    * Cache where initialized dispatchers to individual UI pages will be stored 
    * once initialized. This is only if they can be cached.
    * We can use not synchronized HashMap instead of synchronized Hashtable
    * because once the map in initialized, it will be read only.
    * This cache cannot be static since every instance of this servlet
    * can be configured to use different UI.
    */
   private final Map m_mpUIRendererCache = new HashMap();

   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 322547463691937622L;

   // Servlet operations ///////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   @Override
   public String getServletInfo(
   )
   {
      return this.getClass().getName();
   }

   // Helper methods for accessing user interface //////////////////////////////

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

// TODO: For Miro: Remove this if you remove the lines above      
//      cacheUIPath(scConfig, CLOSE_PREVIEW_PAGE, 
//                  "Path to close preview page is not set in property " 
//                  + CLOSE_PREVIEW_PAGE);
      
      m_strMessageStyleSheet = PropertyUtils.getStringProperty(
                                  prpSettings, WEBUI_MESSAGEBOX_STYLE_SHEET, 
                                  null, 
                                  "Style sheet to use for the message boxes", 
                                  true);
      
      cacheUIPath(prpSettings, WEBUI_MESSAGEBOX_PAGE, "Message box page");
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void preservice(
      HttpSession         hsSession, 
      HttpServletRequest  hsrqRequest, 
      HttpServletResponse hsrpResponse,
      boolean             bLoginVerified
   ) throws ServletException, 
            IOException
   {
      super.preservice(hsSession, hsrqRequest, hsrpResponse, bLoginVerified);
      
      // All gui application may need to know if the user is logged in or not
      setLoggedInFlag(hsrqRequest, hsrpResponse);
   }

   /**
    * Find and cache path to portion of the user interface by examining the
    * specified configuration settings.
    *
    * @param prpSettings - configuration settings where to look for UI 
    *                      definition
    * @param strUIID - unique ID of the configuration parameter used to specify 
    *                  user interface which is URL or path to some kind of 
    *                  rendering page (e.g. JSP page)
    * @param strUIDescription - user friendly description of the user interface
    *                           to cache
    * @throws OSSException - an error has occurred
    * @throws ServletException - an error has occurred
    */
   protected final void cacheUIPath(
      Properties prpSettings,
      String     strUIID,
      String     strUIDescription      
   ) throws ServletException,
            OSSException
   {
      String strUIPath;
      
      strUIPath = PropertyUtils.getStringProperty(prpSettings, strUIID, 
                                                  strUIDescription);
      
      // Found something, try to cache it
      m_mpUIPathCache.put(strUIID, strUIPath);
      cacheDispatcher(strUIID, strUIPath);
   }
   
   /**
    * Cache dispatcher to given resource if caching is possible.
    * 
    * @param strUIID - unique ID of the configuration parameter used to specify this UI
    * @param strUIPath - path to given resource
    * @throws ServletException - problem finding the specified UI
    */
   protected final void cacheDispatcher(
      String strUIID,
      String strUIPath
   ) throws ServletException
   {
      if (isDispatcherCachingEnabled())
      {
         // Create dispatcher since they can be cached
         RequestDispatcher rdCachedDispatcher;
         
         rdCachedDispatcher = m_scServletContext.getRequestDispatcher(strUIPath);
         if (rdCachedDispatcher == null)
         {
            throw new ServletException("Cannot initialize user interface handler"
                                        + " specified by property "
                                        + strUIID + " = " + strUIPath);
         }
         else
         {
            // Cache the dispatched
            m_mpUIRendererCache.put(strUIID, rdCachedDispatcher);
         }
      }
   }
   
   /**
    * Get handle to resource responsible for rendering of specified part of UI
    *
    * @param strUIID - unique ID of the configuration parameter used to specify 
    *                  UI to display. This is the same ID previously passed into 
    *                  cacheUIPath method. 
    * @param hsrqRequest - the servlet request.
    * @param hsrpResponse - the servlet response.
    * @throws ServletException - problems displaying UI
    * @throws IOException - problems displaying UI
    */
   protected final void displayUI(
      String              strUIID,
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws ServletException,
             IOException
   {
      // In Jetty RequestDispatcher is reusable, e.g. in WebLogic it is not 
      // so this code needs to reflect it   
      RequestDispatcher rdReturn;
      String            strUIPath;     
      StopWatch           timer = new StopWatch();
     
      if (isDispatcherCachingEnabled())
      {
         // Get the cached copy, it was supposed to be initialized and cached
         rdReturn = (RequestDispatcher)m_mpUIRendererCache.get(strUIID);
         
         if (GlobalConstants.ERROR_CHECKING)
         {
            assert rdReturn != null
                   : "Request dispatcher for " + strUIID + " wasn't cached properly.";
         }
      }
      else
      {
         strUIPath = (String)m_mpUIPathCache.get(strUIID);

         if (GlobalConstants.ERROR_CHECKING)
         {
            assert strUIPath != null
                   : "Unknown UI rendered to retrieve " + strUIID;
         }

         rdReturn = m_scServletContext.getRequestDispatcher(strUIPath);
         if (rdReturn == null)
         {
            throw new ServletException("Cannot initialize user interface handler"
                                        + " specified by property "
                                        + strUIID + " = " + strUIPath);
         }
      }
      
      // When somebody is getting UI rendered, thats a good sign that processing 
      // is done so add error messages to the request    
      hsrqRequest.setAttribute(MessageTag.MESSAGES_REQUEST_PARAM, 
                               CallContext.getInstance().getMessages());
      
      if (hsrqRequest.getParameter(FORCE_MAXIMIZE_PARAM) != null)
      {
         // set up force maximize flag into the request attribute
         hsrqRequest.setAttribute(FORCE_MAXIMIZE_PARAM, "true");
      }
        
      rdReturn.forward(hsrqRequest, hsrpResponse);
      timer.stop();
      s_logger.log(Level.FINE, "Displaying of {0} took {1}", 
                   new Object[]{strUIID, timer.toString()});
   }
   
   // Helper methods for generating pages //////////////////////////////////////

   /**
    * If the user has logged in, this method will set necessary flag to mark
    * the page that way. User is logged in if the session exists and user name 
    * is set as session attribute. If user is not logged in, the flag will be 
    * set to the flag to false.
    *
    * @param  hsrqRequest  - the servlet request.
    * @param  hsrpResponse - the servlet response.
    */
   protected void setLoggedInFlag(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   )
   {
      HttpSession hsSession;
      
      // Don't create new session becaue if the session doesn't exists, user 
      // is not logged in
      hsSession = hsrqRequest.getSession(false);
      
      if (WebSessionUtils.isLoggedIn(hsSession))
      {
         hsrqRequest.setAttribute(LOGGEDIN_REQUEST_PARAM, Boolean.TRUE);
      }
      else
      {
         hsrqRequest.setAttribute(LOGGEDIN_REQUEST_PARAM, Boolean.FALSE);
      }
   }

   /**
    * Display page with a message to the user.
    *
    * @param hsrqRequest  - the servlet request.
    * @param hsrpResponse - the servlet response.
    * @param strPageTitle - title of the message box
    * @param strMessage - message to display in the message box
    * @param strProceedURL - what URL to proceed to
    * @param cause - what was the cause of the problem if any
    * @throws ServletException - an error has occurred while displaying message box
    * @throws IOException - error writing response 
    */
   protected void messageBoxPage(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse,
      String              strPageTitle,
      String              strMessage,
      String              strProceedURL,
      Throwable           cause
   ) throws IOException,
            ServletException
   {
      messageBoxPage(hsrqRequest, 
                     hsrpResponse,
                     strPageTitle,
                     strMessage,
                     strProceedURL,
                     cause,
                     WEBUI_MESSAGEBOX_PAGE);
   }

   /**
    * Display page with a message to the user, when there was a file error.
    *
    * @param hsrqRequest  - the servlet request.
    * @param hsrpResponse - the servlet response.
    * @param strPageTitle - title of the message box
    * @param strMessage - message to display in the file message box
    * @param strProceedURL - what URL to proceed to
    * @param cause - what was the cause of the problem if any
    * @param strUIPage - 
    * @throws ServletException - an error has occurred while displaying file message box
    * @throws IOException - error writing response 
    */
   protected void messageBoxPage(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse,
      String              strPageTitle,
      String              strMessage,
      String              strProceedURL,
      Throwable           cause,
      String              strUIPage
   ) throws IOException,
            ServletException
   {
      if (m_strMessageStyleSheet != null)
      {
         hsrqRequest.setAttribute(WEBUI_MESSAGEBOX_STYLE_SHEET, 
                                  m_strMessageStyleSheet);
      }
      
      // Include the information about the original request in case we want
      // to display it on the error page to allow troubleshooting
      hsrqRequest.setAttribute(ORIGINAL_REQUEST_INFO_REQUEST_PARAM, 
                               WebUtils.debug(hsrqRequest));
      
      // Maybe we should reconsider displaying the same page where user 
      // came from but with message in popup window
      hsrqRequest.setAttribute(DATA_ATTRIBUTE_REQUEST_PARAM, 
                               (strMessage == null) ? "Unknown error has occurred."
                                                    : strMessage);
      hsrqRequest.setAttribute(PAGE_TITLE_REQUEST_PARAM, 
                               (strPageTitle == null) ? "Unknown error has occurred."
                                                      : strPageTitle);
      if (strProceedURL != null)
      {                                                      
         hsrqRequest.setAttribute(DATA_ATTRIBUTE_REQUEST_PARAM + "2", 
                                  strProceedURL);
      }
      else
      {
         hsrqRequest.setAttribute(DATA_ATTRIBUTE_REQUEST_PARAM + "2", 
                                  WebConstants.DEFAULT_DIRECTORY_WEB_PAGE);
      }
      if (cause != null)
      {                                                      
         hsrqRequest.setAttribute(DATA_ATTRIBUTE_REQUEST_PARAM + "3", 
                                  cause);
      }
      displayUI(strUIPage, hsrqRequest, hsrpResponse);
   }

   // Helper methods for processing forms //////////////////////////////////////

   /**
    * Examine request and find out what form needs to be processed. Unique number
    * assigned to the form will be returned
    *
    * @param  hsrqRequest - the servlet request, which is used to find out
    *                       what form needs to be processed
    * @return int - one of the FORM_XXX constants
    */
   protected int getFormToProcess(
      HttpServletRequest hsrqRequest
   )
   {
      return FORM_UNKNOWN_ID;
   }
}

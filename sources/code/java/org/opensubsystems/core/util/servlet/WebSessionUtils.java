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

package org.opensubsystems.core.util.servlet;

import java.security.Principal;

import javax.servlet.http.HttpSession;
import org.opensubsystems.core.util.OSSObject;

/**
 * Collection of useful methods to work with sessions objects.
 *
 * @author bastafidli
 */
public final class WebSessionUtils extends OSSObject
{
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Name of the attribute in the session containing the internal session id.
    * If it exists, then user is logged in.
    */
   public static final String INTERNAL_SESSIONID_SESSION_PARAM 
                                             = "oss.internal.sessionid";

   /**
    * Identification of logged in user.
    */
   public static final String LOGGEDIN_USERID_SESSION_PARAM 
                                             = "oss.loggedin.userid";

   /**
    * Object which tracks when the session is invalidated by the web container.
    */
   public static final String HTTP_SESSION_TRACKER
                                             = "oss.session.tracker";

   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private WebSessionUtils(
   )
   {
      // Do nothing
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * If the user has logged in, this method will return true.
    *
    * @param  hsSession - HTTP session object, can be null
    * @return boolean
    */
   public static boolean isLoggedIn(
      HttpSession hsSession
   )
   {
      return (getSessionId(hsSession) != null);
   }

   /**
    * If the user has logged in, this method will return information about who 
    * is logged in.
    *
    * @param  hsSession - HTTP session object, can be null
    * @return Object - identification of user who is logged in or null if nobody 
    *                  is logged in
    */
   public static Principal getLoggedInUserInfo(
      HttpSession hsSession
   )
   {
      Principal credentials = null;
      
      if (hsSession != null)
      {
         credentials = (Principal)hsSession.getAttribute(LOGGEDIN_USERID_SESSION_PARAM);
      }
      
      return credentials;
   }
   
   /**
    * Get identifier of current user session.
    * 
    * @param hsSession - current HTTP session user is using
    * @return String - user session identifier or null if no user is logged in
    */
   public static String getSessionId(
      HttpSession hsSession
   )
   {
      String sessionId = null;
      
      if (hsSession != null)
      {
         sessionId = (String)hsSession.getAttribute(INTERNAL_SESSIONID_SESSION_PARAM);
      }

      return sessionId;
   }

   /**
    * Set identifier of current user session and user information
    * 
    * @param hsSession - current HTTP session user is using
    * @param sessionId - user session identifier
    * @param credentials - credentials for user
    */
   public static void setSessionAndUserInfo(
      HttpSession hsSession,
      String      sessionId,
      Principal   credentials
   )
   {
      if (hsSession != null)
      {
         hsSession.setAttribute(INTERNAL_SESSIONID_SESSION_PARAM, sessionId);
         hsSession.setAttribute(LOGGEDIN_USERID_SESSION_PARAM, credentials);
      }
   }

   /**
    * Reset identifier of current user session and user information. If the user 
    * is still logged in, this will expire his server session. 
    * 
    * @param hsSession - current HTTP session user is using
    */
   public static void resetSessionAndUserInfo(
      HttpSession hsSession
   )
   {
      hsSession.removeAttribute(INTERNAL_SESSIONID_SESSION_PARAM);
      hsSession.removeAttribute(LOGGEDIN_USERID_SESSION_PARAM);

      // When this object is removed, the session tracker will remove the server 
      // session
      hsSession.removeAttribute(HTTP_SESSION_TRACKER);
   }
}

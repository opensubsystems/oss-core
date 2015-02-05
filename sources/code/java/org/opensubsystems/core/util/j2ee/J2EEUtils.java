/*
 * Copyright (C) 2005 - 2015 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

package org.opensubsystems.core.util.j2ee;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.OSSObject;

/**
 * Collection of useful utilities to work when running in j2ee environment.
 *
 * TODO: Test: This class is missing test.
 * 
 * @author OpenSubsystems
 */
public final class J2EEUtils extends OSSObject
{
   // Constants ////////////////////////////////////////////////////////////////

   public static enum J2EEServers
   {
		/**
		 * Constant defining j2ee server was not yet detected.
		 * This has to be null so we can skip it to get to actual servers.
		 */
		J2EE_SERVER_UNINITIALIZED("Uninitialized", null),
		/**
		 * Constant defining no j2ee server.
		 * This has to be empty string so we can skip it to get to actual servers.
		 */
		J2EE_SERVER_NONE("None", ""),
		/**
		 * Constant defining JOnAS j2ee server.
		 * Constant identifying JOnAS j2ee server. This is parent of the classloader 
		 * because it identifies jonas server. Using just the classloader it would 
		 * identify particular web server identifier (jetty, tomcat, ...).
		 * ClassLoader parent for JOnAS 4.2.3: 
		 *    org.objectweb.jonas_lib.loader.SimpleWebappClassLoader
		 */
		J2EE_SERVER_JONAS("Jonas", ".objectweb.jonas"),
		/**
		 * Constant defining JBoss j2ee server.
		 * Constant identifying JBoss j2ee server.  
		 * ClassLoader parent for JBoss 3.2.6 and 4.0.1: 
		 *    org.jboss.system.server.NoAnnotationURLClassLoader
		 */
		J2EE_SERVER_JBOSS("JBoss", ".jboss."),
		/**
		 * Constant defining WebLogic j2ee server.
		 * Constant identifying WebLogic j2ee server.
		 * ClassLoader parent for BEA WebLogic 7.0 and 8.1: 
		 *    weblogic.utils.classloaders.GenericClassLoader
		 */
		J2EE_SERVER_WEBLOGIC("Weblogic", "weblogic."),
		/**
		 * Constant defining WebSphere j2ee server.
		 * Constant identifying WebSphere j2ee server.
		 * ClassLoader parent for IBM WebSphere 6:
		 *    com.ibm.ws.classloader.JarClassLoader 
		 */
		J2EE_SERVER_WEBSPHERE("Websphere", ".ibm.ws."),
		/**
		 * Constant defining Apache Tomcat servlet container.
		 * Constant identifying Apache Tomcat j2ee server.
		 * ClassLoader parent for Apache Tomcat 8:
		 *    org.apache.catalina.loader.WebappClassLoader 
		 */
		J2EE_SERVER_APACHE_TOMCAT("Tomcat", ".apache.catalina.");

		// Attributes ///////////////////////////////////////////////////////////////

		/**
		 * Name of the server.
		 */
		protected String m_strName;

		/**
		 * Identifier used to detect given server.
		 */
		protected String m_strIdentifier;

		// Logic ////////////////////////////////////////////////////////////////////

		/**
		 * Non public constructor as required by the enum spec.
		 * 
		 * @param strName - name of the server
		 * @param strIdentifier - identifier for the server
		 */
		J2EEServers(
			String strName,
			String strIdentifier
		)
		{
			m_strIdentifier = strIdentifier;
		}

		/**
		 * Get name of the server.
		 * 
		 * @return String
		 */
		public String getName()
		{
			return m_strName;
		}
		
		/**
		 * Get identifier for the given server.
		 * 
		 * @return String
		 */
		public String getIdentifier()
		{
			return m_strIdentifier;
		}
	}

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(J2EEUtils.class);
   
   /**
    * Since J2EE server doesn't changes during execution we can cache the
    * value for detected server.
    */
   private static J2EEServers s_detectedServer = J2EEServers.J2EE_SERVER_UNINITIALIZED;
  
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private J2EEUtils(
   )
   {
      // Do nothing
   }

   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Get the identifier for the current running j2ee server if any.
    * 
    * @return J2EEServers - identifier of the current running j2ee server if any.
	 *								 Never returns null.
    */
   public static J2EEServers getJ2EEServerType(
   )
   {
      // No need to synchronize since in the worst case we execute this 
      // multiple times
      if (s_detectedServer == J2EEServers.J2EE_SERVER_UNINITIALIZED)
      {
         J2EEServers server;
         String		strClassLoader;
   
         strClassLoader = J2EEUtils.class.getClassLoader().getClass().getName();
         server = detectJ2EEServerType(strClassLoader);
         if (server == J2EEServers.J2EE_SERVER_NONE)
         {
            strClassLoader = J2EEUtils.class.getClassLoader().getParent()
										  .getClass().getName();
            server = detectJ2EEServerType(strClassLoader);
         }
         
         s_detectedServer = server;
      }
      

      return s_detectedServer;
   }
   
   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Detect current running j2ee server based on the specified string 
    * because it is server specific.
    * 
    * @param strIdentifier - string which should uniquely identify the AS
    * @return J2EEServers - representation of the current running j2ee server
    */
   private static J2EEServers detectJ2EEServerType(
      String strIdentifier
   )
   {
      J2EEServers server = J2EEServers.J2EE_SERVER_NONE;

      if (strIdentifier != null)
      {
         s_logger.log(Level.FINEST, "Trying to detect J2EE application server"
                      + " using identifier {0}", strIdentifier);

			String strTempIdentifier;
			
			for (J2EEServers tempServer : J2EEServers.values())
			{
				strTempIdentifier = tempServer.getIdentifier();
				// This condition will skip Uninitialized and None values
				if ((strTempIdentifier != null) && (!strTempIdentifier.isEmpty()))
				{
					if (strIdentifier.contains(strTempIdentifier))
					{
						s_logger.log(Level.FINE, "{0} application server detected.", 
										 tempServer.getName());
						server = tempServer;
						break;
					}
					
				}
			}
      }
      else
      {
         s_logger.warning("J2EE application server detectionis not possible" +
								  " since the specified identifier is null.");
      }

      return server;
   }
}

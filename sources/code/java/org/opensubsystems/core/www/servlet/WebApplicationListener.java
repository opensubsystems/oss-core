/*
 * Copyright (C) 2006 - 2015 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.opensubsystems.core.application.Application;
import org.opensubsystems.core.application.Module;
import org.opensubsystems.core.application.ModuleManager;
import org.opensubsystems.core.application.impl.ApplicationImpl;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.Config;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.MultiConfig;
import org.opensubsystems.core.util.PropertyUtils;
import org.opensubsystems.core.util.servlet.WebUtils;
import org.opensubsystems.core.www.WebModule;

/**
 * WebApplicationListener is responsible for initialization of web application  
 * and modules it consists of. It determines what modules are used by this web 
 * application, loads them and initialize them. The modules can be both backend
 * modules as well as gui modules.
 * 
 * This class acts as a "web application launcher" since the method 
 * contextInitialized acts as a main method in a regular Java application and 
 * method contextDestroyed act as a shutdown hook.
 * 
 * The module are specified either  
 * 1. in the configuration files using numbered properties, such as
 * 
 *    oss.module.0=org.opensubsystems.security.www.SecurityBackendModule
 *    oss.module.1=org.opensubsystems.security.www.SecurityWebModule
 *    oss.module.2=full classname for module 2
 *    ...
 * 
 * 2. or in the web.xml using context parameters, such as
 * 
 *    <context-param>
 *      <param-name>oss.module.0</param-name>
 *      <param-value>org.opensubsystems.security.www.SecurityBackendModule</param-value>
 *    </context-param>      
 *    <context-param>
 *      <param-name>oss.module.1</param-name>
 *      <param-value>org.opensubsystems.security.www.SecurityWebModule</param-value>
 *    </context-param>      
 *    <context-param>
 *      <param-name>oss.module.1.url</param-name>
 *      <param-value>URL to which the sSecurityWebModule maps</param-value>
 *    </context-param>      
 *    <context-param>
 *      <param-name>oss.module.2</param-name>
 *      <param-value>full classname for module 2</param-value>
 *    </context-param>
 *    ...
 *
 * @author OpenSubsystems
 */
public class WebApplicationListener implements ServletContextListener
{   
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(WebApplicationListener.class);

   // Configuration parameters /////////////////////////////////////////////////
   
   /**
    * This is used to name parameters such as oss.module.0.url, 
    * oss.module.1.url and they are used for defining URL related
    * to particular web module.
    */
   public static final String WEBCLIENT_MODULE_URL_PREFIX = ".url"; 

   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   @Override
   public void contextInitialized(
      ServletContextEvent servletContextEvent
   )
   {      
      s_logger.entering(this.getClass().getName(), "contextInitialized");
      try
      {
         
         // Do this when context is created to that the application is aware
         // what access rights are available for each module
         Properties    prpSettings;
         String        strModuleClassName;
         String        strModuleURL;
         int           iIndex = 0;
         Application   webApp;
         Module        module;
         WebModule     webModule;
         ModuleManager manager;
         
         prpSettings = setupConfiguration(servletContextEvent);
         webApp = ApplicationImpl.getInstance();
         manager = ModuleManager.getManagerInstance();
         do
         {
            // Read module name
            strModuleClassName = PropertyUtils.getStringProperty(
               prpSettings, ApplicationImpl.APPLICATION_MODULE_PREFIX + iIndex, 
               null, "Class name of application module #" + iIndex, true); 
            if ((strModuleClassName != null) 
               && (strModuleClassName.length() > 0))
            {                            
               s_logger.log(Level.FINE, "Read module name {0}", strModuleClassName);
               module = manager.getModuleInstance(strModuleClassName);
               s_logger.log(Level.FINE, "Instantiated module {0}", strModuleClassName);

               if (module instanceof WebModule)
               {
                  webModule = (WebModule)module;
                  // Read web module URL that can be used to switch to that web
                  // module on the gui
                  strModuleURL = PropertyUtils.getStringProperty(
                     prpSettings, 
                     ApplicationImpl.APPLICATION_MODULE_PREFIX + iIndex 
                     + WEBCLIENT_MODULE_URL_PREFIX, 
                     null, "URL of application module #" + iIndex, true);
                  if ((strModuleURL != null) && (strModuleURL.length() > 0))
                  {
                     s_logger.log(Level.FINE,"Read web module " 
                                  + ApplicationImpl.APPLICATION_MODULE_PREFIX + "{0}" 
                                  + WEBCLIENT_MODULE_URL_PREFIX + " URL {1}", 
                                  new Object[]{iIndex, strModuleURL});
                     // set up web module URL into the module definition
                     webModule.setURL(strModuleURL);
                  }
               }
               webApp.add(module);
               iIndex++;                                    
            }
         }
         while ((strModuleClassName != null) 
               && (strModuleClassName.length() > 0));
         
         ApplicationImpl.startApplication(webApp);
      }
      catch (OSSException ossExc)
      {
         // No way to throw checked exception so convert it to unchecked 
         s_logger.log(Level.SEVERE, "Unexpected exception.", ossExc);
         throw new RuntimeException("Unexpected exception.", ossExc);
      }
      // This is here just so we get a log about the exception since the 
      // web server may not print it out
      catch (Throwable thr)
      {
         // No way to throw checked exception so convert it to unchecked 
         s_logger.log(Level.SEVERE, "Unexpected exception.", thr);
         throw new RuntimeException("Unexpected exception.", thr);         
      }
      finally
      {
         s_logger.exiting(this.getClass().getName(), "contextInitialized");
      }
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void contextDestroyed(
      ServletContextEvent servletContextEvent
   )
   {
      s_logger.entering(this.getClass().getName(), "contextDestroyed");
      try
      {
         // The application has finished or has crashed so gracefully stop it 
         Application webApp;
         
         webApp = ApplicationImpl.getInstance();
         if (webApp != null)
         {
            ApplicationImpl.stopApplication(webApp);
         }
      }
      catch (OSSException ossExc)
      {
         // No way to throw checked exception so convert it to unchecked 
         s_logger.log(Level.SEVERE, "Unexpected exception.", ossExc);
         throw new RuntimeException("Unexpected exception.", ossExc);
      }
      // This is here just so we get a log about the exception since the 
      // web server may not print it out
      catch (Throwable thr)
      {
         // No way to throw checked exception so convert it to unchecked 
         s_logger.log(Level.SEVERE, "Unexpected exception.", thr);
         throw new RuntimeException("Unexpected exception.", thr);         
      }
      finally
      {
         s_logger.exiting(this.getClass().getName(), "contextDestroyed");
      }
   }   
   
   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Setup configuration of the application that should be used by all the 
    * classes in the application.
    * 
    * @param servletContextEvent - event send during application initialization
    * @return Properties - properties to use to configure the application
    */
   protected Properties setupConfiguration(
      ServletContextEvent servletContextEvent
   )
   {
      MultiConfig    config;
      ServletContext scContext;
      Properties     contextProperties;
      Properties     applicationProperties;
      
      // Setup web config as the default config for the web applications
      // This will give us easy access to all properties specified using
      // init parameters in web application, servlet and filter definitions
      // as well as within the regular property file
      // using the same APIs as the regular properties  in the config file
      // THIS HAS TO BE THE FIRST THING WE DO IN CASE OTHER CLASSES WANTS
      // TO READ PROPERTIES THAT CAN BE SPECIFIED ONLY IN THE WEB APPLICATION
      // CONFIGURATION FILES
      scContext = servletContextEvent.getServletContext();
      contextProperties = WebUtils.getInitParameters(scContext);
      config = new MultiConfig(contextProperties);
      Config.setInstance(config);
      applicationProperties = config.getProperties();
      
      return applicationProperties;
   }
}

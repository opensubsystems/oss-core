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

package org.opensubsystems.core.application.impl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensubsystems.core.application.Application;
import org.opensubsystems.core.application.Module;
import org.opensubsystems.core.application.ProductInfo;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.ClassFactory;
import org.opensubsystems.core.util.Config;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.ImplementationClassFactory;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.OSSObject;

/**
 * Base class for all applications establishing lifecycle of the application. 
 * The derived classes should just declare their own main method, construct the
 * derived Application object and call startApplication method:
 * 
 *    ExampleApplication application;
 *              
 *    application = new ExampleApplication();
 *    startApplication(application);
 *     
 * startApplication method will ensure that the init() and start() of the 
 * application are correctly invoked. When the application terminates by 
 * returning from the start() method without leaving any threads running or when 
 * the last thread is terminated, the application can be stopped using
 * 
 *    stopApplication(application);
 *
 * This method ensures that the application lifecycle is properly finished by 
 * calling stop() method.
 *
 * @author bastafidli
 */
public class ApplicationImpl extends OSSObject
                             implements Application
{
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * Lock used in synchronized sections.
    */
   private static final String IMPL_LOCK = "IMPL_LOCK";

   // Configuration parameters /////////////////////////////////////////////////
   
   /**
    * This is used to name parameters such as oss.module.0, oss.module.1 which 
    * specify what modules the application consists of. The modules are 
    * initialized in the numeric order specified in the configuration setting 
    * name.
    */
   public static final String APPLICATION_MODULE_PREFIX = "oss.module."; 
   
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Information about current product represented by this application.
    */
   protected static ProductInfo m_currentProduct = null;
   
   /**
    * This flag is true if the application is started.
    */
   protected boolean m_bStarted;
   
   /**
    * This flag is true if the application restart is required.
    */
   protected boolean m_bRestartRequired;
   
   /**
    * Map of all modules. Key is a module name, value is a Module object.
    */   
   protected Map<String, Module> m_mpModules;
   
   /**
    * Factory used to dynamically instantiate application instances based on  
    * what technology and what environment is this application running in.
    * For example the thick client application factory can instantiate different 
    * application instance based on the GUI toolkit that is used to display the 
    * GUI of the application. J2EE application factory can instantiate different
    * application instance based on the J2EE server that is used to run the  
    * application.
    */
   protected static ClassFactory<Application> s_applicationFactory;
   
   /**
    * The default application class to construct if there is no other class 
    * specified in the configuration file using setting 
    * [Application.class]=new class to use.
    */
   protected static Class<Application> s_clsDefaultApplicationClass;
   
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(ApplicationImpl.class);

   /**
    * Reference to the instance actually in use.
    */
   private static Application s_defaultInstance;
   
   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Static constructor
    */
   static 
   {
      s_applicationFactory = new ImplementationClassFactory<>(Application.class);
      s_clsDefaultApplicationClass = Application.class;
   }
   
   /**
    * Create new instance of the application. The initialization should be done 
    * in a separate step so we can have more control over error handling.
    * 
    */
   public ApplicationImpl(
   )
   {
      this(new ProductInfoImpl("Generic OpenSubsystems application", "1.0", 
                               "OpenSubsystems.com",
                               "Copyright (c) 2003 - 2013 OpenSubsystems.com."
                               + " All rights reserved.")
          );
   }

   /**
    * Create new instance of the application. The initialization should be done 
    * in a separate step so we can have more control over error handling.
    * 
    * @param product - information about product which is running this 
    *                  application, this way we will force every application to 
    *                  create and publish this information in uniform way
    */
   public ApplicationImpl(
      ProductInfo product
   ) 
   {
      m_bStarted           = false;
      m_bRestartRequired   = false;
      
      m_currentProduct = product;
      // We need go use LinkedHashMap so that we maintain the orde in which
      // the modules were specified in the configuration file
      m_mpModules = new LinkedHashMap<>();
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Set application factory that should be used to construct application
    * instances.
    *
    * @param applicationFactory - factory that should be use to create instances
    *                             of application objects
    * @param cldDefaultApplicationClass - default application class to construct
    *                                     if there is no other class specified
    *                                     in the configuration file using setting
    *                                     [DefaultApplication.class]=new class to use.
    * @see #getInstance
    */
   public static void setApplicationFactory(
      ClassFactory<Application> applicationFactory,
      Class<Application>        cldDefaultApplicationClass
   )
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert applicationFactory != null : "Application factory cannot be null";
         assert cldDefaultApplicationClass != null 
                : "Default application class cannot be null";
      }   
      
      s_applicationFactory = applicationFactory;
      s_clsDefaultApplicationClass = cldDefaultApplicationClass; 
   }

   /**
    * Get the default instance. This method is here to make the application
    * configurable. Once can specify in configuration file derived class to used
    * instead of this one [Application.class]=new class to use.
    *
    * TODO: Improve: Review this decision and try to unify all object creation
    *                (setInstance/getInstance/managers/etc.)
    * Note: The intention is that there is only one application object instance
    *       in the context of the process (even though nothing in the code 
    *       enforces since we do not know what kind of application the developer 
    *       will develop using this framework) therefore we do not provide any 
    *       kind of "manager" class to create  instances of application.
    *
    * @return Application - application that is currently running in the system
    * @throws OSSException - cannot get current database
    */
   public static Application getInstance(
   ) throws OSSException
   {
      if (s_defaultInstance == null)
      {
         // Only if the default instance wasn't set by other means create a new one
         // Synchronize just for the creation
         synchronized (IMPL_LOCK)
         {
            if (s_defaultInstance == null)
            {
               setInstance(s_applicationFactory.createInstance(
                              s_clsDefaultApplicationClass,
                              s_clsDefaultApplicationClass));
            }
         }   
      }
      
      return s_defaultInstance;
   }
   
   /**
    * Set default instance. This instance will be returned by getInstance 
    * method until it is changed.
    *
    * @param defaultInstance - new default instance
    * @see #getInstance
    */
   public static void setInstance(
      Application defaultInstance
   )
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert defaultInstance != null : "Default instance cannot be null";
      }   
      
      synchronized (IMPL_LOCK)
      {
         s_defaultInstance = defaultInstance;
         s_logger.log(Level.FINE, "Default application is {0}", 
                      s_defaultInstance.getClass().getName());
      }   
   }

   /**
    * Start specified application. The functionality to start application was 
    * separated to this method so that derived classes can pass different kinds 
    * of applications to start while still taking advantage of the generic 
    * functionality, such as correct initialization, support for restarts, JVM
    * shutdown hook, etc. This method never throws any throwable.
    *
    * @param app - application to start
    */
   public static void startApplication(
      final Application app
   )
   {
      // Set the shutdown hook so we can stop the application gracefully
      // This code is inspired by Jetty 1.4 class org.mortbay.jetty.Server
      // Install it as a first thing since if the start method is overridden 
      // for thick client, it may never return and we wouldn't have chance
      // to install this hook
      try
      {
         // TODO: Improve: Investigate how the shutdown hook would work if
         // multiple applications are started in the same JVM
         Method shutdownHook = Runtime.class.getMethod("addShutdownHook",
                                                       new Class[] {Thread.class});
                     
         Thread thrdHook = new Thread() 
                           {
                              @Override
                              public void run()
                              {
                                 // If the server was not stopped by other 
                                 // means and it is still started, then stop it
                                 if (app.isStarted())
                                 {
                                    stopApplication(app);
   
                                    try
                                    {
                                       Thread.sleep(1000);
                                    }
                                    catch (Throwable throwable)
                                    {
                                       s_logger.log(Level.WARNING, 
                                                    "Unexpected exception.", 
                                                    throwable);
                                    }
                                 }
                              }
                           };
          shutdownHook.invoke(Runtime.getRuntime(), new Object[]{thrdHook});
      }
      catch (Throwable throwable)
      {
         s_logger.log(Level.WARNING, "No shutdown hook in JVM.", throwable);
      }      
      
      boolean bStartSuccessfull = false;
      
      do
      {
         try
         {
            s_logger.info("Initializing application.");
            app.init();
            s_logger.info("Starting application.");
            try
            {
               app.start();
               // Do not print anything here since for example GUI applications
               // never come out of start until they are shutdown
               bStartSuccessfull = true;
            }
            finally
            {
               if (!bStartSuccessfull)
               {
                  // If the application didn't started successfully, we need to 
                  // destroy it to clean the memory
                 s_logger.log(Level.FINEST, "Stopping the unsuccessful application.");
                 stopApplication(app);
               }
            }
         }
         catch (Throwable thr)
         {
            s_logger.log(
               Level.WARNING, 
               "Unexpected exception has occurred while starting application.", 
               thr);
         }
      }
      while ((!bStartSuccessfull) && (app.isRestartRequired()));
   }

   /**
    * Stop the specified application. This method never throws any throwable.
    *
    * @param app - server to stop
    */
   public static void stopApplication(
      Application app
   )
   {
      s_logger.info("Application is stopping.");
      if (app != null)
      {
         try
         {
           app.stop();
         }
         catch (Throwable thr)
         {
            s_logger.log(
               Level.WARNING, 
               "Unexpected exception has occurred while stopping application.", 
               thr);
         }
      }
      s_logger.info("Application is stopped.");
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public ProductInfo getCurrentProduct()
   {
      return m_currentProduct;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void setCurrentProduct(
      ProductInfo product
   )
   {
      m_currentProduct = product;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void init(
   ) throws OSSException, 
            IOException
   {
      if ((m_mpModules != null) && (!m_mpModules.isEmpty()))
      {
         Module       module;
         Properties   moduleProperties;
         List<Module> lstModules = new ArrayList<>(m_mpModules.values());
         
         // Define the properties required by the modules. Define them in a 
         // reverse order of the module dependency, since the module dependent
         // on other modules may need to redefine some properties of the modules
         // it depends on
         Collections.reverse(lstModules);
         for (Iterator<Module> itModules = lstModules.iterator(); itModules.hasNext();)
         {
            module = itModules.next();

            moduleProperties = module.getPropertiesToDefine();
            if ((moduleProperties != null) && (!moduleProperties.isEmpty()))
            {
               Config.getInstance().defineUndefinedProperties(moduleProperties);
            }
         }
         
         // Go through all definitions and initialize them
         for (Iterator<Module> itModules = m_mpModules.values().iterator();
             itModules.hasNext();)
         {
            module = itModules.next();
            initializeModule(module);
         }
      }
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void start(
   ) throws OSSException
   {
      m_bStarted = true;
      m_bRestartRequired = false;
      s_logger.info("Application has started.");
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void stop(
   ) throws OSSException
   {
      m_bStarted = false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isStarted(
   )
   {
      return m_bStarted;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isRestartRequired()
   {
      return m_bRestartRequired;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void setRestartRequired(
      boolean bRestartRequired
   )
   {
      m_bRestartRequired = bRestartRequired;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public Map<String, Module> getModules() 
   {
      return m_mpModules;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void add(
      Module module
   ) throws OSSException
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert module != null : "Cannot add null module.";
      }
      
      Module existingModule;
      
      existingModule = m_mpModules.get(module.getName());
      if (existingModule != null)
      {
         s_logger.log(Level.FINEST, "Module {0} already exists in the application."
                      + " It is not added second time.", module.getName());
      }
      else
      {
         Module[] arModules;
         
         arModules = module.getDependentModules();
         // TODO: Feature: Add module circular dependency check here
         if (arModules != null)
         {
            for (int iIndex = 0; iIndex < arModules.length; iIndex++)
            {
               add(arModules[iIndex]);
            }
         }
         
         addDirectly(module);
      }
   }  
   
   // Helper methods ///////////////////////////////////////////////////////////

   /**
    * Add new module to the application without checking or adding any 
    * dependencies or checking if the module exist.
    * 
    * @param module - module to add.
    * @throws OSSException - an error has occurred
    */
   protected void addDirectly(
      Module module
   ) throws OSSException
   {
      m_mpModules.put(module.getName(), module);
      s_logger.log(Level.FINEST, "Module {0} added to the application.", 
                   module.getName());
   }
   
   /**
    * Initialize the module when the application is being initialized.
    *  
    * @param module - module to initialize.
    * @throws OSSException - an error has occurred
    */
   protected void initializeModule(
      Module module
   ) throws OSSException
   {
      module.init();
   }
}

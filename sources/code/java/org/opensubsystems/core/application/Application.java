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

package org.opensubsystems.core.application;

import java.io.IOException;
import java.util.Map;

import org.opensubsystems.core.error.OSSException;

/**
 * Interface representing application consisting of multiple modules, that 
 * establishes lifecycle of the application. 
 * 
 * The lifecycle of the application is
 *    1. constructor 
 *    2. init()
 *    3. start()
 *    4. stop()
 * 
 * @author bastafidli
 */
public interface Application
{
   /**
    * Initialize the application. All initialization should be done in this 
    * method so that we can call it repetitively possibly after some 
    * configuration parameters were changed parameters if the initialization 
    * fails. The initialization should succeed only if the the application is 
    * fully capable and ready to run. 
    *
    * @throws OSSException - an error has occurred
    */
   void init(
   ) throws OSSException, 
            IOException;
   
   /**
    * Start the application.
    *
    * @throws OSSException - an error has occurred
    */
   void start(
   ) throws OSSException;

   /**
    * Stop the application.
    *
    * @throws OSSException - an error has occurred
    */
   void stop(
   ) throws OSSException;

   /**
    * Test if the application was started.
    * 
    * @return boolean - true if started
    */
   boolean isStarted(
   );

   /**
    * Check if the application should be restarted because it may have failed
    * while it was being previously started. 
    * 
    * @return boolean - true if the application should be restarted.
    */
   boolean isRestartRequired(
   );
   
   /**
    * Set flag indicating if the application needs to be restarted. This method
    * doesn't actually restarts or forces restart of the application, just sets 
    * the flag. 
    * 
    * @param bRestartRequired - if true then application should be restarted.
    */
   void setRestartRequired(
      boolean bRestartRequired
   );
   
   /**
    * Get modules that this application consists of.
    * 
    * @return Map
    */
   public Map<String, Module> getModules(
   ); 

   /**
    * Add new module to the application. This method adds all dependent modules
    * before the actual module is added. All the modules are added only  if they
    * were not already added.
    * 
    * @param module - module to add.
    * @throws OSSException - an error has occurred
    */
   void add(
      Module module
   ) throws OSSException;
   
   /**
    * Get information about product this application represents. The same 
    * application may be part of several products so this identifies the 
    * currently used product. 
    * 
    * @return ProductInfo - information about current product represented by 
    *                       this application
    */
   ProductInfo getCurrentProduct(
   );
   
   /**
    * Set information about product this application represents. The same 
    * application may be part of several products so this identifies the 
    * currently used product. 
    * 
    * @param product - information about current product represented by this 
    *                  application
    */
   void setCurrentProduct(
      ProductInfo product
   );
}

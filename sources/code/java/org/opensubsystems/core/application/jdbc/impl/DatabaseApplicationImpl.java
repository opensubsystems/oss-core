/*
 * Copyright (C) 2010 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

package org.opensubsystems.core.application.jdbc.impl;

import java.util.logging.Logger;

import org.opensubsystems.core.application.Module;
import org.opensubsystems.core.application.ProductInfo;
import org.opensubsystems.core.application.impl.ApplicationImpl;
import org.opensubsystems.core.application.jdbc.BackendDatabaseModule;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.jdbc.Database;
import org.opensubsystems.core.persist.jdbc.DatabaseSchema;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseImpl;
import org.opensubsystems.core.util.Log;

/**
 * Base class for all applications directly connecting to a database. This class
 * establishes lifecycle of the application. The derived classes should just 
 * declare their own main method, construct the derived Application object and 
 * call startApplication method:
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
public class DatabaseApplicationImpl extends ApplicationImpl
{
   // Attributes ///////////////////////////////////////////////////////////////

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(DatabaseApplicationImpl.class);
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Create new instance of the application. The initialization should be done 
    * in a separate step so we can have more control over error handling.
    * 
    * @param product - information about product which is running this 
    *                  application, this way we will force every application to 
    *                  create and publish this information in uniform way
    */
   public DatabaseApplicationImpl(
      ProductInfo product
   ) 
   {
      super(product);
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void start(
   ) throws OSSException
   {
      // Since the application contains some database modules start the 
      // database that the modules are using
      s_logger.fine("Starting default database.");        
      Database dbDatabase;
         
      dbDatabase = DatabaseImpl.getInstance();
      dbDatabase.start();         
      s_logger.fine("Default database started.");        

      super.start();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void stop(
   ) throws OSSException
   {
      s_logger.fine("Stopping default database.");        
      Database dbDatabase;
         
      dbDatabase = DatabaseImpl.getInstanceIfStarted();
      if (dbDatabase != null)
      {
         dbDatabase.stop();
      }
      s_logger.fine("Default database stopped.");
      
      super.stop();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void initializeModule(
      Module module
   ) throws OSSException
   {
      super.initializeModule(module);
      
      if (module instanceof BackendDatabaseModule)
      {
         BackendDatabaseModule dbModule;
         DatabaseSchema[]      arrSchemas;
         
         dbModule = (BackendDatabaseModule)module;
         arrSchemas = dbModule.getSchemas();
         if ((arrSchemas != null) && (arrSchemas.length > 0))
         {
            Database       database;
            DatabaseSchema schema;
            
            database = DatabaseImpl.getInstance();
            for (int iIndex = 0; iIndex < arrSchemas.length; iIndex++)
            {
               schema = arrSchemas[iIndex];
               database.add(schema);
            }
         }
      }
   }
}

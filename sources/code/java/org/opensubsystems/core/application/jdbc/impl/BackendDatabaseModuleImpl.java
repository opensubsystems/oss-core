/*
 * Copyright (C) 2007 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import org.opensubsystems.core.application.Module;
import org.opensubsystems.core.application.impl.BackendModuleImpl;
import org.opensubsystems.core.application.jdbc.BackendDatabaseModule;
import org.opensubsystems.core.data.DataDescriptor;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.logic.StatelessController;
import org.opensubsystems.core.persist.DataFactory;
import org.opensubsystems.core.persist.jdbc.DatabaseSchema;
import org.opensubsystems.core.util.GlobalConstants;

/**
 * Module interface implementation suitable for representing backend modules 
 * exposing their business logic and data access layer that utilizes database 
 * for the persistence mechanism but doesn't exposes any details about the user 
 * interface.   
 * 
 * Note: The derived methods have to call initModule method in their init 
 * implementation to ensure that the module was properly initialized.
 * 
 * @author bastafidli
 */
public abstract class BackendDatabaseModuleImpl extends    BackendModuleImpl 
                                                implements BackendDatabaseModule
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Flag specifying if the module was correctly initialized.
    */
   private boolean m_bBackendDatabaseModuleInitialized = false; 
   
   /**
    * Instances of all data schemas provided by this module.
    */
   protected DatabaseSchema[] m_arrSchemas;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Full constructor
    * 
    * @param strName - user friendly unique name of the module
    * @param iVersion - version of the module
    * @param arrDependentModules - list of application modules, which this 
    *                              module depends on
    * @param arrPropertiesToDefine - properties that should be set in the config
    *                                in case they are not set yet either in the 
    *                                configuration file or in the module that 
    *                                depends on this one. The properties in 
    *                                the parent module are processed only after  
    *                                the properties in module that depend on it 
    *                                in case the child modules modifies the 
    *                                behavior of the parent module. 
    *                                This is an array of string pairs. The first
    *                                value of each pair is the name of the 
    *                                property which is it is not defined yet, 
    *                                will be set to the value of the second 
    *                                string in the pair.
    */
   public BackendDatabaseModuleImpl(
      String     strName,
      int        iVersion,
      Module[]   arrDependentModules,
      String[][] arrPropertiesToDefine
   )
   {
      super(strName, iVersion, arrDependentModules, arrPropertiesToDefine);
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isInitialized()
   {
      return (m_bBackendDatabaseModuleInitialized) && (super.isInitialized());
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public DatabaseSchema[] getSchemas()
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert isInitialized() 
                : "Module is not propertly initialized by calling initModule()";
      }
      
      return m_arrSchemas;
   }

   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Initialize the module. This method must be called from the init method
    * to ensure that the module is properly initialized. 
    * 
    * @param arrDataDescriptors - descriptors for all data objects provided by 
    *                             this module                              
    * @param arrSchemas - instances of database schemas provided by this module
    * @param arrFactories - instances of data factories provided by this module
    * @param arrControllers - instances of controllers provided by this module
    * @throws OSSException - an error has occurred
    */
   protected void initModule(
      DataDescriptor[]      arrDataDescriptors,
      StatelessController[] arrControllers,
      DataFactory[]         arrFactories,
      DatabaseSchema[]      arrSchemas
   ) throws OSSException
   {
      
      m_arrSchemas = arrSchemas;

      initModule(arrDataDescriptors, arrControllers, arrFactories);
      
      m_bBackendDatabaseModuleInitialized = true;
   }
}

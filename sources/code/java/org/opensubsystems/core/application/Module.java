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

package org.opensubsystems.core.application;

import java.util.Properties;

import org.opensubsystems.core.data.DataDescriptor;
import org.opensubsystems.core.error.OSSException;

/**
 * Interface representing one module of an application. 
 * 
 * The lifecycle of the module is
 * 
 *    1. constructor
 *    2. init()
 *    
 * @author bastafidli
 */
public interface Module
{
   /**
    * Initialize the module. This is to separate construction from the 
    * initialization of the module instance. The module should leave any kind
    * of initialization, reading of properties, etc. for initialization since
    * this give application chance to adjust the runtime environment after
    * the module was constructed based on the properties that should be defined.
    * 
    * @throws OSSException - an error has occurred
    */
   void init(
   ) throws OSSException;
   
   /**
    * Get user friendly UNIQUE name of the module. This name must never change
    * through out the lifetime of the application.
    * 
    * @return String - module name
    */
   String getName();

   /**
    * Get version of the module, which distinguishes various revisions.
    * 
    * @return int - module version
    */
   int getVersion();
   
   /**
    * Get list of application modules, which this module depends on. These 
    * modules will be processed before this module is processed. This way if 
    * this modules has relationships that require the other modules to be 
    * present and ready, they are guaranteed to exist before this module is 
    * processed.
    * 
    * @return DatabaseSchema[] - array of DatabaseSchema instances  
    */
   Module[] getDependentModules();
   
   /**
    * Properties that should be set in the config in case they are not set yet 
    * either in the configuration file or in the module that depends on this 
    * one. The properties in the parent module are processed only after the 
    * properties in module that depend on it in case the child modules modifies 
    * the behavior of the parent module.
    * 
    * @return Properties - properties to define if they are not define yet. Can
    *                      be null, if there are no properties to define.
    */
   Properties getPropertiesToDefine();
   
   /**
    * Get list of descriptors for all data objects provided by this module 
    * provides. This way we can determine set of all data object types in the 
    * system and resolve any conflicts that may exist between them.
    * 
    * @return DataObject[] - array of DataObject instances  
    */
   DataDescriptor[] getDataDescriptors();
}

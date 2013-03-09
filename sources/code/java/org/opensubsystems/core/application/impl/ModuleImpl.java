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

package org.opensubsystems.core.application.impl;

import java.util.Properties;

import org.opensubsystems.core.application.Module;
import org.opensubsystems.core.data.DataDescriptor;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.OSSObject;

/**
 * Simple implementation of interface representing one module of the application. 
 *
 * Note: The derived methods have to call initModule method in their init 
 * implementation to ensure that the module was properly initialized.
 * 
 * @author bastafidli
 */
public abstract class ModuleImpl extends OSSObject
                                 implements Module
{
   // Attributes ///////////////////////////////////////////////////////////////

   /**
    * Flag specifying if the module was correctly initialized.
    */
   private boolean m_bModuleInitialized = false; 
   
   /**
    * Properties that should be set in the config in case they are not set yet 
    * either in the configuration file or in the module that depends on this 
    * one. The properties in the parent module are processed only after the 
    * properties in module that depend on it in case the child modules modifies 
    * the behavior of the parent module. Can be null, if there are no properties 
    * to define.
    */
   protected Properties m_propertiesToDefine;
   
   /**
    * User friendly UNIQUE name of the module.
    */
   protected String m_strName;
   
   /**
    * Version of the module, which distinguishes various revisions.
    */
   protected int m_iVersion;
   
   /**
    * List of application modules, which this module depends on.
    */
   protected Module[] m_arrDependentModules;
   
   /**
    * List of descriptors for all data objects provided by this module provides.
    */
   protected DataDescriptor[] m_arrDataDescriptors;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor. This is to separate construction from initialization from 
    * construction of the module instance to ensure that we have chance to 
    * overwrite the default values of properties that may be needed for 
    * initialization. The derived class needs to implement method init() in 
    * which it calls the initModule method. 
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
    *                                string in the pair. Can be null, if there 
    *                                are no properties to define.
    */
   public ModuleImpl(
      String     strName,
      int        iVersion,
      Module[]   arrDependentModules,
      String[][] arrPropertiesToDefine
   )
   {
      m_strName              = strName;
      m_iVersion             = iVersion;
      m_arrDependentModules  = arrDependentModules;
      
      if ((arrPropertiesToDefine != null) && (arrPropertiesToDefine.length > 0))
      {
         m_propertiesToDefine = new Properties();
         for (int iIndex = 0; iIndex < arrPropertiesToDefine.length; iIndex++)
         {
            if (GlobalConstants.ERROR_CHECKING)
            {
               assert (arrPropertiesToDefine[iIndex] != null)
                      && (arrPropertiesToDefine.length == 2)
                      : "The properties to define must be pairs of two strings"; 
            }
            
            m_propertiesToDefine.put(arrPropertiesToDefine[0], 
                                     arrPropertiesToDefine[1]);
         }
      }
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   public String getName()
   {
      return m_strName;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public int getVersion()
   {
      return m_iVersion;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public Module[] getDependentModules()
   {
      return m_arrDependentModules;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Properties getPropertiesToDefine()
   {
      return m_propertiesToDefine;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public DataDescriptor[] getDataDescriptors()
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert isInitialized() 
                : "Module is not propertly initialized by calling initModule()";
      }
      
      return m_arrDataDescriptors;
   }
   
   /**
    * @return boolean - true if the module was properly initialized initModule. 
    */
   public boolean isInitialized()
   {
      return m_bModuleInitialized;
   }
   
   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Initialize the module. This method must be called from the init method
    * to ensure that the module is properly initialized. 
    * 
    * @param arrDataDescriptors - descriptors for all data objects provided by 
    *                             this module                              
    * @throws OSSException - an error has occurred
    */
   protected void initModule(
      DataDescriptor[] arrDataDescriptors
   ) throws OSSException
   {
      m_arrDataDescriptors   = arrDataDescriptors;
      m_bModuleInitialized = true;
   }
}

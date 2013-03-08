/*
 * Copyright (C) 2006 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.map.LinkedMap;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.ClassFactory;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.OSSObject;

/**
 * Class responsible for instantiation of application modules. This class 
 * determines what module should be used, creates the module instance if it 
 * wasn't created yet and caches created instances. This of course assumes that 
 * the modules are implemented to be stateless and reentrant.
 * 
 * @author OpenSubsystems
 */
//TODO: JDK 1.5: All Manager classes could be refactored using template classes 
public class ModuleManager extends OSSObject
{
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Lock used in synchronized sections.
    */
   private static final String IMPL_LOCK = "IMPL_LOCK";
   
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(ModuleManager.class);
  
   /**
    * Reference to the instance actually in use.
    */
   private static ModuleManager s_defaultInstance;

   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Class factory used to instantiate application modules.
    */
   protected ClassFactory<Module> m_moduleClassFactory;
   
   /**
    * Cache where already instantiated modules will be cached. We can cache them 
    * since modules should be reentrant.
    */
   private final Map<String, Module> m_mpModuleCache; 
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Default constructor
    */
   public ModuleManager() 
   {
      m_moduleClassFactory = new ClassFactory<>(Module.class);
      // This has to be sequenced hashmap to create the modules in the correct 
      // order
      // TODO: Modify it to template class once available
      m_mpModuleCache = new LinkedMap();
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Create module for specified class.
    * 
    * @param clsModule - the module class for which we want applicable module 
    * @return Module - the module to use for given class
    * @throws OSSException - an error has occurred 
    */
   public static Module getInstance(
      Class<Module> clsModule
   ) throws OSSException
   {
      return getManagerInstance().getModuleInstance(clsModule);
   }
   
   /**
    * Create module for specified class.
    * 
    * @param module - the module class name for which we want applicable module  
    * @return Module - the module to use for given class
    * @throws OSSException - an error has occurred 
    */
   public static Module getInstance(
      String module
   ) throws OSSException
   {
      return getManagerInstance().getModuleInstance(module);
   }
   
   /**
    * Get the default instance. This method is here to make the manager
    * configurable. Once can specify in configuration file derived class to used
    * instead of this one [ModuleManager.class]=new class to use.
    * 
    * @return ModuleManager - default instance
    * @throws OSSException - an error has occurred
    */
   public static ModuleManager getManagerInstance(
   ) throws OSSException
   {
      if (s_defaultInstance == null)
      {
         synchronized (IMPL_LOCK)
         {
            if (s_defaultInstance == null)
            {
               Class<ModuleManager> defaultManager = ModuleManager.class;
               ClassFactory<ModuleManager> cf;
               
               cf = new ClassFactory<>(ModuleManager.class);
               setManagerInstance(cf.createInstance(defaultManager, 
                                                    defaultManager));
            }   
         }
         
      }
      return s_defaultInstance;
   }

   /**
    * Set default instance. This instance will be returned by getManagerInstance 
    * method until it is changed.
    *
    * @param defaultInstance - new default instance
    * @see #getManagerInstance
    */
   public static void setManagerInstance(
      ModuleManager defaultInstance
   )
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert defaultInstance != null : "Default instance cannot be null";
      }   
      
      synchronized (IMPL_LOCK)
      {
         s_defaultInstance = defaultInstance;
         s_logger.log(Level.FINE, "Default module manager is {0}", 
                      s_defaultInstance.getClass().getName());
      }   
   }
   
   /**
    * Method to create actual module based on specified class. This method can
    * be overridden and new manager can be setup either through method 
    * setManagerInstance or through configuration file if different strategy is 
    * desired.
    * 
    * @param clsModule - the module class name for which we want applicable 
    *                    module  
    * @return Module - the module to use for given class
    * @throws OSSException - an error has occurred
    */
   public Module getModuleInstance(
      Class<Module> clsModule
   ) throws OSSException
   {
      Module module;
      
      module = m_mpModuleCache.get(clsModule.getName());
      if (module == null)
      {
         synchronized (m_mpModuleCache)
         {
            module = m_moduleClassFactory.createInstance(clsModule);
            m_mpModuleCache.put(clsModule.getName(), module);
         }
      }
      
      return module; 
   }
   
   /**
    * Method to create actual module based on specified class. This method can
    * be overridden and new manager can be setup either through method 
    * setManagerInstance or through configuration file if different strategy is 
    * desired.
    * 
    * @param clsModule - the module class name for which we want applicable 
    *                    module
    * @param factory - specific class factory to use to create the module rather
    *                  than the default one  
    * @return Module - the module to use for given class
    * @throws OSSException - an error has occurred
    */
   public Module getModuleInstance(
      Class<Module>        clsModule,
      ClassFactory<Module> factory
   ) throws OSSException
   {
      Module module;
      
      module = m_mpModuleCache.get(clsModule.getName());
      if (module == null)
      {
         synchronized (m_mpModuleCache)
         {
            module = factory.createInstance(clsModule);
            m_mpModuleCache.put(clsModule.getName(), module);
         }
      }
      
      return module; 
   }
   
   /**
    * Method to create actual module based on specified class. This method can
    * be overridden and new manager can be setup either through method 
    * setManagerInstance or through configuration file if different strategy is 
    * desired.
    * 
    * @param strModuleClassname - the module class name for which we want 
    *                             applicable module  
    * @return Module - the module to use for given class
    * @throws OSSException - an error has occurred
    */
   public Module getModuleInstance(
      String strModuleClassname
   ) throws OSSException
   {
      Module module;
      
      module = m_mpModuleCache.get(strModuleClassname);
      if (module == null)
      {
         synchronized (m_mpModuleCache)
         {
            module = m_moduleClassFactory.createInstance(strModuleClassname);
            m_mpModuleCache.put(strModuleClassname, module);
         }
      }
      
      return module; 
   }
   
   /**
    * Method to create actual module based on specified class. This method can
    * be overridden and new manager can be setup either through method 
    * setManagerInstance or through configuration file if different strategy is 
    * desired.
    * 
    * @param strModuleClassname - the module class name for which we want 
    *                             applicable module  
    * @param factory - specific class factory to use to create the module rather
    *                  than the default one  
    * @return Module - the module to use for given class
    * @throws OSSException - an error has occurred
    */
   public Module getModuleInstance(
      String               strModuleClassname,
      ClassFactory<Module> factory
   ) throws OSSException
   {
      Module module;
      
      module = m_mpModuleCache.get(strModuleClassname);
      if (module == null)
      {
         synchronized (m_mpModuleCache)
         {
            module = factory.createInstance(strModuleClassname);
            m_mpModuleCache.put(strModuleClassname, module);
         }
      }
      
      return module; 
   }
}

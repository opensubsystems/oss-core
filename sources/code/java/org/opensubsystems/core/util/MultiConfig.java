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

package org.opensubsystems.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.error.OSSInvalidContextException;

/**
 * Class responsible for reading of configuration settings specified at multiple 
 * locations in a defined priority order.
 * 
 * 1st try to read from the predefined properties if specified in the 
 *     constructor and if there are none specified then from the properties 
 *     stored in the external configuration file since such file allows to 
 *     customize settings packaged with the application.
 * 2nd try to read from the properties specified by the current execution 
 *     context using the calls to setCurrentProperties in the same order as was
 *     the order of the calls to this method. 
 *     This can be used for example in situation when the current code is 
 *     executing in the context of a filter(s) and servlet in web application. 
 *     It can try to read from filter/servlet config (init-param within filter 
 *     or servlet definition in web.xml) in the order the request passed through
 *     the filters and servlet. There might be multiple of these nested within 
 *     each other in the call stack (e.g. filter 1 called, filter 2 called, 
 *     servlet called).
 *     The properties are removed from the execution context using call to 
 *     resetCurrentProperties method. These calls should be made in the opposite 
 *     order as the calls to setCurrentProperties.
 * 3rd try to read from the default properties if specified in the constructor.
 * 
 * @author bastafidli
 */
public class MultiConfig extends Config
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Properties representing settings that should be considered as last resort
    * if no other matching values are found. 
    */
   protected Properties m_prpDefaultProperties;
   
   /**
    * Each thread calling this class will store here information about current 
    * configuration. Since we support multiple levels of configuration 
    * (predefined properties/external property file, execution context 
    * properties, default properties) this variable contains list where new 
    * configuration is inserted when new config is set and from which it is 
    * removed, when the config is reset. This has an implication and that is 
    * when new thread is created and it inherits the parent's value, it in fact 
    * inherits the list and therefore the parent and child would share the same 
    * list. This is not desirable so the child thread has to create it's own 
    * list and copy the content of the parent list into it so that from that 
    * point these two lists can be manipulated separately. 
    */
   private ThreadLocal<List<Properties>> m_currentConfig; 

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor to allow access to default external property file and the 
    * default properties to consider if values is not found in the external
    * property file or in the properties specified during execution context.
    * 
    * @param defaultProperties - properties representing settings that should 
    *                            be considered as last resort if no other 
    *                            matching values are found.  
    */
   public MultiConfig(
      Properties defaultProperties
   )
   {
      this(null, null, defaultProperties);
   }

   /**
    * Constructor to allow access to specific external property file and the 
    * default properties to consider if values is not found in the external
    * property file or in the properties specified during execution context.
    * 
    * @param strRequestedConfigFile - name of the property file to consider 
    *                                 first if no predefined properties are 
    *                                 specified
    * @param defaultProperties - properties representing settings that should 
    *                            be considered as last resort if no other 
    *                            matching values are found.  
    */
   public MultiConfig(
      String     strRequestedConfigFile,
      Properties defaultProperties
   )
   {
      this(null, strRequestedConfigFile, defaultProperties);
   }

   /**
    * Constructor to allow access to predefined properties to consider first and 
    * the default properties to consider if values is not found in the 
    * predefined properties or in the properties specified during execution 
    * context.
    * 
    * @param predefinedProperties - if not null these properties will be 
    *                               considered first instead of the ones 
    *                               specified in the external configuration file  
    * @param defaultProperties - properties representing settings that should 
    *                            be considered as last resort if no other 
    *                            matching values are found.  
    */
   public MultiConfig(
      Properties predefinedProperties,
      Properties defaultProperties
   )
   {
      this(predefinedProperties, null, defaultProperties);
   }

   /**
    * Full internal constructor to facilitate initialization.
    * 
    * @param predefinedProperties - if not null these properties will be 
    *                               considered first instead of the ones 
    *                               specified in the external configuration file  
    * @param strRequestedConfigFile - name of the property file to consider 
    *                                 first if no predefined properties are 
    *                                 specified
    * @param defaultProperties - properties representing settings that should 
    *                            be considered as last resort if no other 
    *                            matching values are found.  
    */
   protected MultiConfig(
      Properties predefinedProperties,
      String     strRequestedConfigFile,
      Properties defaultProperties
   )
   {
      super(predefinedProperties, strRequestedConfigFile);
   
      m_prpDefaultProperties = defaultProperties;
      if (m_prpDefaultProperties == null)
      {
         m_prpDefaultProperties = new Properties();
      }
      // Make it inheritable so that threads can act on behalf of the web layer 
      // who is using  
      m_currentConfig = new InheritableThreadLocal<List<Properties>>()
      {
         @Override
         protected List<Properties> childValue(
            List<Properties> parentValue
         )
         {
            List<Properties> childList = null;
                                
            if ((parentValue != null) && (!parentValue.isEmpty()))
            {
               // See discussion above why we need to copy this
               childList = new ArrayList<>();
               childList.addAll(parentValue);   
            }
                                
            return childList;
         }   
      };
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
  /** 
   * Set what configuration settings determined during execution context should 
   * be also considered when searching for a specific configuration value. The 
   * order in which the settings are considered is as follows:
   * 
   * 1. predefined properties or properties from external configuration file
   *    as specified in the constructor
   * 2. properties in the same order as the order of calls to 
   *    setCurrentProperties
   * 3. default properties as specified in the constructor
   * 
   * @param currentProperties - configuration object from where to load 
   *                            properties when accessed in the predefined order 
   */
  public void setCurrentProperties(
     Properties currentProperties
  )
  {
     List<Properties> lstConfigs = (m_currentConfig.get());
     if (lstConfigs == null)
     {
        List<Properties> lstInitialConfigs;
        
        lstInitialConfigs = getInitialPropertyList();
        m_currentConfig.set(lstInitialConfigs);
        lstConfigs = m_currentConfig.get();
     }

     int iLength = lstConfigs.size();
     
     if (GlobalConstants.ERROR_CHECKING)
     {
        // There should always be the properties from the parent class and the
        // default property object
        assert iLength >= 2 
               : "There needs to be at least two property objects present.";
     }
     // Add it just after the default properties (it will the the second element 
     // in the list)
     lstConfigs.add(1, currentProperties);
  }
  
  /** 
   * Reset configuration settings that were previously set using call to 
   * setCurrentProperties. By specifying the same config object that was passed 
   * into setCurrentProperties this call can verify if the appropriate config 
   * object is the next one in order to be removed. If it is not, an exception 
   * will be thrown.
   * 
   * @param currentProperties - properties that should be the current properties
   * @throws OSSException - an error has occurred
   */
  public void resetCurrentProperties(
     Properties currentProperties
  ) throws OSSException
  {
     List<Properties> lstConfigs = m_currentConfig.get();

     if ((lstConfigs != null) &&  (!lstConfigs.isEmpty()))
     {
        Properties existingProperties;
        int  iLength = lstConfigs.size();
        
        if (GlobalConstants.ERROR_CHECKING)
        {
           // There should always be the properties from the parent class and the
           // default property object
           assert iLength >= 2 
                  : "There needs to be at least two property objects present.";
        }
        
        if (iLength > 2)
        {
           // Try to check  if the one just after the default properties is the
           // one we are trying to remove (it will the the second element in the 
           // list)
           existingProperties = lstConfigs.get(1);
           if (existingProperties != currentProperties)
           {
              throw new OSSInvalidContextException(
                 "Cannot reset current properties since the properties do not"
                 + " match the ones currently in use.");
           }
           else
           {
              lstConfigs.remove(1);
           }
        }
        else
        {
           throw new OSSInvalidContextException(
              "Cannot reset current properties since not enough properties is"
              + " available.");
        }
     }
     else
     {
        throw new OSSInvalidContextException(
           "Cannot reset current properties since no properties are available.");
     }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Properties getProperties(
  )
  {
     Properties collectedProperties = new Properties();

     List<Properties> lstConfigs = m_currentConfig.get();
     if ((lstConfigs == null) || (lstConfigs.isEmpty()))
     {
        if (m_prpDefaultProperties == null)
        {
           // There are no default properties so just directly return the 
           // properties from super class
           collectedProperties = super.getProperties();
           // Set to null to skip the loop below
           lstConfigs = null;
        }
        else
        {
           lstConfigs = getInitialPropertyList();
        }
     }
     
     if (lstConfigs != null)
     {
        // Just collect all the objects and let the later ones overwrite the
        // older ones. This way the default properties will be first, then the
        // latest context, context before that and all the way through the 
        // predefined properties or properties from external file
        for (Properties props : lstConfigs)
        {
           collectedProperties.putAll(props);
        }
     }
     
     return collectedProperties;
  }
  
  // Helper methods ////////////////////////////////////////////////////////////
  
  /**
   * Get list of initial configuration settings before any call context specific
   * settings were defined
   * 
   * @return List - list of initial configuration settings, first will be the
   *                default properties followed by the properties from the super
   *                class 
   */
  protected List<Properties> getInitialPropertyList(
  ) 
  {
     Properties       prpFirstProperties = super.getProperties();
     List<Properties> lstInitialConfigs = new ArrayList<>();
     
     lstInitialConfigs.add(m_prpDefaultProperties);
     lstInitialConfigs.add(prpFirstProperties);
     
     return lstInitialConfigs;
  }
}

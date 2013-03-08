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

package org.opensubsystems.core.www.impl;

import org.opensubsystems.core.application.Module;
import org.opensubsystems.core.application.impl.ModuleImpl;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.www.WebModule;

/**
 * Simple implementation of interface representing one module of the web 
 * application. 
 * 
 * @author bastafidli
 */
public class WebModuleImpl extends    ModuleImpl     
                           implements WebModule
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Unique module identifier that contains no spaces and is all in lower case 
    * so that it can be used in various internal web interface constructs.
    */
   protected String m_strIdentifier;
   
   /**
    * Tooltip that will be displayed for particular module on the web gui.
    */
   protected String m_strTooltip;
   
   /**
    * URL of the module.
    */
   protected String m_strURL;
   
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
    * @param strIdentifier - Unique module identifier that contains no spaces 
    *                        and is all in lower case so that it can be used in 
    *                        various internal web interface constructs.
    * @param strTooltip - tooltip that will be displayed for particular module 
    *                     on the web gui                            
    * @throws OSSException - an error has occurred
    */
   public WebModuleImpl(
      String     strName,
      int        iVersion,
      Module[]   arrDependentModules,
      String[][] arrPropertiesToDefine,
      String     strIdentifier,
      String     strTooltip
   ) throws OSSException
   {
      super(strName, iVersion, arrDependentModules, arrPropertiesToDefine);
      
      m_strIdentifier = strIdentifier;
      m_strTooltip    = strTooltip;
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   public String getIdentifier(
   )
   {
      return m_strIdentifier;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public String getTooltip(
   )
   {
      return m_strTooltip;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getURL(
   )
   {
      return m_strURL;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setURL(
      String strURL
   )
   {
      m_strURL = strURL;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void init(
   ) throws OSSException
   {
      // Most web modules do not provide any additional data elements, they just
      // use the data elements defined by the backend modules, therefore this
      // method provides default implementation of the init method.
      initModule(null);
   }
}

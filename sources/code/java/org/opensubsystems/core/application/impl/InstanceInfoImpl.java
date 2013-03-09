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

import org.opensubsystems.core.application.InstanceInfo;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.NetUtils;
import org.opensubsystems.core.util.OSSObject;

/**
 * Implementation of the InstanceInfo interface represents unique application 
 * instance on the computer or network. In general unique identification of 
 * application instance is an IP address of the machine where it is running. In 
 * some cases we may want to run multiple applications on the same machine (e.g. 
 * when allowing two users use the same machine in multi-terminal environment) 
 * and in that case the IP address is not unique and must be customized by each 
 * instance (e.g. using port, some sequence, etc.). Therefore this class is used 
 * as an abstraction of information that identify application instance. 
 * 
 * @author bastafidli
 */
public class InstanceInfoImpl extends OSSObject
                              implements InstanceInfo
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Information about the instance of the application.
    */
   protected String m_strInfo;
   
   /**
    * Identification of the default application instance, which is application 
    * running in this VM on the current IP address.
    */
   protected static InstanceInfo s_defaultInstance;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Static initializer.
    */
   static
   {
      try
      {
         // We have no setInstance since we just directly generate the 
         // information about default location
         s_defaultInstance = new InstanceInfoImpl();
      }
      catch (OSSException bfeExc)
      {
         // No way to throw checked exception so convert it to unchecked 
         throw new RuntimeException(bfeExc);
      }      
   }
      
   /**
    * Default constructor.
    * 
    * @throws OSSException - an error has occurred 
    */
   public InstanceInfoImpl(
   ) throws OSSException
   {
      // By default just use server ip address if somebody wants something
      // else, they can change it later
      m_strInfo = NetUtils.getServerIPAddressAndName();
   }
   
   /**
    * Constructor.
    * 
    * @param strInfo - unique application instance on the computer or network
    */
   public InstanceInfoImpl(
      String strInfo
   ) 
   {
      m_strInfo = strInfo;
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Get the default instance of this application.
    * 
    * @return LocationInfo
    */
   public static InstanceInfo getInstance(
   )
   {
      return s_defaultInstance;    
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public String getInfo(
   )
   {
      return m_strInfo;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setInfo(
      String strLocation
   )
   {
      m_strInfo = strLocation;
   }
}

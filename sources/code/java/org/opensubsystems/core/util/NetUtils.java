/*
 * Copyright (C) 2003 - 2014 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * Class containing methods for working with network.
 * 
 * @author OpenSubsystems
 */
public final class NetUtils extends OSSObject
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private NetUtils(
   )
   {
      // Do nothing
   }
   
   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Get textual representation just of the server machine IP address. 
    * 
    * @return strAddress - IP address of the server
    */
   public static String getServerIPAddress(
   )
   {
      String strAddress;
       
      try
      {
         // TODO: Performance: Profile performance and if impacted cache
         // and periodically check for update
         strAddress = InetAddress.getLocalHost().getHostAddress();
      } 
      catch (UnknownHostException eExc)
      {
         // MHALAS: On my Linux box it cannot find the address, but that 
         // is not reason to do not start the server 
         // throw new OSSInternalErrorException("Localhost IP address could not 
         // be determined.");
         strAddress = "Unknown address";
      }
      
      return strAddress;
   }

   /**
    * Get textual representation of the server machine IP address together with
    * its name. 
    * 
    * @return strAddress - IP address of the server
    */
   public static String getServerIPAddressAndName(
   )
   {
      String strAddress;
       
      try
      {
         // TODO: Performance: Profile performance and if impacted cache
         // and periodically check for update
         strAddress = InetAddress.getLocalHost().toString();
      } 
      catch (UnknownHostException eExc)
      {
         // MHALAS: On my Linux box it cannot find the address, but that 
         // is not reason to do not start the server 
         // throw new OSSInternalErrorException("Localhost IP address could not 
         // be determined.");
         strAddress = "Unknown address";
      }
      
      return strAddress;
   }
}

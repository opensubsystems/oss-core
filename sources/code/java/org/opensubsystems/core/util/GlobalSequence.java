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

package org.opensubsystems.core.util;

/**
 * Class for getting of unique sequence numbers. 
 * 
 * @author OpenSubsystems
 */
public final class GlobalSequence extends OSSObject
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Last request ID
    */
   private static int s_lastRequest = 0;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private GlobalSequence(
   )
   {
      // Do nothing
      // Since this cannot be never invoked, this is here just to avoid Checkstyle
      // warning.
      s_lastRequest = 0;
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Get new sequence number which is guaranteed to be unique in this class loader.
    * 
    * @return int
    */
   public static synchronized int getNextSequenceNumber(
   )
   {
      return s_lastRequest++;
   }
}

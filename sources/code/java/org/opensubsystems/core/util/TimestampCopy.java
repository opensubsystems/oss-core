/*
 * Copyright (C) 2003 - 2012 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import java.sql.Timestamp;

/**
 * Timestamp which adds copy constructor which correctly copies nanosecond 
 * portion of the original timestamp. 
 * 
 * @author bastafidli
 */
public class TimestampCopy extends Timestamp
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 135570135998694876L;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Copy constructor which will create exact copy of the timestamp including
    * the nanosecond portion.
    * 
    * @param original - original timestamp to copy
    */
   public TimestampCopy(
      Timestamp original
   )
   {
      // Pass the time portion here
      super(original.getTime());
      // And now set the correct nanoseconds since it is required.
      setNanos(original.getNanos());
   }

   /**
    * Constructor which will create exact copy of the timestamp including
    * the nanosecond portion.
    * 
    * @param lTime - time portion of the timestamp
    * @param iNanos - nanosecond portion of the timestamp
    */
   public TimestampCopy(
      long lTime,
      int  iNanos
   )
   {
      // Pass the time portion here
      super(lTime);
      // And now set the correct nanoseconds since it is required.
      setNanos(iNanos);
   }
}

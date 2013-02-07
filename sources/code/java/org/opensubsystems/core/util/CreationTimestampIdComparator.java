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

import java.util.Comparator;

import org.opensubsystems.core.data.BasicDataObject;

/**
 * Comparator to compare basic data objects based on their creation timestamp 
 * and id.
 * 
 * @author bastafidli
 */
public class CreationTimestampIdComparator extends     OSSObject
                                             implements Comparator<BasicDataObject>
{
   // Cached values ////////////////////////////////////////////////////////////
   
   /**
    * Shared comparator instance. Must be named this way to avoid Checkstyle 
    * warning.
    */
   private static Comparator<BasicDataObject> s_instance 
      = new CreationTimestampIdComparator();

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Get shared comparator instance.
    *
    * @return Comparator  - shared comparator instance
    */
   public static Comparator<BasicDataObject> getInstance(
   )
   {
      return s_instance;
   }

   /**
    * Compare creation timestamp and id of two basic data objects.
    *
    * @param  o1 - BasicDataObject #1
    * @param  o2 - BasicDataObject #2
    * @return int - -1 if o1 < o2,
    *                0 if o1 == o2
    *                1 if o1 > o2
    */
   public int compare(
      BasicDataObject o1,
      BasicDataObject o2
   )
   {
      long lTimestamp1 = o1.getCreationTimestamp().getTime();
      long lTimestamp2 = o2.getCreationTimestamp().getTime();

      if (lTimestamp1 < lTimestamp2)
      {
         return -1;
      }
      else
      {
         if (lTimestamp1 == lTimestamp2)
         {
            long lId1 = o1.getId();
            long lId2 = o2.getId();
            
            if (lId1 < lId2)
            {
               return -1;
            }
            else if (lId1 == lId2)
            {
               return 0;
            }
            else
            {
               return 1;
            }
         }
         else
         {
            return 1;
         }
      }
   }
}

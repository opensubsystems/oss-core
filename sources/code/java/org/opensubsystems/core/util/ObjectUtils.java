/*
 * Copyright (C) 2012 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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
 * Class containing methods for working with objects.
 * 
 * @author bastafidli
 */
public final class ObjectUtils extends OSSObject
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private ObjectUtils(
   )
   {
      // Do nothing
   }
   
   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Compare two objects that can be null for equality. 
    *
    * @param first - first object to compare, it can be null
    * @param second - second object to compare, it can be null 
    * @return boolean - true if the two objects are equal, false otherwise
    */
   public static boolean equals(
      Object first,
      Object second
   )
   {
      return (((first == null) && (second == null))
             || ((first != null) && (first.equals(second))));
   }
}

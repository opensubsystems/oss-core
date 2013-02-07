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
 * Collection of useful utilities to work with arrays. 
 * 
 * @author OpenSubsystems
 */
public final class ArrayUtils extends OSSObject
{
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private ArrayUtils(
   )
   {
      // Do nothing
   }
   
   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Method to exclude 2 arrays of ints so that the result contains all elements
    * from the first array, which are not in the second array.
    *  
    * @param arrBase - base array to exclude from 
    * @param arrExclude - array to exclude from the first one
    * @return int[] - array which contains all elements from the first array 
    *                  which are not in the second array or null
    */
   public static int[] exclude(
      int[] arrBase, 
      int[] arrExclude
   )
   {
      int[] arrReturn = null;

      if ((arrBase != null) && (arrBase.length > 0) && (arrExclude != null)
         && (arrExclude.length > 0))
      {
         int[] arrHelp;
         int   iCount1;
         int   iHelp;
         int   iLength = 0;
         
         arrHelp = new int[arrBase.length];
         for (iCount1 = 0; iCount1 < arrBase.length; iCount1++)
         {
            iHelp = arrBase[iCount1];
            if (ArrayUtils.contains(arrExclude, iHelp) == -1)
            {
               // If the element is not part of the second array then it should
               // be included in the result
               arrHelp[iLength++] = iHelp;
            }
         }
         
         // Shrink the array
         // TODO: Performance: Replace this with System.arraycopy
         arrReturn = new int[iLength];
         for (int iCount = 0; iCount < iLength; iCount++)
         {
            arrReturn[iCount] = arrHelp[iCount];
         }
      }
      else
      {
         arrReturn = arrBase;
      }
      
      return arrReturn;
   }
   
   /**
    * Test if specified array contains given element and if it does, find 
    * its position. The array doesn't have to be sorted.
    * 
    * @param source - array to search, can be null
    * @param iTarget - element to find
    * @return int - -1 if it doesn't exist there otherwise its position
    */
   public static int contains(
      int[] source,
      int   iTarget
   )
   {
      int iReturn = -1;
      
      if ((source != null) && (source.length > 0))
      {   
         int iIndex;
         
         for (iIndex = 0; iIndex < source.length; iIndex++)
         {
            if (source[iIndex] == iTarget)
            {
               iReturn = iIndex;
               break;
            }
         }
      }
      
      return iReturn;
   }

   /**
    * Sum all elements in the array.
    * 
    * @param source - array to sum elements of
    * @return long - sum of the elements in the array
    */
   public static long sum(
      int[] source
   )
   {
      int iReturn = 0;
      
      if ((source != null) && (source.length > 0))
      {   
         int iIndex;
         
         for (iIndex = 0; iIndex < source.length; iIndex++)
         {
            iReturn += source[iIndex];
         }
      }
      
      return iReturn;
   }
}

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
 * Class which can hold two objects. This is useful if one wants to create array 
 * of two objects to pass as argument to some function.
 *  
 * @author OpenSubsystems
 */
public class TwoElementStruct<X, Y> extends OSSObject
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * First element
    */
   protected X m_first;

   /**
    * Second element
    */
   protected Y m_second;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Public constructor.
    * 
    * @param first - first element
    * @param second - second element
    */
   public TwoElementStruct(
      X first, 
      Y second
   )
   {
      m_first = first;
      m_second = second;
   }

   /**
    * Public constructor.
    * 
    * @param input - TwoObjectStruct to copy into
    */
   public TwoElementStruct(
      TwoElementStruct<X, Y> input
   )
   {
      assert input != null : "Can't create empty TwoObjectStruct";

      m_first = input.getFirst();
      m_second = input.getSecond();
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * @return X
    */
   public X getFirst()
   {
      return m_first;
   }

   /**
    * @return Y
    */
   public Y getSecond()
   {
      return m_second;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(
      Object oObject
   )
   {
      boolean bRetval = false;

      if (oObject == this)
      {
         return true;
      }
      else if (oObject != null)
      {
         if (oObject instanceof TwoElementStruct)
         {
            TwoElementStruct input = (TwoElementStruct) oObject;
            
            return ObjectUtils.equals(m_first, input.m_first)
                   && ObjectUtils.equals(m_second, input.m_second);
         }
      }

      return bRetval;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      int iResult = HashCodeUtils.SEED;
      iResult = HashCodeUtils.hash(iResult, m_first);
      iResult = HashCodeUtils.hash(iResult, m_second);
      return iResult;
   }
}

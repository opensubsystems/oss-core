/*
 * Copyright (C) 2008 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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
 * Class which can hold three objects. This is useful if one wants to create  
 * array of three objects to pass as argument to some function.
 *  
 * @author bastafidli
 */
public class ThreeElementStruct<X, Y, Z> extends TwoElementStruct<X, Y> 
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Third element
    */
   protected Z m_third;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Public constructor.
    * 
    * @param first - first object
    * @param second - second object
    */
   public ThreeElementStruct(
      X first, 
      Y second,
      Z third
   )
   {
      super(first, second);
      
      m_third = third;
   }

   /**
    * Public constructor.
    * 
    * @param input - ThreeObjectStruct to copy into
    */
   public ThreeElementStruct(
      ThreeElementStruct<X, Y, Z> input
   )
   {
      super(input);
      
      assert input != null : "Can't create empty ThreeObjectStruct";
      

      m_third = input.getThird();
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * @return Z
    */
   public Z getThird()
   {
      return m_third;
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
         if (oObject instanceof ThreeElementStruct)
         {
            ThreeElementStruct input = (ThreeElementStruct) oObject;
            
            return (super.equals(oObject)) 
                   && ObjectUtils.equals(m_third, input.m_third);
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
      iResult = HashCodeUtils.hash(iResult, m_third);
      iResult = HashCodeUtils.hash(iResult, super.hashCode());
      return iResult;
   }
}

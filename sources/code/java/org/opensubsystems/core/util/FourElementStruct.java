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
 * Class which can hold four objects. This is useful if one wants to create  
 * array of four objects to pass as argument to some function.
 *  
 * @author bastafidli
 */
public class FourElementStruct<X, Y, Z, F> extends ThreeElementStruct<X, Y, Z> 
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Fourth element
    */
   protected F m_fourth;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Public constructor.
    * 
    * @param first - first object
    * @param second - second object
    * @param third  - third object
    * @param fourth - fourth object
    */
   public FourElementStruct(
      X first, 
      Y second,
      Z third,
      F fourth
   )
   {
      super(first, second, third);
      
      m_fourth = fourth;
   }

   /**
    * Public constructor.
    * 
    * @param input - ThreeObjectStruct to copy into
    */
   public FourElementStruct(
      FourElementStruct<X, Y, Z, F> input
   )
   {
      super(input);
      
      assert input != null : "Can't create empty FourElementStruct";
      

      m_fourth = input.getFourth();
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * @return F
    */
   public F getFourth()
   {
      return m_fourth;
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
         if (oObject instanceof FourElementStruct)
         {
            FourElementStruct input = (FourElementStruct) oObject;
            
            return (super.equals(oObject)) 
                   && ObjectUtils.equals(m_fourth, input.m_fourth);
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
      iResult = HashCodeUtils.hash(iResult, m_fourth);
      iResult = HashCodeUtils.hash(iResult, super.hashCode());
      return iResult;
   }
}

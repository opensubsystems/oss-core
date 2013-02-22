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
 * Class which can hold four integers. This is useful if I want to create array 
 * of four integers to pass as argument to some function.
 * 
 * @author OpenSubsystems
 */
public class FourIntStruct extends ThreeIntStruct
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Fourth integer
    */
   protected int m_iFourth;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * @param iFirst - first int
    * @param iSecond - second int
    * @param iThird - third int
    * @param iFourth - fourth int
    */
   public FourIntStruct(
      int iFirst, 
      int iSecond, 
      int iThird, 
      int iFourth
   )
   {
      super(iFirst, iSecond, iThird);
      
      m_iFourth = iFourth;
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * @return fourth integer
    */
   public int getFourth()
   {
      return m_iFourth;
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
         if (oObject instanceof FourIntStruct)
         {
            FourIntStruct input = (FourIntStruct) oObject;
            return (super.equals(oObject)) && (m_iFourth == input.m_iFourth);
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
      iResult = HashCodeUtils.hash(iResult, m_iFourth);
      iResult = HashCodeUtils.hash(iResult, super.hashCode());
      return iResult;
   }
}

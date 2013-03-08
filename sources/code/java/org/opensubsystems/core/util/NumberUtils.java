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

import java.text.NumberFormat;

/**
 * Collection of useful utilities to work with numbers. 
 * 
 * @author OpenSubsystems
 */
public final class NumberUtils extends OSSObject
{
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * Static array used to append leading 0 chars to file name constructed from
    * number.
    */
   private static final char[] ZEROCHARS 
      = {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
         '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
         '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
         '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
         '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
        };

   // Cached values ////////////////////////////////////////////////////////////
   
   /**
    * static Number format for no Exponent 
    */
   public static final NumberFormat NFFORMAT;
   
   /**
    * static Number format for no Exponent for editing
    */
   public static final NumberFormat NFFORMATEDIT;

   /**
    * static Number format for Currency
    */
   public static final NumberFormat NFCURRENCYFORMAT;

   /**
    * static Number format for Currency for editing
    */
   public static final NumberFormat NFCURRENCYFORMATEDIT;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Static initializer.
    */
   static
   {
      
      NFFORMAT = NumberFormat.getNumberInstance();
      NFFORMAT.setMaximumFractionDigits(20);
      
      NFFORMATEDIT = NumberFormat.getNumberInstance();
      NFFORMATEDIT.setMaximumFractionDigits(20);
      NFFORMATEDIT.setGroupingUsed(false);

      NFCURRENCYFORMAT = NumberFormat.getNumberInstance();
      NFCURRENCYFORMAT.setMaximumFractionDigits(2);
      NFCURRENCYFORMAT.setMinimumFractionDigits(2);

      NFCURRENCYFORMATEDIT = NumberFormat.getNumberInstance();
      NFCURRENCYFORMATEDIT.setMaximumFractionDigits(2);
      NFCURRENCYFORMATEDIT.setMinimumFractionDigits(2);
      NFCURRENCYFORMATEDIT.setGroupingUsed(false);

   }

   /** 
    * Private constructor since this class cannot be instantiated
    */
   private NumberUtils(
   )
   {
      // Do nothing
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Method to compute exponentiation.
    * 
    * @param iBbase - base of exponentiation [1..]
    * @param iExponent - exponent of exponentiation [0..14]
    * @return long - result of exponentiation
    * @throws IllegalArgumentException - an error has occurred
    */
   public static long exponentiate(
      int iBbase, 
      int iExponent
   ) throws IllegalArgumentException
   {
      if (iExponent > 14 || iExponent < 0)
      {
         throw new IllegalArgumentException(
               "Exponent could not be greater then 14 and lower then 0");
      }
      if (iBbase < 1)
      {
         throw new IllegalArgumentException(
               "Exponentiate base could not be lower then 1");
      }
      long lReturn = 1;
      for (int iCounter = 0; iCounter < iExponent; iCounter++)
      {
         try
         {
            lReturn = lReturn * iBbase;
         } 
         catch (Exception eExc)
         {
            throw new IllegalArgumentException(
                  "Exponentiate arguments too high");
         }
      }
      return lReturn;
   }

   /**
    * Method to make specified length digit number string representation from 
    * particular input number.
    * For example, if there will be send input number 32 and digit length = 8, 
    * output will be string '00000032'
    * 
    * @param iInputNumber - input number that will be converted into 8 digit 
    *                       number string representation
    * @param iDigitLength - length of the output digit number string
    * 
    * @return String - digit number string representation
    */
   public static String getDigitNumberString(
      int iInputNumber,
      int iDigitLength
   )
   {
      StringBuilder idString = new StringBuilder(Integer.toString(iInputNumber));
      
      if (iDigitLength - idString.length() > 0)
      {
         idString.insert(0, ZEROCHARS, 0, iDigitLength - idString.length());
      }

      return idString.toString();
   }
}

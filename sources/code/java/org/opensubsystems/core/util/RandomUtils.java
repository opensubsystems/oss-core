/*
 * Copyright (C) 2012 - 2014 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import java.security.SecureRandom;
import java.util.logging.Logger;

/**
 * Utility methods related to random data generation.
 * 
 * @author bastafidli
 */
public final class RandomUtils 
{
   // Constants
   
   /**
    * Define safe alphabet that can be used either from command line, from 
    * scripts, from URLs etc.
    */
   public static final String SAFE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                            + "abcdefghijklmnopqrstuvwxyz"
                                            + "1234567890"
                                            + "@#()[]{}-_";
   
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Commons logger variable used to log runtime information.
    */
   private static Logger s_logger = Log.getInstance(RandomUtils.class);
   
   // Constructors /////////////////////////////////////////////////////////////
    
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private RandomUtils(
   )
   {
      // Do nothing
   }
   
   // Public methods ///////////////////////////////////////////////////////////

   /**
    * Generate random data of a specific maximal size.
    * 
    * @param iSize - size of the random data to generate
    * @return byte[] - generated random data
    */
   public static byte[] generateRandomData(
      int iSize
   )
   {
      // TODO: Improve by not always generating max size but maybe adding flag
      // to see if shorter size is possible and generating random size between 
      // let say 75% of max size and max size
      SecureRandom random = new SecureRandom();
      byte bytes[] = new byte[iSize];
      random.nextBytes(bytes);
   
      return bytes;
   }
   
   /**
    * Generate random data of a specific maximal size as a string value. The 
    * data will be generated using SAFE_ALPHABET.
    * 
    * @see #SAFE_ALPHABET
    * @param iSize - size of the random data to generate
    * @return String - text of random data of a specified maximal size
    */
   public static String generateRandomDataAsString(
      int iSize           
   )
   {
      byte          bytes[] = generateRandomData(iSize);
      char          charItem;
      int           iIndex;
      StringBuilder builder = new StringBuilder();

      
      for (byte item : bytes)
      {
         iIndex = item % SAFE_ALPHABET.length();
         if (iIndex < 0)
         {
            iIndex *= -1;
         }
        
         charItem = SAFE_ALPHABET.charAt(iIndex % SAFE_ALPHABET.length());
                 
         builder.append(charItem);
      }
      
      return builder.toString();
   }
}

/*
 * Copyright (C) 2007 - 2014 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Collection of useful utilities to work with exceptions and throwables.
 * 
 * @author bastafidli
 */
public final class ThrowableUtils extends OSSObject
{
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * How many nested causes to include when formatting a throwable.  
    */
   public static final int MAX_NESTED_CAUSES = 20;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private ThrowableUtils(
   )
   {
      // Do nothing
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
  /**
   * Convert the specified throwable into a string including all the nested
   * exceptions and causes.  
   * 
   * @param thr - the throwable to convert to the string
   * @return String - the string representation of the specified throwable
   */
  public static String toString( 
     Throwable thr 
  )
  {
     StringBuilder builder = new StringBuilder();

     toString(1, MAX_NESTED_CAUSES, builder, thr);
    
     return builder.toString();
  }
  
  // Helper methods ////////////////////////////////////////////////////////////
  
  /**
   * Include a given error in the output which can be sent to the client
   * 
   * @param iCurrentLevel - current level of nesting
   * @param iMaxLevel - maximal level of nesting to prevent recursion
   * @param builder - string builder to put the output to
   * @param cause - throwable to include in the output
   */
  private static void toString(
    int           iCurrentLevel,
    int           iMaxLevel,
    StringBuilder builder,
    Throwable     cause
  )
  {
    if (iCurrentLevel <= iMaxLevel)
    {
      builder.append("--> Cause# ");
      builder.append(iCurrentLevel);
      builder.append("\n");
      builder.append(cause.getClass().getName());
      builder.append(": ");
      if (cause.getMessage() != null)
      {
        builder.append(cause.getMessage());
        builder.append("\n");
      }
      else
      {
        builder.append("null\n");
      }
      StringWriter stringWriter = new StringWriter();
      PrintWriter  printWriter = new PrintWriter(stringWriter);
      
      cause.printStackTrace(printWriter);
      builder.append(stringWriter.toString());
      builder.append("\n");
      
      if ((cause.getCause() != null) && (cause.getCause() != cause))
      {
        toString(iCurrentLevel + 1, iMaxLevel, builder, cause.getCause());
      }
    }
    else
    {
      builder.append("--> Cause# ");
      builder.append(iCurrentLevel);
      builder.append(" and higher will not be included since it exceeds maximum"
                     + " allowed level of nesting.\n");
    }
  }
}

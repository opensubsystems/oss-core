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
  
package org.opensubsystems.core.error;

/**
 * Exception thrown when there was a problem with concurrent object
 * modification. This happens for example when one user looks at some edit 
 * screen too long and meanwhile some other user modifies the data. Then when 
 * the first user tries to save his data, he will receive this exception and the 
 * data will not be modified to prevent overwriting and loss of data. 
 * 
 * @author OpenSubsystems
 */
public class OSSConcurentModifyException extends OSSException
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = -1672103414523437585L;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Create new exception
    */
   public OSSConcurentModifyException()
   {
      super();
   }

   /**
    * Create new exception
    * 
    * @param message - message to display
    */
   public OSSConcurentModifyException(String message)
   {
      super(message);
   }

   /**
    * Create new exception
    * 
    * @param message - message to display
    * @param cause - cause for error
    */
   public OSSConcurentModifyException(String message, Throwable cause)
   {
      super(message, cause);
   }

   /**
    * Create new exception
    * 
    * @param cause - cause for error
    */
   public OSSConcurentModifyException(Throwable cause)
   {
      super(cause);
   }
}

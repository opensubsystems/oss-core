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
 * Exception representing general error when accessing the database for example,
 * because connection cannot be established.
 * 
 * @author bastafidli
 */
public class OSSDatabaseAccessException extends OSSException
{   
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = -4738711150142620453L;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Create new exception
    */
   public OSSDatabaseAccessException(
   )
   {
      super();
   }

   /**
    * Create new exception
    * 
    * @param message - message to display
    */
   public OSSDatabaseAccessException(
      String message
   )
   {
      super(message);
   }

   /**
    * Create new exception
    * 
    * @param message - message to display
    * @param cause - cause for error
    */
   public OSSDatabaseAccessException(
      String message, 
      Throwable cause
   )
   {
      super(message, cause);
   }

   /**
    * Create new exception
    * 
    * @param cause - cause for error
    */
   public OSSDatabaseAccessException(
      Throwable cause
   )
   {
      super(cause);
   }
}

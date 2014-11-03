/*
 * Copyright (C) 2003 - 2014 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import org.opensubsystems.core.util.Messages;

/**
 * Exception thrown when there was a problem with request to process invalid 
 * data. Example of problems are overflow (length limit for String exceeded, 
 * range limit for int exceeded, etc.) or in case of unique constrains problems 
 * (unique login name, unique file name, ...). This can occur when user enters 
 * some data, which do not satisfy the constraints set by business logic or 
 * persistence layer.
 * 
 * @author OpenSubsystems
 */
public class OSSInvalidDataException extends OSSMultiException
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 5370198119942638443L;

   /**
    * Error messages for all invalid data errors 
    */
   protected Messages m_errorMessages;
 
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Create new exception
    */
   public OSSInvalidDataException()
   {
      super();
      m_errorMessages = new Messages();
   }

   /**
    * Create new exception
    * 
    * @param strMessage - message to display
    */
   public OSSInvalidDataException(
      String strMessage
   )
   {
      super(strMessage);
      m_errorMessages = new Messages();
      m_errorMessages.addErrorMessage(strMessage);
   }

   /**
    * Create new exception
    * 
    * @param strLocation - location in the structure of the application where 
    *                      the error has occurred
    * @param strMessage - message to display
    */
   public OSSInvalidDataException(
      String strLocation,
      String strMessage
   )
   {
      super(strLocation, strMessage);
      m_errorMessages = new Messages();
      m_errorMessages.addErrorMessage(strMessage);
   }
   
   /**
    * Create new exception
    * 
    * @param strMessage - message to display
    * @param cause - cause for error
    */
   public OSSInvalidDataException(
      String    strMessage, 
      Throwable cause
   )
   {
      super(strMessage, cause);
      m_errorMessages = new Messages();
      m_errorMessages.addErrorMessage(strMessage);
   }

   /**
    * Create new exception
    * 
    * @param strLocation - location in the structure of the application where 
    *                      the error has occurred
    * @param strMessage - message to display
    * @param cause - cause for error
    */
   public OSSInvalidDataException(
      String    strLocation,
      String    strMessage, 
      Throwable cause
   )
   {
      super(strLocation, strMessage, cause);
      m_errorMessages = new Messages();
      m_errorMessages.addErrorMessage(strMessage);
   }

   /**
    * Create new exception
    * 
    * @param cause - cause for error
    */
   public OSSInvalidDataException(
      Throwable cause
   )
   {
      super(cause);
      m_errorMessages = new Messages();
   }
   
   /**
    * Create new exception
    * 
    * @param first - first exception which has occurred
    * @param second - second exception which has occurred
    */
   public OSSInvalidDataException(
      Throwable first,
      Throwable second
   )
   {
      super(first, second);
      m_errorMessages = new Messages();
   }
   
   /**
    * Create new exception
    * 
    * @param strLocation - location in the structure of the application where 
    *                      the error has occurred
    * @param first - first exception which has occurred
    * @param second - second exception which has occurred
    */
   public OSSInvalidDataException(
      String    strLocation,
      Throwable first,
      Throwable second
   )
   {
      super(strLocation, "Multiple exceptions");
      m_errorMessages = new Messages();
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * @return Messages - error messages object
    */
   public Messages getErrorMessages()
   {
      return m_errorMessages;
   }

   /**
    * @param errorMessages - Messages
    */
   public void setErrorMessages(Messages errorMessages)
   {
      m_errorMessages = errorMessages;
   }

   /**
    * Adds message to an exiting exception or if that exception doesn't exists 
    * create a new one, add the message to it and return the newly created 
    * exception.
    * 
    * @param inputException - exception to add message to or null if the exception
    *                         doesn't exist
    * @param iMessageCategory - code representing message category to add the 
    *                           message to (see Messages class)
    * @param strMessage - message to add
    * @return OSSInvalidDataException - new created or modified exception
    */
   public static OSSInvalidDataException addException(
      OSSInvalidDataException inputException,
      int                     iMessageCategory,
      String                  strMessage
   )
   {
      return addException(inputException, new Integer(iMessageCategory), 
                          strMessage);
   }

   /**
    * Adds message to an exiting exception or if that exception doesn't exists 
    * create a new one, add the message to it and return the newly created 
    * exception.
    * 
    * @param inputException - exception to add message to or null if the exception
    *                         doesn't exist
    * @param messageCategory - message category to add the message to (see Messages 
    *                          class)
    * @param strMessage - message to add
    * @return OSSInvalidDataException - new created or modified exception
    */
   public static OSSInvalidDataException addException(
      OSSInvalidDataException inputException,
      Object                  messageCategory,
      String                  strMessage
   )
   {
      if (inputException == null)
      {
         inputException = new OSSInvalidDataException();
      }
      inputException.getErrorMessages().addMessage(messageCategory, strMessage);  
      return inputException;
   }

   /**
    * Adds message to an exiting exception or if that exception doesn't exists 
    * create a new one, add the message to it and return the newly created 
    * exception.
    * 
    * @param inputException - exception to add message to or null if the exception
    *                         doesn't exist
    * @param iMessageCategory - code representing message category to add the 
    *                           message to (see Messages class)
    * @param strMessage - message to add
    * @param thr - exception which cause the error
    * @return OSSInvalidDataException - new created or modified exception
    */
   public static OSSInvalidDataException addException(
      OSSInvalidDataException inputException,
      int                     iMessageCategory,
      String                  strMessage,
      Throwable               thr
   )
   {
      return addException(inputException, new Integer(iMessageCategory),
                          strMessage, thr);
   }
   
   /**
    * Adds message to an exiting exception or if that exception doesn't exists 
    * create a new one, add the message to it and return the newly created 
    * exception.
    * 
    * @param inputException - exception to add message to or null if the exception
    *                         doesn't exist
    * @param messageCategory - message category to add the message to (see Messages 
    *                          class)
    * @param strMessage - message to add
    * @param thr - exception which cause the error
    * @return OSSInvalidDataException - new created or modified exception
    */
   public static OSSInvalidDataException addException(
      OSSInvalidDataException inputException,
      Object                  messageCategory,
      String                  strMessage,
      Throwable               thr
   )
   {
      if (inputException == null)
      {
         inputException = new OSSInvalidDataException(strMessage);
      }
      inputException.getErrorMessages().addMessage(messageCategory, strMessage);
      inputException.add(thr);
      return inputException;
   }
}

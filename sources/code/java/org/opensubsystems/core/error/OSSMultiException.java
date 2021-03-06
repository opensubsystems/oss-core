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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class allows to throw multiple exceptions at once for example in 
 * scenario when we need to propagate one exception even though another
 * exception has occurred. This class is inspired by
 * org.mortbay.util.MultiException from Jetty distribution.
 * 
 * @author bastafidli
 */
public class OSSMultiException extends OSSException
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = -1993344389739792900L;

   /**
    * Exceptions that should be thrown together
    */
   protected List<Throwable> m_lstExceptions;

   // Constructor //////////////////////////////////////////////////////////////
   
   /**
    * Create new exception
    */
   public OSSMultiException()
   {
      super();
   }

   /**
    * Create new exception
    * 
    * @param strMessage - message to display
    */
   public OSSMultiException(
      String strMessage
   )
   {
      super(strMessage);
   }

   /**
    * Create new exception
    * 
    * @param strLocation - location in the structure of the application where 
    *                      the error has occurred
    * @param strMessage - message to display
    */
   public OSSMultiException(
      String strLocation,
      String strMessage
   )
   {
      super(strLocation, strMessage);
   }
   
   /**
    * Create new exception
    * 
    * @param strMessage - message to display
    * @param cause - cause for error
    */
   public OSSMultiException(
      String    strMessage, 
      Throwable cause
   )
   {
      super(strMessage, cause);
      add(cause);
   }

   /**
    * Create new exception
    * 
    * @param strLocation - location in the structure of the application where 
    *                      the error has occurred
    * @param strMessage - message to display
    * @param cause - cause for error
    */
   public OSSMultiException(
      String    strLocation,
      String    strMessage, 
      Throwable cause
   )
   {
      super(strLocation, strMessage, cause);
   }
   
   /**
    * Create new exception
    * 
    * @param cause - cause for error
    */
   public OSSMultiException(
      Throwable cause
   )
   {
      super(cause);
      add(cause);
   }
   
   /**
    * Create new exception
    * 
    * @param first - first exception which has occurred
    * @param second - second exception which has occurred
    */
   public OSSMultiException(
      Throwable first,
      Throwable second
   )
   {
      super("Multiple exceptions");
      
      m_lstExceptions = new ArrayList<>(2);
      add(first);
      add(second);
   }

   /**
    * Create new exception
    * 
    * @param strLocation - location in the structure of the application where 
    *                      the error has occurred
    * @param first - first exception which has occurred
    * @param second - second exception which has occurred
    */
   public OSSMultiException(
      String    strLocation,
      Throwable first,
      Throwable second
   )
   {
      super(strLocation, "Multiple exceptions");
      
      m_lstExceptions = new ArrayList<>(2);
      add(first);
      add(second);
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Add new exception which should be thrown together with others.
    * 
    * @param thr - new exception
    */
   public final void add(
     Throwable thr
   )
   {
      if (m_lstExceptions == null)
      {
         m_lstExceptions = new ArrayList<>();
      }

      if (thr instanceof OSSMultiException)
      {
         // Instead of adding the top level exception add the children directly
         OSSMultiException multiExc = (OSSMultiException)thr;
         m_lstExceptions.addAll(multiExc.m_lstExceptions);
      }
      else
      {
         m_lstExceptions.add(thr);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString(
   )
   {
      // Use String.valueOf in case m_lstExceptions is null
      return getClass().getName() + ": " + String.valueOf(m_lstExceptions);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   @SuppressWarnings("CallToThreadDumpStack")
   public void printStackTrace(
   )
   {
      super.printStackTrace();
      
      if (m_lstExceptions != null)
      {
         for (Throwable exc : m_lstExceptions)
         {   
            exc.printStackTrace();
         }
      }
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void printStackTrace(
      PrintStream stream
   )
   {
      super.printStackTrace(stream);
      
      if (m_lstExceptions != null)
      {
         for (Throwable exc : m_lstExceptions)
         {   
            exc.printStackTrace(stream);
         }
      }
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void printStackTrace(
      PrintWriter writer
   )
   {
      super.printStackTrace(writer);
      
      if (m_lstExceptions != null)
      {
         for (Throwable exc : m_lstExceptions)
         {   
            exc.printStackTrace(writer);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public synchronized Throwable initCause(
      Throwable thr
   )
   {
      if (thr != null)
      {
         add(thr);
      }
      
      return this;
   }
}

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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
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
    * @param message - message to display
    */
   public OSSMultiException(String message)
   {
      super(message);
   }

   /**
    * Create new exception
    * 
    * @param message - message to display
    * @param cause - cause for error
    */
   public OSSMultiException(String message, Throwable cause)
   {
      super(message, cause);
      add(cause);
   }

   /**
    * Create new exception
    * 
    * @param cause - cause for error
    */
   public OSSMultiException(Throwable cause)
   {
      super(cause);
      add(cause);
   }
   

   /**
    * Constructor.
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
      
      m_lstExceptions = new ArrayList<Throwable>(2);
      add(first);
      add(second);
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Add new exception which should be thrown together with others.
    * 
    * @param thr - new exception
    */
   public void add(
     Throwable thr
   )
   {
      if (m_lstExceptions == null)
      {
         m_lstExceptions = new ArrayList<Throwable>();
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
   public String toString(
   )
   {
      // Use String.valueOf in case m_lstExceptions is null
      return getClass().getName() + ": " + String.valueOf(m_lstExceptions);
   }

   /**
    * {@inheritDoc}
    */
   public void printStackTrace(
   )
   {
      super.printStackTrace();
      
      for (Iterator<Throwable> exc = m_lstExceptions.iterator(); exc.hasNext();)
      {   
         exc.next().printStackTrace();
      }
   }
   
   /**
    * {@inheritDoc}
    */
   public void printStackTrace(
      PrintStream stream
   )
   {
      super.printStackTrace(stream);
      
      for (Iterator<Throwable> exc = m_lstExceptions.iterator(); exc.hasNext();)
      {   
         exc.next().printStackTrace(stream);
      }
   }
   
   /**
    * {@inheritDoc}
    */
   public void printStackTrace(
      PrintWriter writer
   )
   {
      super.printStackTrace(writer);
      
      if (m_lstExceptions != null)
      {
         for (Iterator<Throwable> exc = m_lstExceptions.iterator(); exc.hasNext();)
         {   
            exc.next().printStackTrace(writer);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
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

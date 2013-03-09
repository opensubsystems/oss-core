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

import java.util.LinkedList;
import java.util.List;

/**
 * Class that represents unlimited queue, that is synchronized. It means that
 * the consumer of the objects from the queue waits/is blocked in the get 
 * method until there is an object available.
 *
 * @author bastafidli
 */
public class SynchronizedQueue<T> extends OSSObject
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Cache of object produced by producer and consumed by consumer.
    */
   protected List<T> m_lstObjects;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor for Synchronized Queue Object.
    */
   public SynchronizedQueue(
   )
   {
      super();

      m_lstObjects = new LinkedList<>();
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Get the object from the beginning of the queue
    *
    * @return Object - object from the queue, if the thread is blocked in this
    *                  function and you call interrupt method, an InterruptedException
    *                  will be thrown.
    * @exception InterruptedException - if the thread is blocked in this
    *                                   function and you call interrupt method,
    *                                   an InterruptedException will be thrown.
    */
   public synchronized T get(
   ) throws InterruptedException
   {
      T objReturn;

      if (m_lstObjects.isEmpty())
      {
         // There is no object in the queue, go to sleep
         try
         {
            wait();
         }
         catch (InterruptedException ieException)
         {
            // Somebody woke us up, that means all threads waiting on this
            // object competed for the lock and this one won and the object is
            // locked again
            // The thread can be woken up in two conditions, producer put new
            // object into the queue or somebody called interrupt - to interrupt
            // the wait - in this case rethrow an exception
            if (m_lstObjects.isEmpty())
            {
               throw ieException;
            }
         }
      }

      // Remove the first object in the queue
      objReturn = m_lstObjects.remove(0);

      return objReturn;
   }

   /**
    * Put the object to the end of the queue.
    *
    * @param objNew - new object, can be null
    */
   public synchronized void put(
      T objNew
   )
   {
      m_lstObjects.add(objNew);
      // New object in the queue, notify others
      notifyAll();
   }

   /**
    * Test if the queue is empty.
    *
    * @return boolean - true if the queue is empty
    */
   public synchronized boolean isEmpty(
   )
   {
      return m_lstObjects.isEmpty();
   }
}

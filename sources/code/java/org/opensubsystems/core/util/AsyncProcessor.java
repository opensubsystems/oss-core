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

package org.opensubsystems.core.util;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.opensubsystems.core.error.OSSException;

/**
 * Thread used to asynchronously process items added to the queue. The items
 * are processed in the order as the are added to the queue and the thread
 * blocks if the queue is empty. 
 * 
 * This class cannot be abstract so that we can do the in place overriding using 
 * anonymous classes.
 *  
 * The class, which wants to perform asynchronous data processing will usually 
 * inline derive new class from this class and define method processItemNow().
 * 
 *    AsyncProcessor asyncAction; 
 * 
 *    asyncAction = new AsyncProcessor("My action")
 *    {
 *       protected void processItemNow(
 *          Object objItem
 *       ) throws OSSException
 *       {
 *          if (GlobalConstants.ERROR_CHECKING)
 *          {
 *             assert objItem != null : "Cannot process null item";
 *          }
 *              
 *          // do something with the item
 *       }
 *    };
 * 
 * The it will start the processor and optionally mark it as daemon in case it
 * should run forever until the JVM shuts down.
 *    
 *    // Mark this thread as deamon since it will be running forever
 *    asyncAction.setDaemon(true);
 *    // Start the printer releaser to be ready to release printer when needed
 *    asyncAction.start();
 *    
 * Then you can at any time add an item that should be processed asynchronously
 * 
 *    asyncAction.processItemLater(someData);
 *    
 * There is no callback capability built into this class to notify when the
 * processing is done since you can easily implement as a last line it in the 
 * overwritten processItemNow() method.   
 *        
 * @author bastafidli
 */
public class AsyncProcessor<T> extends Thread
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Synchronized queue use to keep data which should be processed 
    * asynchronously. It is also used to block the thread if there is nothing 
    * to process.
    */
   protected SynchronizedQueue<T> m_syncQueue;

   /**
    * By setting this flag to false we can stop the thread.
    */
   protected boolean m_bKeepRunning;

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(AsyncProcessor.class);

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Default constructor
    * 
    * @param strName - name of this processor 
    */
   public AsyncProcessor(
      String strName         
   )
   {
      super(strName);
      
      m_syncQueue = new SynchronizedQueue<>();
      m_bKeepRunning = true;
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Method called during thread execution.  
    */
   @Override
   public void run(
   )
   {
      T itemToProcess;
      
      while (m_bKeepRunning)
      {
         try
         {
            // This call will block if there is no item to process
            itemToProcess = m_syncQueue.get();
            
            // This is invalid assumption since we can put null items to the 
            // queue 
            // assert itemToProcess != null 
            //        : "Synchronized queue cannot return null item";
            
            if (itemToProcess != null)
            {   
               s_logger.log(Level.FINEST, "{0}: Going to process {1}", 
                            new Object[]{getName(), itemToProcess.toString()});
            }
            else
            {
               s_logger.log(Level.FINEST, "{0}: Going to process null object.", 
                            getName());         
            }
            processItemNow(itemToProcess);
         }
         catch (InterruptedException ieExc)
         {
            // The thread was release from block even though there was no item
            // to process
         }
         catch (Throwable thr)
         {
            // Catch the exception inside of the loop to prevent the thread from 
            // dying. THe thread should be a deamon thread.
            s_logger.log(Level.WARNING, 
                         getName() 
                         + ": Unexpected error has occurred while processing data.",
                         thr);
         }
      }
   }
   
   /**
    * Stop processing done by this thread as soon as possible
    *
    */
   public void stopProcessing(
   )
   {
      // TODO: Improve: This may not be completely correct, there might be race
      // condition between while and get() and call of this method. Hopefully
      // most threads will be daemons so solve it later.
      m_bKeepRunning = false;
      // The thread may be blocked so we need to interrupt it
      interrupt();
   }
   
   /**
    * Add item to the queue to be processed later.  
    * This method is called synchronously, the item is put into the queue, 
    * the calling thread returns then the item is taken out of the queue by 
    * this thread and overridden processItemNow method is called to process it.
    * 
    * @param objItem - item to process, this might be null if null was put into
    *                  the queue
    */
   public void processItemLater(
      T objItem
   )
   {
      // This make wake up the thread above
      if (objItem != null)
      {   
         s_logger.log(Level.FINEST, "{0}: Adding object to the queue {1}", 
                      new Object[]{getName(), objItem.toString()});
      }
      else
      {
         s_logger.log(Level.FINEST, "{0}: Adding null object to the queue.", 
                      getName());         
      }
      m_syncQueue.put(objItem);
   }
   
   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Process item which was placed into the queue in processItemLater. 
    * This method is called asynchronously, the item is put into the queue, 
    * the calling thread returns then the item is taken out of the queue by 
    * this thread and this method is called to process it.
    * 
    * This method cannot be abstract so that we can do the in place overriding 
    * of the class.
    * 
    * @param objItem - item to process, this might be null if null was put into
    *                  the queue
    * @throws OSSException - an error has occurred
    */
   protected void processItemNow(
      T objItem
   ) throws OSSException
   {
      throw new UnsupportedOperationException("This method must be overridden.");
   }
}

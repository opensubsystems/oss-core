/*
 * Copyright (C) 2003 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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
 
package org.opensubsystems.core.util.jta;

import java.util.logging.Logger;
import java.util.logging.Level;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.OSSObject;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * Set of common utility methods related to transaction management.
 * 
 * @author bastafidli
 */
public final class TransactionUtils extends OSSObject
{
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(DatabaseUtils.class);
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private TransactionUtils(
   )
   {
      // Do nothing
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Gracefully rollback transaction so that no error is generated. 
    * This method NEVER throws any exception therefore it is safe to call it
    * usually in catch clause when transaction needs to be rollbacked.  
    * 
    * @param transaction - transaction to rollback, if null it is ignored
    */
   public static void rollback(
      UserTransaction transaction
   )
   {
      if (transaction != null)
      {
         try
         {
            transaction.rollback();
         }
         catch (SystemException seExc)
         {
            s_logger.log(Level.SEVERE, "Cannot rollback transaction.", seExc);
         }
      }
   }
   
   /**
    * Test if transaction is in progress.
    * 
    * @param transaction - transaction to test
    * @return boolean - if true then transaction is in progress for current 
    *                   thread.
    * @throws OSSException - error occurred while getting the transaction status
    */
   public static boolean isTransactionInProgress(
      UserTransaction transaction
   ) throws OSSException 
   {
      int iStatus = Status.STATUS_NO_TRANSACTION;
      
      if (transaction != null)
      {
         // Transaction might be null if there is an error to initialize 
         // the persistence layer
         try
         {
            iStatus = transaction.getStatus();
         }
         catch (SystemException seExc)
         {
            throw new OSSDatabaseAccessException(seExc);
         }
      }

      return ((iStatus != Status.STATUS_NO_TRANSACTION)
              && (iStatus != Status.STATUS_COMMITTED)
              && (iStatus != Status.STATUS_ROLLEDBACK));
   }
}

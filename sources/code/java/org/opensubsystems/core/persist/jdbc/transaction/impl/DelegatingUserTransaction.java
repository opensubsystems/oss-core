/*
 * Copyright (C) 2005 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

package org.opensubsystems.core.persist.jdbc.transaction.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.opensubsystems.core.util.Log;

/**
 * This class is wrapper around user transaction so that we can monitor 
 * individual operations.
 *
 * @author bastafidli
 */
public class DelegatingUserTransaction implements UserTransaction
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Original UserTransaction, to which all the calls are delegated.
    */
   protected UserTransaction m_originalTransaction;
   
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(DelegatingUserTransaction.class);

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor.
    * 
    * @param originalTransaction - original transaction to delegate calls to.
    */
   public DelegatingUserTransaction(
      UserTransaction originalTransaction
   )
   {
      assert originalTransaction != null
             : "Cannot delegate to null transaction.";
      
      m_originalTransaction = originalTransaction;   
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void begin(
   ) throws NotSupportedException, 
            SystemException
   {
      s_logger.log(Level.FINEST, "UserTransaction.begin for {0}", 
                   m_originalTransaction.toString());
      m_originalTransaction.begin();
      s_logger.log(Level.FINEST, "UserTransaction.begin successful for {0}", 
                   m_originalTransaction.toString());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void commit(
   ) throws HeuristicMixedException,
            HeuristicRollbackException,
            IllegalStateException,
            RollbackException,
            SecurityException,
            SystemException
   {
      s_logger.log(Level.FINEST, "UserTransaction.commit for {0}", 
                   m_originalTransaction.toString());
      m_originalTransaction.commit();
      s_logger.log(Level.FINEST, "UserTransaction.commit successful for {0}", 
                   m_originalTransaction.toString());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void rollback(
   ) throws IllegalStateException, 
            SecurityException, 
            SystemException
   {
      s_logger.log(Level.FINEST, "UserTransaction.rollback for {0}", 
                   m_originalTransaction.toString());
      m_originalTransaction.rollback();
      s_logger.log(Level.FINEST, "UserTransaction.rollback successful for {0}", 
                   m_originalTransaction.toString());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getStatus(
   ) throws SystemException
   {
      return m_originalTransaction.getStatus();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setRollbackOnly(
   ) throws IllegalStateException, 
            SystemException
   {
      m_originalTransaction.setRollbackOnly();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setTransactionTimeout(
      int iTransactionTimeout
   ) throws SystemException
   {
      m_originalTransaction.setTransactionTimeout(iTransactionTimeout);
   }
}

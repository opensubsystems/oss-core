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

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.opensubsystems.core.error.OSSException;

/**
 * Interface to encapsulate transaction activities.
 * Transaction factory is here to implement the Abstract Factory pattern 
 * as described in http://homepage.mac.com/loeffler/java/patterns/absfac.html 
 * by GoF95 http://homepage.mac.com/loeffler/java/patterns.html.
 * There are different types of transactions. Some may include just the 
 * persistence layer, some may include the even the actions executed at some
 * higher layer. This interface provides standard way how to access the transaction
 * abstraction regardless of how the transaction manager is implemented.
 * 
 * One may wonder why the TransactionFactory is not part of persistence
 * layer and instead it is in utility package. The reason is that transaction
 * is a logical unit of work which may include other activities than just 
 * persisting piece of data. For example sending of a message may be part of
 * transaction. If then the message should be rollbacked, the client needs to 
 * have ability to establish transaction, which allows rollbacking of action 
 * (of sending data) rather than action of persisting the message. 
 * 
 * @author bastafidli
 */
public interface TransactionFactory
{
   /**
    * Get transaction object which we can use to begin/commit/rollback 
    * transactions. This operation is valid only if the transaction factory
    * support explicit transaction across multiple connections. 
    * 
    * @return UserTransaction - null if explicit transactions are not supported.
    * @throws OSSException - an error has occurred
    */
   UserTransaction requestTransaction(
   ) throws OSSException;

   
   /**
    * Get transaction manager for this factory.
    * 
    * @return TransactionManager
    */
   TransactionManager getTransactionManager(
   );
   
   /**
    * This method is here mainly for testing and it should reset the transaction
    * manager to initial status to that tests can start from known environment
    * instead of being influenced by other tests.
    * 
    * @throws OSSException - an error has occurred during reset
    */
   void reset(
   ) throws OSSException;
   
   /**
    * Stop the transaction factory.
    * 
    * @throws OSSException - problem stopping transaction factory.
    */
   void stop(
   ) throws OSSException;
}

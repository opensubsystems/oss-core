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

package org.opensubsystems.core.persist.jdbc.transaction.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseConnectionFactoryImpl;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseTransactionFactoryImpl;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.jta.TransactionUtils;

/**
 * Simplified transaction management implementation based on database connection
 * using following assumptions:
 * 
 * Assumptions:
 * ------------
 * 1. Most applications use/access only single database, therefore there is no 
 *    need for distributed transaction implementation.
 * 2. Applications consists of threads and each thread is using/needing only 
 *    single database connection at a time. Most threads will never need two
 *    connections at the same time to the same database.
 * 3. At most one transaction is in progress at any time in one thread.
 * 
 * Therefore if an application is accessing only a single database and threads 
 * of the application use only single connection at a time and start at most 
 * one transaction at a time the application can use this implementation of 
 * transaction manager.
 * 
 * How it works:
 * -------------
 * 1. Thread is using JDBC database connection to access the database.
 * 2. If thread starts an transaction using UserTransaction.begin then the 
 *    connection used to access the database in this transaction has to have
 *    autocommit set to false and the thread cannot issue commit or rollback on
 *    the transaction. Then when thread ends transaction using 
 *    UserTransaction.commit or UserTransaction.rollback then the transaction 
 *    manager commits or rollbacks connections which was used to access the
 *    database within this transaction.
 * 3. If thread is accessing the database outside of transaction, it can do 
 *    whatever it wants with the connection including setting autocommit to true
 *    and calling commit and rollback on the connection.
 * 
 * There are 4 basic and 4 combined scenarios possible:
 * ----------------------------------------------------
 * 1. Connection is requested before the transaction and returned after the transaction
 *   
 * DatabaseConnectionFactory.requestConnection
 * UserTransaction.begin
 * UserTransaction.commit/rollback
 * DatabaseConnectionFactory.returnConnection
 * 
 * 2. Connection is requested in the transaction and returned after the transaction
 *   
 * UserTransaction.begin
 * DatabaseConnectionFactory.requestConnection
 * UserTransaction.commit/rollback
 * DatabaseConnectionFactory.returnConnection
 * 
 * 3. Connection is requested before the transaction and returned in the transaction
 * 
 * DatabaseConnectionFactory.requestConnection
 * UserTransaction.begin
 * DatabaseConnectionFactory.returnConnection
 * UserTransaction.commit/rollback
 * 
 * 4. Connection is requested in the transaction and returned in the transaction
 * 
 * UserTransaction.begin
 * DatabaseConnectionFactory.requestConnection
 * DatabaseConnectionFactory.returnConnection
 * UserTransaction.commit/rollback
 * 
 * 5. Connection is requested before the transaction and returned in the transaction
 *    and then connection is requested again ithe transaction and returned in
 *    the transaction
 * 
 * DatabaseConnectionFactory.requestConnection
 * UserTransaction.begin
 * DatabaseConnectionFactory.returnConnection
 * DatabaseConnectionFactory.requestConnection
 * DatabaseConnectionFactory.returnConnection
 * UserTransaction.commit/rollback
 * 
 * 6. Connection is requested before the transaction and returned in the transaction
 *    and then connection is requested again ithe transaction and returned after
 *    the transaction
 * 
 * DatabaseConnectionFactory.requestConnection
 * UserTransaction.begin
 * DatabaseConnectionFactory.returnConnection
 * DatabaseConnectionFactory.requestConnection
 * UserTransaction.commit/rollback
 * DatabaseConnectionFactory.returnConnection
 *
 * 7. Connection is requested in the transaction and returned in the transaction
 *    and then connection is requested again ithe transaction and returned after
 *    the transaction
 * 
 * UserTransaction.begin
 * DatabaseConnectionFactory.requestConnection
 * DatabaseConnectionFactory.returnConnection
 * DatabaseConnectionFactory.requestConnection
 * UserTransaction.commit/rollback
 * DatabaseConnectionFactory.returnConnection
 *
 * 8. Connection is requested in the transaction and returned in the transaction
 *    and then connection is requested again ithe transaction and returned in
 *    the transaction
 * 
 * UserTransaction.begin
 * DatabaseConnectionFactory.requestConnection
 * DatabaseConnectionFactory.returnConnection
 * DatabaseConnectionFactory.requestConnection
 * DatabaseConnectionFactory.returnConnection
 * UserTransaction.commit/rollback
 * 
 * What are the issues:
 * --------------------
 * A. If thread acquires connection before the transaction is started then at
 *    the time when the transaction is started we have to associate the 
 *    connection with the transaction. 
 *    Affects: 1,3,5,6
 * 
 * A.1 If the connection wasn't used within the transaction the the commit or 
 *     rollback on transaction should be a no op.
 * 
 * B. If the thread acquired connection during the transaction then this 
 *    connection should be automatically associated with the transaction
 *    Affects: 2,4,5,6,7,8
 * 
 * C. If thread returns the connection before the transaction is finished then
 *    the transaction manager has to keep the connection open until the thread
 *    ends the transaction.
 *    Affects: 3,4,5,8
 * 
 * D: If the thread keeps the connection after the transaction is finished then
 *    after the transaction is finished the connection should be fully usable
 *    as any other JDBC connection including autocommit and commit/rollback
 *    but in the transaction the autocommit/commit/rollback should be disabled.
 *    Affects: 1,2,6,7
 * 
 * E: If the thread requests and returns connection multiple times during the
 *    same transaction then the same connection has to be given to it since
 *    the transaction is responsible for doing commit/rollback on the connection
 *    and our assumption is that the thread is using only single connection.
 *    Affects: 5,6,7,8
 * 
 * What are the solutions:
 * -----------------------
 * A: The DatabaseConnectionFactory has to keep track if there is a connection 
 *    issued to the calling thread so that when the transaction is started then  
 *    this connection can be associated to the transaction.
 *    This can be done using ThreadLocal storing the requested connection. Then
 *    when the transaction is started, the UserTransaction has to check if there 
 *    is an already requested connection and associate it with the transaction. 
 *    This can be done using ThreadLocal storing the transactional connection.   
 * 
 * A.1 This can be done be creating wrapper around Connection. 
 *    The DatabaseConnectionFactory will then on request and return create this
 *    wrapper which delegates all calls into underlying connection. When any 
 *    method is called, it marks the connection as used. The transaction manager
 *    can then reset the used flag when the connection is first time associated
 *    with transaction or check it once commit or rollback were issued. 
 * 
 * B: The DatabaseConnectionFactory has to be aware of the started transaction
 *    and when an connection is requested then the connection is associated 
 *    with the transaction. 
 *    This can be done using ThreadLocal storing a flag if the transaction is
 *    in progress or not. If the transaction is in progress then the returned
 *    connection will be stored in the transactional connection.
 * 
 * C: The UserTransaction has to keep track of the connection which was used
 *    in the transaction and don't allow to return it until the transaction is
 *    finished. Then, once the transaction is finished it has to return the 
 *    connection.
 *    This can be done using ThreadLocal storing a flag for the transactional 
 *    connection if the connection was returned in the transaction or not.
 * 
 * D: The connection should be aware if there is a transaction in progress or 
 *    not and based on that allow autocommit and commit or rollback or not.
 *    This can be done be creating wrapper around Connection. 
 *    The DatabaseConnectionFactory will then on request and return create this
 *    wrapper which delegates all calls into underlying connection and intercepts
 *    and checks autocommit and commit and rollback. 
 * 
 * E. The DatabaseConnectionFactory has to be aware of the started transaction
 *    and when a connection is requested then the connection already associated 
 *    with the transaction should be returned.   
 * 
 * What are the implications:
 * --------------------------
 * 1. If the connection is associated with transaction it is not returned to the
 *    factory and not available for others. This is natural since there are some
 *    operations pending on that connection and even though the thread said it 
 *    no longer needs it, it cannot be reused until the operations are flushed  
 *    with commit/rollback.
 * 
 * Implementation:
 * ---------------
 * 1. D. requires to create wrapper around connection. We will implement 
 *    TransactionalConnection class as wrapper around JDBC Connection class, 
 *    which will delegate all operations directly except setautocommit/commit/rollback,
 *    which will be delegated only after check for pending transaction. 
 * 
 * 2. The previous step required us to create integration between DatabaseTransactionFactory 
 *    and DatabaseConnectionFactory, so that the transaction factory can act as 
 *    a proxy for connection factory and get the real connection and wrap 
 *    it with a wrapper. This will be implemented as a delegator pattern
 *    when DatabaseConnectionFactoryImpl will be delegating calls to 
 *    DatabaseTransactionFactoryImpl and vice versa.
 *  
 *    The requestConnection call will 
 *    - check if there is an transaction in progress, if there is and it has 
 *      already associated connection then the connection associated with the
 *      transaction will be returned.
 *    - if there is no transaction in progress and there is already connection 
 *      issued then return the same connection since this would represent the 
 *      situation (such as subprocedure call)
 *         DatabaseConnectionFactory.requestConnection
 *         DatabaseConnectionFactory.requestConnection
 *         DatabaseConnectionFactory.returnConnection
 *         DatabaseConnectionFactory.returnConnection
 *    - if there is no transaction in progress and no already issued connection
 *      then get connection from underlying connection factory, create wrapper 
 *      and remember the connection as issued using ThreadLocal. 
 *    This will solve A, B and E
 * 
 *    The returnConnection call will
 *    - check if there is an transaction in progress, if there is then the 
 *      connection will not be returned to the factory and will be returned when
 *      the transaction is done
 *    - if there is no transaction in progress then the underlying connection 
 *      will be returned to the factory
 *    This will solve C
 *    
 * 3. We will provide implementation of UserTransaction. 
 *   
 *    The begin call will 
 *     - check if there was connection issued and if it was, it will associate 
 *       it with this transaction. It will also tell the connection that it 
 *       is part of the transaction to ignore the setautocommit/commit/rollback
 *       calls.
 *    
 *    The commit call will
 *    - if there is connection associated with this transaction, it will commit 
 *      the connection. If the connection was returned to the factory in the 
 *      transaction then this time it will be really returned to the factory 
 *      otherwise it will be just disassociated from this transaction so that 
 *      the setautocommit/commit/rollback calls are no longer ignored.
 * 
 *    The rollback call will 
 *    - if there is connection associated with this transaction, it will rollback 
 *      the connection. If the connection was returned to the factory in the 
 *      transaction then this time it will be really returned to the factory 
 *      otherwise it will be just disassociated from this transaction so that 
 *      the setautocommit/commit/rollback calls are no longer ignored.
 * 
 * TransactionalConnection
 * To distinguish if the TransactionalConnection was associated in transaction
 * it will have inTransaction flag. 
 * To distinguish if the TransactionalConnection was used during the transaction
 * it will have used flag which will be false initially and set to true if  
 * any method on connection is called. 
 * To distinguish if the TransactionalConnection was returned to factory or it 
 * is still used by application it will have active counter which will be 
 * incremented when the connection is requested from factory and decremented
 * when it is returned. 
 * 
 * @author bastafidli
 */
public class SimpleLocalTransactionFactoryImpl extends    DatabaseTransactionFactoryImpl
                                               implements UserTransaction
{
   // Constants ////////////////////////////////////////////////////////////////
   
  /**
   * A transaction is associated with the target object and it is in the active state.
   */ 
   public static final Integer STATUS_ACTIVE_OBJ 
                                  = new Integer(Status.STATUS_ACTIVE);
   
  /**
   * A transaction is associated with the target object and it has been committed.
   */ 
   public static final Integer STATUS_COMMITTED_OBJ 
                                  = new Integer(Status.STATUS_COMMITTED);

  /**
   * A transaction is associated with the target object and it is in the process
   * of committing.
   */ 
   public static final Integer STATUS_COMMITTING_OBJ 
                                  = new Integer(Status.STATUS_COMMITTING);

  /**
   * A transaction is associated with the target object and it has been marked 
   * for rollback, perhaps as a result of a setRollbackOnly operation.
   */ 
   public static final Integer STATUS_MARKED_ROLLBACK_OBJ 
                                  = new Integer(Status.STATUS_MARKED_ROLLBACK);

  /**
   * No transaction is currently associated with the target object.
   */ 
   public static final Integer STATUS_NO_TRANSACTION_OBJ 
                                  = new Integer(Status.STATUS_NO_TRANSACTION);

  /**
   * A transaction is associated with the target object and it has been prepared.
   */ 
   public static final Integer STATUS_PREPARED_OBJ 
                                  = new Integer(Status.STATUS_PREPARED);

  /**
   * A transaction is associated with the target object and it is in the process
   * of preparing.
   */ 
   public static final Integer STATUS_PREPARING_OBJ 
                                  = new Integer(Status.STATUS_PREPARING);

  /**
   * A transaction is associated with the target object and the outcome has been
   * determined as rollback.
   */ 
   public static final Integer STATUS_ROLLEDBACK_OBJ 
                                  = new Integer(Status.STATUS_ROLLEDBACK);

  /**
   * A transaction is associated with the target object and it is in the process
   * of rolling back.
   */ 
   public static final Integer STATUS_ROLLING_BACK_OBJ 
                                  = new Integer(Status.STATUS_ROLLING_BACK);

  /**
   * A transaction is associated with the target object but its current status
   * cannot be determined.
   */ 
   public static final Integer STATUS_UNKNOWN_OBJ 
                                  = new Integer(Status.STATUS_UNKNOWN);
      
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Database connection which this thread requested from the factory. The 
    * current assumption is that the thread can have only one connection at 
    * a time. The connection can be part of global transaction (since there is 
    * only one transaction per thread then this connection really represents 
    * the transaction). The stored data have type TransactionConnection.
    */
   private ThreadLocal<TransactionalConnection> m_connection; 
   
   /**
    * This is the real database connection which is wraped in TransactionConnection
    * since the TransactionConnection doesn't allow us to access it.
    */
   private ThreadLocal<Connection> m_realConnection; 

   /**
    * Transaction that is currently in progress if any. The current assumption
    * is that there can be only one transaction active per thread. The stored
    * data have type Integer with values defined in javax.transaction.Status
    * interface.
    */
   private ThreadLocal<Integer> m_transaction;
   
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(
                                       SimpleLocalTransactionFactoryImpl.class);

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Default constructor using default database connection factory.
    * 
    * @throws OSSException - an error has occurred 
    */
   public SimpleLocalTransactionFactoryImpl(
   ) throws OSSException
   {
      super();
      
      // Don't make it inheritable so that spawn threads can have their own
      // connections and transactions
      m_connection     = new ThreadLocal<>();
      m_realConnection = new ThreadLocal<>();
      m_transaction    = new ThreadLocal<>();
   }

   // DatabaseTransactionFactory methods ///////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   public UserTransaction requestTransaction()
   {
      // This class acts also as user transaction
      UserTransaction transaction = this;
      
      if (isTransactionMonitored())
      {
         transaction = new DelegatingUserTransaction(transaction);
      }
      return transaction;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void reset(
   ) throws OSSException
   {
      // The only thing we can do is reset the thread local again. This method
      // should be used only for testing
      s_logger.severe("About to reset state of transaction manager." +
                      " Hope you know what you are doing.");
      m_connection     = new ThreadLocal<>();
      m_realConnection = new ThreadLocal<>();      
      m_transaction    = new ThreadLocal<>();
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void stop(
   ) throws OSSException
   {
      // The transaction factory stop is noop since we don't do here anything 
      // special 
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public TransactionManager getTransactionManager(
   )
   {
      // There is no transaction manager for this factory since it would require
      // us to implement the XA classes it references and we really care only
      // about UserTransaction
      return null;
   }
   
   // UserTransaction methods //////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void begin(
   ) throws NotSupportedException, 
            SystemException
   {
      try
      {
         if (isTransactionInProgress())
         {
            throw new NotSupportedException("Cannot start another transaction" 
                                            + " while one is still in progress."); 
         }
         else
         {
            m_transaction.set(STATUS_ACTIVE_OBJ);
            
            TransactionalConnection existingConnection;
            
            existingConnection = m_connection.get();
            if (existingConnection != null)
            {
               // There is already a connection issued from the factory which  
               // was issued before the transaction was started so make sure  
               // the connection knows it is in transaction so it doesn't allow
               // commit/rollback/setautocommit
               if (GlobalConstants.ERROR_CHECKING)
               {
                  assert (!existingConnection.isInTransaction())
                         : "Connection cannot be already in transaction when" 
                           + " transaction is only starting.";
               }
               
               try
               {
                  // The connection wasn't used in transaction yet
                  existingConnection.setUsed(false);
                  existingConnection.setInTransaction(true);
               }
               catch (SQLException sqleExc)
               {
                  SystemException sysExc = new SystemException(
                               "Cannot associate connection with transaction.");
                  sysExc.initCause(sqleExc);
                  throw sysExc;
               }
            }
            else
            {
               // If there is no connection then we don't have to worry about 
               // and anything when the connection will be requested from the 
               // factory it will be associated to this transaction.
            }
         }
      }
      catch (OSSException ossExc)
      {
         Throwable thr;
         thr = ossExc.getCause();
         if ((thr != null) && (thr instanceof SystemException))
         {
            throw (SystemException)thr;
         }
         else
         {
            throw new SystemException("Error occurred while retrieving information" 
                                      + " about transaction status.");
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void commit(
   ) throws RollbackException, 
            HeuristicMixedException, 
            HeuristicRollbackException, 
            SecurityException, 
            IllegalStateException, 
            SystemException
   {
      endTransaction(true);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getStatus(
   ) throws SystemException
   {
      Integer iTransactionStatus = m_transaction.get();
      int     iStatus;
      
      if (iTransactionStatus == null)
      {
         iStatus = Status.STATUS_NO_TRANSACTION;
      }
      else
      {
         iStatus = iTransactionStatus.intValue();
      }
      
      return iStatus;      
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
      endTransaction(false);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setRollbackOnly(
   ) throws IllegalStateException, 
            SystemException
   {
      try
      {
         if (isTransactionInProgress())
         {
            m_transaction.set(STATUS_MARKED_ROLLBACK_OBJ);
         }
         else
         {
            throw new IllegalStateException("No transaction in progress to be" 
                                            + " marked rollbacked only."); 
         }
      }
      catch (OSSException ossExc)
      {
         Throwable thr;
         thr = ossExc.getCause();
         if ((thr != null) && (thr instanceof SystemException))
         {
            throw (SystemException)thr;
         }
         else
         {
            throw new SystemException("Error occurred while retrieving information"
                                      + " about transaction status.");
         }
      }
   }

   /**
    * Test if transaction is in progress.
    *
    * @return boolean - if true then transaction is in progress for current 
    *                   thread.
    * @throws OSSException - error occurred while getting the transaction status
    */
   @Override
   public boolean isTransactionInProgress(
   ) throws OSSException 
   {
      return TransactionUtils.isTransactionInProgress(requestTransaction());
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void setTransactionTimeout(
      int arg0
   ) throws SystemException
   {
      // At this time there is no way how to set time out for transaction
      // TODO: Feature: Implement transaction timeout for this factory
   }   
   
   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * Check if there is an transaction in progress, if there is and it has 
    * already associated connection then the connection associated with the
    * transaction will be returned.
    * If there is no transaction in progress and there is already connection 
    * issued then return the same connection since this would represent the 
    * situation (such as subprocedure call)
    *         DatabaseConnectionFactory.requestConnection
    *         DatabaseConnectionFactory.requestConnection
    *         DatabaseConnectionFactory.returnConnection
    *         DatabaseConnectionFactory.returnConnection
    * If there is no transaction in progress and no already issued connection
    * then get connection from the calling connection factory, create wrapper 
    * and remember the connection as issued using ThreadLocal.
    * 
    * @param bAutoCommit {@inheritDoc}
    * @param strDataSourceName {@inheritDoc}
    * @param strUser {@inheritDoc}
    * @param strPassword {@inheritDoc}
    * @param connectionFactory {@inheritDoc}
    * @return {@inheritDoc}
    * @throws OSSDatabaseAccessException {@inheritDoc}
    */
   @Override
   protected Connection requestTransactionalConnection(
      boolean                       bAutoCommit,
      String                        strDataSourceName, 
      String                        strUser, 
      String                        strPassword, 
      DatabaseConnectionFactoryImpl connectionFactory
   ) throws OSSException
   {
      TransactionalConnection existingConnection;
      Connection              existingRealConnection;
      boolean                 bTransaction;
      
      existingConnection = m_connection.get();
      existingRealConnection = m_realConnection.get();
      bTransaction = isTransactionInProgress();

      if (existingConnection != null)
      {         
         // There is already a connection issued from the factory so it should
         // be already associated with transaction so test for it 
         if (GlobalConstants.ERROR_CHECKING)
         {
            assert (((!existingConnection.isInTransaction()) && (!bTransaction))
                   || ((existingConnection.isInTransaction()) && (bTransaction)))
                   : "Connection status about transaction in progress doesn't" +
                     " match the reality.";
            assert existingConnection.verifyConnection(existingRealConnection)
                   : "The real database connection is not the one which is wrapped"
                     + " in transactional wrapper.";
         }
         
         // Now make sure that this is connection for the same user as requested
         String strCurrentDataSourceName;
         String strExistingDataSourceName;
         String strExistingUser;
         String strExistingPassword;
         
         if (strDataSourceName == null)
         {
            strCurrentDataSourceName = connectionFactory.getDefaultDataSourceName();
         }
         else
         {
            strCurrentDataSourceName = strDataSourceName;
         }
         strExistingDataSourceName = existingConnection.getDataSourceName();
         strExistingUser = existingConnection.getUser();
         strExistingPassword = existingConnection.getPassword();
         
         // I think we do want to compare as == since we want to verify that the
         // same connection factory for which we already have connection
         // is trying to get a new one
         if (connectionFactory != existingConnection.getConnectionFactory())
         {
            throw new OSSDatabaseAccessException(
                         "Cannot issue connection from the factory for a different" 
                         + " connection factory that the factory for which the" 
                         + " connection is already issued.");         
         }
         
         if ((((strCurrentDataSourceName == null) && (strExistingDataSourceName == null))
               || ((strCurrentDataSourceName != null)  
                  && (strCurrentDataSourceName.equals(strExistingDataSourceName))))
            && (((strUser == null) && (strExistingUser == null))
               || ((strUser != null) && (strUser.equals(strExistingUser))))
            && (((strPassword == null) && (strExistingPassword == null))
               || ((strPassword != null) && (strPassword.equals(strExistingPassword)))))
         {
            // We have to reinitialize the connection since the connection can 
            // have for example incorrect autocommit state and needs to be 
            // initialized to the desired state 
            try
            {
               connectionFactory.initializeConnection(existingRealConnection, 
                                                      bAutoCommit);
            }
            catch (SQLException sqlExc)
            {
               throw new OSSDatabaseAccessException(
                        "Cannot reinitialize connection.", sqlExc);
            }
            // Make this connection active again since we are issuing it from 
            // the factory
            existingConnection.setActive(true);
         }
         else
         {
            // The data source, user name or password doesn't match so do not
            // return connection since the assumption is that there can be
            // only one connection at a time
            throw new OSSDatabaseAccessException(
                         "Cannot issue connection from the factory for a different" 
                         + " user than the user for which the connection is"
                         + " already issued.");
         }
      }
      else
      {
         Connection realConnection;
         
         // There is no connection yet so create a new one. Here we have to
         // call the non transaction version of the requestConnection method
         // since the transactional version is the one which called us. We just
         // let the super class to deal with it and call the proper 
         // nontransactional method
         realConnection = super.requestTransactionalConnection(bAutoCommit, 
                             strDataSourceName, strUser, strPassword, 
                             connectionFactory);
         
         if (strDataSourceName == null)
         {
            strDataSourceName = connectionFactory.getDefaultDataSourceName();
         }
         
         existingConnection = new TransactionalConnection(
                                     realConnection,
                                     strDataSourceName,
                                     strUser,
                                     strPassword,
                                     bTransaction,
                                     connectionFactory);
         // Remember this connection
         m_connection.set(existingConnection);
         // Remember the real connection since we will need to return it and
         // the TransactionalConnection doesn't allow us to access it 
         m_realConnection.set(realConnection);
      }
      
      return existingConnection;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void returnTransactionalConnection(
      Connection                    cntDBConnection,
      DatabaseConnectionFactoryImpl connectionFactory
   )
   {
      if (cntDBConnection != null)
      {
         if (cntDBConnection instanceof TransactionalConnection)
         {
            // This is a transaction aware connection so process it as such
            TransactionalConnection connection;
            Connection              realConnection;
      
            connection = (TransactionalConnection)cntDBConnection;      
            realConnection = m_realConnection.get();
            if (GlobalConstants.ERROR_CHECKING)
            {
               TransactionalConnection existingConnection;
               existingConnection = m_connection.get();
               
               // The returned connection is not the one we think we issued so this 
               // is error
               assert (existingConnection != null) 
                      && (existingConnection == connection) // this should be ==
                      : "The returned connection is not the one we think we issued:" 
                        + " existing connection is " + existingConnection 
                        + " returned connection is " + connection;
               assert existingConnection.verifyConnection(realConnection)
                      : "The real database connection is not the one which is" 
                        + " wrapped in transactional wrapper.";
               // This should be ==
               assert connectionFactory == existingConnection.getConnectionFactory()
                      : "Cannot issue connection from the factory for a different" 
                        + " connection factory that the factory for which the" 
                        + " connection is already issued.";         
            }
      
            // Mark it as inactive since we are returning it
            connection.setActive(false);
            // Since the connection could have been requested multiple times, it 
            // can still be active (it has to be returned the same amount of 
            // times)
            if ((!connection.isInTransaction()) && (!connection.isActive()))
            {
               // This connection is not part of the transaction and it is not 
               // active anymore so return it back to the real factory by calling
               // the nontransactional method
               super.returnTransactionalConnection(realConnection, 
                                                   connectionFactory);
               // Also since this connection is completely released, release it
               m_connection.set(null);
               m_realConnection.set(null);
            }
            else
            {
               // The connection is part of the transaction so the transaction 
               // will return it when it is done or if the connection is still  
               // active even after it was set inactive that means that the 
               // connection was requested multiple times and must be returned 
               // multiple times.
            }
         }
         else
         {
            // This is just a regular connection which may have been allocated
            // before the transaction factory was created so just return it
            super.returnTransactionalConnection(cntDBConnection, 
                                     connectionFactory);
         }
      }
   }
   
   /**
    * End active transaction by commit or rollback.
    * 
    * @param bCommit - if true then transaction will be commited otherwise
    *                  it will be rollbacked.
    * @throws SystemException - and error has occurred during commit/rollback
    */
   protected void endTransaction(
      boolean bCommit
   ) throws SystemException
   {
      Integer iTransactionStatus = m_transaction.get();
      
      if (iTransactionStatus != null)
      {
         if (iTransactionStatus.intValue() == Status.STATUS_MARKED_ROLLBACK)
         {
            // We have to rollback.
            bCommit = false;
         }

         if (iTransactionStatus.intValue() == Status.STATUS_ACTIVE)
         {
            try
            {
               TransactionalConnection existingConnection;
               
               existingConnection = m_connection.get();
               if (existingConnection != null)
               {
                  // There was connection associated with this transaction
                  if (GlobalConstants.ERROR_CHECKING)
                  {
                     assert existingConnection.isInTransaction()
                            : "The connection is not associated to transaction.";
                  }
                  
                  // The connection was used inside of transaction so lets
                  // disassociate it with transaction which will allow us
                  // to commit/rollback on the connection
                  try
                  {
                     existingConnection.setInTransaction(false);
                     // Now commit/rollback
                     if (bCommit)
                     {
                        if (existingConnection.isUsed())
                        {
                           // Commit the connection only if it was used in 
                           // transaction
                           existingConnection.commit();
                        }
                        // We have to always change the state of the transaction
                        m_transaction.set(STATUS_COMMITTED_OBJ);
                     }
                     else
                     {
                        if (existingConnection.isUsed())
                        {
                           // Rollback the connection only if it was used in 
                           // transaction
                           existingConnection.rollback();
                        }
                        // We have to always change the state of the transaction
                        m_transaction.set(STATUS_ROLLEDBACK_OBJ);
                     }
                     // Since we flushed all operations, the connection is not
                     // used anymore
                     existingConnection.setUsed(false);
                  }
                  catch (SQLException sqleExc)
                  {
                     SystemException sysExc;
                     if (bCommit)
                     {
                        sysExc = new SystemException(
                                     "An error has occurred during commit.");
                     }
                     else
                     {
                        sysExc = new SystemException(
                                     "An error has occurred during rollback.");
                     }
                     sysExc.initCause(sqleExc);
                     throw sysExc;
                  }
                  if (!existingConnection.isActive())
                  {
                     // The connection is no longer active so we need to 
                     // return it back to the factory
                     Connection realConnection;
               
                     realConnection = m_realConnection.get();
                     if (GlobalConstants.ERROR_CHECKING)
                     {
                        assert existingConnection.verifyConnection(realConnection)
                               : "The real database connection is not the one"
                                 + " which is wrapped in transactional wrapper.";
                     }
                     // This connection is not part of the transaction and it 
                     // is not active anymore so return it back to the real 
                     // factory.
                     existingConnection.getConnectionFactory()
                                           .returnNonTransactionalConnection(
                                              realConnection);
                     // Also since this connection is completely released, 
                     // release it
                     m_connection.set(null);
                     m_realConnection.set(null);
                  }
               }
               else
               {
                  // If there wasn't any active connection that means nothing 
                  // was done inside of the transaction so we just need to reset
                  // the status
                  if (bCommit)
                  {
                     m_transaction.set(STATUS_COMMITTED_OBJ);
                  }
                  else
                  {
                     m_transaction.set(STATUS_ROLLEDBACK_OBJ);
                  }
               }
            }
            finally
            {
               // This is here just to check that once we are done with the code
               // above we have somehow completed transaction status (that mean
               // every branch somehow sets correctly) transaction status. The
               // reason is that we had a bug here when the transaction wasn't
               // reset if there was no work performed in the transaction
               if (GlobalConstants.ERROR_CHECKING)
               {
                  iTransactionStatus = m_transaction.get();
                  // We can use == since only these two constants can be set
                  assert ((iTransactionStatus == STATUS_ROLLEDBACK_OBJ)
                         || (iTransactionStatus == STATUS_COMMITTED_OBJ))
                         : "Transaction wasn't commited nor rollbacked.";
               }
            }
         }
         else
         {
            if (bCommit)
            {
               throw new IllegalStateException(
                            "Transaction cannot be commited if it wasn't started.");
            }
            else
            {
               throw new IllegalStateException(
                            "Transaction cannot be rollbacked if it wasn't started.");
            }
         }
      }
      else
      {
         if (bCommit)
         {
            throw new IllegalStateException(
                         "Transaction cannot be commited if it wasn't started.");
         }
         else
         {
            throw new IllegalStateException(
                         "Transaction cannot be rollbacked if it wasn't started.");
         }
      }
   }
}

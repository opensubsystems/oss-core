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

package org.opensubsystems.core.persist.jdbc.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSDynamicClassException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.jdbc.DatabaseTransactionFactory;
import org.opensubsystems.core.persist.jdbc.transaction.impl.SimpleLocalTransactionFactoryImpl;
import org.opensubsystems.core.persist.jdbc.transaction.j2ee.J2EETransactionFactoryImpl;
import org.opensubsystems.core.util.ClassFactory;
import org.opensubsystems.core.util.Config;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.OSSObject;
import org.opensubsystems.core.util.PropertyUtils;
import org.opensubsystems.core.util.j2ee.J2EEUtils;
import org.opensubsystems.core.util.jta.TransactionFactory;

/**
 * Base class for implementation of database transaction factories.
 * 
 * @author bastafidli
 */
public abstract class DatabaseTransactionFactoryImpl extends OSSObject
                                                     implements DatabaseTransactionFactory
{
   // Configuration settings ///////////////////////////////////////////////////
   
   /**
    * Transaction timeout in seconds, which should be set for a transaction when 
    * it is started. If this is set to 0, according to documentation the transaction 
    * manager restores the default value.
    */
   public static final String TRANSACTION_TIMEOUT = "oss.transaction.timeout";
   
   /**
    * Boolean flag, which specifies if the system should monitor transactions 
    * that is use a delegating class which intercepts and monitors calls to 
    * UserTransaction class. This is useful for troubleshooting and during 
    * development but can affect the system performance.
    */
   public static final String TRANSACTION_MONITOR = "oss.transaction.monitor";

   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Lock used in synchronized sections.
    */
   private static final String IMPL_LOCK = "IMPL_LOCK";

   /**
    * Default transaction timeout. Keep it longer so that if during debugging
    * we need to step through the code, we have enough time to complete 
    * transaction.
    */
   public static final int TRANSACTION_TIMEOUT_DEFAULT = 600;

   /**
    * Default transaction monitor setting. Keep it false so that it doesn't affect
    * performance.
    */
   public static final Boolean TRANSACTION_MONITOR_DEFAULT = Boolean.FALSE;
   
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(DatabaseTransactionFactoryImpl.class);
  
   /**
    * Reference to default instance.
    */
   private static DatabaseTransactionFactory s_defaultInstance = null;

   // Factory methods //////////////////////////////////////////////////////////

   /**
    * Get the default factory. This method is here to make the transaction factory 
    * is configurable. Once can specify in configuration file derived class to 
    * used instead of this one [DatabaseTransactionFactory.class]=new class to 
    * use or [TransactionFactory.class]=new class to use
    *
    * @return DatabaseTransactionFactory
    * @throws OSSException - an error has occurred 
    */
   public static DatabaseTransactionFactory getInstance(
   ) throws OSSException
   {
      if (s_defaultInstance == null)
      {
         // Only if the default factory wasn't set by other means create a new one
         // Synchronize just for the creation
         synchronized (IMPL_LOCK)
         {
            if (s_defaultInstance == null)
            {
               DatabaseTransactionFactory transactionFactory;
               ClassFactory<DatabaseTransactionFactory> cf;
               
               cf = new ClassFactory<>(DatabaseTransactionFactory.class);
               
               try
               {                
                  transactionFactory = cf.createInstance(
                                          DatabaseTransactionFactory.class);
               }
               catch (OSSDynamicClassException dneExc)
               {
                  // Cannot instantiate database transaction factory 
                  // so try just the generic transaction factory
                  Class<? extends DatabaseTransactionFactory> clsDefaultFactory;
                  
                  // TODO: Dependency: This creates compile time dependency on
                  // transaction and j2ee packages. If we want to allow to build 
                  // and distribute without this packages this dependency needs 
                  // to be removed
                  clsDefaultFactory = SimpleLocalTransactionFactoryImpl.class;
                  // Find out if we are running under a j2ee server. If yes, redefine
                  // default variable defaultTransactionFactoryImpl for using 
                  // j2ee transaction factory implementation.
                  if (J2EEUtils.getJ2EEServerType() != J2EEUtils.J2EE_SERVER_NO)
                  {
                     clsDefaultFactory = J2EETransactionFactoryImpl.class;
                  }
                  
                  // We have to use .toString since we are looking for class
                  // defined for TransactionFactory class name but we are 
                  // expecting it to be DatabaseTransactionFactory
                  transactionFactory = cf.createInstance(
                                          TransactionFactory.class.toString(),
                                          clsDefaultFactory);
               }
               
               setInstance(transactionFactory);                  
            }
         }   
      }
      
      return s_defaultInstance;
   }
   
   /**
    * Set default factory instance. This instance will be returned by 
    * getInstance method until it is changed.
    *
    * @param defaultFactory - new default factory instance
    * @see #getInstance
    */
   public static void setInstance(
      DatabaseTransactionFactory defaultFactory
   )
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         
         assert defaultFactory != null 
                : "Default transaction factory instance cannot be null";
      }   
      
      synchronized (IMPL_LOCK)
      {
         s_defaultInstance = defaultFactory;
         s_logger.log(Level.FINE, "Default database transaction factory is {0}", 
                      s_defaultInstance.getClass().getName());
      }   
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void commitTransaction(
      Connection cntConnection
   ) throws SQLException
   {
      try
      {
         boolean bAutoCommit = cntConnection.getAutoCommit();
         boolean bMonitorTransaction = isTransactionMonitored();
         
         if ((!isTransactionInProgress()) && (!bAutoCommit))
         {
            // If there is no global transaction in progress and then can be 
            // some sql pending since the autocommit is false then just commit
            // this connection since the transaction has to be connection based
            if (bMonitorTransaction)
            {
               s_logger.log(Level.FINEST, "commitTransaction on connection {0}", 
                            cntConnection.toString());
            }
            cntConnection.commit();
            if (bMonitorTransaction)
            {
               s_logger.log(Level.FINEST, "commitTransaction successful on connection {0}", 
                            cntConnection.toString());
            }
         }
         else
         {
            if (bMonitorTransaction)
            {
               s_logger.log(Level.FINEST,"commitTransaction on connection {0}"
                            + " ignored since either transaction in progress" 
                            + " or autoCommit is set. Autocommit value is {1}", 
                            new Object[]{cntConnection.toString(), bAutoCommit});
            }
         }
      }
      catch (OSSException osseExc)
      {
         throw new SQLException("Cannot check state of the transaction", osseExc);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void rollbackTransaction(
      Connection cntConnection
   ) throws SQLException
   {
      try
      {
         // Connection can be null in case an exception is thrown when
         // getting connection
         if (cntConnection != null) 
         {
             boolean bAutoCommit = cntConnection.getAutoCommit();
             boolean bMonitorTransaction = isTransactionMonitored();

            try
            {
               if ((!isTransactionInProgress()) && (!bAutoCommit))
               {
                  // If there is no global transaction in progress and then can be 
                  // some sql pending since the autocommit is false then just 
                  // rollback this connection since the transaction has to be 
                  // connection based
                  if (bMonitorTransaction)
                  {
                     s_logger.log(Level.FINEST, "rollbackTransaction on connection {0}", 
                                  cntConnection.toString());
                  }
                  cntConnection.rollback();
                  if (bMonitorTransaction)
                  {
                     s_logger.log(Level.FINEST, "rollbackTransaction successful"
                                  + " on connection {0}", cntConnection.toString());
                  }
               }
               else
               {
                  if (bMonitorTransaction)
                  {
                     s_logger.log(Level.FINEST,"rollbackTransaction on connection {0}"
                                  + " ignored since either transaction in"
                                  + " progress or autoCommit is set. Autocommit" 
                                  + " value is {1}", 
                                  new Object[]{cntConnection.toString(), bAutoCommit});
                  }
               }
            }
            catch (OSSException osseExc)
            {
               // Rollback anyway, since that should be better than just leaving
               // the transaction be
               cntConnection.rollback();
               // This transaction will be handled in the catch below
               throw osseExc;
            }
         }
      }
      catch (OSSException osseExc)
      {
         throw new SQLException("Cannot check state of the transaction", osseExc);
      }
   }

   /**
    * Test if transaction is in progress.
    * 
    * @return boolean - if true then transaction is in progress for current 
    *                   thread.
    * @throws OSSException - error occurred while getting the transaction status
    */
   public abstract boolean isTransactionInProgress(
   ) throws OSSException;
   
   /**
    * Get the transaction timeout, which should be set for a transaction when it 
    * is started. If this is set to 0, according to documentation transaction 
    * manager restores the default value.
    * 
    * @return int - transaction timeout
    */
   public int getTransactionTimeout(
   )
   {
      // Read it here instead of in static block or constructor since if this 
      // code is executed in different execution context, it might have 
      // different configuration settings.
      Properties prpSettings;
      int        iTransactionTimeout;

      prpSettings = Config.getInstance().getProperties();
      iTransactionTimeout = PropertyUtils.getIntPropertyInRange(
                                 prpSettings, TRANSACTION_TIMEOUT, 
                                 TRANSACTION_TIMEOUT_DEFAULT, 
                                 "Default transaction timout", 
                                 0, // 0 is allowed 
                                 Integer.MAX_VALUE);
      
      return iTransactionTimeout;
   }
   
   /**
    * Get flag, which is telling us if we should monitor transactions that is 
    * use a delegating class which intercepts and monitors calls to 
    * UserTransaction class. 
    * 
    * @return boolean - if true then transactions should be monitored and log
    *                   should capture additional diagnostic information about
    *                   each transaction
    */
   public boolean isTransactionMonitored()
   {
      // Read it here instead of in static block or constructor since if this 
      // code is executed in different execution context, it might have 
      // different configuration settings.
      Properties prpSettings;
      boolean    bTransactionMonitor;

      prpSettings = Config.getInstance().getProperties();
      bTransactionMonitor = PropertyUtils.getBooleanProperty(
                               prpSettings, TRANSACTION_MONITOR,
                               TRANSACTION_MONITOR_DEFAULT,
                               "Print transactions monitoring messages"
                            ).booleanValue();
      
      return bTransactionMonitor;
   }

   // Helper methods ///////////////////////////////////////////////////////////
   
   /**
    * This method should be exclusively used by DatabaseConnectionFactoryImpl
    * to get a transaction aware version of a connection. If there one already 
    * exists (such as there is one associated with the current pending transaction
    * managed by the database transaction factory) then this method will return
    * that one and there is no need to allocate a new one. If there is not one,
    * then this method will call back the database connection factory which
    * allocated the connection and then create a transaction aware version of it.
    * 
    * This method is protected so that only classes in this package can access it.
    * 
    * @param bAutoCommit - The desired autocommit state of the connection. If 
    *                      this connection is invoked in global (JTA) transaction
    *                      then the autocommit is false regardless of what
    *                      value is specified here. Use true here if the client
    *                      only reads the data and false if the client also 
    *                      modifies the data. 
    * @param strDataSourceName - datasource for which the connection was 
    *                            requested, if null then it is requested for 
    *                            default data source
    * @param strUser - user for which the connection was requested, if null
    *                  then it is requested for default user
    * @param strPassword - password for which the connection was requested, if
    *                      null then it is requested for default password
    * @param connectionFactory - connection factory which is requesting the 
    *                            connection
    * @return Connection - transaction aware version of connection
    * @throws OSSDatabaseAccessException - an error has occurred
    */
   protected Connection requestTransactionalConnection(
      boolean                       bAutoCommit,
      String                        strDataSourceName,
      String                        strUser,
      String                        strPassword,
      DatabaseConnectionFactoryImpl connectionFactory
   ) throws OSSException
   {
      // Many external connection pools already integrate with transaction
      // managers so we don't have to do anything, just call the real 
      // connection acquiring method.
      Connection realConnection;
      
      if (strDataSourceName == null)
      {
         if ((strUser == null) && (strPassword == null))
         {
            // Get just connection for default user
            realConnection = connectionFactory.requestNonTransactionalConnection(
                                bAutoCommit);
         }
         else
         {
            realConnection = connectionFactory.requestNonTransactionalConnection(
                                bAutoCommit, strUser, strPassword);            
         }
      }
      else
      {
         if ((strUser == null) && (strPassword == null))
         {
            // Get just connection for default user
            realConnection = connectionFactory.requestNonTransactionalConnection(
                                bAutoCommit, strDataSourceName);
         }
         else
         {
            realConnection = connectionFactory.requestNonTransactionalConnection(
                                bAutoCommit, strDataSourceName, strUser, 
                                strPassword);            
         }
      }
      
      return realConnection;
   }

   /**
    * This method should be exclusively used by DatabaseConnectionFactoryImpl
    * to get a transaction aware version of a connection. If the connection
    * is transaction aware then it will be realy returned if it is not involved
    * in a pending transaction.
    *
    * This method is protected so that only classes from this package can access 
    * it.
    * 
    * @param cntDBConnection - connection to return, can be null
    * @param connectionFactory - connection factory to which the connection 
    *                            should be returned
    */
   protected void returnTransactionalConnection(
      Connection                    cntDBConnection,
      DatabaseConnectionFactoryImpl connectionFactory
   )
   {
      // Many external connection pools already integrate with transaction
      // managers so we don't have to do anything, just call the real 
      // connection returning method.
      connectionFactory.returnNonTransactionalConnection(cntDBConnection);
   }
}

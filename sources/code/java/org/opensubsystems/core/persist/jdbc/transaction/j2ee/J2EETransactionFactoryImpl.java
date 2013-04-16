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

package org.opensubsystems.core.persist.jdbc.transaction.j2ee;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.jdbc.connectionpool.j2ee.J2EEDatabaseConnectionFactoryImpl;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseConnectionFactoryImpl;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseTransactionFactoryImpl;
import org.opensubsystems.core.persist.jdbc.transaction.impl.DelegatingUserTransaction;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.jta.TransactionUtils;

/**
 * Transaction factory using J2EE transaction manager. It will connect and 
 * expose the existing transaction manager of the J2EE server under which it 
 * currently runs.
 *
 * @author OpenSubsystems
 */
public class J2EETransactionFactoryImpl extends DatabaseTransactionFactoryImpl
{
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * An array of object references to the JNDI location of TransactionManager.
    * The values are for the following servers (in order):
    * a.) JBoss and JRun4 servers
    * b.) Resin, Orion (Oracle OC4J), JOnAS (JOTM), BEA WebLogic (unofficial) servers.
    * c.) BEA WebLogic Application (official)
    */ 
   public static final String[] ARR_TRAN_MANAGER = {"java:/TransactionManager",
                                                    "java:comp/UserTransaction",
                                                    "javax.transaction.TransactionManager",
                                                    }; 

   /**
    * An array of object references to the JNDI location of UserTransaction.
    * The values are for the following servers (in order):
    * a.) JBoss server
    * b.) JOnAS (JOTM), BEA WebLogic IBM WebSphere servers
    */ 
   public static final String[] ARR_USER_TRAN = {"UserTransaction",
                                                 "java:comp/UserTransaction",
                                                }; 

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Current UserTransaction that is used for actual j2ee server. 
    * Once it is initialized it should not try to do the lookup again.
    */
   protected UserTransaction m_currentUserTransaction = null;

   /**
    * Current TransactionManager that is used for actual j2ee server. 
    * Once it is initialized it should not try to do the lookup again.
    */
   protected TransactionManager m_currentTransactionManager = null;

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(J2EETransactionFactoryImpl.class);

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   public UserTransaction requestTransaction(
   ) throws OSSException
   {
      // once the m_currentUserTransaction variable is initialized 
      // it should not try to do the lookup again
      UserTransaction transaction = m_currentUserTransaction;
      int iIndex = 0;
      
      // TODO: Improve: For each J2EE server you should know what string should 
      // be used and therefore instead of going through loop we should say if 
      // server is jboss lookup this otherwise if jonas lookup this, etc.
      // Once you change this correct also documentation in 
      // implementation_newj2ee.html
      
      // lookup for each transaction manager string identifier
      while ((iIndex < ARR_USER_TRAN.length) && (transaction == null))
      {
         transaction = lookupUserTransaction(ARR_USER_TRAN[iIndex]);
         if (transaction != null)
         {
            m_currentUserTransaction = transaction;
            break;
         }
         iIndex++;
      }

      if (transaction == null)
      {
         // There is running unknown or unsupported j2ee server.  
         s_logger.log(Level.FINE, "Cannot get UserTransaction because " +
                      "unknown or unsupported J2EE server is running.");
      }
      else
      {
         if (isTransactionMonitored())
         {
            transaction = new DelegatingUserTransaction(transaction);
         }
      }

      // return UserTransaction object or null if not found
      return transaction;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public TransactionManager getTransactionManager(
   )
   {
      // once the m_currentTransactionManager variable is initialized 
      // it should not try to do the lookup again
      TransactionManager actualTM = m_currentTransactionManager;
      int iIndex = 0;
      
      // TODO: Improve: For each J2EE server you should know what string should 
      // be used and therefore instead of going through loop we should say if 
      // server is jboss lookup this otherwise if jonas lookup this, etc.
      // Once you change this correct also documentation in 
      // implementation_newj2ee.html
      
      // lookup for each transaction manager string identifier
      while ((iIndex < ARR_TRAN_MANAGER.length) && (actualTM == null))
      {
         actualTM = lookupTransactionManager(ARR_TRAN_MANAGER[iIndex]);
         if (actualTM != null)
         {
            m_currentTransactionManager = actualTM;
            break;
         }
         iIndex++;
      }

      if (actualTM == null)
      {
         // There is running unknown or unsupported j2ee server.  
         s_logger.log(Level.FINE, "Cannot get TransactionManager because " +
                      "unknown or unsupported J2EE server is running.");
      }

      // return TransactionManager object or null if not found
      return actualTM;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void reset(
   ) throws OSSException
   {
      // Since we do not control the transaction manager this methods may either 
      // just do not do anything 
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void stop(
   ) throws OSSException
   {
      // Since we do not control the transaction manager this methods may either 
      // just do not do anything 
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isTransactionInProgress(
   ) throws OSSException 
   {
      return TransactionUtils.isTransactionInProgress(requestTransaction());
   }
   
   // Helper methods ///////////////////////////////////////////////////////////

   /**
    * Look up the UserTransaction in JNDI via the configured name.
    * 
    * @param userTransactionName - the JNDI name of the UserTransaction
    * @return UserTransaction - the UserTransaction object or null if not found
    */
   protected UserTransaction lookupUserTransaction(
      String userTransactionName
   ) 
   {
      Object          jndiObject;
      UserTransaction userTran   = null;
      InitialContext  context    = null;

      s_logger.log(Level.FINEST, "Looking up UserTransaction at location {0}", 
                   userTransactionName);

      // There are not needed properties when we are running in J2EE server. 
      // It should be just possible to do context = new InitialContext();
      // when we are not running inside of J2EE server. 
      // The server should have either default values or they can be defined in 
      // the command line used to start the server using -D option. If it throws 
      // an exception than should be a good sign we are not in J2EE server.
      try 
      {
         context = new InitialContext();
         jndiObject = context.lookup(userTransactionName);
         
         if (jndiObject instanceof UserTransaction)
         {
            userTran = (UserTransaction) jndiObject;
            s_logger.log(Level.FINE, "UserTransaction found at {0}", userTransactionName);
         }
         else
         {
            // there was found particular string but it does not represent 
            // UserTransaction object so just log it here
            s_logger.log(Level.WARNING, "Object {0} available at JNDI location {1}" 
                         + " does not implement javax.transaction.UserTransaction."
                         + " This may be caused by class loading conflict between"
                         + " multiple jar files containing this interface.", 
                         new Object[]{jndiObject, userTransactionName});
         }
      }
      catch (NameNotFoundException nnfExc)
      {
         // This may not be serious error so just log it
         // Do not print the exception since it would be just concerning
         // user and this is normal situation
         s_logger.log(Level.FINEST, "UserTransaction is not available at JNDI"
                      + " location {0}", userTransactionName);
      }
      catch (NamingException nExc) 
      {
         // This may not be serious error so just log it
         // Do not print the exception since it would be just concerning
         // user and this is normal situation
         s_logger.log(Level.FINEST, "UserTransaction is not available at JNDI"
                      + " location {0}", userTransactionName);
      }
      finally
      {
         if (context != null)
         {
            try
            {
               context.close();
            }
            catch (NamingException nExc)
            {
               s_logger.log(Level.FINE, "Unable to close context for user"
                            + " transaction", nExc);
            }
         }
      }

      return userTran;
   }

   /**
    * Look up the TransactionManager in JNDI via the configured name.
    * 
    * @param transactionManagerName - the JNDI name of the TransactionManager
    * @return TransactionManager - the TransactionManager object or null if not 
    *                              found
    */
   protected TransactionManager lookupTransactionManager(
      String transactionManagerName
   ) 
   {
      Object             jndiObject;
      TransactionManager tranManager = null;
      InitialContext     context     = null;

      s_logger.log(Level.FINEST, "Looking up TransactionManager at location {0}", 
                   transactionManagerName);

      // There are not needed properties when we are running in J2EE server. 
      // It should be just possible to do context = new InitialContext();
      // when we are not running inside of J2EE server. 
      // The server should have either default values or they can be defined in 
      // the command line used to start the server using -D option. If it throws 
      // an exception than should be a good sign we are not in J2EE server.
      try 
      {
         context = new InitialContext();
         jndiObject = context.lookup(transactionManagerName);
         
         if (jndiObject instanceof TransactionManager)
         {
            tranManager = (TransactionManager) jndiObject;
            s_logger.log(Level.FINE, "TransactionManager found at {0}", 
                         transactionManagerName);
         }
         else
         {
            // there was found particular string but it does not represent 
            // TransactionManager object so just log it here
            s_logger.log(Level.WARNING, "Object {0} available at JNDI location {1}" 
                         + " does not implement javax.transaction.TransactionManager"
                         + " This may be caused by class loading conflict between"
                         + " multiple jar files containing this interface.", 
                         new Object[]{jndiObject, transactionManagerName});
         }
      }
      catch (NameNotFoundException nnfExc)
      {
         // This may not be serious error so just log it
         // Do not print the exception since it would be just concerning
         // user and this is normal situation
         s_logger.log(Level.FINEST, "TransactionManager is not available at JNDI"
                      + " location {0}", transactionManagerName);
      }
      catch (NamingException nExc) 
      {
         // This may not be serious error so just log it
         // Do not print the exception since it would be just concerning
         // user and this is normal situation
         s_logger.log(Level.FINEST, "TransactionManager is not available at JNDI"
                      + " location {0}", transactionManagerName);
      }
      finally
      {
         if (context != null)
         {
            try
            {
               context.close();
            }
            catch (NamingException nExc)
            {
               s_logger.log(Level.FINE, 
                            "Unable to close context for transaction manager", 
                            nExc);
            }
         }
      }

      return tranManager;
   }

   /**
    * {@inheritDoc}
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
      if (GlobalConstants.ERROR_CHECKING)
      {
         // J2EE TF currently works only with J2EE CF because J2EE TF can manage
         // only XAResources and only J2EE CF represents database connections
         // as XAResources.
         // Lets check if it is so
         assert connectionFactory instanceof J2EEDatabaseConnectionFactoryImpl
                : "J2EE transaction factory can manage only J2EE connections" +
                  " at this time.";
      }
      
      // J2EE CF created connection is always already connected to J2EE TF 
      // therefore nothing needs to be done, just call the appropriate version 
      // of the method
      return super.requestTransactionalConnection(bAutoCommit, strDataSourceName, 
                                                  strUser, strPassword, 
                                                  connectionFactory);
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
      if (GlobalConstants.ERROR_CHECKING)
      {
         // JOTM currently works only with XAPool because JOTM can manage
         // only XAResources and only XAPool represents database connections
         // as XAResources.
         // Lets check if it is so
         assert connectionFactory instanceof J2EEDatabaseConnectionFactoryImpl
                : "J2EE TM can manage only J2EE CF connections at this time.";
      }

      // Since J2EE TF and J2EE CD are already integrated just call the base class
      super.returnTransactionalConnection(cntDBConnection, connectionFactory);
   }
}

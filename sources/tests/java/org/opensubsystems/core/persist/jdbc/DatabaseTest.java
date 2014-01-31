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

package org.opensubsystems.core.persist.jdbc;

import java.sql.Connection;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.opensubsystems.core.persist.jdbc.impl.DatabaseConnectionFactoryImpl;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseTransactionFactoryImpl;
import org.opensubsystems.core.util.Config;

/**
 * Base class for all tests that access the database. Test cases derived from 
 * this class should use DatabaseTestSetup adapter and follow comments in this 
 * class to properly initialize and shutdown database.
 *
 * @author bastafidli
 */
public abstract class DatabaseTest extends TestCase
{
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Default property file used to run tests.
    */
   public static final String DEFAULT_PROPERTY_FILE = "osstest.properties";

   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Database transaction which can be used by test. This transaction object
    * is instantiated before every test is started and checked if the 
    * transaction was properly finished after the test is done. 
    */
   protected UserTransaction m_transaction;
   
   /**
    * Connection to the database which can be used by test. This connection object
    * is instantiated before every test is started and returned after the test is 
    * done. This connection may be used to quickly access the database in the test
    * without worrying about its creation and destruction.
    */
   protected Connection m_connection;

   /**
    * How many connections were requested at the beginning of the test from 
    * the pool. This should must the number of requested connection after the 
    * test. 
    */
   protected int m_iRequestedConnectionCount;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Static initializer 
    */
   static
   {
      if (Config.getInstance().getRequestedConfigFile() == null)
      {
         Config.getInstance().setPropertyFileName(DEFAULT_PROPERTY_FILE);
      }
   }

   /**
    * Create new DatabaseTest.
    * 
    * @param strTestName - name of the test
    */
   public DatabaseTest(
      String strTestName
   ) 
   {
      super(strTestName);
    
      m_transaction = null;
      m_connection = null;
   }   
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected void setUp(
   ) throws Exception
   {
      super.setUp();

      // Get connection after the transaction so that we do not have to worry
      // about returning it in case it fails
      m_transaction = DatabaseTransactionFactoryImpl.getInstance().requestTransaction();
      // Request autocommit false since we might be modifying database
      m_connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(false);
      
      // Remember how many connections should be out of the pool after the test
      m_iRequestedConnectionCount 
         = DatabaseConnectionFactoryImpl.getInstance().getTotalRequestedConnectionCount();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void tearDown(
   ) throws Exception
   {
      try
      {
         // Remember how many connections should be out of the pool after the test
         assertEquals("Somebody forgot to return connection to the pool.",
            m_iRequestedConnectionCount,  
            DatabaseConnectionFactoryImpl.getInstance().getTotalRequestedConnectionCount());

         int iStatus;
         
         iStatus = m_transaction.getStatus();
         
         if ((iStatus != Status.STATUS_NO_TRANSACTION)
             && (iStatus != Status.STATUS_COMMITTED)
             && (iStatus != Status.STATUS_ROLLEDBACK))
         {
            fail("Transaction wasn't commited or rollbacked. Status is " + iStatus);
         }
      }
      finally
      {
         if (m_connection != null)
         {
            if (!m_connection.isClosed())
            {
               DatabaseConnectionFactoryImpl.getInstance().returnConnection(m_connection);
            }
         }
      }

      super.tearDown();
   }
}

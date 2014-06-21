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

package org.opensubsystems.core.persist.jdbc.database;

import java.sql.PreparedStatement;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.jdbc.Database;
import org.opensubsystems.core.persist.jdbc.DatabaseSchemaManager;
import org.opensubsystems.core.persist.jdbc.test.DatabaseTest;
import org.opensubsystems.core.persist.jdbc.test.DatabaseTestSetup;
import org.opensubsystems.core.persist.jdbc.test.DatabaseTestSuite;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseImpl;
import org.opensubsystems.core.util.test.Tests;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * Test for update DB procedure without output parameter.
 * 
 * @author opensubsystems
 */
public final class UpdateDBProcWithoutOutputTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private UpdateDBProcWithoutOutputTest(
   )
   {
      // Do nothing
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Create the suite for this test since this is the only way how to create
    * test setup which can initialize and shutdown the database for us
    * 
    * @return Test - suite of tests to run for this database
    */
   public static Test suite(
   )
   {
      TestSuite suite = new DatabaseTestSuite("UpdateDBProcWithoutOutputTest");
      suite.addTestSuite(UpdateDBProcWithoutOutputTestInternal.class);
      // Here we are using DatabaseTestSetup instead of ApplicationTestSetup
      // since we are just directly testing  database functionality without
      // accessing any business logic functionality packaged into application 
      // modules
      TestSetup wrapper = new DatabaseTestSetup(suite);

      return wrapper;
   }

   /**
    * Internal class which can be included in other test suites directly without
    * including the above suite. This allows us to group multiple tests 
    * together and the execute the DatabaseTestSetup only once 
    */
   public static class UpdateDBProcWithoutOutputTestInternal extends DatabaseTest
   {
      /**
       * Static initializer
       */
      static
      {
         // This test use special database schema so make the database aware of it
         Database dbDatabase;
   
         try
         {
            dbDatabase = DatabaseImpl.getInstance();
            // Add schema database tests needs to the database
            dbDatabase.add(DatabaseTestSchema.class);
         }
         catch (OSSException bfeExc)
         {
            throw new RuntimeException("Unexpected exception.", bfeExc);
         }
      }
      
      /**
       * Create new test.
       * 
       * @param strTestName - name of the test
       */
      public UpdateDBProcWithoutOutputTestInternal(
         String strTestName
      )
      {
         super(strTestName);
      }
      
      /**
       * Test if the DB procedure without output parameters will return number 
       * of updated items. 
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testUpdateDBProcWithoutOutputParam(
      ) throws Throwable
      {
         final String INSERT_VALUE = "insert into TRANSACTION_TEST (TEST_ID,TEST_VALUE)" +
                                     " values (?,?)";
         final String DELETE_VALUE = "delete from TRANSACTION_TEST where TEST_VALUE in (?, ?)";
         final String DELETE_ALL = "delete from TRANSACTION_TEST";
         final String OLD_VALUE_TEST = "test_value";
         final String NEW_VALUE_TEST = "test_value_updated";
   
         PreparedStatement insertStatement = null;
         PreparedStatement deleteStatement = null;
         int               iInsertCount    = 0;
         int               iUpdateCount    = 0;
         int               iDeletedCount   = 0;         
   
         try
         {
            //******************************************************************
            // Try to select original record to verify that the database is in OK state
            m_transaction.begin();
            try
            {
               deleteStatement = m_connection.prepareStatement(DELETE_ALL);
   
               iDeletedCount = DatabaseUtils.executeUpdateAndClose(deleteStatement);
   
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            
            assertEquals("No records should be initially in the database.", 
                         0, iDeletedCount);
   
            // insert value
            m_transaction.begin();
            try
            {
               try
               {
                  insertStatement = m_connection.prepareStatement(INSERT_VALUE);
                  insertStatement.setInt(1, 100);
                  insertStatement.setString(2, OLD_VALUE_TEST);
      
                  iInsertCount = insertStatement.executeUpdate();
               }
               finally
               {   
                  DatabaseUtils.closeStatement(insertStatement);
               }
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            assertEquals("Exactly one record have been inserted.", 
                         iInsertCount, 1);
   
   
            // try to update inserted value using DB procedure
            m_transaction.begin();
            try
            {
               iUpdateCount = ((DatabaseTestSchema)DatabaseSchemaManager.getInstance(
                                 DatabaseTestSchema.class)).executeUpdateTestValue(
                                                               m_connection,
                                                               OLD_VALUE_TEST,
                                                               NEW_VALUE_TEST);
               m_transaction.commit();
   
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            
            assertEquals("DBMS does not support returning number of" 
                         + " processed rows using"
                         + " [ RowCounter = updateStatement.executeUpdate() ]. " 
                         + " Number of updated rows is incorrect.", 
                         1, iUpdateCount);
         }
         finally
         {
            // delete inserted data
            m_transaction.begin();
            try
            {
               deleteStatement = m_connection.prepareStatement(DELETE_VALUE);
               deleteStatement.setString(1, OLD_VALUE_TEST);
               deleteStatement.setString(2, NEW_VALUE_TEST);
               iDeletedCount = DatabaseUtils.executeUpdateAndClose(deleteStatement);
               m_transaction.commit();
            
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            finally
            {
               DatabaseUtils.closeStatement(deleteStatement);
            }
            assertEquals("Exactly 1 record with data shoud have been deleted.", 
                                1, iDeletedCount);
         }
      }   
   }
}

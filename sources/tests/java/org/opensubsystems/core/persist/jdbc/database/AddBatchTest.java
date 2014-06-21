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
import java.sql.ResultSet;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.jdbc.Database;
import org.opensubsystems.core.persist.jdbc.test.DatabaseTest;
import org.opensubsystems.core.persist.jdbc.test.DatabaseTestSetup;
import org.opensubsystems.core.persist.jdbc.test.DatabaseTestSuite;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseImpl;
import org.opensubsystems.core.util.test.Tests;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * Test for inserting more values into the DB using addBatch() method 
 * 
 * @author bastafidli
 */
public final class AddBatchTest extends Tests 
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private AddBatchTest(
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
      TestSuite suite = new DatabaseTestSuite("AddBatchTest");
      suite.addTestSuite(AddBatchTestInternal.class);
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
   public static class AddBatchTestInternal extends DatabaseTest
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
      public AddBatchTestInternal(
         String strTestName
      )
      {
         super(strTestName);
      }
      
      /**
       * Test for inserting more values into the DB using addBatch() method 
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testAddBatch(
      ) throws Throwable
      {
         final String INSERT_VALUE = "insert into NULL_COLUMN_TEST (NAME) values (?)";
         final String SELECT_VALUE = "select count(NAME) from NULL_COLUMN_TEST where " +
                                     "NAME like 'test_batch_%'";
         final String DELETE_VALUE = "delete from NULL_COLUMN_TEST";
         
         PreparedStatement insertStatement = null;
         ResultSet         rsResults;
         int[]             arrInsertCount = new int[10];
         int               testValue;
   
         // insert values to the table
         m_transaction.begin();
         try
         {
            try
            {
               insertStatement = m_connection.prepareStatement(INSERT_VALUE);
               // insert all data in the cycle
               for (int iIndex = 1; iIndex < 11; iIndex++)
               {
                insertStatement.setString(1, "test_batch_" + iIndex);
                insertStatement.addBatch();
               }
               arrInsertCount = insertStatement.executeBatch();
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            finally
            {   
               DatabaseUtils.closeStatement(insertStatement);
               rsResults = null;
            }
   
            assertEquals("Number of inserted records is incorrect", 10, arrInsertCount.length);
   
            // try to select values
            PreparedStatement selectStatement = null;
         
            try
            {
               selectStatement = m_connection.prepareStatement(SELECT_VALUE);
               rsResults = selectStatement.executeQuery();
            
               assertTrue("No result in select", rsResults.next());
               testValue = rsResults.getInt(1);
               assertEquals("Incorrect number of specified records selected", 10, testValue);
            }
            finally
            {   
               DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
               rsResults = null;
            }
         }
         finally
         {
            // delete test data
            m_transaction.begin();
            try
            {
               PreparedStatement deleteStatement;
               
               deleteStatement = m_connection.prepareStatement(DELETE_VALUE);
               deleteStatement.execute();
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
         }
      }
   }
}

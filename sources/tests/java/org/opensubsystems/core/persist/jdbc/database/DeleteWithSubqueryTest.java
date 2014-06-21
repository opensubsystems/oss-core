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
import java.sql.SQLException;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.jdbc.Database;
import org.opensubsystems.core.persist.jdbc.test.DatabaseTest;
import org.opensubsystems.core.persist.jdbc.test.DatabaseTestSetup;
import org.opensubsystems.core.persist.jdbc.test.DatabaseTestSuite;
import org.opensubsystems.core.persist.jdbc.database.mysql.MySQLDatabaseImpl;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseImpl;
import org.opensubsystems.core.util.test.Tests;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * Test for delete query using subquery.
 * 
 * @author opensubsystems
 */
public final class DeleteWithSubqueryTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private DeleteWithSubqueryTest(
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
      TestSuite suite = new DatabaseTestSuite("DeleteWithSubqueryTest");
      suite.addTestSuite(DeleteWithSubqueryTestInternal.class);
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
   public static class DeleteWithSubqueryTestInternal extends DatabaseTest
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
      public DeleteWithSubqueryTestInternal(
         String strTestName
      )
      {
         super(strTestName);
      }
      
      /**
       * Test if the DB supports delete using subquery in the SQL command.
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testDeleteWithSubquery(
      ) throws Throwable
      {
         final String INSERT_VALUE_1
                       = "insert into DELETE_TEST (TEST_ID,TEST_VALUE) values (?,?)";
         final String INSERT_VALUE_2 
                       = "insert into DELETE_RELATED_TEST (TEST_REL_ID,TEST_ID,TEST_VALUE) " +
                         "values (?,?,?)";
         final String DELETE_VALUE 
                       = "delete from DELETE_TEST where DELETE_TEST.TEST_ID " +
                         " in (select DELETE_TEST.TEST_ID from DELETE_TEST" +
                         " left join DELETE_RELATED_TEST on " +
                         " DELETE_TEST.TEST_ID = DELETE_RELATED_TEST.TEST_ID" +
                         " where DELETE_RELATED_TEST.TEST_ID is null)";
   
         final String DELETE_VALUE_1 = "delete from DELETE_TEST where TEST_ID in (?, ?)";
         final String DELETE_VALUE_2 = "delete from DELETE_RELATED_TEST" +
                                       " where TEST_REL_ID in (?, ?)";
   
         PreparedStatement insertStatement = null;
         PreparedStatement deleteStatement = null;
         int               iDeletedCount   = 0;         
   
         try
         {
            // insert value
            m_transaction.begin();
            try
            {
               try
               {
                  // insert values to the table TRANSACTION_TEST
                  insertStatement = m_connection.prepareStatement(INSERT_VALUE_1);
                  insertStatement.setInt(1, 100);
                  insertStatement.setString(2, "dt_value_1");
                  insertStatement.executeUpdate();
   
                  // insert values to the table TRANSACTION_TEST
                  insertStatement = m_connection.prepareStatement(INSERT_VALUE_1);
                  insertStatement.setInt(1, 200);
                  insertStatement.setString(2, "dt_value_2");
                  insertStatement.executeUpdate();
   
                  // insert values to the table TRANSACTION_RELATED_TEST
                  insertStatement = m_connection.prepareStatement(INSERT_VALUE_2);
                  insertStatement.setInt(1, 100);
                  insertStatement.setInt(2, 100);
                  insertStatement.setString(3, "drt_value_1");
                  insertStatement.executeUpdate();
   
                  // insert values to the table TRANSACTION_RELATED_TEST
                  insertStatement = m_connection.prepareStatement(INSERT_VALUE_2);
                  insertStatement.setInt(1, 200);
                  insertStatement.setInt(2, 100);
                  insertStatement.setString(3, "drt_value_2");
                  insertStatement.executeUpdate();
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
   
            // try to delete records from TRANSACTION_TABLE that does not have belonging records 
            // in the TRANSACTION_RELATED_TEST table
            m_transaction.begin();
            try
            {
               deleteStatement = m_connection.prepareStatement(DELETE_VALUE);
               iDeletedCount = DatabaseUtils.executeUpdateAndClose(deleteStatement);
               m_transaction.commit();
               
               if (DatabaseImpl.getInstance() instanceof MySQLDatabaseImpl)
               {
                  // MySQL as of version 4.1.14 doesn't support subqueries
                  // if this behavior changes report it 
                  fail("MySQL subquery behavior has changed. Review it.");
               }
               else
               {
                  assertEquals("Exactly one record should be deleted.", 
                               1, iDeletedCount);
               }
            }
            catch (SQLException sqleExc)
            {
               m_transaction.rollback();
               
               if (DatabaseImpl.getInstance() instanceof MySQLDatabaseImpl)
               {
                  // MySQL as of version 4.1.14 doesn't support subqueries
                  // therefore this is expected behavior and not our bug and 
                  // therefore we will not report it as an error
               }
               else
               {
                  fail("It seems like database doesn't support delete with" 
                       + " subquery: " + sqleExc.getMessage());
                  throw sqleExc;
               }
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
         }
         finally
         {
            // delete inserted data
            m_transaction.begin();
            try
            {
               // delete from TRANSACTION_RELATED_TEST table
               deleteStatement = m_connection.prepareStatement(DELETE_VALUE_2);
               deleteStatement.setInt(1, 100);
               deleteStatement.setInt(2, 200);
               deleteStatement.execute();
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            finally
            {
               DatabaseUtils.closeStatement(deleteStatement);
            }
            
            m_transaction.begin();
            try
            {
               // delete from TRANSACTION_RELATED_TEST table
               deleteStatement = m_connection.prepareStatement(DELETE_VALUE_1);
               deleteStatement.setInt(1, 100);
               deleteStatement.setInt(2, 200);
               deleteStatement.execute();
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            finally
            {
               DatabaseUtils.closeStatement(deleteStatement);
            }
         }
      }   
   }
}

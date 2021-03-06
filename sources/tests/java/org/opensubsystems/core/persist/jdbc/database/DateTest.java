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
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Calendar;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.jdbc.Database;
import org.opensubsystems.core.persist.jdbc.test.DatabaseTest;
import org.opensubsystems.core.persist.jdbc.test.DatabaseTestSetup;
import org.opensubsystems.core.persist.jdbc.test.DatabaseTestSuite;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseImpl;
import org.opensubsystems.core.util.DateUtils;
import org.opensubsystems.core.util.test.Tests;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * All tests related to date functionality.
 * 
 * @author bastafidli
 */
public final class DateTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private DateTest(
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
      TestSuite suite = new DatabaseTestSuite("DateTest");
      suite.addTestSuite(DateTestInternal.class);
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
   public static class DateTestInternal extends DatabaseTest
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
       * Create new date test.
       * 
       * @param strTestName - name of the test
       */
      public DateTestInternal(
         String strTestName
      )
      {
         super(strTestName);
      }
      
      /**
       * Test the database support for Date objects. Date object ignores the time 
       * portion of the Java Date.
       * 
       * This class inserts date into database, then retrieve it back using 
       * different java time and deletes it using cursor.
       * 
       * Uses the already setup connection and transaction. 
       * No need to close the connection since base class is doing it for us.
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testBasicDateSupport(
      ) throws Throwable
      {
         final String INSERT_DATE = "insert into DATE_TEST(DATE_TEST) values (?)";
         // See OracleTests class why we need to select tablename.* 
         final String SELECT_DATE = "select DATE_TEST.* from DATE_TEST where DATE_TEST = ?";
         final String DELETE_DATE = "delete from DATE_TEST where DATE_TEST = ?";
          
         Calendar          calGenerate = Calendar.getInstance();
         java.sql.Date     insertDate;
         PreparedStatement insertStatement;
         int               iUpdateCount;
         
         // Set test date
         calGenerate.set(1995, 9, 15, 1, 2, 3);
         insertDate = new java.sql.Date(calGenerate.getTimeInMillis());
         
         m_transaction.begin();
   
         try
         {
            insertStatement = m_connection.prepareStatement(INSERT_DATE);
            insertStatement.setDate(1, insertDate);
   
            iUpdateCount = DatabaseUtils.executeUpdateAndClose(insertStatement);
   
            m_transaction.commit();
         }
         catch (Throwable throwable)
         {
            m_transaction.rollback();
            throw throwable;
         }
         
         assertEquals("Exactly one record with date data shoud have been inserted.", 
                      iUpdateCount, 1);
         
         // Now select it back to be sure it is there
         PreparedStatement selectStatement = null;
         PreparedStatement deleteStatement = null;
         ResultSet         results = null;
         java.sql.Date     retrievedDate;
         boolean           bHasMoreThanOne;
         int               iDeletedCount = 0;
   
         m_transaction.begin();
   
         try
         {
            java.sql.Date selectDate;
            boolean       bUpdatableResultSet = true;
            
            try
            {
               selectStatement = m_connection.prepareStatement(
                                    SELECT_DATE,
                                    ResultSet.TYPE_SCROLL_SENSITIVE,
                                    ResultSet.CONCUR_UPDATABLE);
            }
            catch (SQLException sqleExc)
            {
               // Maybe it doesn't support updatable results set (e.g HSQLDB)
               // so try to work around it
               bUpdatableResultSet = false;
               selectStatement = m_connection.prepareStatement(SELECT_DATE);
               // DOn't catch any exception here, if this doesn't work, something
               // is really wrong
            }
            
            // Set different time, since when we are dealing with just dates it 
            // shouldn't matter                                                         
            calGenerate.set(1995, 9, 15, 2, 3, 4);
            selectDate = new java.sql.Date(calGenerate.getTimeInMillis());
            selectStatement.setDate(1, selectDate);
            results = selectStatement.executeQuery();
            
            // Get the date from the database
            assertTrue("The inserted date is not in the database.", results.next());
            retrievedDate = results.getDate(1);
            // Delete the inserted row
            if (bUpdatableResultSet)
            {
               // This is the optimal way to do it, but not every database supports
               // updatable ResultSet (e.g. HSQLDB, ResultSetUnitTest tests for this) 
               // so provide an alternative
               try
               { 
                  results.deleteRow();
                  iDeletedCount = 1;
               }
               catch (SQLException sqleExc)
               {
                  // Maybe it doesn't support updatable results set (e.g Firebird)
                  // so try to work around it
                  bUpdatableResultSet = false;
               }
            }
            if (!bUpdatableResultSet)
            {
               deleteStatement = m_connection.prepareStatement(DELETE_DATE);
               deleteStatement.setDate(1, insertDate);
               iDeletedCount = DatabaseUtils.executeUpdateAndClose(deleteStatement);
            }         
            
            // Get the assert check without throwing exception so we can correctly 
            // cleanup
            bHasMoreThanOne = results.next();
            m_transaction.commit();
         }
         catch (Throwable throwable)
         {
            m_transaction.rollback();
            throw throwable;
         }
         finally
         {
            DatabaseUtils.close(results, selectStatement);
            
            m_transaction.begin();
            try
            {
               deleteStatement = m_connection.prepareStatement(
                                    "delete from DATE_TEST");
               deleteStatement.execute();
               m_transaction.commit();
            }
            catch (Throwable throwable2)
            {
               m_transaction.rollback();
               throw throwable2;
            }
            finally
            {
               DatabaseUtils.close(deleteStatement);
            }      
         }
         
         assertFalse("There should be only one inserted date in the database.", 
                     bHasMoreThanOne);
      
         assertEquals("Exactly one record with date data shoud have been deleted.", 
                      iDeletedCount, 1);
   
         // And now test the date                           
         assertNotNull("The inserted date shouldn't be retrieved as null from the database", 
                       retrievedDate);
         assertTrue("The date retrieved from database "
                    + DateFormat.getDateTimeInstance().format(retrievedDate)
                    + " is not the same as the inserted one "
                    + DateFormat.getDateTimeInstance().format(insertDate),
                    DateUtils.dateEquals(retrievedDate, insertDate));
                                           
      }
   }
}

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
import org.opensubsystems.core.persist.jdbc.DatabaseTest;
import org.opensubsystems.core.persist.jdbc.DatabaseTestSetup;
import org.opensubsystems.core.persist.jdbc.DatabaseTestSuite;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseImpl;
import org.opensubsystems.core.util.DateUtils;
import org.opensubsystems.core.util.Tests;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * All tests related to time functionality.
 * 
 * @author bastafidli
 */
public final class TimeTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private TimeTest(
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
      TestSuite suite = new DatabaseTestSuite("TimeTest");
      suite.addTestSuite(TimeTestInternal.class);
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
   public static class TimeTestInternal extends DatabaseTest
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
       * Create new time test.
       * 
       * @param strTestName - name of the test
       */
      public TimeTestInternal(
         String strTestName
      )
      {
         super(strTestName);
      }
      
      /**
       * Test the database support for Time objects. Time object ignores the date 
       * portion of the Java Date. The java.sql.Time requires the date portion
       * of the Time to be set to January 1, 1970 but it doesn't enforces it
       * when using the long contructor therefore allowing us to create time
       * values greater then 24 hours.
       * 
       * This class inserts time into database, then retrieve it back using 
       * different java date and deletes it using cursor.
       * 
       * Uses the already setup connection and transaction. 
       * No need to close the connection since base class is doing it for us.
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testBasicTimeSupport(
      ) throws Throwable
      {
         final String INSERT_TIME = "insert into TIME_TEST(TIME_TEST) values (?)";
         // See OracleTests class why we need to select tablename.* 
         final String SELECT_TIME = "select TIME_TEST.* from TIME_TEST where TIME_TEST = ?";
         final String DELETE_TIME = "delete from TIME_TEST where TIME_TEST = ?";
          
         Calendar          calGenerate = Calendar.getInstance();
         java.sql.Time     insertTime;
         PreparedStatement insertStatement;
         int               iUpdateCount;
         
         // Set date to a test date
         calGenerate.set(1995, 9, 15, 1, 2, 3);
         insertTime = new java.sql.Time(calGenerate.getTimeInMillis());
         
         m_transaction.begin();
   
         try
         {
            insertStatement = m_connection.prepareStatement(INSERT_TIME);
            insertStatement.setTime(1, insertTime);
   
            iUpdateCount = DatabaseUtils.executeUpdateAndClose(insertStatement);
   
            m_transaction.commit();
         }
         catch (Throwable throwable)
         {
            m_transaction.rollback();
            throw throwable;
         }
         
         assertEquals("Exactly one record with time data shoud have been inserted.", 
                             iUpdateCount, 1);
         
         // Now select it back to be sure it is there
         PreparedStatement selectStatement = null;
         PreparedStatement deleteStatement = null;
         ResultSet         results = null;
         java.sql.Time     retrievedTime;
         boolean           bHasMoreThanOne;
         int               iDeletedCount = 0;
   
         m_transaction.begin();
   
         try
         {
            java.sql.Time selectTime;
            boolean       bUpdatableResultSet = true;
            
            try
            {
               selectStatement = m_connection.prepareStatement(
                                    SELECT_TIME,
                                    ResultSet.TYPE_SCROLL_SENSITIVE,
                                    ResultSet.CONCUR_UPDATABLE);
            }
            catch (SQLException sqleExc)
            {
               // Maybe it doesn't support updatable results set (e.g HSQLDB)
               // so try to work around it
               bUpdatableResultSet = false;
               selectStatement = m_connection.prepareStatement(SELECT_TIME);
               // DOn't catch any exception here, if this doesn't work, something
               // is really wrong
            }
   
            // Set different date, since when we are dealing with just time it 
            // shouldn't matter                                                         
            calGenerate.set(1975, 10, 16, 1, 2, 3);
            selectTime = new java.sql.Time(calGenerate.getTimeInMillis());
            selectStatement.setTime(1, selectTime);
            results = selectStatement.executeQuery();
               
            // Get the time from the database
            assertTrue("The inserted time not found in the database since" 
                       + " most likely because java.sql.Time and driver" 
                       + " implementation doesn't enforce date portion to be" 
                       + " set to January 1, 1970 when using constructor(long).", 
                       results.next());
            
            retrievedTime = results.getTime(1);
            // Delete the inserted row
            if (bUpdatableResultSet)
            {
               // This is the optimal way to do it, but not every database
               // supports updatable ResultSet (e.g. HSQLDB, ResultSetUnitTest  
               // tests for this) so provide an alternative 
               try
               { 
                  results.deleteRow();
                  iDeletedCount = 1;
               }
               catch (SQLException sqleExc)
               {
                  // Maybe it doesn't support updatable results set 
                  // (e.g Firebird) so try to work around it
                  bUpdatableResultSet = false;
               }
            }
            if (!bUpdatableResultSet)
            {
               deleteStatement = m_connection.prepareStatement(DELETE_TIME);
               deleteStatement.setTime(1, insertTime);
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
            DatabaseUtils.closeResultSetAndStatement(results, selectStatement);
            
            m_transaction.begin();
            try
            {
               deleteStatement = m_connection.prepareStatement(
                        "delete from TIME_TEST");
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
               DatabaseUtils.closeStatement(deleteStatement);
            }   
         }
         
         assertFalse("There should be only one inserted time in the database.", 
                     bHasMoreThanOne);
      
         assertEquals("Exactly one record with time data shoud have been deleted.", 
                      iDeletedCount, 1);
   
         // And now test the date                           
         assertNotNull("The inserted time haven't been found in the database", 
                       retrievedTime);
         
         // Ignore milliseconds when comparing dates                              
         assertTrue("The time retrieved from database "
                    + DateFormat.getDateTimeInstance().format(retrievedTime)
                    + " is not the same as the inserted one "
                    + DateFormat.getDateTimeInstance().format(insertTime),
                    DateUtils.timeEquals(retrievedTime, insertTime, true));
      }   
   }
}

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
import org.opensubsystems.core.persist.jdbc.database.mysql.MySQLDatabaseImpl;
import org.opensubsystems.core.persist.jdbc.database.postgresql.PostgreSQLDatabaseImpl;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseImpl;
import org.opensubsystems.core.util.DateUtils;
import org.opensubsystems.core.util.Tests;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * All tests related to timestamp functionality.
 * 
 * To compute timestamp based on the current timestamp in sapdb you can use
 * one of these:
 * 
 * select timestamp(date(now()), maketime((24 + hour(now()) - 4) mod 24, 
 * minute(now()), second(now()))) from dual
 * 
 * select timestamp(date(present), maketime((24 + hour(present) - 4) mod 24, 
 * minute(present), second(present))) from (select now() as present from dual) 
 *  
 * @author bastafidli
 */
public final class TimestampTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private TimestampTest(
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
      TestSuite suite = new DatabaseTestSuite("TimestampTest");
      suite.addTestSuite(TimestampTestInternal.class);
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
   public static class TimestampTestInternal extends DatabaseTest
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
       * Create new timestamp test.
       * 
       * @param strTestName - name of the test
       */
      public TimestampTestInternal(
         String strTestName
      )
      {
         super(strTestName);
      }   
   
      /**
       * Test the database support for Timestamp objects. 
       * Timestamp object takes into account both, the date and the time portion 
       * of the Java Date.
       * 
       * This class inserts timestamp into database, then retrieve it back using 
       * the same java date and deletes it using cursor.
       * 
       * Uses the already setup connection and transaction. 
       * No need to close the connection since base class is doing it for us.
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testBasicTimestampSupport(
      ) throws Throwable
      {
         final String INSERT_TIMESTAMP = "insert into TIMESTAMP_TEST(TIMESTAMP_TEST) values (?)";
         // See OracleTests class why we need to select tablename.* 
         final String SELECT_TIMESTAMP = "select TIMESTAMP_TEST.* from TIMESTAMP_TEST " +
                                         "where TIMESTAMP_TEST = ?";
         final String DELETE_TIMESTAMP = "delete from TIMESTAMP_TEST where TIMESTAMP_TEST = ?";
         // Set this to maximal possible number of nanoseconds to see if the nanoseconds matter
         // SapDB retrieves the large value for nanoseconds
         final int    PRESET_NANOSECONDS = 999999999;
          
         Calendar           calGenerate = Calendar.getInstance();
         java.sql.Timestamp insertTimestamp;
         PreparedStatement  insertStatement;
         int                iUpdateCount;
         
         // Set date of my birthday ;-)
         calGenerate.set(1974, 9, 15, 1, 2, 3);
         insertTimestamp = new java.sql.Timestamp(calGenerate.getTimeInMillis());
         // Set the nanosecond portion 
         insertTimestamp.setNanos(PRESET_NANOSECONDS);
         
//         DatabaseConnectionFactoryImpl.getInstance().returnConnection(m_connection);
         m_transaction.begin();
//         m_connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(false);
   
         try
         {
            insertStatement = m_connection.prepareStatement(INSERT_TIMESTAMP);
            insertStatement.setTimestamp(1, insertTimestamp);
   
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
   
         ResultSet          results = null;
         java.sql.Timestamp retrievedTimestamp;
         boolean            bHasMoreThanOne;
         int                iDeletedCount = 0;
   
//         DatabaseConnectionFactoryImpl.getInstance().returnConnection(m_connection);
         m_transaction.begin();
//         m_connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(false);
   
         try
         {
            java.sql.Timestamp selectTimestamp;
            boolean            bUpdatableResultSet = true;
            
            try
            {
               selectStatement = m_connection.prepareStatement(
                                    SELECT_TIMESTAMP,
                                    ResultSet.TYPE_SCROLL_SENSITIVE,
                                    ResultSet.CONCUR_UPDATABLE);
            }
            catch (SQLException sqleExc)
            {
               // Maybe it doesn't support updatable results set (e.g HSQLDB)
               // so try to work around it
               bUpdatableResultSet = false;
               selectStatement = m_connection.prepareStatement(SELECT_TIMESTAMP);
               // Don't catch any exception here, if this doesn't work, something
               // is really wrong
            }
   
            // Set the same date because it should be exactly the same
            calGenerate.set(1974, 9, 15, 1, 2, 3);
            selectTimestamp = new java.sql.Timestamp(calGenerate.getTimeInMillis());
            // Set the nanosecond portion 
            selectTimestamp.setNanos(PRESET_NANOSECONDS);
   
            selectStatement.setTimestamp(1, selectTimestamp);
            results = selectStatement.executeQuery();
            
            // Get the timestamp from the database
            assertTrue("The inserted timestamp is not in the database.", results.next());
            retrievedTimestamp = results.getTimestamp(1);
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
               deleteStatement = m_connection.prepareStatement(DELETE_TIMESTAMP);
               deleteStatement.setTimestamp(1, insertTimestamp);
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
            
//            DatabaseConnectionFactoryImpl.getInstance().returnConnection(m_connection);
            m_transaction.begin();
//            m_connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(false);
            try
            {
               deleteStatement = m_connection.prepareStatement(
                        "delete from TIMESTAMP_TEST");
               deleteStatement.execute();
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw thr;
            }
            finally
            {
               DatabaseUtils.closeStatement(deleteStatement);
            } 
         }
         
         assertFalse("There should be only one inserted timestamp in the database.", 
                            bHasMoreThanOne);
      
         assertEquals("Exactly one record with date data shoud have been deleted.", 
                             iDeletedCount, 1);
   
         // And now test the timestamp                           
         assertNotNull("The inserted timestamp shouldn't be retrieved" +
                              " as null from the database", 
                              retrievedTimestamp);
         
         if (DatabaseImpl.getInstance() instanceof MySQLDatabaseImpl)
         {
            // MySQL as of version 4.1.14 doesn't support nanosecond 
            // portion of the timestamp at all. Since this is expected
            // behavior, include here checks that test that behavior in case it
            // changes in the future
            assertTrue("The test behavior for MySQL has changed. Review it",
                       insertTimestamp.getNanos() == 999999999);
            assertTrue("The MySQL time support has changed. Review it",
                       retrievedTimestamp.getNanos() == 0);
         }
         else
         {
            // Use toString instead of DateFormat.getDateTimeInstance().format
            // since format doesn't take nanoseconds into account.
            assertTrue("The timestamp retrieved from database "
                       + retrievedTimestamp.toString()
                       + " is not the same as the inserted one "
                       + insertTimestamp.toString(),
                       DateUtils.dateAndTimeEquals(retrievedTimestamp, 
                                                   insertTimestamp));
         }
                              
         if (DatabaseImpl.getInstance() instanceof MySQLDatabaseImpl)
         {
            // MySQL as of version 4.1.14  doesn't support nanosecond portion of 
            // the timestamp at all. Since this is expected behavior, include 
            // here checks that test that behavior in case it changes in the 
            // future
            assertTrue("The test behavior for MySQL has changed. Review it",
                       insertTimestamp.getNanos() == 999999999);
            assertTrue("The MySQL timestamp support has changed. Review it",
                       retrievedTimestamp.getNanos() == 0);
         }
         else if (DatabaseImpl.getInstance() instanceof PostgreSQLDatabaseImpl)
         {
            // PostgreSQL as of version 8.0.3 looses details from the
            // nanosecond portion of timestamp fields. Since this is expected
            // behavior, include here checks that test that behavior in case it
            // changes in the future
            assertTrue("The test behavior for PostgreSQL has changed. Review it",
                       insertTimestamp.getNanos() == 999999999);
            assertTrue("The PostgreSQL timestamp support has changed. Review it",
                       retrievedTimestamp.getNanos() == 999999000);
         }
         else
         {
            assertEquals("The database doesn't support correct retrieval of"
                         + " large nanosecond portion of the timestamp. The"
                         + " retrieved one is not the same as the inserted one.",
                         insertTimestamp.getNanos(), 
                         retrievedTimestamp.getNanos());
         }
      }   
   
      /**
       * Test the database support for Timestamp objects. 
       * Timestamp object takes into account both, the date and the time portion 
       * of the Java Date.
       * 
       * This class inserts timestamp into database, then retrieve it back using 
       * the same java date and deletes it using cursor. This test with small value
       * for nanoseconds to see how the database behaves.
       * 
       * Uses the already setup connection and transaction. 
       * No need to close the connection since base class is doing it for us.
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testSmallNanosTimestampSupport(
      ) throws Throwable
      {
         final String INSERT_TIMESTAMP = "insert into TIMESTAMP_TEST(TIMESTAMP_TEST) values (?)";
         // See OracleTests class why we need to select tablename.* 
         final String SELECT_TIMESTAMP 
                          = "select TIMESTAMP_TEST.* from TIMESTAMP_TEST where TIMESTAMP_TEST = ?";
         final String DELETE_TIMESTAMP = "delete from TIMESTAMP_TEST where TIMESTAMP_TEST = ?";
         // Use very small nanosecond number since SapDB Ignores the small nannoseconds
         final int    PRESET_NANOSECONDS = 5;
          
         Calendar           calGenerate = Calendar.getInstance();
         java.sql.Timestamp insertTimestamp;
         PreparedStatement  insertStatement;
         int                iUpdateCount;
         
         // Set date of my birthday ;-)
         calGenerate.set(1974, 9, 15, 1, 2, 3);
         insertTimestamp = new java.sql.Timestamp(calGenerate.getTimeInMillis());
         // Set the nanosecond portion 
         insertTimestamp.setNanos(PRESET_NANOSECONDS);
         
//         DatabaseConnectionFactoryImpl.getInstance().returnConnection(m_connection);
         m_transaction.begin();
//         m_connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(false);
   
         try
         {
            insertStatement = m_connection.prepareStatement(INSERT_TIMESTAMP);
            insertStatement.setTimestamp(1, insertTimestamp);
   
            iUpdateCount = DatabaseUtils.executeUpdateAndClose(insertStatement);
   
            m_transaction.commit();
         }
         catch (Throwable throwable)
         {
            m_transaction.rollback();
            throw throwable;
         }
         
         assertEquals("Exactly one record with time data shoud have been inserted.",
                      1, iUpdateCount);
         
         // Now select it back to be sure it is there
         PreparedStatement selectStatement = null;
         PreparedStatement deleteStatement = null;      
         ResultSet          results = null;
         java.sql.Timestamp retrievedTimestamp;
         boolean            bHasMoreThanOne;
         int                iDeletedCount = 0;
   
//         DatabaseConnectionFactoryImpl.getInstance().returnConnection(m_connection);
         m_transaction.begin();
//         m_connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(false);
   
         try
         {
            java.sql.Timestamp selectTimestamp;
            boolean            bUpdatableResultSet = true;
            
            try
            {
               selectStatement = m_connection.prepareStatement(
                                    SELECT_TIMESTAMP,
                                    ResultSet.TYPE_SCROLL_SENSITIVE,
                                    ResultSet.CONCUR_UPDATABLE);
            }
            catch (SQLException sqleExc)
            {
               // Maybe it doesn't support updatable results set (e.g HSQLDB)
               // so try to work around it
               bUpdatableResultSet = false;
               selectStatement = m_connection.prepareStatement(SELECT_TIMESTAMP);
               // DOn't catch any exception here, if this doesn't work, something
               // is really wrong
            }
   
            // Set the same date because it should be exactly the same
            calGenerate.set(1974, 9, 15, 1, 2, 3);
            selectTimestamp = new java.sql.Timestamp(calGenerate.getTimeInMillis());
            // Set the nanosecond portion 
            selectTimestamp.setNanos(PRESET_NANOSECONDS);
            
            selectStatement.setTimestamp(1, selectTimestamp);
            results = selectStatement.executeQuery();
            
            // Get the timestamp from the database
            assertTrue("The inserted timestamp is not in the database.", 
                              results.next());                           
            retrievedTimestamp = results.getTimestamp(1);
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
               deleteStatement = m_connection.prepareStatement(DELETE_TIMESTAMP);
               deleteStatement.setTimestamp(1, insertTimestamp);
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
            
//            DatabaseConnectionFactoryImpl.getInstance().returnConnection(m_connection);
            m_transaction.begin();
//            m_connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(false);
            
            try
            {
               deleteStatement = m_connection.prepareStatement(
                        "delete from TIMESTAMP_TEST");
               deleteStatement.execute();
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw thr;
            }
            finally
            {
               DatabaseUtils.closeStatement(deleteStatement);
            } 
         }
         
         assertFalse("There should be only one inserted timestamp in the database.", 
                            bHasMoreThanOne);
      
         assertEquals("Exactly one record with date data shoud have been deleted.", 
                             iDeletedCount, 1);
   
         // And now test the timestamp                           
         assertNotNull("The inserted timestamp shouldn't be retrieved" +
                              " as null from the database", 
                              retrievedTimestamp);
         
         assertTrue("The timestamp retrieved from database "
                    + DateFormat.getDateTimeInstance().format(retrievedTimestamp)
                    + " is not the same as the inserted one "
                    + DateFormat.getDateTimeInstance().format(insertTimestamp),
                    DateUtils.dateAndTimeEquals(retrievedTimestamp, 
                                                insertTimestamp));
   
         if (DatabaseImpl.getInstance() instanceof MySQLDatabaseImpl)
         {
            // MySQL as of version 4.1.14  doesn't support nanosecond 
            // portion of the timestamp at all. Since this is expected
            // behavior, include here checks that test that behavior in case it
            // changes in the future
            assertTrue("The test behavior for MySQL has changed. Review it",
                       insertTimestamp.getNanos() == 5);
            assertTrue("The MySQL timestamp support has changed. Review it",
                       retrievedTimestamp.getNanos() == 0);
         }
         else if (DatabaseImpl.getInstance() instanceof PostgreSQLDatabaseImpl)
         {
            // PostgreSQL as of version 8.0.3 looses details from the
            // nanosecond portion of timestamp fields. Since this is expected
            // behavior, include here checks that test that behavior in case it
            // changes in the future
            assertTrue("The test behavior for PostgreSQL has changed. Review it",
                       insertTimestamp.getNanos() == 5);
            assertTrue("The PostgreSQL timestamp support has changed. Review it",
                       retrievedTimestamp.getNanos() == 0);
         }
         else
         {
            assertEquals("The database doesn't support correct retrieval of" 
                         + " small nanosecond portion of the timestamp. The" 
                         + " retrieved one is not the same as the inserted one.",
                         insertTimestamp.getNanos(), 
                         retrievedTimestamp.getNanos());
         }
      }   
   
      /**
       * Test the database support for Timestamp objects. 
       * Timestamp object takes into account both, the date and the time portion 
       * of the Java Date.
       * 
       * This class inserts timestamp into database, then retrieve it back using 
       * the same java date and deletes it using cursor.
       * 
       * Uses the already setup connection and transaction. 
       * No need to close the connection since base class is doing it for us.
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testTimestampNanosSupport(
      ) throws Throwable
      {
         final String INSERT_TIMESTAMP = "insert into TIMESTAMP_TEST(TIMESTAMP_TEST) values (?)";
         // See OracleTests class why we need to select tablename.* 
         final String SELECT_TIMESTAMP 
                          = "select TIMESTAMP_TEST.* from TIMESTAMP_TEST where TIMESTAMP_TEST = ?";
         final int    PRESET_NANOSECONDS = 5;
         final int    DIFFERENT_PRESET_NANOSECONDS = 999999999;
          
         Calendar           calGenerate = Calendar.getInstance();
         java.sql.Timestamp insertTimestamp;
         PreparedStatement  insertStatement;
         int                iUpdateCount;
         
         // Set date of my birthday ;-)
         calGenerate.set(1974, 9, 15, 1, 2, 3);
         insertTimestamp = new java.sql.Timestamp(calGenerate.getTimeInMillis());
         // Set the nanosecond portion 
         insertTimestamp.setNanos(PRESET_NANOSECONDS);
         
//         DatabaseConnectionFactoryImpl.getInstance().returnConnection(m_connection);
         m_transaction.begin();
//         m_connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(false);
   
         try
         {
            insertStatement = m_connection.prepareStatement(INSERT_TIMESTAMP);
            insertStatement.setTimestamp(1, insertTimestamp);
   
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
         ResultSet          results = null;
   
         try
         {
            java.sql.Timestamp selectTimestamp;
            
            selectStatement = m_connection.prepareStatement(SELECT_TIMESTAMP);
   
            // Set the same date using the time we have inserted 
            selectTimestamp = new java.sql.Timestamp(insertTimestamp.getTime());
            selectTimestamp.setNanos(DIFFERENT_PRESET_NANOSECONDS);
            
            selectStatement.setTimestamp(1, selectTimestamp);
            results = selectStatement.executeQuery();
            
            if (DatabaseImpl.getInstance() instanceof MySQLDatabaseImpl)
            {
               java.sql.Timestamp retrievedTimestamp;
               
               // MySQL as of version 4.1.14  doesn't support nanosecond 
               // portion of the timestamp at all and therefore will retrieve 
               // the value even when different time stamp is specified. Since 
               // this is expected behavior, include here checks that test that 
               // behavior in case it changes in the future
               assertTrue("The test behavior for MySQL has changed. Review it.",
                          results.next());
               
               retrievedTimestamp = results.getTimestamp(1);
               
               assertTrue("The timestamp behavior for MySQL has changed."
                          + " Review it.",
                          retrievedTimestamp.getNanos() == 0);
               // Reset the nanosecond portion so that we can test the rest
               // of the date for equality
               retrievedTimestamp.setNanos(insertTimestamp.getNanos());
               
               assertTrue("The test behavior for MySQL has changed. Review it.",
                          DateUtils.dateAndTimeEquals(insertTimestamp, 
                                                      retrievedTimestamp)); 
            }
            else
            {
               // Get the timestamp from the database. We shouldn't get one
               assertFalse("The inserted timestamp was retrieved from the"
                           + " database even though different nanoseconds" 
                           + " were specified and no result should have been"
                           + " retrieved.", results.next());
            }
         }
         catch (Throwable throwable)
         {
            throw throwable;
         }
         finally
         {
            DatabaseUtils.closeResultSetAndStatement(results, selectStatement);
            
//            DatabaseConnectionFactoryImpl.getInstance().returnConnection(m_connection);
            m_transaction.begin();
//            m_connection = DatabaseConnectionFactoryImpl.getInstance().requestConnection(false);
            
            try
            {
               deleteStatement = m_connection.prepareStatement(
                        "delete from TIMESTAMP_TEST");
               deleteStatement.executeUpdate();
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw thr;
            }
            finally
            {
               DatabaseUtils.closeStatement(deleteStatement);
            } 
         }
      }   
   }
}

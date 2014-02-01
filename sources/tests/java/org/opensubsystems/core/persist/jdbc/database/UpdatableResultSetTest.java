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
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.jdbc.Database;
import org.opensubsystems.core.persist.jdbc.DatabaseTest;
import org.opensubsystems.core.persist.jdbc.DatabaseTestSetup;
import org.opensubsystems.core.persist.jdbc.DatabaseTestSuite;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseImpl;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.Tests;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * All tests related to result set functionality. Mainly it tests if result set
 * is updatable. 
 * 
 * @author bastafidli
 */
public final class UpdatableResultSetTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private UpdatableResultSetTest(
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
      TestSuite suite = new DatabaseTestSuite("UpdatableResultSetTest");
      suite.addTestSuite(UpdatableResultSetTestInternal.class);
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
   public static class UpdatableResultSetTestInternal extends DatabaseTest
   {
      // Cached values ////////////////////////////////////////////////////////////
   
      /**
       * Logger for this class
       */
      private static Logger s_logger = Log.getInstance(UpdatableResultSetTestInternal.class);
   
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
       * Create new result set test.
       * 
       * @param strTestName - name of the test
       */
      public UpdatableResultSetTestInternal(
         String strTestName
      )
      {
         super(strTestName);
      }
      
      /**
       * Test if the database driver support updatable result set. 
       * 
       * Uses the already setup connection and transaction. 
       * No need to close the connection since base class is doing it for us.
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testDeleteRow(
      ) throws Throwable
      {
         final String INSERT_VALUE = "insert into RESULTSET_TEST(RESULTSET_TEST) values (?)";
         // See OracleTests class why we need to select tablename.* 
         final String SELECT_VALUE 
                          = "select RESULTSET_TEST.* from RESULTSET_TEST where RESULTSET_TEST = ?";
         final String DELETE_VALUE = "delete from RESULTSET_TEST where RESULTSET_TEST = ?";
         final String VALUE_TEST = "test value";
          
         PreparedStatement insertStatement;
         int               iUpdateCount;
         
         m_transaction.begin();
   
         try
         {
            insertStatement = m_connection.prepareStatement(INSERT_VALUE);
            insertStatement.setString(1, VALUE_TEST);
   
            iUpdateCount = DatabaseUtils.executeUpdateAndClose(insertStatement);
   
            m_transaction.commit();
         }
         catch (Throwable throwable)
         {
            m_transaction.rollback();
            throw throwable;
         }
         
         assertEquals("Exactly one record have been inserted.", 
                             iUpdateCount, 1);
         
         // Now select it back to be sure it is there so we cna delete it
         PreparedStatement selectStatement = null;
         PreparedStatement deleteStatement = null;
         
         ResultSet         results = null;
         int                iDeletedCount = 0;
         boolean            bUpdatableResultSet = true;
   
         boolean bHasMoreThanOne;
         String  retrievedValue;
         
         m_transaction.begin();
   
         try
         {
            SQLException sqleThrownExc = null;
            
            try
            {
               selectStatement = m_connection.prepareStatement(
                                    SELECT_VALUE,
                                    ResultSet.TYPE_SCROLL_SENSITIVE,
                                    ResultSet.CONCUR_UPDATABLE);
            }
            catch (SQLException sqleExc)
            {
               // Maybe it doesn't support updatable results set (e.g HSQLDB)
               // so try to work around it
               bUpdatableResultSet = false;
               sqleThrownExc = sqleExc;
               selectStatement = m_connection.prepareStatement(SELECT_VALUE);
               // DOn't catch any exception here, if this doesn't work, something
               // is really wrong
            }
            selectStatement.setString(1, VALUE_TEST);
            results = selectStatement.executeQuery();
            
            assertTrue("The inserted record is not in the database.", 
                              results.next());                           
            retrievedValue = results.getString(1);
            
            // Delete the inserted row, this method shouldn't cause exception 
            if (bUpdatableResultSet)
            {
               // This is the optimal way to do it, but not every database driver supports
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
                  sqleThrownExc = sqleExc;
               }
            }
            if (!bUpdatableResultSet)
            {
               // I really don't want to fail this test, just to detect that 
               // this database driver still doesn't support the updatable 
               // result set
               s_logger.log(Level.WARNING,"Database driver still doesn't support"
                            + " updatable resultset: {0}", sqleThrownExc); 
               
               deleteStatement = m_connection.prepareStatement(DELETE_VALUE);
               deleteStatement.setString(1, VALUE_TEST);
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
                        "delete from RESULTSET_TEST");
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
         
         assertFalse("There should be only one inserted record in the database.", 
                            bHasMoreThanOne);
      
         assertEquals("Exactly one record with date data shoud have been deleted.", 
                             iDeletedCount, 1);
   
         // And now test the value                           
         assertNotNull("The inserted value shouldn't be retrieved as null" +
                              " from the database", 
                              retrievedValue);
         assertEquals("The value retrieved from database "
                             + " is not the same as the inserted one ",
                             VALUE_TEST, retrievedValue);             
   
         
      }   
   }
}

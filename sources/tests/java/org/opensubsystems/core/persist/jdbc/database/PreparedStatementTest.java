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
import java.util.logging.Logger;
import java.util.logging.Level;

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
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.test.Tests;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;


/**
 * All tests related to prepared statement.
 * 
 * @author bastafidli
 */
public final class PreparedStatementTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private PreparedStatementTest(
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
      TestSuite suite = new DatabaseTestSuite("PreparedStatementTest");
      suite.addTestSuite(PreparedStatementTestInternal.class);
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
   public static class PreparedStatementTestInternal extends DatabaseTest
   {
      // Cached values ////////////////////////////////////////////////////////////
   
      /**
       * Logger for this class
       */
      private static Logger s_logger = Log.getInstance(PreparedStatementTest.class);
   
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
      public PreparedStatementTestInternal(
         String strTestName
      )
      {
         super(strTestName);
      }
      
      /**
       * Test if the database driver supports fetching of generated keys. 
       * 
       * Uses the already setup connection and transaction. 
       * No need to close the connection since base class is doing it for us.
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testRepeatedTransactionInsert(
      ) throws Throwable
      {
         final String INSERT_VALUE = ((DatabaseTestSchema)DatabaseSchemaManager.getInstance(
                                        DatabaseTestSchema.class)).getInsertGeneratedKey();
         final String DELETE_VALUE = "delete from GENERATEDKEY_TEST where TEST_VALUE = ?";
         final String VALUE_TEST = "insert test value";
         // This should be above 100 to test JOTM/XAPool looping bug
         final int    REPEAT_COUNT = 530;
          
         PreparedStatement insertStatement = null;
         ResultSet         rsResults = null;
         int               iInsertCount;
         int               iIndex;
         
         for (iIndex = 0; iIndex < REPEAT_COUNT; iIndex++)
         {
            m_transaction.begin();
            try
            {
               try
               {
                  insertStatement = m_connection.prepareStatement(INSERT_VALUE);
                  insertStatement.setString(1, VALUE_TEST);
      
                  iInsertCount = insertStatement.executeUpdate();
               }
               finally
               {   
                  DatabaseUtils.closeResultSetAndStatement(rsResults, insertStatement);
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
         }
         s_logger.log(Level.FINEST, "Inserted {0} records, each in separate"
                      + " transaction.", iIndex);
                                                             
         // Now select it back to be sure it is there so we can delete it
         int iDeletedCount;
   
         m_transaction.begin();
         try
         {
            PreparedStatement deleteStatement;
            
            deleteStatement = m_connection.prepareStatement(DELETE_VALUE);
            deleteStatement.setString(1, VALUE_TEST);
            iDeletedCount = DatabaseUtils.executeUpdateAndClose(deleteStatement);
            m_transaction.commit();
            
            assertEquals("Exactly " + iIndex + " records with data shoud have"
                         + " been deleted.", iDeletedCount, iIndex);
   
            s_logger.log(Level.FINEST, "Deleted {0} records, all in one transaction.", 
                         iIndex);
         }
         catch (Throwable throwable)
         {
            m_transaction.rollback();
            throw throwable;
         }
      }   
   }
}

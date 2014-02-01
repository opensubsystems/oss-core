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
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.jdbc.Database;
import org.opensubsystems.core.persist.jdbc.DatabaseSchemaManager;
import org.opensubsystems.core.persist.jdbc.DatabaseTest;
import org.opensubsystems.core.persist.jdbc.DatabaseTestSetup;
import org.opensubsystems.core.persist.jdbc.DatabaseTestSuite;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseImpl;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.Tests;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * All tests related to test key generation by database.
 * 
 * @author bastafidli
 */
public final class GeneratedKeyTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private GeneratedKeyTest(
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
      TestSuite suite = new DatabaseTestSuite("GeneratedKeyTest");
      suite.addTestSuite(GeneratedKeyTestInternal.class);
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
   public static class GeneratedKeyTestInternal extends DatabaseTest
   {
      // Cached values ////////////////////////////////////////////////////////////
   
      /**
       * Logger for this class
       */
      private static Logger s_logger = Log.getInstance(GeneratedKeyTest.class);
   
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
      public GeneratedKeyTestInternal(
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
      public void testGeneratedKey(
      ) throws Throwable
      {
         final String INSERT_VALUE = ((DatabaseTestSchema)DatabaseSchemaManager.getInstance(
                                        DatabaseTestSchema.class)).getInsertGeneratedKey();
         final String DELETE_VALUE = "delete from GENERATEDKEY_TEST where TEST_VALUE = ?";
         final String VALUE_TEST = "test value";
          
         PreparedStatement insertStatement = null;
         ResultSet         rsResults = null;
         int               iInsertCount;
         
         m_transaction.begin();
   
         try
         {
            insertStatement = m_connection.prepareStatement(INSERT_VALUE);
            insertStatement.setString(1, VALUE_TEST);
   
            iInsertCount = insertStatement.executeUpdate();
   
            // Now try to retrieve the generated key
            try
            {
               rsResults = insertStatement.getGeneratedKeys();
               rsResults.getInt(1);
            }
            catch (Throwable throwable)
            {
               // I really don't want to fail this test, just to detect that
               // this database driver still doesn't support the updatable result set
               s_logger.log(Level.WARNING, "Warning: Database driver still"
                            + " doesn't support retrieval of generated keys: {0}", 
                            throwable); 
            }                  
   
            m_transaction.commit();
         }
         catch (Throwable throwable)
         {
            m_transaction.rollback();
            throw throwable;
         }
         finally
         {
            DatabaseUtils.closeResultSetAndStatement(rsResults, insertStatement);
         }
         
         assertEquals("Exactly one record have been inserted.", 
                             iInsertCount, 1);
                                                             
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
            
            assertEquals("Exactly one record with date data shoud have been deleted.", 
                                1, iDeletedCount);
         }
         catch (Throwable throwable)
         {
            m_transaction.rollback();
            throw throwable;
         }
      }   
      
      /**
       * Test if we can fetch generated keys using stored procedure 
       * 
       * Uses the already setup connection and transaction. 
       * No need to close the connection since base class is doing it for us.
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testGeneratedKeyUsingStoredProcedure(
      ) throws Throwable
      {
         final String DELETE_VALUE = "delete from GENERATEDKEY_TEST where TEST_VALUE = ?";
         final String VALUE_TEST = "test value";
          
         int iInsertCount = 0;
         int iGeneratedKey = 0;
         
         m_transaction.begin();
   
         try
         {
            int[] returnValues;
            
            returnValues = ((DatabaseTestSchema)DatabaseSchemaManager.getInstance(
                              DatabaseTestSchema.class)).executeInsertGeneratedKey2(
                                 m_connection, VALUE_TEST);
            if (returnValues != null)
            {
               iInsertCount = returnValues[0];                           
               iGeneratedKey = returnValues[1];                           
            }
            
            m_transaction.commit();
         }
         catch (Throwable throwable)
         {
            m_transaction.rollback();
            throw throwable;
         }
         
         assertEquals("Exactly one record have been inserted.", 
                             1, iInsertCount);
         assertTrue("Generated key must be greater than 0", iGeneratedKey > 0);
         s_logger.log(Level.FINEST, "Database generated and using stored"
                      + " procedure returned key {0}", iGeneratedKey);
                                                             
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
            
            assertEquals("Exactly one record with date data shoud have been deleted.", 
                         iDeletedCount, 1);
         }
         catch (Throwable throwable)
         {
            m_transaction.rollback();
            throw throwable;
         }
      }   
      
      /**
       * Test if the database driver supports inserting into tables where the foreign key
       * pointing to the same row which is being inserted. 
       * 
       * Uses the already setup connection and transaction. 
       * No need to close the connection since base class is doing it for us.
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testForeignKey(
      ) throws Throwable
      {
         final String INSERT_VALUE = "insert into OWN_FK_TEST (TEST_ID,FK_ID) values (?,?)";
         final String UPDATE_VALUE = "update OWN_FK_TEST set FK_ID = ? where TEST_ID = ?";
         final String DELETE_VALUE = "delete from OWN_FK_TEST where TEST_ID = ?";
          
         PreparedStatement insertStatement = null;
         PreparedStatement deleteStatement = null;
         int               iInsertCount;
         int               iDeleteCount;
         
         m_transaction.begin();
   
         try
         {
            try
            {
               insertStatement = m_connection.prepareStatement(INSERT_VALUE);
               insertStatement.setInt(1, 1);
               insertStatement.setInt(2, 1);
               iInsertCount = insertStatement.executeUpdate();
               if (GlobalConstants.ERROR_CHECKING)
               {
                  // This is here to avoid Checkstyle warning
                  assert iInsertCount >= 0 : "executeUpdate cannot return negative value.";
               }
            }
            catch (Throwable throwable)
            {
               assertTrue("Not possible to insert data which FK is pointing to themselves.", false);
   
               insertStatement = m_connection.prepareStatement(INSERT_VALUE);
               insertStatement.setInt(1, 1);
               insertStatement.setNull(2, Types.INTEGER);
               iInsertCount = insertStatement.executeUpdate();
               assertEquals("The inserted count doesn't match the expected value", 
                            0, iInsertCount);
   
               insertStatement = m_connection.prepareStatement(UPDATE_VALUE);
               insertStatement.setInt(1, 1);
               insertStatement.setInt(2, 1);
               assertEquals("The inserted count doesn't match the expected value", 
                            0, iInsertCount);
   
            }
            
            // Now try to retrieve the generated key
            try
            {
               deleteStatement = m_connection.prepareStatement(DELETE_VALUE);
               deleteStatement.setInt(1, 1);
               iDeleteCount = deleteStatement.executeUpdate();
               if (GlobalConstants.ERROR_CHECKING)
               {
                  // This is here to avoid Checkstyle warning
                  assert iDeleteCount >= 0 : "executeUpdate cannot return negative value.";
               }               
            }
            catch (Throwable throwable)
            {
               assertTrue("Not possible to delete data which FK is pointing to themselves.", 
                                    false); 
               throw throwable;
            }                  
   
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
            DatabaseUtils.closeStatement(insertStatement);
         }
      }     
   }
}

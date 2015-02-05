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
import org.opensubsystems.core.persist.jdbc.DatabaseSchemaManager;
import org.opensubsystems.core.persist.jdbc.test.DatabaseTest;
import org.opensubsystems.core.persist.jdbc.test.DatabaseTestSetup;
import org.opensubsystems.core.persist.jdbc.test.DatabaseTestSuite;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseImpl;
import org.opensubsystems.core.util.test.Tests;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * Test for selecting data after inserting records and then inserting duplicate 
 * record into the unique table column.
 * 
 * @author opensubsystems
 */
public final class SelectAfterInsertIntoUniqueColumnTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private SelectAfterInsertIntoUniqueColumnTest(
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
      TestSuite suite = new DatabaseTestSuite("SelectAfterInsertIntoUniqueColumnTest");
      suite.addTestSuite(SelectAfterInsertIntoUniqueColumnTestInternal.class);
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
   public static class SelectAfterInsertIntoUniqueColumnTestInternal extends DatabaseTest
   {
      // Constructors //////////////////////////////////////////////////////////      
      
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
      public SelectAfterInsertIntoUniqueColumnTestInternal(
         String strTestName
      )
      {
         super(strTestName);
      }
      
      /**
       * Test for selecting data after inserting records and then inserting 
       * duplicate record into the unique table column.
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testSelectAfterInsertIntoUniqueColumn(
      ) throws Throwable
      {
         final String INSERT_VALUE = "insert into UNIQUE_COLUMN_TEST (TEST_ID) values (?)";
         final String SELECT_VALUE = "select count(*) from UNIQUE_COLUMN_TEST";
         final String DELETE_ALL = "delete from UNIQUE_COLUMN_TEST";
   
         PreparedStatement deleteStatement = null;
         PreparedStatement selectStatement = null;
         ResultSet         rsResults       = null;
         int               iDeletedCount   = 0;
   
         try
         {
            // Try to select original record to verify that the database is in 
            // OK state
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
            finally
            {
               DatabaseUtils.close(deleteStatement);
               deleteStatement = null;
            }
            
            assertEquals("No records should be initially in the database.", 
                         0, iDeletedCount);
   
            // insert value
            m_transaction.begin();
            try
            {
               boolean bInsert;
               
               // Delegate this operation to database since for different
               // databases we may need to do different things to handle
               // failing command
               bInsert = ((DatabaseTestSchema)DatabaseSchemaManager.getInstance(
                           DatabaseTestSchema.class)).executeDuplicateInsert(
                                                         m_connection,
                                                         INSERT_VALUE);
               if (bInsert)
               {
                  fail("Database allowed to insert duplicate value into"
                           + " unique column.");
               }
                  
               try
               {
                  selectStatement = m_connection.prepareStatement(SELECT_VALUE);
                  rsResults = selectStatement.executeQuery();
                  
                  if (rsResults.next())
                  {
                     assertEquals("Incorrect number of selected items", 
                                  5, rsResults.getInt(1));
                  }
               }
               finally
               {
                  DatabaseUtils.close(rsResults, 
                                                           selectStatement);
               }
               
               m_transaction.commit();
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
               deleteStatement = m_connection.prepareStatement(DELETE_ALL);
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
               DatabaseUtils.close(deleteStatement);
            }
         }
      }   
   }
}

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
import org.opensubsystems.core.persist.jdbc.DatabaseTest;
import org.opensubsystems.core.persist.jdbc.DatabaseTestSetup;
import org.opensubsystems.core.persist.jdbc.DatabaseTestSuite;
import org.opensubsystems.core.persist.jdbc.database.hsqldb.HsqlDBDatabaseImpl;
import org.opensubsystems.core.persist.jdbc.database.mysql.MySQLDatabaseImpl;
import org.opensubsystems.core.persist.jdbc.database.postgresql.PostgreSQLDatabaseImpl;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseImpl;
import org.opensubsystems.core.util.Tests;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * Test for returning number of inserted rows from stored procedure.
 * 
 * @author opensubsystems
 */
public final class DBProcReturnInsertedRowsCountTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private DBProcReturnInsertedRowsCountTest(
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
      TestSuite suite = new DatabaseTestSuite("DBProcReturnInsertedRowsCountTest");
      suite.addTestSuite(DBProcReturnInsertedRowsCountTestInternal.class);
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
   public static class DBProcReturnInsertedRowsCountTestInternal extends DatabaseTest
   {
      /**
       * Static initializer
       */
      static
      {
         // This test use special database schema so make the database aware of 
         // it
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
      public DBProcReturnInsertedRowsCountTestInternal(
         String strTestName
      )
      {
         super(strTestName);
      }
      
      /**
       * Test if the DB procedure will return number of inserted rows. 
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testDBProcReturnInsertRowsCount(
      ) throws Throwable
      {
         String DELETE_VALUE = "delete from GENERATEDKEY_TEST where TEST_VALUE = ?";
         String VALUE_TEST = "test value";
          
         int iInsertCount = 0;
         int iInsertedRows = 0;
   
         PreparedStatement deleteStatement = null;
   
         m_transaction.begin();
   
         try
         {
            try
            {
               int[] returnValues;
               // Call stored procedure that will insert 1 record into the DB and returns
               // number of inserted rows
               returnValues = ((DatabaseTestSchema)DatabaseSchemaManager.getInstance(
                                 DatabaseTestSchema.class)).executeInsertRow(
                                    m_connection, VALUE_TEST);
               if (returnValues != null)
               {
                  // value (number of affected rows) returned from insertStatement.executeUpdate(); 
                  iInsertCount = returnValues[0];    
                  // value (number of inserted rows) returned from stored procedure.
                  iInsertedRows = returnValues[1];
               }
               
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            
            if (DatabaseImpl.getInstance() instanceof PostgreSQLDatabaseImpl)
            {
               // As of version 8.0.3 PostgreSQL doesn't support returning 
               // number of affected rows from stored procedure call. Since this 
               // is expected behavior doesn't report as our error but test if 
               // the behavior changes.
               assertTrue("PostgreSQL stored procedure call behavior has changed."
                        + " Review it.", iInsertCount == -1);
            }
            else
            {
               assertEquals("DBMS does not support returning number of" 
                            + " processed rows using"
                            + " [ RowCounter = insertStatement.executeUpdate() ]. " 
                            + " Number of inserted rows is incorrect.", 
                            1, iInsertCount);
            }
            
            if (DatabaseImpl.getInstance() instanceof HsqlDBDatabaseImpl)
            {
               // As of version 1.8.0.1 HsqlDB doesn't support stored procedures
               assertTrue("HsqlDB stored procedure behavior has changed."
                          + " Review it.", iInsertedRows == -1);
            }
            else if (DatabaseImpl.getInstance() instanceof MySQLDatabaseImpl)
            {
               // As of version 4.1.14 MySQL doesn't support stored procedures
               assertTrue("MySQL stored procedure behavior has changed."
                          + " Review it.", iInsertedRows == -1);
            }
            else
            {
               // This assert tests if particular DBS doesn't support stored 
               // procedures or doesn't support retrieving number of affected 
               // rows.
               assertTrue("DBMS does not support stored procedures or stored"
                          + " procedure does not support returning number of" 
                          + " affected rows. Number of inserted rows returned" 
                          + " from stored procedure must be greater than 0.",
                          iInsertedRows > 0);
            }
                                                                
            int iDeletedCount = 0;
      
            m_transaction.begin();
      
            try
            {
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
         finally
         {
            // delete inserted data
            m_transaction.begin();
            try
            {
               // delete from GENERATEDKEY_TEST table
               deleteStatement = m_connection.prepareStatement(DELETE_VALUE);
               deleteStatement.setString(1, VALUE_TEST);
               DatabaseUtils.executeUpdateAndClose(deleteStatement);
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
         }
      }   
   }
}

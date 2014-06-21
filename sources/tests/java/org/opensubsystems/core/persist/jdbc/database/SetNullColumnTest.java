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
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.test.Tests;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * Test for setting of NULL value and retrieving it from the varchar type DB column 
 * 
 * @author OpenSubsystems
 */
public final class SetNullColumnTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private SetNullColumnTest(
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
      TestSuite suite = new DatabaseTestSuite("SetNullColumnTest");
      suite.addTestSuite(SetNullColumnTestInternal.class);
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
   public static class SetNullColumnTestInternal extends DatabaseTest
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
      public SetNullColumnTestInternal(
         String strTestName
      )
      {
         super(strTestName);
      }
      
      /**
       * Test if there can be stored empty string in the VARCHAR type column 
       * This is not working with some older version of 'com.p6spy.engine.spy.P6SpyDriver'
       * also Oracle doesn't make any difference betweeen null and empty string
       * which may cause problems in the code.
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testInsertSelectEmptyString(
      ) throws Throwable
      {
         final String INSERT_VALUE = "insert into NULL_COLUMN_TEST (NAME) values (?)";
         final String SELECT_VALUE = "select NAME from NULL_COLUMN_TEST";
         final String DELETE_VALUE = "delete from NULL_COLUMN_TEST";
         
         PreparedStatement insertStatement = null;
         ResultSet         rsResults = null;
         int               iInsertCount;
         
         String  testValue;
         boolean testResult;
         // insert values to the table
         m_transaction.begin();
         try
         {
            try
            {
               // insert empty string
               insertStatement = m_connection.prepareStatement(INSERT_VALUE);
               insertStatement.setString(1, "");
               iInsertCount = insertStatement.executeUpdate();
               if (GlobalConstants.ERROR_CHECKING)
               {
                  // This is here to avoid Checkstyle warning
                  assert iInsertCount >= 0 
                         : "executeUpdate cannot return negative value.";
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
               rsResults = null;
            }
   
            // try to select values
            PreparedStatement selectStatement = null;
         
            try
            {
               selectStatement = m_connection.prepareStatement(SELECT_VALUE);
               rsResults = selectStatement.executeQuery();
            
               assertTrue("No result in select", rsResults.next());
               testValue = rsResults.getString(1);
               testResult = rsResults.wasNull();
               
               assertNotNull("Inserted empty string should not be retrieved"
                             + " as null", testValue);
               assertFalse("Inserted empty string shoul be not treated as"
                           + " null when retrieved", testResult);
            }
            finally
            {   
               DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
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

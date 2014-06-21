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
 * Test for same columns
 * 
 * @author opensubsystems
 */
public final class SameColumnTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private SameColumnTest(
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
      TestSuite suite = new DatabaseTestSuite("SameColumnTest");
      suite.addTestSuite(SameColumnTestInternal.class);
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
   public static class SameColumnTestInternal extends DatabaseTest
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
      public SameColumnTestInternal(
         String strTestName
      )
      {
         super(strTestName);
      }
      
      /**
       * Test if DB supports same column names in select query
       * This is not working with some older version of 'com.p6spy.engine.spy.P6SpyDriver'
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testSameColumSelect(
      ) throws Throwable
      {
         final String INSERT_VALUE1 = "insert into SAME_TEST1 (ID) values (?)";
         final String INSERT_VALUE2 = "insert into SAME_TEST2 (ID) values (?)";
         final String SELECT_VALUE = "select SAME_TEST1.ID,SAME_TEST2.ID" +
                                     " from SAME_TEST1, SAME_TEST2";
         final String DELETE_VALUE1 = "delete from SAME_TEST1";
         final String DELETE_VALUE2 = "delete from SAME_TEST2";
         
         // two different values
         final int VALUE1 = 1;
         final int VALUE2 = 2;
         
         PreparedStatement insertStatement = null;
         ResultSet         rsResults = null;
         int               iInsertCount;
         
         // insert values to each table
         m_transaction.begin();
         try
         {
            try
            {
               insertStatement = m_connection.prepareStatement(INSERT_VALUE1);
               insertStatement.setInt(1, VALUE1);
               iInsertCount = insertStatement.executeUpdate();
               if (GlobalConstants.ERROR_CHECKING)
               {
                  // This is here to avoid Checkstyle warning
                  assert iInsertCount >= 0 : "executeUpdate cannot return negative value.";
               }

               insertStatement = m_connection.prepareStatement(INSERT_VALUE2);
               insertStatement.setInt(1, VALUE2);
               iInsertCount = insertStatement.executeUpdate();
               if (GlobalConstants.ERROR_CHECKING)
               {
                  // This is here to avoid Checkstyle warning
                  assert iInsertCount >= 0 : "executeUpdate cannot return negative value.";
               }   
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
            m_transaction.commit();
   
            // try to select values
            PreparedStatement selectStatement;
         
            selectStatement = m_connection.prepareStatement(SELECT_VALUE);
            rsResults = selectStatement.executeQuery();
         
            assertTrue("No result in select", rsResults.next());
            assertEquals("The first returned value is not correct", VALUE1, rsResults.getInt(1));
            assertEquals("The second returned value is not correct", VALUE2, rsResults.getInt(2));
   
         }
         finally
         {
            // delete test data
            m_transaction.begin();
            try
            {
               PreparedStatement deleteStatement;
               
               deleteStatement = m_connection.prepareStatement(DELETE_VALUE1);
               deleteStatement.execute();
               deleteStatement = m_connection.prepareStatement(DELETE_VALUE2);
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

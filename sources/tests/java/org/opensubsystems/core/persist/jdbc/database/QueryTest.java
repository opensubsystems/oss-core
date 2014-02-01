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

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.jdbc.Database;
import org.opensubsystems.core.persist.jdbc.DatabaseSchemaManager;
import org.opensubsystems.core.persist.jdbc.DatabaseTest;
import org.opensubsystems.core.persist.jdbc.DatabaseTestSetup;
import org.opensubsystems.core.persist.jdbc.DatabaseTestSuite;
import org.opensubsystems.core.persist.jdbc.database.mysql.MySQLDatabaseImpl;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseImpl;
import org.opensubsystems.core.util.Tests;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * All tests related to sql queries problems.
 * 
 * @author opensubsystems
 */
public final class QueryTest extends Tests
{
   // Constructors /////////////////////////////////////////////////////////////
   
   /** 
    * Private constructor since this class cannot be instantiated
    */
   private QueryTest(
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
      TestSuite suite = new DatabaseTestSuite("QueryTest");
      suite.addTestSuite(QueryTestInternal.class);
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
   public static class QueryTestInternal extends DatabaseTest
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
      public QueryTestInternal(
         String strTestName
      )
      {
         super(strTestName);
      }
      
      /**
       * Test if the join with also other conditions in ON part will go through
       * In case that no join data there also should be result row
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testJoinQuery(
      ) throws Throwable
      {
         final String INSERT_BASE = "insert into GROUP_BASE_TEST values (?,?)";
         final String DELETE_BASE = "delete from GROUP_BASE_TEST";
         final String INSERT_CHILD = "insert into GROUP_CHILD_TEST values (?,?,?)";
         final String DELETE_CHILD = "delete from GROUP_CHILD_TEST";
         
         final String SELECT = "select GROUP_BASE_TEST.TEST_BASE_ID, " +
                               "GROUP_CHILD_TEST.TEST_CHILD_VALUE " +
                               "from GROUP_BASE_TEST " +
                               "left join GROUP_CHILD_TEST " +
                               "on GROUP_CHILD_TEST.TEST_BASE_FK_ID=GROUP_BASE_TEST.TEST_BASE_ID " +
                               "and GROUP_CHILD_TEST.TEST_CHILD_VALUE=? " +
                               "where GROUP_BASE_TEST.TEST_BASE_ID=?";
                                       
         PreparedStatement insertStatement = null;
         PreparedStatement deleteStatement = null;
         PreparedStatement selectStatement = null;
         ResultSet         rsResults = null;
         
         try
         {
            m_transaction.begin();
            try
            {
               insertStatement = m_connection.prepareStatement(INSERT_BASE);
               insertStatement.setInt(1, 1);
               insertStatement.setInt(2, 1);
               insertStatement.execute();
               
               insertStatement.setInt(1, 2);
               insertStatement.setInt(2, 2);
               insertStatement.execute();
   
               insertStatement = m_connection.prepareStatement(INSERT_CHILD);
               insertStatement.setInt(1, 1);
               insertStatement.setInt(2, 1);
               insertStatement.setInt(3, 1);
               insertStatement.execute();
   
               insertStatement.setInt(1, 2);
               insertStatement.setInt(2, 1);
               insertStatement.setInt(3, 2);
               insertStatement.execute();
   
               insertStatement.setInt(1, 3);
               insertStatement.setInt(2, 2);
               insertStatement.setInt(3, 3);
               insertStatement.execute();
   
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            finally
            {
               DatabaseUtils.closeStatement(insertStatement);
            }
            
            try
            {
               selectStatement = m_connection.prepareStatement(SELECT);
               selectStatement.setInt(1, 3);
               selectStatement.setInt(2, 1);
               rsResults = selectStatement.executeQuery();
               
               assertTrue("There have to be rows in result", rsResults.next());
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
            }
            
         }
         finally
         {
            m_transaction.begin();
            try
            {
               deleteStatement = m_connection.prepareStatement(DELETE_CHILD);
               deleteStatement.execute();
               deleteStatement = m_connection.prepareStatement(DELETE_BASE);
               deleteStatement.execute();
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            finally
            {
               DatabaseUtils.closeStatement(deleteStatement);
            }
         }
      }   
      
      /**
       * Test if the group will be correct if no specified data in DB 
       * In that case there should be no row in result
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testGroupQuery(
      ) throws Throwable
      {
         final String INSERT_BASE = "insert into GROUP_BASE_TEST values (?,?)";
         final String DELETE_BASE = "delete from GROUP_BASE_TEST";
         final String INSERT_CHILD = "insert into GROUP_CHILD_TEST values (?,?,?)";
         final String DELETE_CHILD = "delete from GROUP_CHILD_TEST";
         
         final String SELECT = "select GROUP_BASE_TEST.TEST_BASE_ID," +
                          "GROUP_BASE_TEST.TEST_BASE_VALUE," +
                          "sum(GROUP_CHILD_TEST.TEST_CHILD_VALUE) " +
                          "from GROUP_BASE_TEST, GROUP_CHILD_TEST " +
                          "where GROUP_CHILD_TEST.TEST_BASE_FK_ID=GROUP_BASE_TEST.TEST_BASE_ID " +
                          "and GROUP_CHILD_TEST.TEST_CHILD_VALUE=? " +
                          "group by GROUP_BASE_TEST.TEST_BASE_ID,GROUP_BASE_TEST.TEST_BASE_VALUE";
                                      
         PreparedStatement insertStatement = null;
         PreparedStatement deleteStatement = null;
         PreparedStatement selectStatement = null;
         ResultSet         rsResults = null;
         
         try
         {
            m_transaction.begin();
            try
            {
               insertStatement = m_connection.prepareStatement(INSERT_BASE);
               insertStatement.setInt(1, 1);
               insertStatement.setInt(2, 1);
               insertStatement.execute();
               
               insertStatement = m_connection.prepareStatement(INSERT_CHILD);
               insertStatement.setInt(1, 1);
               insertStatement.setInt(2, 1);
               insertStatement.setInt(3, 1);
               insertStatement.execute();
   
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            finally
            {
               DatabaseUtils.closeStatement(insertStatement);
            }
            
            try
            {
               selectStatement = m_connection.prepareStatement(SELECT);
               selectStatement.setInt(1, 3);
               rsResults = selectStatement.executeQuery();
               
               assertFalse("There should be no rows in result", rsResults.next());
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
            }
            
         }
         finally
         {
            m_transaction.begin();
            try
            {
               deleteStatement = m_connection.prepareStatement(DELETE_CHILD);
               deleteStatement.execute();
               deleteStatement = m_connection.prepareStatement(DELETE_BASE);
               deleteStatement.execute();
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            finally
            {
               DatabaseUtils.closeStatement(deleteStatement);
            }
         }
      }   
   
      /**
       * Test if the union is working
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testUnionQuery(
      ) throws Throwable
      {
         final String INSERT = "insert into QUERY_TEST (VALUE_1, VALUE_2) values (?,?)";
         final String DELETE = "delete from QUERY_TEST";
   
         final String SELECT = "select VALUE_1 from QUERY_TEST where VALUE_2 in " +
            "(select VALUE_2 from QUERY_TEST union  select VALUE_1 from QUERY_TEST)";
                                       
         PreparedStatement insertStatement = null;
         PreparedStatement deleteStatement = null;
         PreparedStatement selectStatement = null;
         ResultSet         rsResults = null;
         
         try
         {
            m_transaction.begin();
            try
            {
               insertStatement = m_connection.prepareStatement(INSERT);
               insertStatement.setInt(1, 1);
               insertStatement.setInt(2, 11);
               insertStatement.execute();
               
               insertStatement = m_connection.prepareStatement(INSERT);
               insertStatement.setInt(1, 2);
               insertStatement.setInt(2, 12);
               insertStatement.execute();
   
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            finally
            {
               DatabaseUtils.closeStatement(insertStatement);
            }
            
            try
            {
               selectStatement = m_connection.prepareStatement(SELECT);
               rsResults = selectStatement.executeQuery();
               
               assertTrue("There should be rows in result", rsResults.next());
               assertTrue("There should be rows in result", rsResults.next());
               assertFalse("There should be no more rows in result", 
                           rsResults.next());
            }
            catch (SQLException sqleExc)
            {
               assertTrue("It seems like the database doesn't support UNION: " 
                          + sqleExc.getMessage(), false);
               
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
            }
         }
         finally
         {
            m_transaction.begin();
            try
            {
               deleteStatement = m_connection.prepareStatement(DELETE);
               deleteStatement.execute();
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            finally
            {
               DatabaseUtils.closeStatement(deleteStatement);
            }
         }
      }
      
      /**
       * Test if the intersect is working
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testIntersectQuery(
      ) throws Throwable
      {
         final String INSERT = "insert into QUERY_TEST (VALUE_1, VALUE_2) values (?,?)";
         final String DELETE = "delete from QUERY_TEST";
   
         final String SELECT = "select VALUE_1 from QUERY_TEST where VALUE_2 in " +
            "(select VALUE_2 from QUERY_TEST intersect select VALUE_1 from QUERY_TEST)";
                                       
         PreparedStatement insertStatement = null;
         PreparedStatement deleteStatement = null;
         PreparedStatement selectStatement = null;
         ResultSet         rsResults = null;
         
         try
         {
            m_transaction.begin();
            try
            {
               insertStatement = m_connection.prepareStatement(INSERT);
               insertStatement.setInt(1, 1);
               insertStatement.setInt(2, 11);
               insertStatement.execute();
               
               insertStatement = m_connection.prepareStatement(INSERT);
               insertStatement.setInt(1, 11);
               insertStatement.setInt(2, 12);
               insertStatement.execute();
   
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            finally
            {
               DatabaseUtils.closeStatement(insertStatement);
            }
            
            try
            {
               selectStatement = m_connection.prepareStatement(SELECT);
               rsResults = selectStatement.executeQuery();
               
               if (DatabaseImpl.getInstance() instanceof MySQLDatabaseImpl)
               {
                  // MySQL as of version 4.1.14 doesn't support queries with 
                  // INTERSECT. So test if this behavior has changed. 
                  fail("MySQL INTERSECT query behavior has changed. Review it.");
               }
               else
               {
                  assertTrue("There should be rows in result", rsResults.next());
                  assertFalse("There should be no more rows in result", 
                              rsResults.next());
               }
            }
            catch (SQLException sqleExc)
            {
               if (DatabaseImpl.getInstance() instanceof MySQLDatabaseImpl)
               {
                  // MySQL as of version 4.1.14 doesn't support queries with 
                  // INTERSECT. Since this is expected behavior ignore this error. 
               }
               else
               {
                  fail("It seems like the database doen't support INTERSECT: " 
                       + sqleExc.getMessage());
               }
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
            }
         }
         finally
         {
            m_transaction.begin();
            try
            {
               deleteStatement = m_connection.prepareStatement(DELETE);
               deleteStatement.execute();
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            finally
            {
               DatabaseUtils.closeStatement(deleteStatement);
            }
         }
      }      
      
      /**
       * Test if the insert-select with parameter is working
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testInsertSelectQuery(
      ) throws Throwable
      {
         final String INSERT = "insert into QUERY_TEST (VALUE_1, VALUE_2) values (?,?)";
         final String DELETE = "delete from QUERY_TEST";
   
         final String INSERT_SELECT = ((DatabaseTestSchema)DatabaseSchemaManager.getInstance(
                              DatabaseTestSchema.class)).getInsertSelectQuery();
   
         final String SELECT = "select VALUE_1, VALUE_2 from QUERY_TEST where VALUE_1=?";
                                       
         PreparedStatement insertStatement = null;
         PreparedStatement deleteStatement = null;
         PreparedStatement insertSelectStatement = null;
         PreparedStatement selectStatement;
         ResultSet         rsResults = null;
         
         try
         {
            m_transaction.begin();
            try
            {
               insertStatement = m_connection.prepareStatement(INSERT);
               insertStatement.setInt(1, 1);
               insertStatement.setInt(2, 1);
               insertStatement.execute();
               
               insertStatement = m_connection.prepareStatement(INSERT);
               insertStatement.setInt(1, 2);
               insertStatement.setInt(2, 2);
               insertStatement.execute();
   
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            finally
            {
               DatabaseUtils.closeStatement(insertStatement);
            }
            
            m_transaction.begin();
            try
            {
               insertSelectStatement = m_connection.prepareStatement(INSERT_SELECT);
               insertSelectStatement.setInt(1, 3);
               insertSelectStatement.execute();
   
               m_transaction.commit();
            }
            catch (SQLException sqleExc)
            {
               m_transaction.rollback();
               throw sqleExc;
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(rsResults, insertSelectStatement);
            }
            
            selectStatement = m_connection.prepareStatement(SELECT);
            selectStatement.setInt(1, 3);
            rsResults = selectStatement.executeQuery();
            
            assertTrue("The inserted data not in DB", rsResults.next());
            assertFalse("More data in DB", rsResults.next());
         }
         finally
         {
            m_transaction.begin();
            try
            {
               deleteStatement = m_connection.prepareStatement(DELETE);
               deleteStatement.execute();
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            finally
            {
               DatabaseUtils.closeStatement(deleteStatement);
            }
         }
      }
      
      /**
       * Test if select except except without parenthesis work
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testExceptExcept(
      ) throws Throwable
      {
         final String INSERT = "insert into QUERY_TEST_EXCEPT (VALUE_1) values (?)";
         
         final String DELETE = "delete from QUERY_TEST_EXCEPT";
         
         final String EXCEPT_EXCEPT_SELECT = ((DatabaseTestSchema)DatabaseSchemaManager.getInstance(
                              DatabaseTestSchema.class)).getSelectExceptQuery();
   
         PreparedStatement insertStatement = null;
         PreparedStatement deleteStatement = null;
         PreparedStatement selectStatement;
         ResultSet         rsResults;
         
         try
         {
            m_transaction.begin();
            try
            {
               insertStatement = m_connection.prepareStatement(INSERT);
               insertStatement.setInt(1, 1);
               insertStatement.execute();
   
               insertStatement.setInt(1, 2);
               insertStatement.execute();
   
               insertStatement.setInt(1, 3);
               insertStatement.execute();
   
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            finally
            {
               DatabaseUtils.closeStatement(insertStatement);
            }
            
            // Here we are doing 1,2,3 except 1,2 except 2,3
            // Based on the parenthesis this can have two different interpretations
            // ((1,2,3 except 1,2) except 2,3) is nothing what we expect
            // 1,2,3 except (1,2 except 2,3) is 2,3 what we don't want         
            selectStatement = m_connection.prepareStatement(EXCEPT_EXCEPT_SELECT);
            selectStatement.setInt(1, 1);
            selectStatement.setInt(2, 2);
            selectStatement.setInt(3, 3);
            selectStatement.setInt(4, 1);
            selectStatement.setInt(5, 2);
            selectStatement.setInt(6, 2);
            selectStatement.setInt(7, 3);
            
            try
            {
               rsResults = selectStatement.executeQuery();
               if (DatabaseImpl.getInstance() instanceof MySQLDatabaseImpl)
               {
                  // MySQL as of version 4.1.14 doesn't support queries with 
                  // EXCEPT. So test if this behavior has changed. 
                  fail("MySQL EXCEPT query behavior has changed. Review it.");
               }
               else
               {
                  assertFalse("Select should not return any data", 
                              rsResults.next());
               }
            }
            catch (SQLException sqleExc)
            {
               if (DatabaseImpl.getInstance() instanceof MySQLDatabaseImpl)
               {
                  // MySQL as of version 4.1.14 doesn't support queries with 
                  // EXCEPT. Since this is expected behavior ignore this error. 
               }
               else
               {
                  fail("It seems like database doesn't support queries with"
                       + " EXCEPT: " + sqleExc.getMessage());
                  throw sqleExc;
               }
            }
         }
         finally
         {
            m_transaction.begin();
            try
            {
               deleteStatement = m_connection.prepareStatement(DELETE);
               
               deleteStatement.execute();
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            finally
            {
               DatabaseUtils.closeStatement(deleteStatement);
            }
         }
      }
   
      /**
       * Test if select except union without parenthesis work
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testExceptUnion(
      ) throws Throwable
      {
         final String INSERT = "insert into QUERY_TEST_EXCEPT (VALUE_1) values (?)";
         
         final String DELETE = "delete from QUERY_TEST_EXCEPT";
         
         final String EXCEPT_UNION_SELECT  = ((DatabaseTestSchema)DatabaseSchemaManager.getInstance(
                              DatabaseTestSchema.class)).getSelectExceptUnionQuery();
   
         PreparedStatement insertStatement = null;
         PreparedStatement deleteStatement = null;
         PreparedStatement selectStatement;
         ResultSet         rsResults;
         
         try
         {
            m_transaction.begin();
            try
            {
               insertStatement = m_connection.prepareStatement(INSERT);
               insertStatement.setInt(1, 1);
               insertStatement.execute();
               
               insertStatement.setInt(1, 2);
               insertStatement.execute();
   
               insertStatement.setInt(1, 3);
               insertStatement.execute();
   
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            finally
            {
               DatabaseUtils.closeStatement(insertStatement);
            }
            
            // Here we are doing 1,2,3 except 1,2 union 2,3
            // Based on the parenthesis this can have two different interpretations
            // ((1,2,3 except 1,2) union 2,3) is 2,3 
            // 1,2,3 except (1,2 union 2,3) is nothing           
            selectStatement = m_connection.prepareStatement(EXCEPT_UNION_SELECT);
            selectStatement.setInt(1, 1);
            selectStatement.setInt(2, 2);
            selectStatement.setInt(3, 3);
            selectStatement.setInt(4, 1);
            selectStatement.setInt(5, 2);
            selectStatement.setInt(6, 2);
            selectStatement.setInt(7, 3);
            
            try
            {
               rsResults = selectStatement.executeQuery();
               if (DatabaseImpl.getInstance() instanceof MySQLDatabaseImpl)
               {
                  // MySQL as of version 4.1.14 doesn't support queries with 
                  // EXCEPT. So test if this behavior has changed. 
                  fail("MySQL EXCEPT query behavior has changed. Review it.");
               }
               else
               {
                  assertTrue("Select should return first value", rsResults.next());
                  assertEquals("First value should be 2", 2, rsResults.getInt(1));
                  assertTrue("Select should return second value", rsResults.next());
                  assertEquals("First value should be 3", 3, rsResults.getInt(1));
               }
            }
            catch (SQLException sqleExc)
            {
               if (DatabaseImpl.getInstance() instanceof MySQLDatabaseImpl)
               {
                  // MySQL as of version 4.1.14 doesn't support queries with 
                  // EXCEPT. Since this is expected behavior ignore this error. 
               }
               else
               {
                  fail("It seems like database doesn't support queries with" 
                       + "  EXCEPT or with UNION: " + sqleExc.getMessage());
                       throw sqleExc;
               }
            }            
         }
         finally
         {
            m_transaction.begin();
            try
            {
               deleteStatement = m_connection.prepareStatement(DELETE);
               
               deleteStatement.execute();
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            finally
            {
               DatabaseUtils.closeStatement(deleteStatement);
            }
         }
      }
   
      /**
       * Test if the exists is working
       * 
       * @throws Throwable - an error has occurred during test
       */
      public void testExistsQuery(
      ) throws Throwable
      {
         final String INSERT = "insert into QUERY_TEST (VALUE_1, VALUE_2) values (?,?)";
         final String DELETE = "delete from QUERY_TEST";
   
         final String SELECT = ((DatabaseTestSchema)DatabaseSchemaManager.getInstance(
                              DatabaseTestSchema.class)).getSelectExistQuery();
                                       
         PreparedStatement insertStatement = null;
         PreparedStatement deleteStatement = null;
         PreparedStatement selectStatement = null;
         ResultSet         rsResults = null;
         
         try
         {
            m_transaction.begin();
            try
            {
               insertStatement = m_connection.prepareStatement(INSERT);
               insertStatement.setInt(1, 1);
               insertStatement.setInt(2, 11);
               insertStatement.execute();
               
               insertStatement = m_connection.prepareStatement(INSERT);
               insertStatement.setInt(1, 11);
               insertStatement.setInt(2, 12);
               insertStatement.execute();
   
               m_transaction.commit();
            }
            catch (Throwable throwable)
            {
               m_transaction.rollback();
               throw throwable;
            }
            finally
            {
               DatabaseUtils.closeStatement(insertStatement);
            }
            
            try
            {
               selectStatement = m_connection.prepareStatement(SELECT);
               rsResults = selectStatement.executeQuery();
               
               assertTrue("There should be rows in result", rsResults.next());
               assertFalse("There should be no more rows in result", rsResults.next());
            }
            catch (SQLException sqleExc)
            {
               assertTrue(sqleExc.getMessage(), false);
            }
            finally
            {
               DatabaseUtils.closeResultSetAndStatement(rsResults, selectStatement);
            }
         }
         finally
         {
            m_transaction.begin();
            try
            {
               deleteStatement = m_connection.prepareStatement(DELETE);
               deleteStatement.execute();
               m_transaction.commit();
            }
            catch (Throwable thr)
            {
               m_transaction.rollback();
               throw new Exception(thr);
            }
            finally
            {
               DatabaseUtils.closeStatement(deleteStatement);
            }
         }
      }         
   }
}

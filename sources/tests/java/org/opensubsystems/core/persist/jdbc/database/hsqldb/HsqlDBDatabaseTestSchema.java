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

package org.opensubsystems.core.persist.jdbc.database.hsqldb;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.jdbc.DatabaseConnectionFactory;
import org.opensubsystems.core.persist.jdbc.database.DatabaseTestSchema;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseImpl;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * This class encapsulates details about creation and upgrade
 * of database schema required to test database driver functionality
 * for tables which are HSQLDB database specific 
 *
 * @author bastafidli
 */
public class HsqlDBDatabaseTestSchema extends DatabaseTestSchema
{   
   /*
      These tables are database specific 
      
      CREATE TABLE GENERATEDKEY_TEST 
      (
         TEST_KEY   INTEGER IDENTITY,
         TEST_VALUE VARCHAR(50) NOT NULL
      )
   */
   
   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(HsqlDBDatabaseTestSchema.class);

   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Default constructor.
    * 
    * @throws OSSException - error occurred.
    */
   public HsqlDBDatabaseTestSchema(
   ) throws OSSException
   {
      super();
   }   
  

   // Lifecycle events /////////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   @Override
   public void create(
      Connection cntDBConnection,
      String     strUserName
   ) throws SQLException
   {
      // First create any generic tables
      super.create(cntDBConnection, strUserName);

      // Now try to create any database specific tables      
      Statement stmQuery = null;
      try
      {        
         stmQuery = cntDBConnection.createStatement();

         if (stmQuery.execute("CREATE TABLE GENERATEDKEY_TEST" + NL +
                              "(" + NL +
                              "   TEST_KEY   INTEGER IDENTITY," + NL + 
                              "   TEST_VALUE VARCHAR(50) NOT NULL" + NL +
                              ")"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table GENERATEDKEY_TEST created.");
         /*
         if (stmQuery.execute("grant all on GENERATEDKEY_TEST to " + strUserName))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         Log.getLogger().log(Level.FINEST, 
                             "Access for table GENERATEDKEY_TEST set for user " 
                             + strUserName);
         */                             
      }
      catch (SQLException sqleExc)
      {
         s_logger.log(Level.WARNING, "Failed to create database test schema.", 
                             sqleExc);
         throw sqleExc;
      }
      finally
      {
         DatabaseUtils.closeStatement(stmQuery);
      }
   }

   /**
    * {@inheritDoc} 
    */
   @Override
   public void createDataSource(
     DatabaseConnectionFactory dbConnectionFactory,
     String                    strDataSourceName,
     String                    strDatabaseDriver,
     String                    strDatabaseURL,
     String                    strUserName,
     String                    strUserPassword,
     int                       iTransactionIsolation
   ) throws OSSException
   {
      // TODO: Improve: Explain why are we doing this?
      // For HsqlDB has to be created data source with another URL 
      // (another database name)
      dbConnectionFactory.addDataSource(strDataSourceName,
                                        DatabaseImpl.getInstance(),
                                        strDatabaseDriver,
                                        strDatabaseURL + "2", 
                                        strUserName,
                                        strUserPassword,
                                        iTransactionIsolation);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getInsertGeneratedKey(
   )
   {
      return "insert into generatedkey_test(test_key, test_value) values (null, ?)";
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int[] executeInsertGeneratedKey2(
      Connection dbConnection,
      String     strValue
   ) throws SQLException
   {
      PreparedStatement insertStatement = null;
      CallableStatement callStatement = null;
      ResultSet         rsResults = null;
      int               iInsertCount;
      int               iGeneratedKey;
      int[]             returnValues = null;
      
      try
      {
         insertStatement = dbConnection.prepareStatement(getInsertGeneratedKey());
         insertStatement.setString(1, strValue);
         iInsertCount = insertStatement.executeUpdate();

         callStatement = dbConnection.prepareCall("call identity()");

         rsResults = callStatement.executeQuery();
         if (rsResults.next())
         {
            iGeneratedKey = rsResults.getInt(1);

            returnValues = new int[2];
            returnValues[0] = iInsertCount;
            returnValues[1] = iGeneratedKey;
         }
      }
      finally
      {
         DatabaseUtils.closeStatement(insertStatement);
         DatabaseUtils.closeResultSetAndStatement(rsResults, callStatement);
      }
      
      return returnValues;
   }

   /**
    * {@inheritDoc} 
    */
   @Override
   public int executeUpdateTestValue(
      Connection dbConnection,
      String     strOldValue,
      String     strNewValue
   ) throws SQLException
   {
      PreparedStatement updateStatement = null;
      int               iUpdateCount   = 0;

      try
      {
         updateStatement = dbConnection.prepareStatement(
              "update TRANSACTION_TEST set TEST_VALUE = ? where TEST_VALUE = ?");
         updateStatement.setString(1, strNewValue);
         updateStatement.setString(2, strOldValue);
            
         // here is the bug in SAP DB which is not seen in HSQLDB, if there is 
         // called stored procedure without output parameters, there is not 
         // returned number of updated records   
         iUpdateCount = updateStatement.executeUpdate();
      }
      finally
      {
         DatabaseUtils.closeStatement(updateStatement);
      }
      
      return iUpdateCount;
   }


   /**
    * {@inheritDoc}
    */
   @Override
   public int[] executeInsertRow(
      Connection dbConnection, 
      String strValue
   ) throws SQLException
   {
      PreparedStatement insertStatement = null;
      int               iInsertCount;
      int               iInsertCountReturnedFromSP = -1;
      int[]             returnValues = null;
      
      try
      {
         insertStatement = dbConnection.prepareStatement(getInsertGeneratedKey());
         insertStatement.setString(1, strValue);
         iInsertCount = insertStatement.executeUpdate();

         returnValues = new int[2];

         // Value (number of affected rows) returned from 
         // insertStatement.executeUpdate();
         returnValues[0] = iInsertCount;
         
         // Value (number of inserted rows) returned from stored procedure. 
         // It will be always -1 here, because HsqlDB doesn't support stored 
         // procedures.
         returnValues[1] = iInsertCountReturnedFromSP;
      }
      finally
      {
         DatabaseUtils.closeStatement(insertStatement);
      }
      // HsqlDB doesn't support stored procedures 
      return returnValues;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void createTestUser(
      Connection cntAdminDBConnection,
      String strDatabaseURL,
      String strUserName,
      String strUserPassword
   ) throws SQLException
   {
      Statement stmQuery = null;
      try
      {
         String strCreateUserQuery = "CREATE USER " + strUserName + " PASSWORD " 
                                     + strUserPassword + " ADMIN";

         stmQuery = cntAdminDBConnection.createStatement();

         if (stmQuery.execute(strCreateUserQuery))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
      }
      finally
      {
         DatabaseUtils.closeStatement(stmQuery);
      }                        
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void dropTestUser(
      Connection cntAdminDBConnection, 
      String strDatabaseURL, 
      String strUserName
   ) throws SQLException
   {
      Statement stmQuery = null;
      try
      {
         String strDropUserQuery = "DROP USER " + strUserName;

         stmQuery = cntAdminDBConnection.createStatement();

         if (stmQuery.execute(strDropUserQuery))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
      }
      finally
      {
         DatabaseUtils.closeStatement(stmQuery);
      }                        
   }
}

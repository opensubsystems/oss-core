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

package org.opensubsystems.core.persist.jdbc.database.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.opensubsystems.core.data.BasicDataObject;
import org.opensubsystems.core.data.ModifiableDataObject;
import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.error.OSSInconsistentDataException;
import org.opensubsystems.core.persist.jdbc.DatabaseConnectionFactory;
import org.opensubsystems.core.persist.jdbc.DatabaseSourceDefinition;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseImpl;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseTransactionFactoryImpl;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * Management layer for PostgreSQL database (www.postgresql.org)
 *
 * @author OpenSubsystems
 */
public class PostgreSQLDatabaseImpl extends DatabaseImpl
{
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * The textual identifier representing PostgreSQL.
    */
   public static final String POSTGRESQL_DATABASE_TYPE_IDENTIFIER = "PostgreSQL";

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(PostgreSQLDatabaseImpl.class);

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Default constructor for empty database. 
    * 
    * @throws OSSException - problem connecting to database
    */
   public PostgreSQLDatabaseImpl(
   ) throws OSSException
   {
      super(POSTGRESQL_DATABASE_TYPE_IDENTIFIER,
            "now()",
            "count(*)",
            // For PostgreSQL it is generally faster to execute count(*) than to 
            // do last() since it seems that it creates copy of the result set, 
            // which takes some time
            true, // prefer count to last,
            // PostgreSQL supports rows limitation by using clause 
            // LIMIT X OFFSET Y
            true); // has select list range support
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void stop(
   ) throws OSSException
   {
      // Starting a system service is by its nature going to be 
      // operating-system specific, so I think it best bet would be to call 
      // some external scripts to do this.

      s_logger.entering(this.getClass().getName(), "stop");

      // TODO: PostgreSQL: Implement this so we can safely stop the database when
      // the application is finished.
      super.stop();

      s_logger.entering(this.getClass().getName(), "stop");
   }
   
   
   /**
    * {@inheritDoc}
    */
   @Override
   public Object[] getSQLAnalyzeFunctionCall(
      Map<Integer, String> mpTableNames
   )
   {
      // PostgreSQL uses VACUUM ANALYZE [<table_name>] to update indexes and 
      // increase performance  
      // VACUUM ANALYZE command must be processed outside of the transaction, 
      // so there has to be set up autoCommit = true

      String[]         arrReturn = new String[mpTableNames.size()];
      StringBuilder    buffer = new StringBuilder();
      Iterator<String> itItem;
      int              iIndex = 0;

      itItem = mpTableNames.values().iterator();
      while (itItem.hasNext()) 
      {
         // construct analyze query for each table from the array
         buffer.append("vacuum analyze ");
         buffer.append(itItem.next());
         // add constructed query to the output array
         arrReturn[iIndex++] = buffer.toString();
         // delete buffer for next usage
         buffer.delete(0, buffer.length());
      }

      return new Object[] {arrReturn, Boolean.TRUE};
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isCallableStatement(
      String strQuery
   )
   {
      // TODO: PostgreSQL: is just doing normal select so we should
      // probably check if it matches pattern select .... from xyz (...)
      return strQuery.indexOf("select ") != -1; 
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void insertAndFetchGeneratedValues(
      Connection        dbConnection,
      PreparedStatement insertStatement,
      boolean           bIsInDomain,
      String            strTableName,
      int               iIndex,
      BasicDataObject   data
   ) throws SQLException,
            OSSException
   {
      ResultSet rsResults = null;
      int       iGeneratedKey = -1;
      Timestamp tmTimestamp = null;

      try
      {
         rsResults = insertStatement.executeQuery();         
            
         if (rsResults.next())
         {
            iGeneratedKey = rsResults.getInt(1);
            tmTimestamp = rsResults.getTimestamp(2);
         }
      }
      finally
      {
         DatabaseUtils.close(rsResults);
      }

      data.setId(iGeneratedKey);
      data.setCreationTimestamp(tmTimestamp);
      if (data instanceof ModifiableDataObject)
      {
         ((ModifiableDataObject)data).setModificationTimestamp(tmTimestamp);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void updatedAndFetchGeneratedValues(
      String               strDataName,
      Connection           dbConnection,
      PreparedStatement    updateStatement,
      boolean              bIsInDomain,
      String               strTableName,
      int                  iIndex,
      ModifiableDataObject data
   ) throws SQLException,
            OSSException
   {
      ResultSet rsResults = null;
      int       iUpdateCount = 0;
      Timestamp tmTimestamp = null;
      
      try
      {
         rsResults = updateStatement.executeQuery();         
         if (rsResults.next())
         {
            iUpdateCount = rsResults.getInt(1);
            tmTimestamp = rsResults.getTimestamp(2);
         }
      }
      finally
      {
         DatabaseUtils.close(rsResults);
      }

      if (iUpdateCount > 1)
      {
         throw new OSSInconsistentDataException(
            "Inconsistent database contains multiple (" + iUpdateCount + 
            ") data with the same ID modified at the same time");
      }
      if (iUpdateCount == 1)
      {
        data.setModificationTimestamp(tmTimestamp);
      }
   }
   
   // Helper methods ///////////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   @Override
   protected void createUser(
      DatabaseConnectionFactory connectionFactory, 
      Connection                cntAdminDBConnection
   ) throws OSSException
   {
      // Now create the user
      PreparedStatement pstmQuery = null;
      try
      {
         StringBuilder            buffer = new StringBuilder();
         DatabaseSourceDefinition defaultSource;
         
         defaultSource = connectionFactory.getDefaultDataSource();
         
         // PostgreSQL must have password typed within the ''
         /*
            pstmQuery = cntAdminDBConnection.prepareStatement(
                           "CREATE USER ? WITH password ?");
            pstmQuery.setString(1, defaultSource.getUser());
            pstmQuery.setString(2, defaultSource.getPassword());
         */

         buffer.append("CREATE USER ");
         buffer.append(defaultSource.getUser());
         buffer.append(" WITH PASSWORD '");
         buffer.append(defaultSource.getPassword());
         buffer.append("'");

         try
         {
            pstmQuery = cntAdminDBConnection.prepareStatement(buffer.toString());
   
            if (pstmQuery.execute())
            {
               // Close any results
               pstmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
            }
         }
         finally
         {
            DatabaseUtils.close(pstmQuery);
            pstmQuery = null;
         }
         
         // we have to explicitly create database schema with user name
         buffer.delete(0, buffer.length());
         buffer.append("CREATE SCHEMA ");
         buffer.append(defaultSource.getUser());
         buffer.append(" AUTHORIZATION ");
         buffer.append(defaultSource.getUser());
         
         try
         {
            pstmQuery = cntAdminDBConnection.prepareStatement(buffer.toString());
   
            if (pstmQuery.execute())
            {
               // Close any results
               pstmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
            }
         }
         finally
         {
            DatabaseUtils.close(pstmQuery);
            pstmQuery = null;
         }

         // At this point we don't know if this is just a single operation
         // and we need to commit or if it is a part of bigger transaction
         // and the commit is not desired until all operations proceed. 
         // Therefore let the DatabaseTransactionFactory resolve it 
         DatabaseTransactionFactoryImpl.getInstance().commitTransaction(
                                                 cntAdminDBConnection);
         
         s_logger.log(Level.FINER, "Database user {0} with password {1} and"
                      + " database schema {2} created.", 
                      new Object[]{defaultSource.getUser(), 
                                   defaultSource.getPassword(), 
                                   defaultSource.getUser()});
      }
      catch (SQLException sqleExc)
      {
         try
         {
            // At this point we don't know if this is just a single operation
            // and we need to commit or if it is a part of bigger transaction
            // and the commit is not desired until all operations proceed. 
            // Therefore let the DatabaseTransactionFactory resolve it 
            DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(
               cntAdminDBConnection);
         }
         catch (SQLException sqleExc2)
         {
            // Ignore this
            s_logger.log(Level.WARNING, 
                                "Failed to rollback changes for creation of user.", 
                                sqleExc2);
         }
         s_logger.log(Level.SEVERE, 
                             "Unable to create default database user.",
                             sqleExc);
         throw new OSSDatabaseAccessException("Unable to create default database user.",
                                              sqleExc);
      }
      finally
      {
         DatabaseUtils.close(pstmQuery);
      }                        
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void startDatabaseServer() throws OSSException
   {
      // TODO: PostgreSQL: Implement starting database server
      // Starting a system service is by its nature going to be 
      // operating-system specific, so I think it best bet would be to call 
      // some external scripts to do this.
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void createDatabaseInstance() throws OSSException
   {
      // TODO: PostgreSQL: Implement creating database instance
      // Creating a new database can be done by connecting to database template1 
      // and issuing "CREATE DATABASE ...", you can do similarly with users. See 
      // the "SQL Commands" section of the manuals for details.
   }
}

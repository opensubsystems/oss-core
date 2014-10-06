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
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.opensubsystems.core.data.BasicDataObject;
import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.data.ModifiableDataObject;
import org.opensubsystems.core.error.OSSDataCreateException;
import org.opensubsystems.core.error.OSSDataSaveException;
import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.error.OSSInconsistentDataException;
import org.opensubsystems.core.persist.jdbc.DatabaseConnectionFactory;
import org.opensubsystems.core.persist.jdbc.DatabaseSourceDefinition;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseConnectionFactoryImpl;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseImpl;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseTransactionFactoryImpl;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * Management layer for HSQLDB database (hsqldb.sourceforge.net)
 *
 * @author bastafidli
 */
public class HsqlDBDatabaseImpl extends DatabaseImpl
{
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * The textual identifier representing HSQLDB.
    */
   public static final String HSQLDB_DATABASE_TYPE_IDENTIFIER = "HsqlDB";

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(HsqlDBDatabaseImpl.class);

   // Inner classes ////////////////////////////////////////////////////////////
   
   /**
    * Class used to encapsulate and hold cached statements between method calls.
    */
   public class CachedInsertStatements
   {
      /**
       * Are the statements generated for a domain.
       */
      private boolean m_bIsInDomain;
      
      /**
       * Cached call statement.
       */
      private CallableStatement m_call; 

      /**
       * Cached select statement.
       */
      private PreparedStatement m_select; 

      /**
       * @param bIsInDomain - are the statements generated for a domain
       * @param call - new call statement to cache
       * @param select - new select statement to cache
       */
      public CachedInsertStatements(
         boolean           bIsInDomain,
         CallableStatement call, 
         PreparedStatement select
      )
      {
         super();
         
         m_bIsInDomain = bIsInDomain;
         m_call = call;
         m_select = select;
      }

      /**
       * @return CallableStatement
       */
      private CallableStatement getCall()
      {
         return m_call;
      }

      /**
       * @return PreparedStatement
       */
      private PreparedStatement getSelect()
      {
         return m_select;
      }
      
      /**
       * @return boolean
       */
      public boolean isInDomain()
      {
         return m_bIsInDomain;
      }
   }
   
   /**
    * Class used to encapsulate and hold cached statements between method calls.
    */
   public class CachedUpdateStatements
   {
      /**
       * Are the statements generated for a domain.
       */
      private boolean m_bIsInDomain;
      
      /**
       * Cached select statement.
       */
      private PreparedStatement m_select; 
   
      /**
       * @param bIsInDomain - are the statements generated for a domain
       * @param select - new select statement to cache
       */
      public CachedUpdateStatements(
         boolean           bIsInDomain,
         PreparedStatement select
      )
      {
         super();
         
         m_bIsInDomain = bIsInDomain;
         m_select = select;
      }
   
      /**
       * @return PreparedStatement
       */
      public PreparedStatement getSelect()
      {
         return m_select;
      }
      
      /**
       * @return boolean
       */
      public boolean isInDomain()
      {
         return m_bIsInDomain;
      }
   }

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Default constructor.
    * 
    * @throws OSSException - an error has occurred. 
    */
   public HsqlDBDatabaseImpl(
   ) throws OSSException
   {
      super(HSQLDB_DATABASE_TYPE_IDENTIFIER,
            "now", // current timestamp
            "count(*)",
            false, // prefer count to last,
            false);  // has select list range support
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void stop(
   ) throws OSSException
   {
      // HSQLDB 1.7 had bug which prevented to restart the database if it was
      // stopped. This should be fixed and now version 1.8 requires explicit
      // shutdown rather than closing the database when the last connection
      // is closed so lets issue explicit shutdown
      if (m_bDatabaseStarted)
      {
         Connection cntAdminDBConnection = null;
         boolean    bStopped = false;
   
         try
         {
            // Try to shutdown the database         
            // HSQLDB requires us to connect as administrator
            cntAdminDBConnection = DatabaseConnectionFactoryImpl.getInstance()
                                      .requestAdminConnection(true);
   
            if (cntAdminDBConnection == null)
            {
               s_logger.log(Level.WARNING, 
                            "Unable to connect to database as admin to stop it.");
            }
            else
            {
               Statement stmQuery = null;
               try
               {
                  stmQuery = cntAdminDBConnection.createStatement();
                  if (stmQuery.execute("shutdown"))
                  {
                     // We should close all statements, but the database was 
                     // already shutdown
                     // stQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
                  }
                  bStopped = true;
                  s_logger.log(Level.FINER, "Database is stopped.");
               }
               catch (SQLException sqleExc)
               {
                  s_logger.log(Level.WARNING, "Unable to stop the database.", 
                               sqleExc);
               }
               finally
               {
                  if ((!bStopped) && (stmQuery != null))
                  {
                     // If the database is stopped, no need to close the statement
                     try
                     {
                        stmQuery.close();
                     }
                     catch (SQLException sqleExc)
                     {
                        s_logger.log(Level.WARNING, 
                                     "Closing of statement has failed.", 
                                     sqleExc);
                     }
                  }
               }                        
            }
         }
         finally
         {
            try
            {
               if (cntAdminDBConnection != null)
               {
                  // We still need to return the connection since the factory
                  // keeps track of them 
                  DatabaseConnectionFactoryImpl.getInstance().returnConnection(
                           cntAdminDBConnection);                     
               }
            }
            finally
            {
               super.stop();
            }
         }
      }      
   }  
    
   /**
    * {@inheritDoc}
    */
   @Override
   public Object[] getSQLAnalyzeFunctionCall(
      Map<Integer, String> mpTableNames
   )
   {
      // For HsqlDB is not necessary to update statistics because this DB is not 
      // appropriate for processing huge amount of records
      return null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getTransactionIsolation(
      int iTransactionIsolation
   )
   {
      // TODO: Bug: HSQLDB 1.7.1: Doesn't support setTransactionIsolation call
      // so change this to -1 to do not set any (XAPool requires it)
      return -1;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isCallableStatement(
      String strQuery
   )
   {
      // HSQLDB doesn't support stored procedures
      return false; 
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
      CachedInsertStatements cache = null;
      
      try
      {
         cache = cacheStatementsForInsert(
                    dbConnection, bIsInDomain, strTableName,
                    data instanceof ModifiableDataObject);
         insertAndFetchGeneratedValues(insertStatement, cache, data);
      }
      finally
      {
         closeStatements(cache);
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
      CachedUpdateStatements cache = null;
      
      try
      {
         int iUpdateCount;
         
         iUpdateCount = updateStatement.executeUpdate();
         if (iUpdateCount == 0)
         {
            m_vdsSchema.checkUpdateError(dbConnection, strDataName, 
                                         strTableName, data.getId(), 
                                         data.getModificationTimestamp());
         }
         else if (iUpdateCount > 1)
         {
            throw new OSSInconsistentDataException(
                         "Inconsistent database contains multiple ("
                         + iUpdateCount + ") records with the same ID"
                         + " and modified at the same time");
         }
   
         cache = cacheStatementsForUpdate(dbConnection, bIsInDomain, 
                                          strTableName);
         fetchModifiedTimestamps(cache, data);
      }
      finally
      {
         closeStatements(cache);
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
         
         // HSQLDB doesn't support creation of users using ?, we must 
         // immediately specify name and password
         // HSQLDB requires user to have administrator privileges to create 
         // tables
         /*
         pstmQuery = cntAdminDBConnection.prepareStatement(
                        "create user ? password ? admin");
         pstmQuery.setString(1, defaultSource.getUser());
         pstmQuery.setString(2, defaultSource.getPassword());
         */

         buffer.append("CREATE user ");
         buffer.append(defaultSource.getUser());
         buffer.append(" PASSWORD ");
         buffer.append(defaultSource.getPassword());
         // We have to make the user ADMIN otherwise it is not possible
         // to create tables, since following exception will be thrown
         // Caused by: java.sql.SQLException: Access is denied in statement [create table]
         // and hsqldb doesn't provide grant to create tables
         buffer.append(" ADMIN ");
         pstmQuery = cntAdminDBConnection.prepareStatement(buffer.toString());

         if (pstmQuery.execute())
         {
            // Close any results
            pstmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         
         // At this point we don't know if this is just a single operation
         // and we need to commit or if it is a part of bigger transaction
         // and the commit is not desired until all operations proceed. 
         // Therefore let the DatabaseTransactionFactory resolve it 
         DatabaseTransactionFactoryImpl.getInstance().commitTransaction(
                                                 cntAdminDBConnection);
         
         s_logger.log(Level.FINER, "Database user {0} with password {1} created.", 
                      new Object[]{defaultSource.getUser(), defaultSource.getPassword()});
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
         DatabaseUtils.closeStatement(pstmQuery);
      }                        
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void startDatabaseServer(
   ) throws OSSException
   {
      // Implementation of this method is not necessary because HSQLDB database
      // starts (or schema is created) when the administrator connects to it.
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void createDatabaseInstance(
   ) throws OSSException 
   {
      // Implementation of this method is not necessary because HSQLDB database
      // is created when the administrator connects to it.
   }

   /**
    * Cache the statements required by subsequent calls to this class.
    * You must call closeGeneratedValuesStatements in finally
    * to properly free resources.
    * 
    * @param dbConnection - connection to use to access the datavase
    * @param bIsInDomain - are the data objects maintained in domains
    * @param strTableName - name of the table
    * @param bModifiable - is the data object modifiable
    * @return CachedInsertStatements - cached jdbc statements
    * @throws OSSException - an error accessing the database
    */
   protected CachedInsertStatements cacheStatementsForInsert(
      Connection dbConnection,
      boolean    bIsInDomain,
      String     strTableName,
      boolean    bModifiable
   ) throws OSSException
   {
      StringBuilder          sbQuery = new StringBuilder();
      CachedInsertStatements cache;
      
      sbQuery.append("select CREATION_DATE");
      if (bModifiable)
      {   
         sbQuery.append(", MODIFICATION_DATE");
      }
      sbQuery.append(" from ");
      sbQuery.append(strTableName);
      sbQuery.append(" where ID = ?"); 
      if (bIsInDomain)
      {   
         sbQuery.append(" and DOMAIN_ID = ?");
      }

      try
      {
         cache =  new CachedInsertStatements(
                         bIsInDomain,
                         dbConnection.prepareCall("call identity()"),
                         dbConnection.prepareStatement(sbQuery.toString()));
      }
      catch (SQLException eExc)
      {
         throw new OSSDatabaseAccessException(
               "Cannot create jdbc statements to access the database.", 
               eExc);
      }
         
      return cache;
   }

   /**
    * Release the statements cached by cacheStatementsForXXX.
    * 
    * @param cache - cache to release
    */
   protected void closeStatements(
      CachedInsertStatements cache
   )
   {
      if (cache != null)
      {   
         DatabaseUtils.closeStatement(cache.getCall());
         DatabaseUtils.closeStatement(cache.getSelect());
      }
   }
   
   /**
    * Insert the data, fetch from the database id and generated creation and 
    * modification timestamps for the newly created data object.
    * 
    * Note: Since the caller created the prepared statement, the caller is
    * responsible for its closing.
    *  
    * @param insertStatement - statement used to insert the data
    * @param cache - cached jdbc statements
    * @param data - data object to update
    * @throws OSSException - an error accessing database
    * @throws SQLException - an error while inserting data
    */
   protected void insertAndFetchGeneratedValues(
      PreparedStatement      insertStatement,
      CachedInsertStatements cache,
      BasicDataObject        data
   ) throws OSSException, 
            SQLException
   {
      ResultSet rsResults = null;
      long      lGeneratedKey = DataObject.NEW_ID;

      insertStatement.executeUpdate();
      
      try
      {
         // I prefer to do it step by step so that we don't have to keep 2 statements
         // and result sets opened
         try
         {
            rsResults = cache.getCall().executeQuery();
            if (rsResults.next())
            {
               lGeneratedKey = rsResults.getLong(1);
            }
            else
            {
               throw new OSSDataCreateException(
                            "Cannot read the generated ID from the database.");
            }
         }
         finally
         {      
            DatabaseUtils.closeResultSet(rsResults);
         }   
         
         if (lGeneratedKey != DataObject.NEW_ID)
         {   
            PreparedStatement selectStatement;
   
            try
            {
               
               selectStatement = cache.getSelect();
               selectStatement.clearParameters();
               selectStatement.setLong(1, lGeneratedKey);
               if (cache.isInDomain())
               {   
                  selectStatement.setLong(2, data.getDomainId());
               }
               rsResults = selectStatement.executeQuery();
               if (rsResults.next())
               { 
                  data.setId(lGeneratedKey);
                  data.setCreationTimestamp(rsResults.getTimestamp(1));
                  if (data instanceof ModifiableDataObject)
                  {   
                     ((ModifiableDataObject)data).setModificationTimestamp(
                                                     rsResults.getTimestamp(2));
                  }
               }
               else
               {
                  throw new OSSDataCreateException(
                               "Cannot read the generated creation and modification " +
                               "time from the database.");
               }
            }
            finally
            {      
               DatabaseUtils.closeResultSet(rsResults);
            }
         }
      }
      catch (SQLException eExc)
      {
         throw new OSSDataCreateException(
                     "Cannot read the generated creation and modification time" +
                     " from the database.", eExc);
      }
   }

   /**
    * Check errors and fetch from the database generated modification timestamps 
    * for the updated data object.
    * 
    * @param cache - cached jdbc statements
    * @param data - data object to update
    * @throws OSSException - an error has occurred
    */
   protected void fetchModifiedTimestamps(
      CachedUpdateStatements cache,
      ModifiableDataObject  data
   ) throws OSSException
   {
      ResultSet         rsResults = null;
      PreparedStatement selectStatement;
   
      try
      {
         selectStatement = cache.getSelect();
         selectStatement.setLong(1, data.getId());
         if (cache.isInDomain())
         {   
            selectStatement.setLong(2, data.getDomainId());
         }
         rsResults = selectStatement.executeQuery();
         if (rsResults.next())
         { 
            data.setModificationTimestamp(rsResults.getTimestamp(1));
         }
         else
         {
            throw new OSSDataSaveException("Cannot read the generated modification " +
                                          "time from the database.");
   
         }
      }
      catch (SQLException eExc)
      {
         throw new OSSDataSaveException(
               "Cannot read the generated modification time" +
               " from the database.", 
               eExc);
      }
      finally
      {      
         DatabaseUtils.closeResultSet(rsResults);
      }
   }

   /**
    * Cache the statements required by subsequent calls to this class.
    * You must call closeGeneratedValuesStatements in finally
    * to properly free resources.
    * 
    * @param dbConnection - connection to the database to use
    * @param bIsInDomain - are the data objects maintained in domains
    * @param strTableName - name of the table
    * @return CachedInsertStatements
    * @throws OSSException - an error accessing the database
    */
   protected CachedUpdateStatements cacheStatementsForUpdate(
      Connection dbConnection,
      boolean    bIsInDomain,
      String     strTableName
   ) throws OSSException
   {
      StringBuilder          sbQuery = new StringBuilder();
      CachedUpdateStatements cache;
      
      sbQuery.append("select MODIFICATION_DATE from ");
      sbQuery.append(strTableName);
      sbQuery.append(" where ID = ?"); 
      if (bIsInDomain)
      {   
         sbQuery.append(" and DOMAIN_ID = ?");
      }
   
      try
      {
         cache =  new CachedUpdateStatements(
                         bIsInDomain,
                         dbConnection.prepareStatement(sbQuery.toString()));
      }
      catch (SQLException eExc)
      {
         throw new OSSDatabaseAccessException(
               "Cannot create jdbc statements to access the database.", 
               eExc);
      }
         
      return cache;
   }

   /**
    * Release the statements cached by cacheStatementsForXXX.
    * 
    * @param cache - cached jdbc statements
    */
   protected void closeStatements(
      CachedUpdateStatements cache
   )
   {
      if (cache != null)
      {   
         DatabaseUtils.closeStatement(cache.getSelect());
      }
   }
}

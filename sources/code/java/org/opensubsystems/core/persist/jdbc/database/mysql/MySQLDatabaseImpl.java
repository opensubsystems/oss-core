/*
 * Copyright (C) 2003 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

package org.opensubsystems.core.persist.jdbc.database.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
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
import org.opensubsystems.core.persist.jdbc.impl.DatabaseImpl;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseTransactionFactoryImpl;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * Management layer for My SQL database (www.mysql.com)
 * 
 * TODO: Improve: Consider storing all data in UTF-8
 * See http://uwstopia.nl/blog/2007/01/simple-utf-8-and-mysql-how-to
 * - Make sure you append CHARACTER SET 'UTF8' to each CREATE TABLE statement. 
 *   Example: CREATE TABLE test (id SERIAL PRIMARY KEY, somefield VARCHAR(4)) 
 *   CHARACTER SET 'UTF8';. This can be done at the database level as well (when 
 *   using CREATE DATABASE), but most of the times this is done for you by an 
 *   external party (hoster, sysadmins, ...)
 * - Issue the following query immediately after you established a database 
 *   connection (most likely somewhere in your initialization routines): 
 *   SET NAMES='UTF8'
 *
 * @author OpenSubsystems
 */
public class MySQLDatabaseImpl extends DatabaseImpl
{
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * The textual identifier representing MySQL.
    */
   public static final String MYSQL_DATABASE_TYPE_IDENTIFIER = "MySQL";

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(MySQLDatabaseImpl.class);

   // Inner classes ////////////////////////////////////////////////////////////
   
   /**
    * Class used to encapsulate and hold cached statements between method calls.
    */
   public static class CachedInsertStatements
   {
      /**
       * Are the statements generated for a domain.
       */
      private boolean m_bIsInDomain;
      
      /**
       * Cached select ID statement.
       */
      private PreparedStatement m_selectID; 

      /**
       * Cached select statement.
       */
      private PreparedStatement m_select; 

      /**
       * @param bIsInDomain - are the statements generated for a domain
       * @param selectID - new select ID statement to cache
       * @param select - new select statement to cache
       */
      public CachedInsertStatements(
         boolean           bIsInDomain,
         PreparedStatement selectID, 
         PreparedStatement select
      )
      {
         super();
         
         m_bIsInDomain = bIsInDomain;
         m_selectID = selectID;
         m_select = select;
      }

      /**
       * @return PreparedStatement
       */
      private PreparedStatement getSelectID()
      {
         return m_selectID;
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
   public static class CachedUpdateStatements
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
   public MySQLDatabaseImpl(
   ) throws OSSException
   {
      super(MYSQL_DATABASE_TYPE_IDENTIFIER,
            "now()",
            "count(*)",
            // For MySQL it is generally faster to execute count(*) than to do 
            // last(). We can speed up performance by using select LIMIT X, Y 
            // that allows us to retrieve just specified range of items. But 
            // when we use LIMIT X, Y we cannot use last()
            true, // prefer count to last
            // MySQL supports rows limitation by using clause LIMIT X, Y
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
      // There are few ways how to stop database server:
      // 1. To stop MySQL from Java why not invoke a simple batch file:
      //    Runtime.getRuntime().exec("MySqlStart.bat");
      //    Not very funky admittedly but quick and easy to write!
      // 2. start with the Connector/J documentation
      //    http://dev.mysql.com/doc/connector/j/en/index.html
      //    http://dev.mysql.com/doc/connector/j/en/index.html#id2424110
      // 3. using JBoss - it was released MySQL Connector/MXJ 1.0.2-alpha 
      //    of a JMX MBean for deploying and managing MySQL using the 
      //    JBoss "jmx-console".
      //    You can download sources and binaries from:       
      //    http://dev.mysql.com/downloads/connector/mxj/1.0.html

      s_logger.entering(this.getClass().getName(), "stop");

      // TODO: MySQL: Implement this so we can safely stop the database when
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
      // MySQL uses ANALYZE TABLE <table_name1>, <table_name> to update indexes 
      // and increase performance.

      String[]         arrReturn = new String[1];
      StringBuilder    buffer = new StringBuilder();
      Iterator<String> itItem;
      int              iIndex = 0;

      buffer.append("analyze table ");
      itItem = mpTableNames.values().iterator();
      while (itItem.hasNext()) 
      {
         // construct analyze query for each table from the array
         if (iIndex > 0)
         {
            buffer.append(", ");
         }
         buffer.append(itItem.next());
      }
      arrReturn[0] = buffer.toString();

      // The autocommit has to be false for MySQL
      return new Object[] {arrReturn, Boolean.FALSE};
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isCallableStatement(
      String strQuery
   )
   {
      // MySQL doesn't support stored procedures
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
         String                   strDatabaseURL; 
         String                   strHost; 
         StringBuilder            buffer = new StringBuilder();
         DatabaseSourceDefinition defaultSource;
         
         defaultSource = connectionFactory.getDefaultDataSource();
         strDatabaseURL = defaultSource.getUrl();
         strHost = strDatabaseURL.substring(strDatabaseURL.indexOf("://") + 3, 
                                            strDatabaseURL.lastIndexOf("/"));
         
         buffer.append("GRANT Select, Insert, Update, Delete, Index, Alter, " +
                       "Create, Drop, References ON ");
         buffer.append(strDatabaseURL.substring(
                          strDatabaseURL.lastIndexOf("/") + 1, strDatabaseURL.length()));
         buffer.append(".* TO '");
         buffer.append(defaultSource.getUser());
         buffer.append("'@'");
         buffer.append(strHost);
         buffer.append("' ");
         buffer.append("IDENTIFIED BY '");
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

         // Grant privileges
         buffer.delete(0, buffer.length());
         buffer.append(
            "UPDATE mysql.user SET Select_priv = 'Y', Insert_priv = 'Y', " 
            + "Update_priv = 'Y',  Delete_priv = 'Y',  Create_priv = 'Y',  " 
            + "Drop_priv = 'Y',  Reload_priv = 'N',  Shutdown_priv = 'N',  " 
            + "Process_priv = 'N',  File_priv = 'N',  Grant_priv = 'Y',  " 
            + "References_priv = 'Y',  Index_priv = 'Y',  Alter_priv = 'Y' " 
            + "WHERE Host = '");
         buffer.append(strHost);
         buffer.append("' AND User = '");
         buffer.append(defaultSource.getUser());
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
         throw new OSSDatabaseAccessException(
                      "Unable to create default database user.", sqleExc);
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
      // TODO: MySQL: Implement starting database server
      // There are few ways how to start databae server:
      // 1. To start MySQL from Java why not invoke a simple batch file:
      //    Runtime.getRuntime().exec("MySqlStart.bat");
      //    Not very funky admittedly but quick and easy to write!
      // 2. start with the Connector/J documentation
      //    http://dev.mysql.com/doc/connector/j/en/index.html
      //    http://dev.mysql.com/doc/connector/j/en/index.html#id2424110
      // 3. using JBoss - it was released MySQL Connector/MXJ 1.0.2-alpha 
      //    of a JMX MBean for deploying and managing MySQL using the 
      //    JBoss "jmx-console".
      //    You can download sources and binaries from:       
      //    http://dev.mysql.com/downloads/connector/mxj/1.0.html
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void createDatabaseInstance() throws OSSException
   {
      // TODO: MySQL: Implement creating database instance
      // using JBoss - it was released MySQL Connector/MXJ 1.0.2-alpha 
      // of a JMX MBean for deploying and managing MySQL using the 
      // JBoss "jmx-console".
      // You can download sources and binaries from:       
      // http://dev.mysql.com/downloads/connector/mxj/1.0.html
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
      long       lGeneratedKey = DataObject.NEW_ID;

      insertStatement.executeUpdate();

      try
      {
         
         // I prefer to do it step by step so that we don't have to keep 2 statements
         // and result sets opened
         try
         {
            rsResults = cache.getSelectID().executeQuery();
            if (rsResults.next())
            {
               lGeneratedKey = rsResults.getInt(1);
            }
            else
            {
               throw new OSSDataCreateException(
                            "Cannot read the generated ID from the database.");
            }
         }
         finally
         {      
            DatabaseUtils.close(rsResults);
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
               DatabaseUtils.close(rsResults);
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
      StringBuilder          sbQueryID = new StringBuilder();
      StringBuilder          sbQuery = new StringBuilder();
      CachedInsertStatements cache;
      
      // construct query to select last inserted ID (generated key)
      sbQueryID.append("select LAST_INSERT_ID() from ");
      sbQueryID.append(strTableName);

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
                         dbConnection.prepareStatement(sbQueryID.toString()),
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
   protected static void closeStatements(
      CachedInsertStatements cache
   )
   {
      if (cache != null)
      {   
         DatabaseUtils.close(cache.getSelectID());
         DatabaseUtils.close(cache.getSelect());
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
         DatabaseUtils.close(rsResults);
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
         DatabaseUtils.close(cache.getSelect());
      }
   }
}

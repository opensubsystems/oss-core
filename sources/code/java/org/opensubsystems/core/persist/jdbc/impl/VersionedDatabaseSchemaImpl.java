/*
 * Copyright (C) 2003 - 2012 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

package org.opensubsystems.core.persist.jdbc.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.map.LinkedMap;
import org.opensubsystems.core.data.DataDescriptorManager;
import org.opensubsystems.core.data.impl.DataDescriptorImpl;
import org.opensubsystems.core.error.OSSDatabaseAccessException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.jdbc.DatabaseSchema;
import org.opensubsystems.core.persist.jdbc.VersionedDatabaseSchema;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;

/**
 * Versioned database schema provide functionality of creating and upgrading 
 * of individual database schemas in the database based on their versions.
 * This class keeps track of existing and current versions of database
 * schemas and upgrades them as necessary
 *
 * @author bastafidli
 */
public abstract class VersionedDatabaseSchemaImpl extends    ModifiableDatabaseSchemaImpl 
                                                  implements VersionedDatabaseSchema
{
   // Inner classes ////////////////////////////////////////////////////////////
   
   public static class VersionedDatabaseSchemaDataDescriptor extends DataDescriptorImpl
   {
      // Constants ////////////////////////////////////////////////////////////////
      
      /**
       * Desired value for the data type code. This can be reconfigured if there 
       * are multiple data objects which desire the same value. The rest of the 
       * constants in this class can safely use the desired value since they are 
       * valid only in the context of the data type and therefore it doesn't matter
       * what the real value is. 
       * Protected since it can be reconfigured by the framework and the real value
       * can be different.
       */
      protected static final int VERSIONED_DATABASE_SCHEMA_DATA_TYPE_DESIRED_VALUE = 1;
      
      /**
       * Displayable name for specified data type code object. 
       * Protected since it can be customized and therefore code should use method
       * exposing it rather than the constants.
       */
      protected static final String VERSIONED_DATABASE_SCHEMA_DATA_TYPE_NAME 
                                       = "Versioned database schema";

      /**
       * Logical name identifying the default view for the specified data 
       * type object. Data type objects can be displayed in multiple various ways
       * called views. This constant identifies the default one. This constant
       * should have a value, that can be used to construct various identifiers, 
       * which means no special characters, no spaces, etc.
       * Protected since it can be customized and therefore code should use method
       * exposing it rather than the constants.
       */
      protected static final String VERSIONED_DATABASE_SCHEMA_TYPE_VIEW 
                                       = "versionedbschema";

      /**
       * Code for table column.
       */
      public static final int COL_VERSIONED_DATABASE_SCHEMA_DATA_ID 
         = VERSIONED_DATABASE_SCHEMA_DATA_TYPE_DESIRED_VALUE + 1;

      /**
       * Code for table column.
       */
      public static final int COL_VERSIONED_DATABASE_SCHEMA_DATA_NAME 
         = VERSIONED_DATABASE_SCHEMA_DATA_TYPE_DESIRED_VALUE + 2;

      /**
       * Code for table column.
       */
      public static final int COL_VERSIONED_DATABASE_SCHEMA_DATA_VERSION 
         = VERSIONED_DATABASE_SCHEMA_DATA_TYPE_DESIRED_VALUE + 3;

      /**
       * Code for table column.
       */
      public static final int COL_VERSIONED_DATABASE_SCHEMA_DATA_CREATION_DATE 
         = VERSIONED_DATABASE_SCHEMA_DATA_TYPE_DESIRED_VALUE + 4;

      /**
       * Code for table column.
       */
      public static final int COL_VERSIONED_DATABASE_SCHEMA_DATA_MODIFICATION_DATE 
         = VERSIONED_DATABASE_SCHEMA_DATA_TYPE_DESIRED_VALUE + 5;

      /**
       * Static variable for array of all columns codes.
       * The order is important since it is used to retrieve all data from the
       * persistence store efficiently so do not modify it unless you make
       * changes to other places as well.
       * Protected since derived classes can add more attributes and therefore code 
       * should use method exposing it rather than the constants.
       */
      protected static final int[] VERSIONED_DATABASE_SCHEMA_ALL_COLUMNS 
         = {COL_VERSIONED_DATABASE_SCHEMA_DATA_ID,
            COL_VERSIONED_DATABASE_SCHEMA_DATA_NAME,
            COL_VERSIONED_DATABASE_SCHEMA_DATA_VERSION,
            COL_VERSIONED_DATABASE_SCHEMA_DATA_CREATION_DATE,
            COL_VERSIONED_DATABASE_SCHEMA_DATA_MODIFICATION_DATE,
           };

      // Attributes ////////////////////////////////////////////////////////////
      
      /**
       * Maximal length of the name field. 
       * The value depends on the underlying persistence mechanism and it is set 
       * once the persistence layer is initialized.
       */
      protected int m_iNameMaxLength;
      
      // Constructors //////////////////////////////////////////////////////////
      
      /**
       * Default constructor.
       */
      public VersionedDatabaseSchemaDataDescriptor()
      {
         super(VERSIONED_DATABASE_SCHEMA_DATA_TYPE_DESIRED_VALUE, 
               VERSIONED_DATABASE_SCHEMA_DATA_TYPE_NAME, 
               VERSIONED_DATABASE_SCHEMA_TYPE_VIEW);
      }
      
      // Logic /////////////////////////////////////////////////////////////////

      /**
       * @return int
       */
      public int getNameMaxLength(
      )
      {
         return m_iNameMaxLength;
      }

      /**
       * Maximal length for the name.
       *
       * @param iName - maximal length of the name field in the persistence store
       */
      public void setNameMaxLength(
         int iName
      )
      {
         m_iNameMaxLength = iName;
      }
   }

   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Name identifies this schema in the database. 
    */
   public static final String VERSIONED_SCHEMA_NAME = "SCHEMA";

   /**
    * Version of this schema in the database.
    * Version 1 - original
    * Version 2 - PostgreSQL added user defined type
    */
   public static final int VERSIONED_SCHEMA_VERSION = 1;

   /**
    * Maximal length of database schema name.
    */
   public static final int SCHEMA_NAME_MAXLENGTH = 50;

   /**
    * Full name of the table used by this schema.
    */
   public static final String SCHEMA_TABLE_NAME = getVersionedDatabaseSchemaTableName();

   // Attributes ///////////////////////////////////////////////////////////////

   /**
    * Map of schemas which are versioned in the database. 
    * Key is the name of the schema, value is instance of DatabaseSchema object.
    */
   private Map<String, DatabaseSchema> m_mpSchemas;

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(VersionedDatabaseSchemaImpl.class);

   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Default constructor.
    * 
    * @throws OSSException - database cannot be started.
    */
   public VersionedDatabaseSchemaImpl(
   ) throws OSSException
   {
      super(null, VERSIONED_SCHEMA_NAME, VERSIONED_SCHEMA_VERSION, true,
            DataDescriptorManager.getInstance(
               VersionedDatabaseSchemaDataDescriptor.class).getDataTypeAsObject(), 
            SCHEMA_TABLE_NAME);
      
      // This has to be sequenced hashmap to create the schemas in the correct 
      // order
      // TODO: commons-collection: Fix after support for generics is released
      m_mpSchemas = new LinkedMap();
      // This schema itself is managed by itself
      add(this);
   }

   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * @return String - get full name of the table used by this schema.
    */
   private static String getVersionedDatabaseSchemaTableName(
   )
   {
      return getSchemaPrefix() + "SCHEMA";
   }

   /**
    * {@inheritDoc}
    */
   public void add(
      DatabaseSchema dsSchema
   ) throws OSSException
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert dsSchema != null : "Cannot add null schema.";
         // This is not valid assert since due to dependencies the same
         // schema might be added multiple times
         // assert m_mpSchemas.get(dsSchema.getName())
         //   != null : "Database schema with name "
         //      + dsSchema.getName()
         //      + " is already managed by versioned database schema.";
      }

      if (m_mpSchemas.get(dsSchema.getName()) != null)
      {
         s_logger.log(Level.FINEST, "Database schema " + dsSchema.getName()
                             + " is already managed by versioned schema."
                             + " It is not added second time.");
      }
      else
      {
         DatabaseSchema[] arSchemas;

         arSchemas = dsSchema.getDependentSchemas();
         // add all dependent schemas
         // TODO: Feature: Add schemas circular dependency check here
         if (arSchemas != null)
         {
            for (int iIndex = 0; iIndex < arSchemas.length; iIndex++)
            {
               add(arSchemas[iIndex]);
            }
         }
         m_mpSchemas.put(dsSchema.getName(), dsSchema);
         s_logger.log(Level.FINEST, "Database schema " + dsSchema.getName()
                             + " version " + dsSchema.getVersion()
                             + " is now managed by versioned schema.");
      }
   }

   /**
    * {@inheritDoc}
    */
   public void init(
      Connection cntDBConnection, 
      String     strUserName
   ) throws OSSException,
            SQLException
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert cntDBConnection != null : "Connection is not valid.";
      }

      // This has to be sequenced hash map to create the schemas in correct order
      // Current schemas will at the end contains schemas which must be added
      // TODO: commons-collection: Fix after support for generics is released
      Map<String, DatabaseSchema> mpSchemasToAdd = new LinkedMap(m_mpSchemas);
      // Schemas to upgrade will contain schemas which must be upgraded
      // TODO: commons-collection: Fix after support for generics is released
      Map<String, Integer> mpSchemasToUpgrade = new LinkedMap();

      loadExistingSchemas(cntDBConnection, mpSchemasToAdd, mpSchemasToUpgrade);
      createOrUpgradeSchemas(cntDBConnection, strUserName, mpSchemasToAdd, 
                             mpSchemasToUpgrade);
   }

   /**
    * Method returns simple insert user query. This method is common for all
    * databases and can be overwritten for each specific database schema.
    *
    * @return String - simple insert schema query
    * @throws OSSException - exception during getting query
    */
   public String getInsertSchema(
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();
      
      buffer.append("insert into " + SCHEMA_TABLE_NAME 
                    + " (SCHEMA_NAME, SCHEMA_VERSION,"
                    + " CREATION_DATE, MODIFICATION_DATE)"
                    + " values (?,?,");
      buffer.append(DatabaseImpl.getInstance().getSQLCurrentTimestampFunctionCall());
      buffer.append(",");
      buffer.append(DatabaseImpl.getInstance().getSQLCurrentTimestampFunctionCall());
      buffer.append(")");

      return buffer.toString();
   }

   // Helper methods ///////////////////////////////////////////////////////////

   /**
    * Create one schema and update the records.
    *
    * @param cntDBConnection - valid connection to database
    * @param strUserName - name of user who will be accessing this table
    * @param dsSchema - schema to create
    * @param pstmUpdate - statement to use to update records if it is null
    *                     it will be created and returned so it can be reused.
    *                     Caller is then responsible for closing this statement.
    * @return PreparedStatement
    * @throws OSSException - problem creating the schema
    * @throws SQLException - problem creating the schema
    */
   private PreparedStatement createSchema(
      Connection        cntDBConnection,
      String            strUserName,
      DatabaseSchema    dsSchema,
      PreparedStatement pstmUpdate
   ) throws OSSException,
            SQLException
   {
      int iUpdateCount;

      try
      {

         s_logger.log(Level.FINEST, "Creating new database schema "
                             + dsSchema.getName() + " version " 
                             + dsSchema.getVersion() + ".");
         dsSchema.create(cntDBConnection, strUserName);
   
         // Update the version schema tables with the latest version 
         // of just created schema
         if (pstmUpdate == null)
         {
            pstmUpdate = cntDBConnection.prepareStatement(getInsertSchema());
         }
   
         pstmUpdate.setString(1, dsSchema.getName());
         pstmUpdate.setInt(2, dsSchema.getVersion());
         iUpdateCount = pstmUpdate.executeUpdate();
         if (iUpdateCount == 0)
         {
            String strMessage;
            
            strMessage = "Record for database schema " + dsSchema.getName() 
                         + " version " + dsSchema.getVersion() 
                         + " was not created.";
            s_logger.log(Level.WARNING, strMessage);
            throw new OSSDatabaseAccessException(strMessage);
            
         }
         s_logger.log(Level.FINER, "Database schema " + dsSchema.getName() 
                             + " created.");

         // Try to commit on the connection if the schema was created successfully
         // see Bug #1110485 for explanation what we are doing. We cannot user
         // UserTransaction here since the transaction manager may not be even
         // initialized and we don't know what kind of transaction this operation
         // is performed in. 
         
         // At this point we don't know if this is just a single operation
         // and we need to commit or if it is a part of bigger transaction
         // and the commit is not desired until all operations proceed. 
         // Therefore let the DatabaseTransactionFactory resolve it 
         DatabaseTransactionFactoryImpl.getInstance().commitTransaction(
                                                         cntDBConnection);
      }
      catch (OSSException ossExc)
      {
         // At this point we don't know if this is just a single operation
         // and we need to commit or if it is a part of bigger transaction
         // and the commit is not desired until all operations proceed.
         // Therefore let the DatabaseTransactionFactory resolve it
         DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(
                                                         cntDBConnection);
         // Just rethrow the original exception
         throw ossExc;
      }
      catch (SQLException sqlExc)
      {
         // At this point we don't know if this is just a single operation
         // and we need to commit or if it is a part of bigger transaction
         // and the commit is not desired until all operations proceed.
         // Therefore let the DatabaseTransactionFactory resolve it
         DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(
                                                         cntDBConnection);
         // Just rethrow the original exception
         throw sqlExc;
      }
      catch (Throwable thr)
      {
         // At this point we don't know if this is just a single operation
         // and we need to commit or if it is a part of bigger transaction
         // and the commit is not desired until all operations proceed.
         // Therefore let the DatabaseTransactionFactory resolve it
         DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(
                                                         cntDBConnection);
         throw new OSSDatabaseAccessException(thr);
      }

      return pstmUpdate;
   }

   /**
    * Upgrade one schema and update its records.
    *
    * @param cntDBConnection - valid connection to database
    * @param strUserName - name of user who will be accessing this table
    * @param dsSchema - schema to upgrade
    * @param iOriginalVersion - original version of the schema from which to 
    *                           upgrade
    * @param pstmUpdate - statement to use to update records if it is null
    *                     it will be created and returned so it can be reused.
    *                     Caller is then responsible for closing this statement.
    * @return PreparedStatement
    * @throws OSSException - problem updating the schema
    * @throws SQLException - problem updating the schema
    */
   private PreparedStatement upgradeSchema(
      Connection        cntDBConnection,
      String            strUserName,
      DatabaseSchema    dsSchema,
      int               iOriginalVersion,
      PreparedStatement pstmUpdate
   ) throws OSSException,
            SQLException
   {
      int iUpdateCount;
      
      s_logger.log(Level.FINEST, "Upgrading new database schema "
                   + dsSchema.getName() + " version " + dsSchema.getVersion()
                   + ".");
      dsSchema.upgrade(cntDBConnection, strUserName, iOriginalVersion);

      // Update the version schema tables with the latest version 
      if (pstmUpdate == null)
      {
         // If this schema is updated, we can prepare statement
         pstmUpdate = cntDBConnection.prepareStatement(
               "update " + SCHEMA_TABLE_NAME + " set SCHEMA_VERSION = ?,"
               + " MODIFICATION_DATE = " 
               + DatabaseImpl.getInstance().getSQLCurrentTimestampFunctionCall()
               + " where SCHEMA_NAME = ? and SCHEMA_VERSION < ?");
      }

      pstmUpdate.setInt(1, dsSchema.getVersion());
      pstmUpdate.setString(2, dsSchema.getName());
      pstmUpdate.setInt(3, dsSchema.getVersion());
      iUpdateCount = pstmUpdate.executeUpdate(); 
      if (iUpdateCount == 0)
      {
         String strMessage;
         
         strMessage = "No record for database schema " + dsSchema.getName() 
                      + " version " + dsSchema.getVersion() 
                      + " was updated to new version.";
         s_logger.log(Level.WARNING, strMessage);
         throw new OSSDatabaseAccessException(strMessage);
         
      }
      else if (iUpdateCount > 1)
      {
         String strMessage;
         
         strMessage = "Multiple (" + iUpdateCount + ") records for database schema " 
                      + dsSchema.getName()  + " version " + dsSchema.getVersion() 
                      + " were updated to new version.";
         s_logger.log(Level.WARNING, strMessage);
         throw new OSSDatabaseAccessException(strMessage);
         
      }
      s_logger.log(Level.FINER, "Database schema " + dsSchema.getName() 
                          + " upgraded.");

      return pstmUpdate;
   }

   /**
    * Check what schemas already exists in the repository.
    * 
    * @param cntDBConnection - valid connection to database
    * @param mpSchemasToAdd - current schemas will at the end contains schemas,
    *                         which must be added
    * @param mpSchemasToUpgrade - schemas to upgrade will contain schemas,
    *                             which must be upgraded
    * @throws OSSException - problem initializing the schema
    */
   @SuppressWarnings("resource") // See DatabaseUtils.closeResultSetAndStatement
   protected void loadExistingSchemas(
      Connection                  cntDBConnection,
      Map<String, DatabaseSchema> mpSchemasToAdd,
      Map<String, Integer>        mpSchemasToUpgrade
   ) throws OSSException
   {
      // Try to load information about version of current schema 
      Statement stmQuery = null;
      ResultSet rsQueryResults = null;
      try
      {
         s_logger.fine("Verifying existing database schemas");

         stmQuery = cntDBConnection.createStatement();
         rsQueryResults = stmQuery.executeQuery(
               "select SCHEMA_NAME, SCHEMA_VERSION from " + SCHEMA_TABLE_NAME);

         String         strSchemaName;
         int            iLastVersion;
         DatabaseSchema dsSchema;
         
         while (rsQueryResults.next())
         {
            strSchemaName = rsQueryResults.getString(1);
            // See if this schema exists, if it exists, remove it from the
            // list and that way we will detect the schemas we need to create
            dsSchema = mpSchemasToAdd.remove(strSchemaName);
            if (dsSchema != null)
            {
               // This schema exists, compare versions
               iLastVersion = rsQueryResults.getInt(2);
               if (iLastVersion < dsSchema.getVersion())
               {
                  s_logger.finer("Database contains schema " + strSchemaName
                                 + " version " + iLastVersion
                                 + ", but the current schema has version "
                                 + dsSchema.getVersion()
                                 + ". It needs to be upgraded.");
                  // Remember the last version
                  mpSchemasToUpgrade.put(dsSchema.getName(),
                                         new Integer(iLastVersion));
               }
               else if (iLastVersion > dsSchema.getVersion())
               {
                  String strMessage;
                  
                  strMessage = "Database contains schema " + strSchemaName
                               + " version " + iLastVersion
                               + ", which is newer that the current schema version "
                               + dsSchema.getVersion()
                               + ". You should obtain newer version of the product.";
                  
                  s_logger.finer(strMessage);
                  throw new OSSDatabaseAccessException(strMessage);
               }
               else
               {
                  s_logger.finer("Database schema " + strSchemaName
                                 + " version " + iLastVersion + " is up to date.");
               }
            }
            else
            {
               // There is schema in the database which doesn't exists anymore
               // This is not an assert since the same database can be used 
               // by multiple products
               s_logger.finer("Database contains schema " + strSchemaName
                              + ", which doesn't exist anymore in current schema.");
            }
         }
         
         s_logger.fine("Existing database schemas verified: " 
                       + mpSchemasToAdd.size() + " schemas to add and "
                       + mpSchemasToUpgrade.size() + " schemas to upgrade.");
      }
      catch (SQLException sqleExc)
      {
         s_logger.log(Level.FINER,
                      "Failed to load version schema, trying to create one.",
                      sqleExc);
         // The code below will create all schemas in the mpSchemasToAdd
         if (GlobalConstants.ERROR_CHECKING)
         {
            assert mpSchemasToUpgrade.isEmpty() 
                   : "There should be no schemas to upgrade.";
            assert mpSchemasToAdd.size() == m_mpSchemas.size()
                   : "All schemas should be set to be created.";
         }
      }
      finally
      {
         DatabaseUtils.closeResultSetAndStatement(rsQueryResults, stmQuery);
      }
   }

   /**
    * Creating or upgrading schemas, which needs to be added or changes.
    * 
    * @param cntDBConnection - valid connection to database
    * @param strUserName - name of user who will be accessing this table
    * @param mpSchemasToAdd - current schemas will at the end contains schemas 
    *                         which must be added
    * @param mpSchemasToUpgrade - schemas to upgrade will contain schemas which 
    *                             must be upgraded
    * @throws OSSException - problem initializing the schema
    * @throws SQLException - problem initializing the schema
    */
   protected void createOrUpgradeSchemas(
      Connection                  cntDBConnection,
      String                      strUserName,
      Map<String, DatabaseSchema> mpSchemasToAdd,
      Map<String, Integer>        mpSchemasToUpgrade
   ) throws OSSException,
            SQLException
   {
      // Now see if there are some new schemas and schemas which needs to be 
      // upgraded. Since some of the new schemas may depend on some changes
      // in the upgraded schemas or some changes in the upgraded schemas 
      // may depend on some new schemas, we sill need to create and upgrade
      // them in the same order as they were specified
      if ((!mpSchemasToAdd.isEmpty()) || (!mpSchemasToUpgrade.isEmpty()))
      {
         s_logger.fine("Adding new and upgraging existing database schemas");

         Collection<DatabaseSchema> clSchemas;
         Iterator<DatabaseSchema>   itrSchemas;
         DatabaseSchema             dsSchema;
         PreparedStatement          pstmCreate = null;
         PreparedStatement          pstmUpdate = null;
         Integer                    intLastVersion;

         clSchemas = m_mpSchemas.values();         
         try
         {
            // First we need to create this schema if it needs to be created
            // or update it if it needs to be updated
            if (mpSchemasToAdd.get(getName()) != null)
            {
               pstmCreate = createSchema(cntDBConnection, strUserName, 
                                         this, pstmCreate);
            }
            else
            {
               intLastVersion = (Integer)mpSchemasToUpgrade.get(getName());
               if (intLastVersion != null)
               {
                  pstmUpdate = upgradeSchema(cntDBConnection, strUserName, 
                                            this, intLastVersion.intValue(),
                                            pstmUpdate);
               }               
            }

            // And now when versioned schema is up to date, we can create or update
            // all other schemas, so just go through the list
            for (itrSchemas = clSchemas.iterator(); itrSchemas.hasNext();)
            {
               dsSchema = itrSchemas.next();
               if (dsSchema != this)
               {
                  if (mpSchemasToAdd.get(dsSchema.getName()) != null)
                  {
                     pstmCreate = createSchema(cntDBConnection, strUserName, 
                                               dsSchema, pstmCreate);
                  }
                  else
                  {
                     intLastVersion = (Integer)mpSchemasToUpgrade.get(
                                                  dsSchema.getName());
                     if (intLastVersion != null)
                     {
                        pstmUpdate = upgradeSchema(cntDBConnection, strUserName, 
                                                  dsSchema, 
                                                  intLastVersion.intValue(),
                                                  pstmUpdate);
                     }               
                  }
               }
            }
            
            s_logger.fine("Database schemas have been updated.");
         }
         finally
         {
            DatabaseUtils.closeStatement(pstmCreate);
            DatabaseUtils.closeStatement(pstmUpdate);
         }
      }
   }
}

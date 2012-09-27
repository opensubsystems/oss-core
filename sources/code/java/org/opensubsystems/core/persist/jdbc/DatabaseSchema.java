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

package org.opensubsystems.core.persist.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.opensubsystems.core.error.OSSException;

/**
 * Interface representing database schema, which is set of related database
 * objects such as tables, constraints and indexes. One database instance 
 * will usually consists of multiple schemas corresponding to components used 
 * in the application. Each component or subsystem will provide one or multiple
 * database schemas, which will be responsible for creation of all database 
 * structures for this components, for the upgrade of existing database 
 * structures to the latest version and also will encapsulate all specific 
 * database dependent information (such as database dependent queries).
 *
 * @author bastafidli
 */
public interface DatabaseSchema
{   
   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Get the name of this database schema.
    *
    * @return String - name which uniquely identifies this schema.
    */
   String getName(
   );
   
   /**
    * Get the current (most recent) version of database schema.
    *
    * @return int - most current version of the schema
    */
   int getVersion(
   );

   /**
    * Check if the the data object belongs to a domain or not. If data object 
    * belongs to a domain, its table contains DOMAIN_ID column, which should
    * be checked in generated SQL for this data type against the current call
    * context domain.
    * 
    * The assumption is that if the schema is managing multiple data objects
    * and one of them is in domain then all of them are in domain (and vice
    * versa) since it doesn't make sense for closely related data objects to do
    * not exists in the same domain at the same time. 
    * 
    * @return boolean - true if data object belongs to domain, false otherwise
    */
   boolean isInDomain(); 
   
   // Lifecycle events /////////////////////////////////////////////////////////
   
   /**
    * Create the schema. 
    *
    * @param cntDBConnection - valid connection to database
    * @param strUserName - name of user who will be accessing this table
    * @throws SQLException - problem creating the database schema
    * @throws OSSException - problem creating the database schema
    */
   void create(
      Connection cntDBConnection,
      String     strUserName
   ) throws SQLException, OSSException;   

   /**
    * Upgrade the schema. 
    *
    * @param cntDBConnection - valid connection to database
    * @param strUserName - name of user who will be accessing this table
    * @param iOriginalVersion - original version from which to upgrade
    * @throws SQLException - problem creating the database schema
    */
   void upgrade(
      Connection cntDBConnection,
      String     strUserName,
      int        iOriginalVersion
   ) throws SQLException;   

   /**
    * Get list of database schemas, which this schema depends on. These schemas
    * will be created and initialized before this schema is initialized. This
    * way if this schema has relationships (foreign keys, stored procedure 
    * access) with the other schemas, they are guaranteed to exist before this
    * schema is created.
    * 
    * @return DatabaseSchema[] - array of DatabaseSchema instances  
    * @throws OSSException - database cannot be started.
    */
   DatabaseSchema[] getDependentSchemas(
   ) throws OSSException;
   
   /**
    * Handle SQL Exception caused by some database operations.
    * 
    * @param exc - sql exception to be handled
    * @param dbConnection - database connection used when the exception occurred
    * @param iOperationType - type of the operation that caused the exception, 
    *                         see DatabaseOperations for possible values
    * @param iDataType - data type the data object represents (e.g if this is
    *                    type user and data is Integer, that means it is id
    *                    of user object). This is one of the DataConstant 
    *                    constants.
    * @param strDisplayableViewName - displayable name for the view of the data 
    *                                 object instance. This name allows different 
    *                                 views of the same data objects to be called 
    *                                 differently to make them more user friendly.  
    * @param data - data object the exception is handled for 
    * @throws OSSException - problem handling exception
    */
   void handleSQLException(
      SQLException exc,
      Connection   dbConnection,
      int          iOperationType,
      int          iDataType,
      String       strDisplayableViewName,
      Object       data
   ) throws OSSException;
}

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
import java.sql.Timestamp;
import java.util.Map;

import org.opensubsystems.core.error.OSSException;

/**
 * Interface representing database schema that support modifying of data stored 
 * in it. Database schema is set of related database objects such as tables, 
 * constraints and indexes. One database instance will usually consists of 
 * multiple schemas corresponding to components used in the application. Each 
 * component or subsystem will provide one or multiple database schemas, which 
 * will be responsible for creation of all database structures for this 
 * components, for the upgrade of existing database structures to the latest 
 * version and also will encapsulate all specific database dependent information 
 * (such as database dependent queries).
 *
 * @author bastafidli
 */
public interface ModifiableDatabaseSchema extends DatabaseSchema
{   
   // Logic ////////////////////////////////////////////////////////////////////

   /**
    * Get the names of all tables that can be modified by the operations 
    * provided by this database schema. The operations that can modify the data 
    * include for example insert, update or delete of data in this table.
    *
    * @return Map - map of table names for particular schema.
    *               key = object data type
    *               value = table name
    *               null if the schema doesn't allow to modify any of the tables 
    */
   Map<Integer, String> getModifiableTableNames(
   );

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Method deletes related child data when main data object is being deleted. 
    * This method has to be overridden for databases that don't support cascade 
    * delete (eg. Sybase ASE). 
    * 
    * @param cntDBConnection - valid connection to database
    * @param iDataType - data type identifying what to delete
    * @param lId - ID of the record that has to be deleted
    * @throws OSSException - problem deleting related data
    * @throws SQLException - problem deleting related data
    * @return int - number of deleted records
    */
   int deleteRelatedData(
      Connection cntDBConnection,
      int        iDataType,
      long       lId
   ) throws OSSException, 
            SQLException;

   /**
    * Method to check if there was a concurrent modification error when update 
    * doesn't update anything in the database (updated count == 0).
    * 
    * @param dbConnection - database connection
    * @param strDataName - name of the data object
    * @param strTableName - name of the table
    * @param lId - id of the data object
    * @param tmstpModificationDate - modification date of the data object
    * @throws OSSException - more descriptive exception
    */
   public void checkUpdateError(
      Connection dbConnection,
      String     strDataName,
      String     strTableName,
      long       lId,
      Timestamp  tmstpModificationDate
   ) throws OSSException;
}

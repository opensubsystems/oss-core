/*
 * Copyright (C) 2009 - 2012 OpenSubsystems.com/net/org and its owners. All rights reserved.
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
 * Versioned database schema provide functionality of creating and upgrading 
 * of individual database schemas in the database based on their versions.
 * This class keeps track of existing and current versions of database
 * schemas and upgrades them as necessary
 *
 * @author bastafidli
 */
public interface VersionedDatabaseSchema extends ModifiableDatabaseSchema
{
   /**
    * Add new database schema to be managed by versioned schema.
    *
    * @param dsSchema - new database schema to be managed
    * @throws OSSException - database cannot be started.
    */
   void add(
      DatabaseSchema dsSchema
   ) throws OSSException;

   /**
    * Initialize the schema. This function should make sure that the schema in 
    * the database is up to date, perform any upgrades if necessary, etc.
    *
    * @param cntDBConnection - valid connection to database
    * @param strUserName - name of user who will be accessing this table
    * @throws OSSException - problem initializing the schema
    * @throws SQLException - problem initializing the schema
    */
   void init(
      Connection cntDBConnection, 
      String strUserName
   ) throws OSSException, 
            SQLException;
}

/*
 * Copyright (C) 2006 - 2012 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSException;

/**
 * This interface defines methods, which should be implemented by factory
 * which support batched update operation. User can then use
 * DatabaseCreateSingleDataObjectOperation and
 * DatabaseUpdateMultipleDataObjectsOperation to simplify coding of updates 
 * since these two classes take advantage of methods defined here.
 *
 * @author OpenSubsystems
 */
public interface ModifiableDatabaseFactory extends BasicDatabaseFactory
{
   /**
    * Set values to the statement to update data object. 
    *
    * @param updateStatement - prepared statement the values will be set up for
    * @param data - data object to update, based on the type of the data object
    *               it can be determined what data are we updating
    * @param initialIndex - initial index for values to be set up into statement
    * @return int - index of the last parameter in prepared statement (can be used
    *               for later processing outside of this method)   
    * @throws OSSException - exception during setting values
    * @throws SQLException - exception during setting values
    */
   int setValuesForUpdate(
      PreparedStatement updateStatement,
      DataObject        data,
      int               initialIndex
   ) throws OSSException, 
            SQLException;
}

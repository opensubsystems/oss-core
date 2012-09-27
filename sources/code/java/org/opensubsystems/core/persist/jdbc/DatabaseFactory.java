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

import java.sql.ResultSet;

import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.DataFactory;

/**
 * This interface should be implemented by all data object factories, which
 * support basic operations with data objects persisted in the database.
 * 
 * @author bastafidli
 */
public interface DatabaseFactory extends DataFactory
{
   /**
    * Get database on which the operation should operate. 
    * 
    * @throws OSSException - problem accessing the database
    */
   public Database getDatabase(
   ) throws OSSException; 
   
   /**
    * Load complete data object from the result set. It is used when all 
    * attributes of the data objects should be loaded as efficiently as possible.
    * 
    * Every data has to be loaded otherwise we couldn't test the existence data 
    * therefore this method is common to all the database factories 
    * 
    * @param rsQueryResults - ResultSet retrieved from the database
    * @param initialIndex - initial column index in the result set to retrieve,
    *                       by default this is 1. This allows to include columns
    *                       for this database factory mixed with columns for
    *                       other database factories and the load them 
    *                       efficiently.
    * @return DataObject - with specific columns set
    * @throws OSSException - an error has occurred
    */
   DataObject load(
      ResultSet rsQueryResults,
      int       initialIndex
   ) throws OSSException;
}

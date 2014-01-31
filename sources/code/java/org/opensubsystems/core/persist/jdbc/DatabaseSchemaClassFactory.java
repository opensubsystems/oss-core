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

package org.opensubsystems.core.persist.jdbc;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.jdbc.impl.DatabaseImpl;
import org.opensubsystems.core.util.ModifierClassFactory;

/**
 * Class factory responsible for instantiation of database schemas. Based on the
 * schema interface or default implementation and currently active database 
 * instantiate correct database schema which should be used for this database.
 * 
 * See parent class for sequence of class names that will attempted to be 
 * constructed. As a modifier we will use the database type identifier from
 * the database class. 
 * 
 * @author bastafidli
 */
public class DatabaseSchemaClassFactory extends ModifierClassFactory<DatabaseSchema>
{
   // Constant /////////////////////////////////////////////////////////////////
   
   public static final String OPTIONAL_PACKAGE_NAME = "database";
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Default constructor
    */
   public DatabaseSchemaClassFactory(
   ) throws OSSException
   {
      super(DatabaseSchema.class, OPTIONAL_PACKAGE_NAME,
            // TODO: Dependency: Remove this dependency on implementation class
            DatabaseImpl.getInstance().getDatabaseTypeIdentifier());
   }         
}

/*
 * Copyright (C) 2007 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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

package org.opensubsystems.core.application.jdbc;

import org.opensubsystems.core.application.BackendModule;
import org.opensubsystems.core.persist.jdbc.DatabaseSchema;

/**
 * Module interface suitable for representing backend modules exposing their 
 * business logic and data access layer that utilizes database for the 
 * persistence mechanism but doesn't exposes any details about the user 
 * interface.   
 * 
 * @author bastafidli
 */
public interface BackendDatabaseModule extends BackendModule
{
   /**
    * @return DatabaseSchema[] - database schemas provided by this subsystem 
    */
   DatabaseSchema[] getSchemas();
}

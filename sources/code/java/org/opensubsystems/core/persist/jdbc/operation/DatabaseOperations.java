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

package org.opensubsystems.core.persist.jdbc.operation;

/**
 * Interface to define abstraction for database operations. 
 * 
 * TODO: Improvement: Consider converting this to enum
 * 
 * @author OpenSubsystems
 */
public interface DatabaseOperations
{
   // Constants ////////////////////////////////////////////////////////////////
   
   /**
    * Database insert.
    */
   int DBOP_INSERT = 1;

   /**
    * Database update.
    */
   int DBOP_UPDATE = 2;

   /**
    * Database delete.
    */
   int DBOP_DELETE = 3;

   /**
    * Database read.
    */
   int DBOP_SELECT = 4;
}

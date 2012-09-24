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

package org.opensubsystems.core.data;

/**
 * Interface to expose information about instance of data that can be easily 
 * used to identify the data by user or software.
 *
 * @author bastafidli
 */
public interface IdentifiableDataObject
{
   /**
    * Get ID of the data object
    * 
    * @return long - id of the generic data
    */
   long getId();
   
   /**
    * Get name of the data object
    * 
    * @return String - name of the generic data
    */
   String getName();
   
   /**
    * Get description of the data object
    * 
    * @return String - description of the generic data
    */
   String getDescription();
}
